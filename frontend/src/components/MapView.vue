<template>
  <div class="map-container">
    <header class="header">
      <h1>智能车辆监控系统</h1>
      <div class="controls">
        <button @click="refreshData" :disabled="loading" class="btn-primary">
          {{ loading ? '刷新中...' : '刷新数据' }}
        </button>
        <button @click="toggleAutoRefresh" class="btn-secondary">
          {{ autoRefresh ? '停止自动刷新' : '开启自动刷新' }}
        </button>
      </div>
    </header>
    
    <div class="dashboard">
      <div class="stats-card">
        <h3>车辆统计</h3>
        <div class="stat-item">
          <span class="label">在线车辆:</span>
          <span class="value">{{ carLocations.length }}</span>
        </div>
        <div class="stat-item">
          <span class="label">活跃路线:</span>
          <span class="value">{{ routes.length }}</span>
        </div>
      </div>
      
      <div class="map-view">
        <div id="map" ref="mapRef"></div>
        <div class="map-overlay" v-if="selectedCar">
          <h4>车辆详情</h4>
          <p>车牌号: {{ selectedCar.licensePlate }}</p>
          <p>速度: {{ selectedCar.speed }} km/h</p>
          <p>状态: {{ selectedCar.status }}</p>
          <button @click="selectedCar = null" class="btn-close">×</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

export default {
  name: 'MapView',
  setup() {
    const mapRef = ref(null)
    const carLocations = ref([])
    const routes = ref([])
    const loading = ref(false)
    const autoRefresh = ref(false)
    const selectedCar = ref(null)
    let refreshInterval = null
    
    const fetchData = async () => {
      try {
        loading.value = true
        const [carsResponse, routesResponse] = await Promise.all([
          axios.get('/api/car-locations'),
          axios.get('/api/routes')
        ])
        carLocations.value = carsResponse.data
        routes.value = routesResponse.data
        updateMap()
      } catch (error) {
        console.error('数据获取失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    const refreshData = () => {
      fetchData()
    }
    
    const toggleAutoRefresh = () => {
      autoRefresh.value = !autoRefresh.value
      if (autoRefresh.value) {
        refreshInterval = setInterval(fetchData, 5000)
      } else {
        clearInterval(refreshInterval)
      }
    }
    
    const updateMap = () => {
      if (!mapRef.value) return
      
      const mapContainer = mapRef.value
      mapContainer.innerHTML = ''
      
      carLocations.value.forEach(car => {
        const marker = document.createElement('div')
        marker.className = 'car-marker'
        marker.style.cssText = `
          position: absolute;
          width: 20px;
          height: 20px;
          background: #ff4757;
          border-radius: 50%;
          border: 2px solid white;
          cursor: pointer;
          transform: translate(-50%, -50%);
          left: ${50 + car.longitude * 0.1}%;
          top: ${50 - car.latitude * 0.1}%;
          box-shadow: 0 0 10px rgba(255, 71, 87, 0.5);
          transition: all 0.3s ease;
        `
        marker.title = `${car.licensePlate} - ${car.speed}km/h`
        marker.onclick = () => {
          selectedCar.value = car
        }
        mapContainer.appendChild(marker)
      })
    }
    
    onMounted(() => {
      fetchData()
    })
    
    onUnmounted(() => {
      if (refreshInterval) {
        clearInterval(refreshInterval)
      }
    })
    
    return {
      mapRef,
      carLocations,
      routes,
      loading,
      autoRefresh,
      selectedCar,
      refreshData,
      toggleAutoRefresh
    }
  }
}
</script>

<style scoped>
.map-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.header h1 {
  color: white;
  margin: 0;
  font-size: 2rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.controls {
  display: flex;
  gap: 10px;
}

.btn-primary, .btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.btn-primary {
  background: linear-gradient(45deg, #ff6b6b, #ffa502);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 107, 0.4);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.3);
}

.dashboard {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  padding: 20px;
  height: calc(100vh - 80px);
}

.stats-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stats-card h3 {
  margin-top: 0;
  margin-bottom: 20px;
  text-align: center;
  font-size: 1.3rem;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
}

.label {
  font-weight: bold;
}

.value {
  font-size: 1.2rem;
  font-weight: bold;
  color: #4ade80;
}

.map-view {
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

#map {
  width: 100%;
  height: 100%;
  position: relative;
  background: linear-gradient(45deg, #1e3c72, #2a5298);
  min-height: 500px;
}

.car-marker:hover {
  transform: translate(-50%, -50%) scale(1.2) !important;
  z-index: 1000;
}

.map-overlay {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px;
  min-width: 250px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.map-overlay h4 {
  margin-top: 0;
  color: #333;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}

.map-overlay p {
  margin: 10px 0;
  color: #555;
}

.btn-close {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #ff4757;
  color: white;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
}

@media (max-width: 768px) {
  .dashboard {
    grid-template-columns: 1fr;
  }
  
  .header {
    flex-direction: column;
    gap: 15px;
  }
  
  .controls {
    width: 100%;
    justify-content: center;
  }
}
</style>