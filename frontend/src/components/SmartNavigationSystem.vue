<template>
  <div class="smart-navigation-system">
    <!-- 头部 -->
    <div class="header">
      <h1><i class="fas fa-car"></i> 智能汽车导航系统</h1>
      <p>实时车辆监控 | 智能路径规划 | 云端数据同步</p>
    </div>

    <!-- 主容器 -->
    <div class="container">
      <!-- 车辆定位更新卡片 -->
      <div class="card">
        <div class="card-header">
          <i class="fas fa-location-dot"></i>
          <h2>车辆定位更新</h2>
        </div>
        <div class="form-group">
          <label><i class="fas fa-hashtag"></i> 车牌号码</label>
          <input 
            v-model="locationForm.carName" 
            type="text" 
            class="form-control" 
            placeholder="请输入车牌号 如: 苏K12345"
          >
        </div>
        <div class="form-group">
          <label><i class="fas fa-globe"></i> 纬度坐标</label>
          <input 
            v-model.number="locationForm.latitude" 
            type="number" 
            class="form-control" 
            step="0.0001" 
            placeholder="32.3931"
          >
        </div>
        <div class="form-group">
          <label><i class="fas fa-globe-americas"></i> 经度坐标</label>
          <input 
            v-model.number="locationForm.longitude" 
            type="number" 
            class="form-control" 
            step="0.0001" 
            placeholder="119.4128"
          >
        </div>
        <div class="form-group">
          <label><i class="fas fa-tachometer-alt"></i> 车辆状态</label>
          <select v-model="locationForm.status" class="form-control status-select">
            <option value="active">🟢 运行中</option>
            <option value="inactive">🔴 停止</option>
            <option value="maintenance">🟡 维修中</option>
          </select>
        </div>
        <button class="btn" @click="updateLocation">
          <i class="fas fa-sync-alt"></i> 更新位置
        </button>
      </div>

      <!-- 智能路径规划卡片 -->
      <div class="card">
        <div class="card-header">
          <i class="fas fa-route"></i>
          <h2>智能路径规划</h2>
        </div>
        <div class="form-group">
          <label><i class="fas fa-play-circle"></i> 起始地点</label>
          <input 
            v-model="routeForm.startPoint" 
            type="text" 
            class="form-control" 
            placeholder="请输入起点 如: 扬州"
          >
        </div>
        <div class="form-group">
          <label><i class="fas fa-flag-checkered"></i> 目标地点</label>
          <input 
            v-model="routeForm.endPoint" 
            type="text" 
            class="form-control" 
            placeholder="请输入终点 如: 南京"
          >
        </div>
        <button class="btn" @click="planRoute">
          <i class="fas fa-brain"></i> 智能规划
        </button>
        <div v-if="routeResult" id="routeResult">
          <div class="route-info pulse">
            <h3><i class="fas fa-map-signs"></i> 最优路径</h3>
            <div class="route-detail">
              <span class="route-label"><i class="fas fa-road"></i> 行驶距离</span>
              <span class="route-value">{{ routeResult.distance }} 公里</span>
            </div>
            <div class="route-detail">
              <span class="route-label"><i class="fas fa-play"></i> 起点</span>
              <span class="route-value">{{ routeResult.startPoint }}</span>
            </div>
            <div class="route-detail">
              <span class="route-label"><i class="fas fa-flag"></i> 终点</span>
              <span class="route-value">{{ routeResult.endPoint }}</span>
            </div>
            <div class="route-detail">
              <span class="route-label"><i class="fas fa-info-circle"></i> 路线详情</span>
              <span class="route-value">{{ routeResult.routeInfo }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 车队实时监控卡片 -->
      <div class="card">
        <div class="card-header">
          <i class="fas fa-car-side"></i>
          <h2>车队实时监控</h2>
        </div>
        <div class="btn-group">
          <button class="btn" @click="loadCars">
            <i class="fas fa-sync"></i> 全部车辆
          </button>
          <button class="btn" @click="loadActiveCars">
            <i class="fas fa-bolt"></i> 运行中
          </button>
        </div>
        <div id="carList" class="car-list">
          <div v-if="cars.length === 0" class="no-data">
            暂无车辆数据
          </div>
          <div v-else class="car-item" v-for="car in cars" :key="car.id">
            <div><strong><i class="fas fa-car"></i> 车牌:</strong> {{ car.carName }}</div>
            <div><strong><i class="fas fa-map-marker-alt"></i> 位置:</strong> ({{ car.latitude }}, {{ car.longitude }})</div>
            <div><strong><i class="fas fa-heartbeat"></i> 状态:</strong> 
              <span :class="['status-badge', 'status-' + car.status]">
                {{ getStatusText(car.status) }}
              </span>
            </div>
            <div><strong><i class="fas fa-clock"></i> 时间:</strong> {{ currentTime }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="toastMessage" :class="['toast', toastType]" v-html="toastMessage"></div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'SmartNavigationSystem',
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
      try {
        const response = await axios.get('/api/map/locations')
        cars.value = response.data
      } catch (error) {
        showMessage('加载车辆列表失败', 'error')
      }
    }

    // 加载运行中的车辆
    const loadActiveCars = async () => {
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

    // 显示消息提示
    const showMessage = (message, type) => {
      toastMessage.value = `<i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'exclamation-triangle'}"></i> ${message}`
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
      updateLocation,
      planRoute,
      loadCars,
      loadActiveCars,
      getStatusText
    }
  }
}
</script>

<style scoped>
:root {
  --primary-color: #00ffff;
  --secondary-color: #0066ff;
  --background-dark: #0a0a1a;
  --card-bg: #121224;
  --text-primary: #ccc;
  --text-secondary: #a0a0c0;
  --accent-glow: 0 0 15px rgba(0, 255, 255, 0.5);
  --border-gradient: linear-gradient(45deg, #00ffff, #0066ff);
}

.smart-navigation-system {
  min-height: 100vh;
  background: var(--background-dark);
  color: var(--text-primary);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(0, 255, 255, 0.1) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(0, 102, 255, 0.1) 0%, transparent 20%);
  position: relative;
  overflow-x: hidden;
}

.smart-navigation-system::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    linear-gradient(transparent 50%, rgba(10, 10, 26, 0.8) 100%),
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 2px,
      rgba(0, 255, 255, 0.03) 2px,
      rgba(0, 255, 255, 0.03) 4px
    );
  pointer-events: none;
  z-index: -1;
}

.header {
  text-align: center;
  padding: 40px 20px;
  background: rgba(18, 18, 36, 0.6);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 255, 255, 0.2);
  position: relative;
}

.header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 200px;
  height: 2px;
  background: var(--border-gradient);
  border-radius: 2px;
}

.header h1 {
  font-size: 2.5rem;
  margin-bottom: 15px;
  background: var(--border-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: var(--accent-glow);
}

.header p {
  font-size: 1.1rem;
  color: var(--text-secondary);
  letter-spacing: 1px;
}

.container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.card {
  background: var(--card-bg);
  border: 1px solid rgba(0, 255, 255, 0.2);
  border-radius: 15px;
  padding: 25px;
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--border-gradient);
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 255, 255, 0.2);
  border-color: rgba(0, 255, 255, 0.4);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.card-header i {
  font-size: 1.8rem;
  color: var(--primary-color);
  text-shadow: var(--accent-glow);
}

.card-header h2 {
  font-size: 1.5rem;
  color: var(--text-primary);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 0.95rem;
}

.form-control {
  width: 100%;
  padding: 12px 15px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 1rem;
  transition: all 0.3s ease;
}

.form-control:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(0, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
}

.form-control::placeholder {
  color: rgba(160, 160, 192, 0.6);
}

.btn {
  background: linear-gradient(45deg, var(--primary-color), var(--secondary-color));
  color: var(--background-dark);
  border: none;
  padding: 12px 25px;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 5px 15px rgba(0, 255, 255, 0.3);
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 255, 255, 0.5);
}

.btn-group {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.btn-group .btn {
  flex: 1;
  min-width: 120px;
  justify-content: center;
}

.car-list {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}

.car-list::-webkit-scrollbar {
  width: 6px;
}

.car-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.car-list::-webkit-scrollbar-thumb {
  background: var(--primary-color);
  border-radius: 3px;
}

.car-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.car-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(0, 255, 255, 0.3);
  transform: translateX(5px);
}

.car-item div {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.car-item strong {
  color: var(--text-secondary);
  min-width: 70px;
}

.no-data {
  text-align: center;
  color: var(--text-secondary);
  padding: 20px;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.status-active {
  background: rgba(0, 255, 100, 0.2);
  color: #00ff64;
  border: 1px solid rgba(0, 255, 100, 0.4);
}

.status-inactive {
  background: rgba(255, 100, 0, 0.2);
  color: #ff6400;
  border: 1px solid rgba(255, 100, 0, 0.4);
}

.status-maintenance {
  background: rgba(255, 255, 0, 0.2);
  color: #ffff00;
  border: 1px solid rgba(255, 255, 0, 0.4);
}

.route-info {
  background: rgba(0, 102, 255, 0.1);
  border: 1px solid rgba(0, 102, 255, 0.3);
  border-radius: 10px;
  padding: 20px;
  margin-top: 20px;
}

.route-info h3 {
  color: var(--primary-color);
  margin-bottom: 15px;
  text-align: center;
  font-size: 1.3rem;
}

.route-detail {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.route-detail:last-child {
  border-bottom: none;
}

.route-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.route-value {
  color: var(--text-primary);
  font-weight: 600;
}

.pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 25px;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  z-index: 1000;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
  animation: slideIn 0.3s ease;
}

.toast.success {
  background: linear-gradient(45deg, #00ff64, #00cc52);
}

.toast.error {
  background: linear-gradient(45deg, #ff4757, #ff3742);
}

.toast.warning {
  background: linear-gradient(45deg, #ffa502, #ff9500);
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

@media (max-width: 768px) {
  .container {
    grid-template-columns: 1fr;
    padding: 20px 15px;
  }
  
  .header h1 {
    font-size: 2rem;
  }
  
  .btn-group {
    flex-direction: column;
  }
}
</style>