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
  color: #e0e0e0;
  font-family: 'Segoe UI', system-ui, sans-serif;
  position: relative;
  overflow-x: hidden;
}

.tech-dashboard::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 10% 20%, rgba(58, 123, 213, 0.1) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(0, 210, 255, 0.1) 0%, transparent 20%),
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 2px,
      rgba(58, 123, 213, 0.05) 2px,
      rgba(58, 123, 213, 0.05) 4px
    );
  pointer-events: none;
  z-index: 1;
}

.dashboard-header {
  background: rgba(15, 12, 41, 0.8);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(58, 123, 213, 0.3);
  padding: 20px 40px;
  position: relative;
  z-index: 2;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
}

.system-title {
  font-size: 2.2rem;
  font-weight: 700;
  background: linear-gradient(45deg, #00d2ff, #3a7bd5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-icon {
  font-size: 2.5rem;
  filter: drop-shadow(0 0 10px rgba(0, 210, 255, 0.5));
}

.header-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  color: #a0a0c0;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #00d2ff;
}

.stat-value.active {
  color: #00ff9d;
  text-shadow: 0 0 10px rgba(0, 255, 157, 0.5);
}

.control-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 25px;
  padding: 30px 40px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

.control-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(58, 123, 213, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(15px);
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.control-card:hover {
  transform: translateY(-5px);
  border-color: rgba(58, 123, 213, 0.4);
  box-shadow: 0 15px 40px rgba(0, 210, 255, 0.2);
}

.location-card {
  border-top: 3px solid #00d2ff;
}

.route-card {
  border-top: 3px solid #ff6b6b;
}

.monitor-card {
  border-top: 3px solid #00ff9d;
}

.card-header {
  padding: 20px 25px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-icon {
  font-size: 1.8rem;
  filter: drop-shadow(0 0 8px currentColor);
}

.card-header h2 {
  font-size: 1.4rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
}

.card-content {
  padding: 25px;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  color: #a0a0c0;
  font-size: 0.95rem;
  font-weight: 500;
}

.input-group input,
.input-group select {
  width: 100%;
  padding: 12px 15px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(58, 123, 213, 0.3);
  border-radius: 10px;
  color: #e0e0e0;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.input-group input:focus,
.input-group select:focus {
  outline: none;
  border-color: #00d2ff;
  box-shadow: 0 0 0 3px rgba(0, 210, 255, 0.2);
  background: rgba(255, 255, 255, 0.12);
}

.coordinate-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.action-btn {
  width: 100%;
  padding: 14px 20px;
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.action-btn.primary {
  background: linear-gradient(45deg, #00d2ff, #3a7bd5);
  color: #0f0c29;
  box-shadow: 0 5px 20px rgba(0, 210, 255, 0.4);
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 210, 255, 0.6);
}

.action-btn.secondary {
  background: linear-gradient(45deg, #ff6b6b, #ffa502);
  color: white;
  box-shadow: 0 5px 20px rgba(255, 107, 107, 0.4);
}

.action-btn.secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.6);
}

.route-result {
  margin-top: 25px;
  background: rgba(0, 210, 255, 0.1);
  border: 1px solid rgba(0, 210, 255, 0.3);
  border-radius: 12px;
  padding: 20px;
  animation: fadeIn 0.5s ease;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.result-icon {
  font-size: 1.5rem;
}

.result-header h3 {
  font-size: 1.2rem;
  color: #00d2ff;
  margin: 0;
}

.result-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  color: #a0a0c0;
  font-weight: 500;
}

.detail-value {
  color: #e0e0e0;
  font-weight: 600;
}

.monitor-controls {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(58, 123, 213, 0.3);
  border-radius: 8px;
  color: #a0a0c0;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(58, 123, 213, 0.5);
}

.tab-btn.active {
  background: linear-gradient(45deg, #00d2ff, #3a7bd5);
  color: #0f0c29;
  border-color: #00d2ff;
  box-shadow: 0 0 15px rgba(0, 210, 255, 0.3);
}

.vehicle-list {
  max-height: 400px;
  overflow-y: auto;
}

.vehicle-list::-webkit-scrollbar {
  width: 6px;
}

.vehicle-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.vehicle-list::-webkit-scrollbar-thumb {
  background: linear-gradient(45deg, #00d2ff, #3a7bd5);
  border-radius: 3px;
}

.vehicle-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 18px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.vehicle-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(58, 123, 213, 0.3);
  transform: translateX(5px);
}

.vehicle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.vehicle-id {
  font-size: 1.1rem;
  font-weight: 600;
  color: #ffffff;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-tag.active {
  background: rgba(0, 255, 157, 0.2);
  color: #00ff9d;
  border: 1px solid rgba(0, 255, 157, 0.4);
}

.status-tag.inactive {
  background: rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
  border: 1px solid rgba(255, 107, 107, 0.4);
}

.status-tag.maintenance {
  background: rgba(255, 210, 0, 0.2);
  color: #ffd200;
  border: 1px solid rgba(255, 210, 0, 0.4);
}

.vehicle-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
}

.detail-key {
  color: #a0a0c0;
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
  opacity: 0.5;
}

.toast-notification {
  position: fixed;
  top: 25px;
  right: 25px;
  padding: 15px 25px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
  z-index: 1000;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
  animation: slideIn 0.3s ease;
}

.toast-notification.success {
  background: linear-gradient(45deg, #00ff9d, #00cc7a);
  color: #0f0c29;
}

.toast-notification.error {
  background: linear-gradient(45deg, #ff6b6b, #ff3b3b);
  color: white;
}

.toast-notification.warning {
  background: linear-gradient(45deg, #ffd200, #ffaa00);
  color: #0f0c29;
}

.toast-enter-active, .toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideIn {
  from { transform: translateX(100%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}

@media (max-width: 768px) {
  .control-panel {
    grid-template-columns: 1fr;
    padding: 20px 15px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .coordinate-grid {
    grid-template-columns: 1fr;
  }
  
  .monitor-controls {
    flex-direction: column;
  }
}
</style>