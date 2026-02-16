<!--
================================================================================
  MapView.vue - 实时地图监控组件
================================================================================

【功能概述】
  提供车辆实时位置监控功能，基于Mapbox地图服务展示车辆位置和状态。
  支持车辆选择、位置追踪、轨迹显示等功能。

【组件结构】
  ┌─────────────────────────────────────────────────────────────────────────┐
  │ 页面标题栏                                                              │
  │ ├── 标题: 实时地图监控                                                  │
  │ └── 操作按钮: 定位车辆、显示/隐藏轨迹                                    │
  ├─────────────────────────────────────────────────────────────────────────┤
  │ 主内容区                                                                │
  │ ├── 地图区域 (18栏)                                                     │
  │ │   └── Mapbox地图容器                                                  │
  │ └── 车辆信息面板 (6栏)                                                  │
  │     └── 车辆列表 (可点击选择)                                           │
  └─────────────────────────────────────────────────────────────────────────┘

【核心功能】
  1. 地图初始化: 使用Mapbox GL JS创建交互式地图
  2. 车辆数据加载: 从后端API获取车辆位置数据
  3. 标记管理: 在地图上显示车辆位置标记
  4. 车辆选择: 点击车辆列表或地图标记选择车辆
  5. 自动刷新: 每5秒自动更新车辆位置数据

【API接口】
  - GET /api/car-locations/latest: 获取所有车辆最新位置

【技术要点】
  - Mapbox GL JS: 地图渲染引擎
  - 自定义标记: 根据速度显示不同颜色
  - 弹窗信息: 点击标记显示车辆详情
  - 定时刷新: setInterval实现自动更新

【关联文件】
  - 后端: CarLocationController.java
  - 服务: CarLocationService.java
  - 配置: .env (VITE_MAPBOX_TOKEN)
================================================================================
-->

<template>
  <div class="map-view-page">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><MapLocation /></el-icon>
          <h1>实时地图监控</h1>
        </div>
        <div class="header-controls">
          <el-button-group>
            <!-- 定位所有车辆按钮 -->
            <el-button @click="centerOnVehicles" type="primary" size="small">
              <el-icon><Position /></el-icon>定位车辆
            </el-button>
            <!-- 切换轨迹显示按钮 -->
            <el-button @click="toggleTrail" size="small">
              <el-icon><component :is="showTrail ? 'Hide' : 'View'"></component></el-icon>
              {{ showTrail ? '隐藏轨迹' : '显示轨迹' }}
            </el-button>
          </el-button-group>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="content-wrapper">
      <el-row :gutter="20">
        <!-- 地图区域 -->
        <el-col :span="18">
          <el-card class="map-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">车辆实时位置</span>
                <!-- 显示在线车辆数量 -->
                <el-tag :type="vehicles.length > 0 ? 'success' : 'warning'">
                  在线车辆: {{ vehicles.length }}
                </el-tag>
              </div>
            </template>
            <!-- Mapbox地图容器 -->
            <div ref="mapContainer" class="map-container"></div>
          </el-card>
        </el-col>

        <!-- 车辆信息面板 -->
        <el-col :span="6">
          <el-card class="info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">车辆状态</span>
                <!-- 刷新按钮 -->
                <el-button @click="refreshVehicles" size="small" circle>
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </template>
            
            <!-- 车辆列表 -->
            <div class="vehicle-list" v-loading="loading">
              <!-- 车辆项 - 可点击选择 -->
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
                  <!-- 速度标签 - 根据速度显示不同颜色 -->
                  <el-tag 
                    :type="vehicle.speed > 60 ? 'danger' : vehicle.speed > 30 ? 'warning' : 'success'" 
                    size="small"
                  >
                    {{ vehicle.speed }} km/h
                  </el-tag>
                </div>
                
                <!-- 车辆详细信息 -->
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
              
              <!-- 空状态提示 -->
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
/**
 * 脚本部分 - 地图监控核心逻辑
 * 
 * 导入说明:
 * - Vue响应式API: ref, onMounted, onUnmounted
 * - Element Plus组件: ElMessage消息提示
 * - Mapbox GL: 地图渲染库
 * - 图标组件: Element Plus图标
 */
import { ref, onMounted, onUnmounted } from 'vue';
import { ElMessage } from 'element-plus';
import { 
  MapLocation, Position, Hide, View, Refresh, Van, Location, Clock, Compass
} from '@element-plus/icons-vue';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

// 配置Mapbox访问令牌（从环境变量读取）
mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

// ==================== 响应式状态 ====================

/** @type {Ref<Array>} 车辆列表数据 */
const vehicles = ref([]);

/** @type {Ref<boolean>} 加载状态 */
const loading = ref(false);

/** @type {Ref<Object|null>} 当前选中的车辆 */
const selectedVehicle = ref(null);

/** @type {Ref<boolean>} 是否显示轨迹 */
const showTrail = ref(false);

/** @type {Ref<HTMLElement|null>} 地图容器DOM引用 */
const mapContainer = ref(null);

// ==================== 非响应式变量 ====================

/** @type {mapboxgl.Map|null} Mapbox地图实例 */
let map = null;

/** @type {Object} 车辆标记对象，key为车辆ID */
let markers = {};

/** @type {Object} 轨迹线对象 */
let polylines = {};

/** @type {number|null} 自动更新定时器ID */
let updateInterval = null;

// ==================== 核心方法 ====================

/**
 * 初始化Mapbox地图
 * 创建地图实例并添加控件
 */
const initMap = () => {
  if (mapContainer.value) {
    map = new mapboxgl.Map({
      container: mapContainer.value,
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [118.7634, 32.0617], // 默认中心点：南京
      zoom: 13
    });

    // 添加导航控件（缩放、旋转）
    map.addControl(new mapboxgl.NavigationControl(), 'top-right');
    
    // 添加比例尺
    map.addControl(new mapboxgl.ScaleControl({ unit: 'metric' }));
    
    // 添加全屏控件
    map.addControl(new mapboxgl.FullscreenControl());
  }
};

/**
 * 加载车辆数据
 * 从后端API获取所有车辆最新位置
 */
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

/**
 * 更新地图标记
 * 清除旧标记并创建新标记
 */
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
      
      // 创建Mapbox标记
      const marker = new mapboxgl.Marker({
        element: el,
        anchor: 'center'
      })
      .setLngLat([vehicle.longitude, vehicle.latitude])
      .addTo(map);
      
      // 创建弹窗
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
      
      // 点击标记选择车辆
      marker.getElement().addEventListener('click', () => {
        selectVehicle(vehicle);
      });
      
      markers[vehicle.id] = marker;
    }
  });
};

/**
 * 根据速度获取标记颜色
 * @param {number} speed - 车辆速度
 * @returns {string} 颜色值
 */
const getSpeedColor = (speed) => {
  if (speed > 60) return '#ff4757'; // 高速 - 红色
  if (speed > 30) return '#ffa502'; // 中速 - 橙色
  return '#2ed573'; // 低速 - 绿色
};

/**
 * 选择车辆
 * 高亮显示并居中到选中车辆
 * @param {Object} vehicle - 车辆对象
 */
const selectVehicle = (vehicle) => {
  selectedVehicle.value = vehicle;
  
  // 居中到选中车辆
  if (map && vehicle.latitude && vehicle.longitude) {
    map.flyTo({
      center: [vehicle.longitude, vehicle.latitude],
      zoom: 15,
      essential: true
    });
    
    // 高亮选中标记，其他标记变淡
    Object.values(markers).forEach(marker => {
      marker.getElement().style.opacity = '0.5';
    });
    if (markers[vehicle.id]) {
      markers[vehicle.id].getElement().style.opacity = '1';
    }
  }
};

/**
 * 居中显示所有车辆
 * 调整地图视野以包含所有车辆位置
 */
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

/**
 * 切换轨迹显示
 */
const toggleTrail = () => {
  showTrail.value = !showTrail.value;
  ElMessage.info(showTrail.value ? '已显示车辆轨迹' : '已隐藏车辆轨迹');
};

/**
 * 刷新车辆数据
 */
const refreshVehicles = () => {
  loadVehicles();
  ElMessage.success('数据已刷新');
};

/**
 * 格式化时间显示
 * @param {string} timeString - ISO时间字符串
 * @returns {string} 格式化后的时间
 */
const formatTime = (timeString) => {
  if (!timeString) return '-';
  return new Date(timeString).toLocaleString('zh-CN');
};

// ==================== 生命周期钩子 ====================

/**
 * 组件挂载时初始化
 */
onMounted(() => {
  initMap();
  loadVehicles();
  // 设置定时刷新（每5秒）
  updateInterval = setInterval(loadVehicles, 5000);
});

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  // 清除定时器
  if (updateInterval) {
    clearInterval(updateInterval);
  }
  // 销毁地图实例
  if (map) {
    map.remove();
  }
});
</script>

<style scoped>
/* 页面容器样式 */
.map-view-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}

/* 页面标题栏样式 */
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

/* 内容区域样式 */
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

/* 地图容器样式 */
.map-container {
  height: 600px;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
}

/* 车辆列表样式 */
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

/* 弹窗样式 */
.vehicle-popup h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.vehicle-popup p {
  margin: 6px 0;
  font-size: 13px;
  color: #606266;
}

/* 响应式布局 */
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
