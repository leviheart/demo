<template>
  <div class="tech-dashboard">
    <!-- 头部 -->
    <div class="dashboard-header">
      <div class="header-content">
        <h1 class="system-title">
          <span class="title-icon">🚗</span>
          智能车辆监控系统
        </h1>
        <div class="header-stats">
          <div class="stat-item">
            <span class="stat-label">在线车辆</span>
            <span class="stat-value">{{ cars.length }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">运行中</span>
            <span class="stat-value active">{{ activeCarsCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 控制面板 -->
    <div class="control-panel">
      <!-- 车辆定位卡片 -->
      <div class="control-card location-card">
        <div class="card-header">
          <div class="header-icon">📍</div>
          <h2>车辆定位</h2>
        </div>
        <div class="card-content">
          <div class="input-group">
            <label>车牌号码</label>
            <input v-model="locationForm.carName" type="text" placeholder="苏K12345">
          </div>
          <div class="coordinate-grid">
            <div class="input-group">
              <label>纬度</label>
              <input v-model.number="locationForm.latitude" type="number" step="0.0001" placeholder="32.3931">
            </div>
            <div class="input-group">
              <label>经度</label>
              <input v-model.number="locationForm.longitude" type="number" step="0.0001" placeholder="119.4128">
            </div>
          </div>
          <div class="input-group">
            <label>车辆状态</label>
            <select v-model="locationForm.status">
              <option value="active">🟢 运行中</option>
              <option value="inactive">🔴 停止</option>
              <option value="maintenance">🟡 维修中</option>
            </select>
          </div>
          <button class="action-btn primary" @click="updateLocation">
            <span class="btn-icon">📡</span>
            更新位置
          </button>
        </div>
      </div>

      <!-- 路径规划卡片 -->
      <div class="control-card route-card">
        <div class="card-header">
          <div class="header-icon">🧭</div>
          <h2>路径规划</h2>
        </div>
        <div class="card-content">
          <div class="input-group">
            <label>起始地点</label>
            <input v-model="routeForm.startPoint" type="text" placeholder="扬州">
          </div>
          <div class="input-group">
            <label>目标地点</label>
            <input v-model="routeForm.endPoint" type="text" placeholder="南京">
          </div>
          <button class="action-btn secondary" @click="planRoute">
            <span class="btn-icon">🧠</span>
            智能规划
          </button>
          <div v-if="routeResult" class="route-result">
            <div class="result-header">
              <span class="result-icon">🛣️</span>
              <h3>最优路径</h3>
            </div>
            <div class="result-details">
              <div class="detail-item">
                <span class="detail-label">行驶距离</span>
                <span class="detail-value">{{ routeResult.distance }} 公里</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">起点</span>
                <span class="detail-value">{{ routeResult.startPoint }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">终点</span>
                <span class="detail-value">{{ routeResult.endPoint }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 车辆监控卡片 -->
      <div class="control-card monitor-card">
        <div class="card-header">
          <div class="header-icon">📊</div>
          <h2>实时监控</h2>
        </div>
        <div class="card-content">
          <div class="monitor-controls">
            <button class="tab-btn" :class="{ active: monitorTab === 'all' }" @click="loadCars">
              全部车辆
            </button>
            <button class="tab-btn" :class="{ active: monitorTab === 'active' }" @click="loadActiveCars">
              运行中
            </button>
          </div>
          <div class="vehicle-list">
            <div v-if="cars.length === 0" class="empty-state">
              <div class="empty-icon">📭</div>
              <p>暂无车辆数据</p>
            </div>
            <div v-else class="vehicle-item" v-for="car in cars" :key="car.id">
              <div class="vehicle-header">
                <span class="vehicle-id">🚗 {{ car.carName }}</span>
                <span :class="['status-tag', car.status]">
                  {{ getStatusText(car.status) }}
                </span>
              </div>
              <div class="vehicle-details">
                <div class="detail-row">
                  <span class="detail-key">位置坐标</span>
                  <span class="detail-value">({{ car.latitude }}, {{ car.longitude }})</span>
                </div>
                <div class="detail-row">
                  <span class="detail-key">更新时间</span>
                  <span class="detail-value">{{ currentTime }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 消息提示 -->
    <transition name="toast">
      <div v-if="toastMessage" :class="['toast-notification', toastType]">
        <span class="toast-icon">{{ getToastIcon(toastType) }}</span>
        <span class="toast-text">{{ toastMessage }}</span>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'TechDashboard',
  setup() {
    // 表单数据
    const locationForm = reactive({
      carName: '',
      latitude: 32.3931,
      longitude: 119.4128,
      status: 'active'
    })

    const routeForm = reactive({
      startPoint: '',
      endPoint: ''
    })

    // 状态数据
    const cars = ref([])
    const routeResult = ref(null)
    const toastMessage = ref('')
    const toastType = ref('')
    const currentTime = ref(new Date().toLocaleString())
    const monitorTab = ref('all')

    // 计算属性
    const activeCarsCount = computed(() => {
      return cars.value.filter(car => car.status === 'active').length
    })

    // 更新时间定时器
    let timeInterval = null

    // 更新车辆位置
    const updateLocation = async () => {
      try {
        const response = await axios.post('/api/map/location', locationForm)
        showMessage('车辆位置更新成功！', 'success')
        loadCars()
      } catch (error) {
        showMessage('更新失败：' + error.message, 'error')
      }
    }

    // 智能路径规划
    const planRoute = async () => {
      if (!routeForm.startPoint || !routeForm.endPoint) {
        showMessage('请输入起点和终点', 'warning')
        return
      }

      try {
        const response = await axios.get(`/api/map/route?start=${encodeURIComponent(routeForm.startPoint)}&end=${encodeURIComponent(routeForm.endPoint)}`)
        routeResult.value = response.data
      } catch (error) {
        showMessage('路线规划失败：' + error.message, 'error')
      }
    }

    // 加载所有车辆
    const loadCars = async () => {
      monitorTab.value = 'all'
      try {
        const response = await axios.get('/api/map/locations')
        cars.value = response.data
      } catch (error) {
        showMessage('加载车辆列表失败', 'error')
      }
    }

    // 加载运行中的车辆
    const loadActiveCars = async () => {
      monitorTab.value = 'active'
      try {
        const response = await axios.get('/api/map/active-cars')
        cars.value = response.data
      } catch (error) {
        showMessage('加载运行中车辆失败', 'error')
      }
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'active': '运行中',
        'inactive': '停止',
        'maintenance': '维修中'
      }
      return statusMap[status] || status
    }

    // 获取消息图标
    const getToastIcon = (type) => {
      const iconMap = {
        'success': '✅',
        'error': '❌',
        'warning': '⚠️'
      }
      return iconMap[type] || 'ℹ️'
    }

    // 显示消息提示
    const showMessage = (message, type) => {
      toastMessage.value = message
      toastType.value = type
      
      setTimeout(() => {
        toastMessage.value = ''
        toastType.value = ''
      }, 3000)
    }

    // 初始化
    onMounted(() => {
      loadCars()
      timeInterval = setInterval(() => {
        currentTime.value = new Date().toLocaleString()
      }, 1000)
    })

    return {
      locationForm,
      routeForm,
      cars,
      routeResult,
      toastMessage,
      toastType,
      currentTime,
      monitorTab,
      activeCarsCount,
      updateLocation,
      planRoute,
      loadCars,
      loadActiveCars,
      getStatusText,
      getToastIcon
    }
  }
}
</script>

<style scoped>
.tech-dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
  color: #ffffff;
  font-family: 'Arial', sans-serif;
  padding: 20px;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 15px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.system-title {
  font-size: 2.5rem;
  margin: 0 0 20px 0;
  background: linear-gradient(45deg, #00d2ff, #00ff9d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 20px rgba(0, 210, 255, 0.3);
}

.title-icon {
  margin-right: 15px;
  filter: drop-shadow(0 0 10px rgba(0, 210, 255, 0.5));
}

.header-stats {
  display: flex;
  justify-content: center;
  gap: 30px;
  flex-wrap: wrap;
}

.stat-item {
  background: rgba(255, 255, 255, 0.08);
  padding: 15px 25px;
  border-radius: 12px;
  text-align: center;
  border: 1px solid rgba(0, 210, 255, 0.3);
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 210, 255, 0.2);
  border-color: rgba(0, 210, 255, 0.6);
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  color: #a0a0c0;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #00d2ff;
}

.stat-value.active {
  color: #00ff9d;
  text-shadow: 0 0 15px rgba(0, 255, 157, 0.4);
}

.control-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}

.control-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 15px;
  padding: 25px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.control-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(0, 210, 255, 0.4);
}

.location-card {
  border-top: 3px solid #00d2ff;
}

.route-card {
  border-top: 3px solid #00ff9d;
}

.monitor-card {
  border-top: 3px solid #ff4757;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-icon {
  font-size: 1.8rem;
  margin-right: 15px;
  filter: drop-shadow(0 0 8px rgba(0, 210, 255, 0.5));
}

.card-header h2 {
  margin: 0;
  font-size: 1.4rem;
  color: #00d2ff;
  text-shadow: 0 0 10px rgba(0, 210, 255, 0.3);
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  color: #a0a0c0;
  font-size: 0.95rem;
}

.input-group input,
.input-group select,
.input-group textarea {
  width: 100%;
  padding: 12px 15px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(0, 210, 255, 0.3);
  border-radius: 10px;
  color: #e0e0e0;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.input-group input:focus,
.input-group select:focus,
.input-group textarea:focus {
  outline: none;
  border-color: #00d2ff;
  box-shadow: 0 0 15px rgba(0, 210, 255, 0.3);
  background: rgba(255, 255, 255, 0.12);
}

.coordinate-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 20px;
}

.action-btn {
  width: 100%;
  padding: 14px 20px;
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn-icon {
  font-size: 1.3rem;
  filter: drop-shadow(0 0 5px currentColor);
}

.primary {
  background: linear-gradient(45deg, #00d2ff, #00ff9d);
  color: #000;
  box-shadow: 0 5px 15px rgba(0, 210, 255, 0.4);
}

.primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 25px rgba(0, 210, 255, 0.6);
}

.secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #00d2ff;
  border: 1px solid rgba(0, 210, 255, 0.5);
}

.secondary:hover {
  background: rgba(0, 210, 255, 0.2);
  transform: translateY(-2px);
}

.route-result {
  margin-top: 25px;
  padding: 20px;
  background: rgba(0, 255, 157, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(0, 255, 157, 0.3);
}

.result-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.result-icon {
  font-size: 1.5rem;
  margin-right: 10px;
  filter: drop-shadow(0 0 8px rgba(0, 255, 157, 0.5));
}

.result-header h3 {
  margin: 0;
  color: #00ff9d;
  font-size: 1.2rem;
}

.result-details {
  display: grid;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(0, 255, 157, 0.2);
}

.detail-label {
  color: #a0a0c0;
  font-size: 0.95rem;
}

.detail-value {
  color: #00ff9d;
  font-weight: 600;
  font-size: 1.05rem;
}

.monitor-controls {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: #a0a0c0;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #ffffff;
}

.tab-btn.active {
  background: linear-gradient(45deg, #00d2ff, #00ff9d);
  color: #000;
  border-color: transparent;
  box-shadow: 0 0 15px rgba(0, 210, 255, 0.4);
}

.vehicle-list,
.track-list {
  max-height: 400px;
  overflow-y: auto;
}

.vehicle-item,
.track-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.vehicle-item:hover,
.track-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(0, 210, 255, 0.3);
  transform: translateX(5px);
}

.vehicle-header,
.track-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.vehicle-id,
.track-id {
  font-size: 1.2rem;
  font-weight: 600;
  color: #ffffff;
}

.status-tag,
.track-status {
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.status-tag.active,
.track-status.active {
  background: rgba(0, 255, 157, 0.2);
  color: #00ff9d;
  border: 1px solid rgba(0, 255, 157, 0.4);
}

.status-tag.inactive,
.track-status.inactive {
  background: rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
  border: 1px solid rgba(255, 107, 107, 0.4);
}

.status-tag.maintenance {
  background: rgba(255, 165, 2, 0.2);
  color: #ffa502;
  border: 1px solid rgba(255, 165, 2, 0.4);
}

.vehicle-details,
.track-details {
  display: grid;
  gap: 10px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-key {
  color: #a0a0c0;
  font-size: 0.9rem;
}

.detail-value {
  color: #e0e0e0;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #a0a0c0;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  filter: grayscale(100%);
}

.toast-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 25px;
  border-radius: 10px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  z-index: 1000;
  backdrop-filter: blur(10px);
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.toast-success {
  background: rgba(0, 255, 157, 0.2);
  border: 1px solid rgba(0, 255, 157, 0.4);
  color: #00ff9d;
  box-shadow: 0 0 20px rgba(0, 255, 157, 0.3);
}

.toast-error {
  background: rgba(255, 71, 87, 0.2);
  border: 1px solid rgba(255, 71, 87, 0.4);
  color: #ff4757;
  box-shadow: 0 0 20px rgba(255, 71, 87, 0.3);
}

.toast-warning {
  background: rgba(255, 165, 2, 0.2);
  border: 1px solid rgba(255, 165, 2, 0.4);
  color: #ffa502;
  box-shadow: 0 0 20px rgba(255, 165, 2, 0.3);
}

.toast-icon {
  font-size: 1.2rem;
  filter: drop-shadow(0 0 5px currentColor);
}

/* ... existing styles ... */

.group-card {
  border-top: 3px solid #ff6b6b;
}

.group-form textarea {
  width: 100%;
  padding: 12px 15px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 107, 107, 0.3);
  border-radius: 10px;
  color: #e0e0e0;
  font-size: 1rem;
  min-height: 80px;
  resize: vertical;
}

.button-group {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  flex-wrap: wrap;
}

.button-group .action-btn {
  flex: 1;
  min-width: 120px;
}

.group-list {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.group-list h3 {
  color: #ff6b6b;
  margin-bottom: 15px;
  text-align: center;
}

.group-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 107, 107, 0.2);
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
}

.group-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 107, 107, 0.4);
}

.group-info {
  flex: 1;
}

.group-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 5px;
}

.group-desc {
  font-size: 0.9rem;
  color: #a0a0c0;
  margin-bottom: 8px;
}

.group-status {
  font-size: 0.85rem;
  font-weight: 500;
}

.group-status.active {
  color: #00ff9d;
}

.group-status.inactive {
  color: #ff6b6b;
}

.group-actions {
  display: flex;
  gap: 8px;
}

.icon-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 5px;
  border-radius: 5px;
  transition: all 0.3s ease;
}

.icon-btn.edit:hover {
  background: rgba(0, 210, 255, 0.2);
  transform: scale(1.1);
}

.icon-btn.delete:hover {
  background: rgba(255, 107, 107, 0.2);
  transform: scale(1.1);
}

.group-view {
  max-height: 500px;
  overflow-y: auto;
}

.group-summary {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 107, 107, 0.2);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.group-header h3 {
  color: #ff6b6b;
  margin: 0;
}

.group-status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.group-status-badge.active {
  background: rgba(0, 255, 157, 0.2);
  color: #00ff9d;
}

.group-status-badge.inactive {
  background: rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
}

.group-description {
  color: #a0a0c0;
  font-size: 0.95rem;
  margin-bottom: 15px;
}

.group-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
}

.stat {
  text-align: center;
}

.stat-label {
  display: block;
  color: #a0a0c0;
  font-size: 0.85rem;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 1.3rem;
  font-weight: 700;
  color: #00d2ff;
}

.stat-value.active {
  color: #00ff9d;
}

.group-cars {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
}

.mini-car {
  background: rgba(0, 210, 255, 0.1);
  border: 1px solid rgba(0, 210, 255, 0.3);
  border-radius: 8px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.car-icon {
  font-size: 1.5rem;
}

.car-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: #ffffff;
}

.car-status {
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 10px;
}

.car-status.active {
  background: rgba(0, 255, 157, 0.2);
  color: #00ff9d;
}

.car-status.inactive {
  background: rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
}

.stat-value.group {
  color: #ff6b6b;
}

.warning {
  background: linear-gradient(45deg, #ffa502, #ff6b6b);
  color: white;
}

/* ... existing styles ... */

.alert-card {
  border-top: 3px solid #ff4757;
}

.fence-form {
  margin-bottom: 25px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  border: 1px solid rgba(255, 71, 87, 0.2);
}

.fence-form h3 {
  color: #ff4757;
  margin-top: 0;
  margin-bottom: 15px;
  text-align: center;
}

.fence-list h3,
.alert-section h3 {
  color: #ff4757;
  margin: 20px 0 15px 0;
  text-align: center;
}

.fence-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 71, 87, 0.2);
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fence-info {
  flex: 1;
}

.fence-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 8px;
}

.fence-details {
  display: flex;
  gap: 15px;
  font-size: 0.9rem;
  color: #a0a0c0;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.fence-status {
  font-weight: 500;
}

.fence-status.active {
  color: #00ff9d;
}

.fence-status.inactive {
  color: #ff6b6b;
}

.fence-alert-type {
  font-size: 0.85rem;
  color: #00d2ff;
}

.alert-section {
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.alert-controls {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.alert-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 71, 87, 0.2);
  border-radius: 12px;
  padding: 18px;
  margin-bottom: 15px;
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 10px;
}

.alert-car {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffffff;
}

.alert-type {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.alert-type.entry {
  background: rgba(0, 210, 255, 0.2);
  color: #00d2ff;
}

.alert-type.exit {
  background: rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
}

.alert-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
}

.alert-status.unhandled {
  background: rgba(255, 71, 87, 0.2);
  color: #ff4757;
}

.alert-status.handled {
  background: rgba(0, 255, 157, 0.2);
  color: #00ff9d;
}

.alert-details {
  margin-bottom: 15px;
}

.alert-actions {
  text-align: right;
}

.stat-value.alert {
  color: #ff4757;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
}

/* ... existing styles ... */

.stats-card {
  border-top: 3px solid #00d2ff;
}

.stats-controls {
  margin-bottom: 25px;
}

.date-range {
  display: flex;
  gap: 15px;
  align-items: end;
  margin-top: 15px;
  flex-wrap: wrap;
}

.date-range .input-group {
  flex: 1;
  min-width: 120px;
}

.stats-overview {
  margin-bottom: 25px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  background: linear-gradient(135deg, rgba(0, 210, 255, 0.1), rgba(0, 255, 157, 0.1));
  border: 1px solid rgba(0, 210, 255, 0.3);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 210, 255, 0.2);
}

.stat-icon {
  font-size: 2rem;
  filter: drop-shadow(0 0 8px rgba(0, 210, 255, 0.5));
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
  color: #00d2ff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.9rem;
  color: #a0a0c0;
}

.stats-detail h3 {
  color: #00d2ff;
  margin: 25px 0 15px 0;
  text-align: center;
}

.stats-table {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(0, 210, 255, 0.2);
}

.table-header {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  background: rgba(0, 210, 255, 0.1);
  padding: 12px 15px;
  font-weight: 600;
  color: #00d2ff;
  border-bottom: 1px solid rgba(0, 210, 255, 0.3);
}

.table-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  padding: 12px 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background 0.3s ease;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row:hover {
  background: rgba(0, 210, 255, 0.05);
}

.table-cell {
  padding: 5px;
  text-align: center;
  color: #e0e0e0;
  font-size: 0.9rem;
}

/* ... existing styles ... */

.track-list {
  max-height: 400px;
  overflow-y: auto;
}

.track-display h4 {
  color: #00d2ff;
  margin-bottom: 15px;
  text-align: center;
}

.track-points {
  margin-bottom: 20px;
  max-height: 150px;
  overflow-y: auto;
}

.track-point {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(0, 210, 255, 0.2);
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
}

.point-time {
  color: #00d2ff;
  font-weight: 500;
}

.point-coords {
  color: #e0e0e0;
}

.point-speed {
  color: #00ff9d;
  font-weight: 600;
}

.track-map {
  margin-top: 20px;
  height: 200px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
  border: 1px solid rgba(0, 210, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.map-placeholder {
  width: 100%;
  height: 100%;
  position: relative;
  background: 
    linear-gradient(45deg, transparent 49%, rgba(0, 210, 255, 0.1) 49%, rgba(0, 210, 255, 0.1) 51%, transparent 51%),
    linear-gradient(-45deg, transparent 49%, rgba(0, 210, 255, 0.1) 49%, rgba(0, 210, 255, 0.1) 51%, transparent 51%);
  background-size: 20px 20px;
}

.trajectory-line {
  position: absolute;
  top: 50%;
  left: 10%;
  width: 80%;
  height: 2px;
  background: linear-gradient(90deg, #00d2ff, #00ff9d);
  transform: translateY(-50%);
  box-shadow: 0 0 10px rgba(0, 210, 255, 0.5);
}

.track-marker {
  position: absolute;
  font-size: 20px;
  filter: drop-shadow(0 0 5px rgba(0, 210, 255, 0.8));
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translate(-50%, -50%) translateY(0); }
  50% { transform: translate(-50%, -50%) translateY(-10px); }
}
</style>