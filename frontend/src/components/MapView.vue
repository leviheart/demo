<template>
  <div class="map-view-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><MapLocation /></el-icon>
          <h1>实时地图监控</h1>
        </div>
        <div class="header-controls">
          <el-button-group>
            <el-button @click="centerOnVehicles" type="primary" size="small">
              <el-icon><Position /></el-icon>定位车辆
            </el-button>
            <el-button @click="toggleTrail" size="small">
              <el-icon><component :is="showTrail ? 'Hide' : 'View'"></component></el-icon>
              {{ showTrail ? '隐藏轨迹' : '显示轨迹' }}
            </el-button>
          </el-button-group>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <el-row :gutter="20">
        <!-- 地图区域 -->
        <el-col :span="18">
          <el-card class="map-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">车辆实时位置</span>
                <el-tag :type="vehicles.length > 0 ? 'success' : 'warning'">
                  在线车辆: {{ vehicles.length }}
                </el-tag>
              </div>
            </template>
            <div ref="mapContainer" class="map-container"></div>
          </el-card>
        </el-col>

        <!-- 车辆信息面板 -->
        <el-col :span="6">
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">车辆状态</span>
                <el-button @click="refreshVehicles" size="small" circle>
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </template>
            
            <div class="vehicle-list" v-loading="loading">
              <div 
                v-for="vehicle in vehicles" 
                :key="vehicle.id"
                class="vehicle-item"
                :class="{ 'selected': selectedVehicle?.id === vehicle.id }"
                @click="selectVehicle(vehicle)"
              >
                <div class="vehicle-header">
                  <div class="vehicle-icon">
                    <el-icon><Van /></el-icon>
                  </div>
                  <div class="vehicle-info">
                    <h4 class="vehicle-name">{{ vehicle.carName }}</h4>
                    <span class="vehicle-id">#{{ vehicle.id }}</span>
                  </div>
                  <el-tag 
                    :type="vehicle.speed > 60 ? 'danger' : vehicle.speed > 30 ? 'warning' : 'success'" 
                    size="small"
                  >
                    {{ vehicle.speed }} km/h
                  </el-tag>
                </div>
                
                <div class="vehicle-details">
                  <div class="detail-row">
                    <el-icon class="detail-icon"><Location /></el-icon>
                    <span class="detail-text">{{ vehicle.latitude.toFixed(6) }}, {{ vehicle.longitude.toFixed(6) }}</span>
                  </div>
                  <div class="detail-row">
                    <el-icon class="detail-icon"><Clock /></el-icon>
                    <span class="detail-text">{{ formatTime(vehicle.updateTime) }}</span>
                  </div>
                  <div class="detail-row">
                    <el-icon class="detail-icon"><Compass /></el-icon>
                    <span class="detail-text">方向: {{ vehicle.direction }}°</span>
                  </div>
                </div>
              </div>
              
              <div v-if="vehicles.length === 0" class="empty-state">
                <el-empty description="暂无车辆数据" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { 
  MapLocation, Position, Hide, View, Refresh, Van, Location, Clock, Compass
} from '@element-plus/icons-vue';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

// 注意：在实际项目中，请将此token替换为您的Mapbox访问令牌

const vehicles = ref([]);
const loading = ref(false);
const selectedVehicle = ref(null);
const showTrail = ref(false);
const mapContainer = ref(null);

let map = null;
let markers = {};
let polylines = {};

// 初始化Mapbox地图
const initMap = () => {
  if (mapContainer.value) {
    map = new mapboxgl.Map({
      container: mapContainer.value,
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [118.7634, 32.0617],
      zoom: 13
    });

    // 添加导航控件
    map.addControl(new mapboxgl.NavigationControl(), 'top-right');
    
    // 添加比例尺
    map.addControl(new mapboxgl.ScaleControl({ unit: 'metric' }));
    
    // 添加全屏控件
    map.addControl(new mapboxgl.FullscreenControl());
  }
};

// 加载车辆数据
const loadVehicles = async () => {
  loading.value = true;
  try {
    const response = await fetch('http://localhost:8081/api/car-locations/latest');
    const data = await response.json();
    vehicles.value = Array.isArray(data) ? data : [];
    updateMapMarkers();
  } catch (error) {
    ElMessage.error('加载车辆数据失败');
  } finally {
    loading.value = false;
  }
};

// 更新地图标记
const updateMapMarkers = () => {
  // 清除旧标记
  Object.values(markers).forEach(marker => marker.remove());
  markers = {};
  
  vehicles.value.forEach(vehicle => {
    if (vehicle.latitude && vehicle.longitude) {
      // 创建自定义标记元素
      const el = document.createElement('div');
      el.className = 'vehicle-marker';
      el.innerHTML = `
        <div class="marker-inner" style="background-color: ${getSpeedColor(vehicle.speed)};">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="white">
            <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.85 7h10.29l1.08 3.11H5.77L6.85 7zM19 18H5v-6h14v6z"/>
          </svg>
        </div>
      `;
      
      const marker = new mapboxgl.Marker({
        element: el,
        anchor: 'center'
      })
      .setLngLat([vehicle.longitude, vehicle.latitude])
      .addTo(map);
      
      // 添加弹窗
      const popup = new mapboxgl.Popup({
        offset: 25,
        closeButton: false
      }).setHTML(`
        <div class="vehicle-popup">
          <h4>${vehicle.carName}</h4>
          <p><strong>速度:</strong> ${vehicle.speed} km/h</p>
          <p><strong>方向:</strong> ${vehicle.direction}°</p>
          <p><strong>更新时间:</strong> ${formatTime(vehicle.updateTime)}</p>
        </div>
      `);
      
      marker.setPopup(popup);
      
      marker.getElement().addEventListener('click', () => {
        selectVehicle(vehicle);
      });
      
      markers[vehicle.id] = marker;
    }
  });
};

// 获取速度对应颜色
const getSpeedColor = (speed) => {
  if (speed > 60) return '#ff4757';
  if (speed > 30) return '#ffa502';
  return '#2ed573';
};

// 选择车辆
const selectVehicle = (vehicle) => {
  selectedVehicle.value = vehicle;
  
  // 居中到选中车辆
  if (map && vehicle.latitude && vehicle.longitude) {
    map.flyTo({
      center: [vehicle.longitude, vehicle.latitude],
      zoom: 15,
      essential: true
    });
    
    // 高亮标记
    Object.values(markers).forEach(marker => {
      marker.getElement().style.opacity = '0.5';
    });
    if (markers[vehicle.id]) {
      markers[vehicle.id].getElement().style.opacity = '1';
    }
  }
};

// 居中到所有车辆
const centerOnVehicles = () => {
  if (vehicles.value.length > 0 && map) {
    const coordinates = vehicles.value.map(v => [v.longitude, v.latitude]);
    const bounds = coordinates.reduce((bounds, coord) => {
      return bounds.extend(coord);
    }, new mapboxgl.LngLatBounds(coordinates[0], coordinates[0]));
    
    map.fitBounds(bounds, {
      padding: 50,
      animate: true
    });
    
    ElMessage.success('已居中显示所有车辆');
  }
};

// 切换轨迹显示
const toggleTrail = () => {
  showTrail.value = !showTrail.value;
  ElMessage.info(showTrail.value ? '已显示车辆轨迹' : '已隐藏车辆轨迹');
};

// 刷新车辆数据
const refreshVehicles = () => {
  loadVehicles();
  ElMessage.success('数据已刷新');
};

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return '-';
  return new Date(timeString).toLocaleString('zh-CN');
};

// 定期更新数据
let updateInterval = null;

onMounted(() => {
  initMap();
  loadVehicles();
  updateInterval = setInterval(loadVehicles, 5000); // 每5秒更新一次
});

onUnmounted(() => {
  if (updateInterval) {
    clearInterval(updateInterval);
  }
  if (map) {
    map.remove();
  }
});
</script>

<style scoped>
.map-view-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-icon {
  font-size: 32px;
  color: #409eff;
}

.header-title h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(90deg, #409eff, #64b5f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-controls {
  display: flex;
  gap: 12px;
}

.content-wrapper {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.map-card, .info-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  height: 100%;
}

:deep(.el-card__header) {
  background: linear-gradient(90deg, #f5f7fa, #e4e7ed);
  border-bottom: 1px solid #ebeef5;
  padding: 20px 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.map-container {
  height: 600px;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
}

.vehicle-list {
  max-height: 600px;
  overflow-y: auto;
}

.vehicle-item {
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 12px;
  background: #f8f9fa;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.3s ease;
}

.vehicle-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.vehicle-item.selected {
  background: linear-gradient(90deg, #e3f2fd, #bbdefb);
  border-color: #409eff;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.2);
}

.vehicle-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.vehicle-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(45deg, #409eff, #64b5f6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.vehicle-info {
  flex: 1;
}

.vehicle-name {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.vehicle-id {
  font-size: 12px;
  color: #909399;
}

.vehicle-details {
  padding-left: 52px;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
}

.detail-icon {
  margin-right: 8px;
  color: #909399;
  font-size: 14px;
}

.detail-text {
  flex: 1;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

/* Mapbox标记样式 */
.vehicle-marker {
  width: 30px;
  height: 30px;
  cursor: pointer;
}

.marker-inner {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  animation: pulse 2s infinite;
  transition: all 0.3s ease;
}

.vehicle-marker:hover .marker-inner {
  transform: scale(1.2);
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.vehicle-popup h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.vehicle-popup p {
  margin: 6px 0;
  font-size: 13px;
  color: #606266;
}

@media (max-width: 1200px) {
  .content-wrapper {
    padding: 16px;
  }
  
  :deep(.el-col-18), :deep(.el-col-6) {
    width: 100%;
    margin-bottom: 24px;
  }
  
  .map-container {
    height: 400px;
  }
  
  .vehicle-list {
    max-height: 400px;
  }
}
</style>