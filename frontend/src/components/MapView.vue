<!--
================================================================================
  MapView.vue - 企业级实时地图监控组件
================================================================================

  【功能概述】
  提供车辆实时位置监控功能，基于Mapbox地图服务展示车辆位置和状态。
  支持车辆选择、位置追踪、轨迹显示、车辆沿预定义路线移动等功能。

  【设计标准】
  - 真实汽车图标：使用俯视图汽车图标，车头朝向行驶方向
  - 路线可视化：实线显示，已行驶深色高亮，待行驶浅色
  - 方向指示：车辆图标自动朝向行驶方向
================================================================================
-->

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
            <el-button 
              :type="isSimulating ? 'danger' : 'success'" 
              size="small"
              @click="toggleSimulation"
            >
              <el-icon><component :is="isSimulating ? 'VideoPause' : 'VideoPlay'"></component></el-icon>
              {{ isSimulating ? '停止模拟' : '开始模拟' }}
            </el-button>
            <el-button @click="centerOnVehicles" type="primary" size="small">
              <el-icon><Position /></el-icon>定位车辆
            </el-button>
            <el-button 
              :type="showTrail ? 'primary' : 'default'" 
              size="small"
              @click="toggleTrail"
            >
              <el-icon><Aim /></el-icon>
              {{ showTrail ? '隐藏路线' : '显示路线' }}
            </el-button>
          </el-button-group>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <el-row :gutter="20">
        <el-col :span="18">
          <el-card class="map-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">车辆实时位置</span>
                <div class="header-tags">
                  <el-tag :type="vehicles.length > 0 ? 'success' : 'warning'" effect="dark">
                    <el-icon><Van /></el-icon>
                    在线: {{ vehicles.length }} 辆
                  </el-tag>
                  <el-tag v-if="isSimulating" type="danger" effect="dark">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    运行中
                  </el-tag>
                </div>
              </div>
            </template>
            <div ref="mapContainer" class="map-container"></div>
            
            <div class="map-legend">
              <div class="legend-title">图例说明</div>
              <div class="legend-items">
                <div class="legend-item">
                  <div class="legend-line passed"></div>
                  <span>已行驶路段</span>
                </div>
                <div class="legend-item">
                  <div class="legend-line upcoming"></div>
                  <span>待行驶路段</span>
                </div>
                <div class="legend-item">
                  <div class="legend-car normal"></div>
                  <span>正常行驶 (&lt;60km/h)</span>
                </div>
                <div class="legend-item">
                  <div class="legend-car speeding"></div>
                  <span>超速行驶 (&gt;60km/h)</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

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
                  <div class="vehicle-icon-wrapper" :style="{ borderColor: getSpeedColor(vehicle.speed) }">
                    <svg class="vehicle-svg" viewBox="0 0 24 24" :style="{ fill: getSpeedColor(vehicle.speed) }">
                      <path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.5 16c-.83 0-1.5-.67-1.5-1.5S5.67 13 6.5 13s1.5.67 1.5 1.5S7.33 16 6.5 16zm11 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zM5 11l1.5-4.5h11L19 11H5z"/>
                    </svg>
                  </div>
                  <div class="vehicle-info">
                    <h4 class="vehicle-name">{{ vehicle.carName }}</h4>
                    <span class="vehicle-route">{{ getRouteName(vehicle) }}</span>
                  </div>
                  <div class="vehicle-speed" :class="getSpeedClass(vehicle.speed)">
                    <span class="speed-value">{{ Math.round(vehicle.speed) }}</span>
                    <span class="speed-unit">km/h</span>
                  </div>
                </div>
                
                <div class="vehicle-details">
                  <div class="detail-row">
                    <el-icon class="detail-icon"><Location /></el-icon>
                    <span class="detail-text">{{ vehicle.latitude.toFixed(5) }}, {{ vehicle.longitude.toFixed(5) }}</span>
                  </div>
                  <div class="detail-row">
                    <el-icon class="detail-icon"><Compass /></el-icon>
                    <span class="detail-text">航向 {{ Math.round(vehicle.direction) }}° · {{ getDirectionName(vehicle.direction) }}</span>
                  </div>
                  <div class="detail-row">
                    <el-icon class="detail-icon"><Odometer /></el-icon>
                    <el-progress 
                      :percentage="getRouteProgress(vehicle)" 
                      :stroke-width="6"
                      :color="getProgressColor(vehicle)"
                      class="route-progress"
                    />
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
  MapLocation, Position, Refresh, Van, Location, Compass,
  VideoPause, VideoPlay, Loading, Odometer, Aim
} from '@element-plus/icons-vue';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

const vehicles = ref([]);
const loading = ref(false);
const selectedVehicle = ref(null);
const showTrail = ref(true);
const mapContainer = ref(null);
const isSimulating = ref(false);

let map = null;
let markers = {};
let vehicleStates = {};
let animationFrameId = null;

const VEHICLE_ROUTES = {
  '京A10001': {
    name: '内环线A',
    color: '#1890ff',
    points: [
      [118.7500, 32.0650], [118.7550, 32.0680], [118.7620, 32.0700],
      [118.7700, 32.0690], [118.7780, 32.0660], [118.7830, 32.0600],
      [118.7820, 32.0540], [118.7760, 32.0500], [118.7680, 32.0480],
      [118.7600, 32.0500], [118.7540, 32.0550], [118.7500, 32.0600],
      [118.7500, 32.0650]
    ]
  },
  '京A10002': {
    name: '内环线B',
    color: '#52c41a',
    points: [
      [118.7800, 32.0550], [118.7760, 32.0500], [118.7680, 32.0480],
      [118.7600, 32.0500], [118.7540, 32.0550], [118.7500, 32.0600],
      [118.7500, 32.0650], [118.7550, 32.0680], [118.7620, 32.0700],
      [118.7700, 32.0690], [118.7780, 32.0660], [118.7830, 32.0600],
      [118.7800, 32.0550]
    ]
  },
  '京B20001': {
    name: '外环线A',
    color: '#fa8c16',
    points: [
      [118.7650, 32.0700], [118.7580, 32.0720], [118.7500, 32.0710],
      [118.7450, 32.0650], [118.7440, 32.0550], [118.7480, 32.0480],
      [118.7580, 32.0440], [118.7700, 32.0450], [118.7820, 32.0500],
      [118.7880, 32.0580], [118.7850, 32.0680], [118.7750, 32.0720],
      [118.7650, 32.0700]
    ]
  },
  '京B20002': {
    name: '外环线B',
    color: '#f5222d',
    points: [
      [118.7700, 32.0450], [118.7820, 32.0500], [118.7880, 32.0580],
      [118.7850, 32.0680], [118.7750, 32.0720], [118.7650, 32.0700],
      [118.7580, 32.0720], [118.7500, 32.0710], [118.7450, 32.0650],
      [118.7440, 32.0550], [118.7480, 32.0480], [118.7580, 32.0440],
      [118.7700, 32.0450]
    ]
  },
  '沪C30001': {
    name: '南北干线A',
    color: '#722ed1',
    points: [
      [118.7550, 32.0450], [118.7550, 32.0500], [118.7550, 32.0550],
      [118.7550, 32.0600], [118.7550, 32.0650], [118.7550, 32.0700],
      [118.7550, 32.0750], [118.7550, 32.0700], [118.7550, 32.0650],
      [118.7550, 32.0600], [118.7550, 32.0550], [118.7550, 32.0500],
      [118.7550, 32.0450]
    ]
  },
  '沪C30002': {
    name: '南北干线B',
    color: '#13c2c2',
    points: [
      [118.7750, 32.0750], [118.7750, 32.0700], [118.7750, 32.0650],
      [118.7750, 32.0600], [118.7750, 32.0550], [118.7750, 32.0500],
      [118.7750, 32.0450], [118.7750, 32.0500], [118.7750, 32.0550],
      [118.7750, 32.0600], [118.7750, 32.0650], [118.7750, 32.0700],
      [118.7750, 32.0750]
    ]
  },
  '沪D40001': {
    name: '东西干线A',
    color: '#eb2f96',
    points: [
      [118.7400, 32.0620], [118.7450, 32.0620], [118.7500, 32.0620],
      [118.7550, 32.0620], [118.7600, 32.0620], [118.7650, 32.0620],
      [118.7700, 32.0620], [118.7750, 32.0620], [118.7800, 32.0620],
      [118.7850, 32.0620], [118.7800, 32.0620], [118.7750, 32.0620],
      [118.7700, 32.0620], [118.7650, 32.0620], [118.7600, 32.0620],
      [118.7550, 32.0620], [118.7500, 32.0620], [118.7450, 32.0620],
      [118.7400, 32.0620]
    ]
  },
  '沪D40002': {
    name: '东西干线B',
    color: '#faad14',
    points: [
      [118.7850, 32.0580], [118.7800, 32.0580], [118.7750, 32.0580],
      [118.7700, 32.0580], [118.7650, 32.0580], [118.7600, 32.0580],
      [118.7550, 32.0580], [118.7500, 32.0580], [118.7450, 32.0580],
      [118.7500, 32.0580], [118.7550, 32.0580], [118.7600, 32.0580],
      [118.7650, 32.0580], [118.7700, 32.0580], [118.7750, 32.0580],
      [118.7800, 32.0580], [118.7850, 32.0580]
    ]
  },
  '粤E50001': {
    name: '配送区A',
    color: '#2f54eb',
    points: [
      [118.7580, 32.0720], [118.7620, 32.0720], [118.7660, 32.0720],
      [118.7660, 32.0680], [118.7660, 32.0640], [118.7620, 32.0640],
      [118.7580, 32.0640], [118.7580, 32.0680], [118.7580, 32.0720]
    ]
  },
  '粤E50002': {
    name: '配送区B',
    color: '#f5222d',
    points: [
      [118.7720, 32.0480], [118.7680, 32.0480], [118.7640, 32.0480],
      [118.7640, 32.0520], [118.7640, 32.0560], [118.7680, 32.0560],
      [118.7720, 32.0560], [118.7720, 32.0520], [118.7720, 32.0480]
    ]
  },
  '苏F60001': {
    name: '商业区巡游A',
    color: '#1890ff',
    points: [
      [118.7680, 32.0635], [118.7720, 32.0650], [118.7700, 32.0680],
      [118.7660, 32.0670], [118.7620, 32.0650], [118.7600, 32.0620],
      [118.7620, 32.0590], [118.7660, 32.0600], [118.7700, 32.0610],
      [118.7680, 32.0635]
    ]
  },
  '苏F60002': {
    name: '商业区巡游B',
    color: '#52c41a',
    points: [
      [118.7620, 32.0570], [118.7580, 32.0550], [118.7560, 32.0520],
      [118.7600, 32.0500], [118.7640, 32.0510], [118.7680, 32.0530],
      [118.7660, 32.0560], [118.7640, 32.0580], [118.7620, 32.0570]
    ]
  }
};

const initMap = () => {
  if (mapContainer.value) {
    map = new mapboxgl.Map({
      container: mapContainer.value,
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [118.7634, 32.0617],
      zoom: 13.5,
      pitch: 0
    });

    map.addControl(new mapboxgl.NavigationControl(), 'top-right');
    map.addControl(new mapboxgl.ScaleControl({ unit: 'metric' }));
    map.addControl(new mapboxgl.FullscreenControl());
    
    map.on('load', () => {
      map.addSource('passed-route-source', {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] }
      });
      
      map.addSource('upcoming-route-source', {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] }
      });
      
      map.addLayer({
        id: 'upcoming-route-layer',
        type: 'line',
        source: 'upcoming-route-source',
        paint: {
          'line-color': ['get', 'color'],
          'line-width': 5,
          'line-opacity': 0.35
        }
      });
      
      map.addLayer({
        id: 'passed-route-layer',
        type: 'line',
        source: 'passed-route-source',
        paint: {
          'line-color': ['get', 'color'],
          'line-width': 6,
          'line-opacity': 1.0
        }
      });
      
      renderAllRoutes();
    });
  }
};

const renderAllRoutes = () => {
  const source = map.getSource('upcoming-route-source');
  if (!source) return;
  
  const features = Object.entries(VEHICLE_ROUTES).map(([carName, route]) => ({
    type: 'Feature',
    properties: { color: route.color, carName },
    geometry: {
      type: 'LineString',
      coordinates: route.points
    }
  }));
  
  source.setData({ type: 'FeatureCollection', features });
};

const updateRouteSegments = () => {
  const passedSource = map.getSource('passed-route-source');
  const upcomingSource = map.getSource('upcoming-route-source');
  if (!passedSource || !upcomingSource) return;
  
  const passedFeatures = [];
  const upcomingFeatures = [];
  
  vehicles.value.forEach(vehicle => {
    const state = vehicleStates[vehicle.id];
    if (!state || !state.route) return;
    
    const points = state.route.points;
    const totalPoints = points.length;
    const currentIdx = state.currentIndex;
    const progress = state.progress;
    
    if (currentIdx > 0 || progress > 0) {
      const passedPoints = [];
      for (let i = 0; i <= currentIdx; i++) {
        passedPoints.push(points[i]);
      }
      
      if (progress > 0 && currentIdx < totalPoints - 1) {
        const currentPoint = points[currentIdx];
        const nextPoint = points[currentIdx + 1];
        const currentPos = [
          currentPoint[0] + (nextPoint[0] - currentPoint[0]) * progress,
          currentPoint[1] + (nextPoint[1] - currentPoint[1]) * progress
        ];
        passedPoints.push(currentPos);
      }
      
      if (passedPoints.length >= 2) {
        passedFeatures.push({
          type: 'Feature',
          properties: { color: state.route.color },
          geometry: {
            type: 'LineString',
            coordinates: passedPoints
          }
        });
      }
    }
    
    const upcomingPoints = [];
    if (currentIdx < totalPoints - 1) {
      const currentPoint = points[currentIdx];
      const nextPoint = points[currentIdx + 1];
      const currentPos = [
        currentPoint[0] + (nextPoint[0] - currentPoint[0]) * progress,
        currentPoint[1] + (nextPoint[1] - currentPoint[1]) * progress
      ];
      upcomingPoints.push(currentPos);
      
      for (let i = currentIdx + 1; i < totalPoints; i++) {
        upcomingPoints.push(points[i]);
      }
      
      if (upcomingPoints.length >= 2) {
        upcomingFeatures.push({
          type: 'Feature',
          properties: { color: state.route.color },
          geometry: {
            type: 'LineString',
            coordinates: upcomingPoints
          }
        });
      }
    } else {
      upcomingFeatures.push({
        type: 'Feature',
        properties: { color: state.route.color },
        geometry: {
          type: 'LineString',
          coordinates: [points[totalPoints - 1], points[0]]
        }
      });
    }
  });
  
  passedSource.setData({ type: 'FeatureCollection', features: passedFeatures });
  upcomingSource.setData({ type: 'FeatureCollection', features: upcomingFeatures });
};

const loadVehicles = async () => {
  loading.value = true;
  try {
    const response = await fetch('http://localhost:8081/api/car-locations/latest');
    const data = await response.json();
    vehicles.value = Array.isArray(data) ? data : [];
    
    vehicles.value.forEach(v => {
      const route = VEHICLE_ROUTES[v.carName];
      if (route && !vehicleStates[v.id]) {
        const points = route.points;
        const p0 = points[0];
        const p1 = points[1];
        const dx = p1[0] - p0[0];
        const dy = p1[1] - p0[1];
        let initialDirection = Math.atan2(dx, dy) * 180 / Math.PI;
        if (initialDirection < 0) initialDirection += 360;
        
        vehicleStates[v.id] = {
          route: route,
          currentIndex: 0,
          progress: 0,
          speed: v.speed || 30 + Math.random() * 30
        };
        v.longitude = p0[0];
        v.latitude = p0[1];
        v.direction = initialDirection;
      }
    });
    
    updateMapMarkers();
  } catch (error) {
    ElMessage.error('加载车辆数据失败');
  } finally {
    loading.value = false;
  }
};

const createVehicleMarker = (vehicle) => {
  const state = vehicleStates[vehicle.id];
  const color = state?.route?.color || '#1890ff';
  const speed = vehicle.speed || 30;
  const direction = vehicle.direction || 0;
  const isSpeeding = speed > 60;
  
  const el = document.createElement('div');
  el.className = 'vehicle-marker-container';
  
  const bodyColor = color;
  const windowColor = 'rgba(180, 210, 240, 0.9)';
  const wheelColor = '#2c2c2c';
  const lightColor = isSpeeding ? '#ff4d4f' : '#f5f5f5';
  
  el.innerHTML = `
    <div class="vehicle-marker-wrapper" style="transform: rotate(${direction}deg);">
      <svg class="vehicle-icon" viewBox="0 0 16 28" width="16" height="28">
        <defs>
          <linearGradient id="car-body-${vehicle.id}" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:${bodyColor};stop-opacity:1" />
            <stop offset="100%" style="stop-color:${bodyColor};stop-opacity:0.8" />
          </linearGradient>
          <filter id="car-shadow-${vehicle.id}">
            <feDropShadow dx="0" dy="1" stdDeviation="0.5" flood-color="rgba(0,0,0,0.3)"/>
          </filter>
        </defs>
        <g filter="url(#car-shadow-${vehicle.id})">
          <path d="M4 2 L6 0 L10 0 L12 2 L14 6 L14 22 L12 26 L10 28 L6 28 L4 26 L2 22 L2 6 Z" 
                fill="url(#car-body-${vehicle.id})" stroke="#fff" stroke-width="0.8"/>
          <rect x="4" y="5" width="8" height="6" rx="1" fill="${windowColor}"/>
          <rect x="4" y="14" width="8" height="8" rx="1" fill="${windowColor}"/>
          <circle cx="3" cy="4" r="1.5" fill="${wheelColor}"/>
          <circle cx="13" cy="4" r="1.5" fill="${wheelColor}"/>
          <circle cx="3" cy="24" r="1.5" fill="${wheelColor}"/>
          <circle cx="13" cy="24" r="1.5" fill="${wheelColor}"/>
          <rect x="6" y="0" width="2" height="1.5" rx="0.3" fill="${lightColor}"/>
          <rect x="9" y="0" width="2" height="1.5" rx="0.3" fill="${lightColor}"/>
          <rect x="6" y="26" width="2" height="1.5" rx="0.3" fill="#ff4444"/>
          <rect x="9" y="26" width="2" height="1.5" rx="0.3" fill="#ff4444"/>
        </g>
        ${isSpeeding ? `
          <circle cx="14" cy="2" r="3" fill="#ff4d4f"/>
          <text x="14" y="4" text-anchor="middle" fill="#fff" font-size="5" font-weight="bold">!</text>
        ` : ''}
      </svg>
    </div>
  `;
  
  const marker = new mapboxgl.Marker({
    element: el,
    anchor: 'center',
    offset: [0, 0]
  })
  .setLngLat([vehicle.longitude, vehicle.latitude])
  .addTo(map);
  
  const popup = new mapboxgl.Popup({
    offset: 25,
    closeButton: false,
    className: 'vehicle-popup'
  }).setHTML(`
    <div class="popup-content">
      <div class="popup-header" style="border-left: 4px solid ${color}">
        <span class="popup-name">${vehicle.carName}</span>
        <span class="popup-route">${state?.route?.name || '未知路线'}</span>
      </div>
      <div class="popup-body">
        <div class="popup-row">
          <span class="popup-label">速度</span>
          <span class="popup-value ${isSpeeding ? 'speeding' : ''}">${Math.round(speed)} km/h</span>
        </div>
        <div class="popup-row">
          <span class="popup-label">航向</span>
          <span class="popup-value">${Math.round(direction)}° ${getDirectionName(direction)}</span>
        </div>
        <div class="popup-row">
          <span class="popup-label">进度</span>
          <span class="popup-value">${getRouteProgress(vehicle)}%</span>
        </div>
      </div>
    </div>
  `);
  
  marker.setPopup(popup);
  
  el.addEventListener('click', (e) => {
    e.stopPropagation();
    selectVehicle(vehicle);
  });
  
  markers[vehicle.id] = marker;
};

const getSpeedColor = (speed) => {
  if (speed > 60) return '#ff4d4f';
  if (speed > 40) return '#faad14';
  return '#52c41a';
};

const getSpeedClass = (speed) => {
  if (speed > 60) return 'speeding';
  if (speed > 40) return 'warning';
  return 'normal';
};

const getDirectionName = (direction) => {
  const dirs = ['正北', '东北', '正东', '东南', '正南', '西南', '正西', '西北'];
  const index = Math.round(direction / 45) % 8;
  return dirs[index];
};

const getRouteName = (vehicle) => {
  const state = vehicleStates[vehicle.id];
  return state?.route?.name || '未分配路线';
};

const getRouteProgress = (vehicle) => {
  const state = vehicleStates[vehicle.id];
  if (!state) return 0;
  return Math.round((state.currentIndex + state.progress) / state.route.points.length * 100);
};

const getProgressColor = (vehicle) => {
  const state = vehicleStates[vehicle.id];
  return state?.route?.color || '#1890ff';
};

const selectVehicle = (vehicle) => {
  selectedVehicle.value = vehicle;
  
  if (map && vehicle.latitude && vehicle.longitude) {
    map.flyTo({
      center: [vehicle.longitude, vehicle.latitude],
      zoom: 15,
      essential: true
    });
    
    Object.values(markers).forEach(marker => {
      marker.getElement().style.opacity = '0.5';
    });
    if (markers[vehicle.id]) {
      markers[vehicle.id].getElement().style.opacity = '1';
    }
  }
};

const centerOnVehicles = () => {
  if (vehicles.value.length > 0 && map) {
    const coordinates = vehicles.value.map(v => [v.longitude, v.latitude]);
    const bounds = coordinates.reduce((bounds, coord) => {
      return bounds.extend(coord);
    }, new mapboxgl.LngLatBounds(coordinates[0], coordinates[0]));
    
    map.fitBounds(bounds, { padding: 80, animate: true });
    ElMessage.success('已居中显示所有车辆');
  }
};

const toggleTrail = () => {
  showTrail.value = !showTrail.value;
  
  const visibility = showTrail.value ? 'visible' : 'none';
  
  if (map && map.getLayer('passed-route-layer')) {
    map.setLayoutProperty('passed-route-layer', 'visibility', visibility);
  }
  if (map && map.getLayer('upcoming-route-layer')) {
    map.setLayoutProperty('upcoming-route-layer', 'visibility', visibility);
  }
  
  ElMessage.info(showTrail.value ? '已显示路线' : '已隐藏路线');
};

const refreshVehicles = () => {
  loadVehicles();
  ElMessage.success('数据已刷新');
};

const updateMapMarkers = () => {
  Object.values(markers).forEach(marker => marker.remove());
  markers = {};
  
  vehicles.value.forEach(vehicle => {
    if (vehicle.latitude && vehicle.longitude) {
      createVehicleMarker(vehicle);
    }
  });
};

const toggleSimulation = () => {
  isSimulating.value = !isSimulating.value;
  
  if (isSimulating.value) {
    startSimulation();
    ElMessage.success('车辆模拟已启动');
  } else {
    stopSimulation();
    ElMessage.info('车辆模拟已停止');
  }
};

const startSimulation = () => {
  const simulate = () => {
    if (!isSimulating.value) return;
    
    vehicles.value.forEach(vehicle => {
      const state = vehicleStates[vehicle.id];
      if (!state || !state.route) return;
      
      const points = state.route.points;
      const totalPoints = points.length;
      const currentIdx = state.currentIndex;
      const nextIdx = (currentIdx + 1) % totalPoints;
      
      const currentPoint = points[currentIdx];
      const nextPoint = points[nextIdx];
      
      const dx = nextPoint[0] - currentPoint[0];
      const dy = nextPoint[1] - currentPoint[1];
      let direction = Math.atan2(dx, dy) * 180 / Math.PI;
      if (direction < 0) direction += 360;
      
      const moveSpeed = state.speed * 0.00015;
      state.progress += moveSpeed;
      
      if (state.progress >= 1) {
        state.progress = 0;
        state.currentIndex = nextIdx;
      }
      
      const lng = currentPoint[0] + (nextPoint[0] - currentPoint[0]) * state.progress;
      const lat = currentPoint[1] + (nextPoint[1] - currentPoint[1]) * state.progress;
      
      state.speed += (Math.random() - 0.5) * 2;
      state.speed = Math.max(25, Math.min(75, state.speed));
      
      vehicle.longitude = lng;
      vehicle.latitude = lat;
      vehicle.speed = state.speed;
      vehicle.direction = direction;
      
      if (markers[vehicle.id]) {
        markers[vehicle.id].setLngLat([lng, lat]);
        
        const wrapper = markers[vehicle.id].getElement().querySelector('.vehicle-marker-wrapper');
        if (wrapper) {
          wrapper.style.transform = `rotate(${direction}deg)`;
        }
      }
    });
    
    updateRouteSegments();
    
    animationFrameId = requestAnimationFrame(simulate);
  };
  
  animationFrameId = requestAnimationFrame(simulate);
};

const stopSimulation = () => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId);
    animationFrameId = null;
  }
};

onMounted(() => {
  initMap();
  loadVehicles();
});

onUnmounted(() => {
  stopSimulation();
  if (map) {
    map.remove();
  }
});
</script>

<style scoped>
.map-view-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 28px;
  color: #1890ff;
}

.header-title h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-controls {
  display: flex;
  gap: 12px;
}

.header-tags {
  display: flex;
  gap: 8px;
}

.content-wrapper {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.map-card, .info-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  height: 100%;
}

:deep(.el-card__header) {
  background: linear-gradient(90deg, #f8f9fa, #e9ecef);
  border-bottom: 1px solid #e8e8e8;
  padding: 16px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.map-container {
  height: 580px;
  border-radius: 8px;
  overflow: hidden;
  background: #1a1a2e;
  position: relative;
}

.map-legend {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.legend-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #595959;
}

.legend-line {
  width: 30px;
  height: 5px;
  border-radius: 2px;
}

.legend-line.passed {
  background: #1890ff;
}

.legend-line.upcoming {
  background: #1890ff;
  opacity: 0.35;
}

.legend-car {
  width: 16px;
  height: 10px;
  border-radius: 2px;
}

.legend-car.normal {
  background: #52c41a;
}

.legend-car.speeding {
  background: #ff4d4f;
}

.vehicle-list {
  max-height: 580px;
  overflow-y: auto;
}

.vehicle-item {
  padding: 14px;
  border-radius: 10px;
  margin-bottom: 10px;
  background: #f8f9fa;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.vehicle-item:hover {
  transform: translateX(4px);
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.vehicle-item.selected {
  background: linear-gradient(90deg, #e6f7ff, #f0f5ff);
  border-color: #1890ff;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.vehicle-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.vehicle-icon-wrapper {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #1890ff;
  transition: border-color 0.3s ease;
}

.vehicle-svg {
  width: 26px;
  height: 26px;
}

.vehicle-info {
  flex: 1;
  min-width: 0;
}

.vehicle-name {
  margin: 0 0 2px 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

.vehicle-route {
  font-size: 12px;
  color: #8c8c8c;
}

.vehicle-speed {
  text-align: center;
  padding: 4px 8px;
  border-radius: 6px;
  background: #f0f0f0;
}

.vehicle-speed.normal {
  background: #f6ffed;
}

.vehicle-speed.warning {
  background: #fffbe6;
}

.vehicle-speed.speeding {
  background: #fff2f0;
}

.speed-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1;
}

.vehicle-speed.speeding .speed-value {
  color: #ff4d4f;
}

.speed-unit {
  font-size: 10px;
  color: #8c8c8c;
}

.vehicle-details {
  padding-left: 54px;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  color: #595959;
}

.detail-icon {
  margin-right: 6px;
  color: #8c8c8c;
  font-size: 12px;
}

.detail-text {
  flex: 1;
}

.route-progress {
  flex: 1;
}

:deep(.el-progress__text) {
  font-size: 11px !important;
  margin-left: 8px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.vehicle-marker-container {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vehicle-marker-container:hover {
  transform: scale(1.2);
}

.vehicle-marker-wrapper {
  transition: transform 0.1s ease;
  transform-origin: center center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vehicle-icon {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.4));
}

:deep(.vehicle-popup) {
  max-width: 220px;
}

:deep(.mapboxgl-popup-content) {
  padding: 0;
  border-radius: 8px;
  overflow: hidden;
}

.popup-content {
  background: #fff;
}

.popup-header {
  padding: 10px 12px;
  background: #f8f9fa;
  border-bottom: 1px solid #e8e8e8;
}

.popup-name {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.popup-route {
  font-size: 11px;
  color: #8c8c8c;
}

.popup-body {
  padding: 8px 12px;
}

.popup-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 12px;
}

.popup-label {
  color: #8c8c8c;
}

.popup-value {
  font-weight: 500;
  color: #1a1a2e;
}

.popup-value.speeding {
  color: #ff4d4f;
  font-weight: 600;
}

.is-loading {
  animation: rotating 1s linear infinite;
}

@keyframes rotating {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 1200px) {
  .content-wrapper {
    padding: 16px;
  }
  
  :deep(.el-col-18), :deep(.el-col-6) {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .map-container {
    height: 450px;
  }
  
  .vehicle-list {
    max-height: 350px;
  }
  
  .map-legend {
    display: none;
  }
}
</style>
