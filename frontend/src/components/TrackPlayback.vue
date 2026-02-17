<!--
================================================================================
  TrackPlayback.vue - 轨迹回放组件
================================================================================

  【功能概述】
  提供车辆历史轨迹回放功能，支持播放、暂停、倍速、进度控制等。
  在地图上动态展示车辆的历史行驶路线。

  【组件结构】
  ┌─────────────────────────────────────────────────────────────────────────┐
  │ 控制面板                                                                 │
  │ ├── 车辆选择器                                                          │
  │ ├── 时间范围选择                                                        │
  │ └── 统计信息（总距离、时长、平均速度）                                    │
  ├─────────────────────────────────────────────────────────────────────────┤
  │ 地图区域                                                                 │
  │ └── 轨迹线 + 车辆标记                                                    │
  ├─────────────────────────────────────────────────────────────────────────┤
  │ 播放控制                                                                 │
  │ ├── 播放/暂停按钮                                                       │
  │ ├── 进度条                                                              │
  │ ├── 倍速选择                                                            │
  │ └── 时间显示                                                            │
  └─────────────────────────────────────────────────────────────────────────┘

  【使用示例】
  <TrackPlayback />

  【关联文件】
  - VehicleTrackController.java: 后端轨迹API
  - VehicleTrackService.java: 轨迹数据处理
================================================================================
-->

<template>
  <div class="track-playback-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><VideoPlay /></el-icon>
          <h1>轨迹回放</h1>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <el-row :gutter="20">
        <!-- 左侧控制面板 -->
        <el-col :span="6">
          <el-card class="control-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">回放设置</span>
              </div>
            </template>

            <!-- 车辆选择 -->
            <div class="control-section">
              <label class="section-label">选择车辆</label>
              <el-select v-model="selectedCarName" placeholder="请选择车辆" 
                style="width: 100%" @change="loadTrackData">
                <el-option
                  v-for="car in carNames"
                  :key="car"
                  :label="car"
                  :value="car"
                />
              </el-select>
            </div>

            <!-- 时间范围 -->
            <div class="control-section">
              <label class="section-label">时间范围</label>
              <el-date-picker
                v-model="timeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
                @change="loadTrackData"
              />
            </div>

            <!-- 快捷时间选择 -->
            <div class="control-section">
              <div class="quick-time-buttons">
                <el-button size="small" @click="setQuickTime(1)">最近1小时</el-button>
                <el-button size="small" @click="setQuickTime(6)">最近6小时</el-button>
                <el-button size="small" @click="setQuickTime(24)">最近24小时</el-button>
              </div>
            </div>

            <!-- 统计信息 -->
            <div class="control-section" v-if="playbackData">
              <label class="section-label">行程统计</label>
              <div class="stats-grid">
                <div class="stat-item">
                  <span class="stat-value">{{ formatDistance(playbackData.totalDistance) }}</span>
                  <span class="stat-label">总距离</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ formatDuration(playbackData.totalDuration) }}</span>
                  <span class="stat-label">行驶时长</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ formatSpeed(playbackData.avgSpeed) }}</span>
                  <span class="stat-label">平均速度</span>
                </div>
                <div class="stat-item">
                  <span class="stat-value">{{ formatSpeed(playbackData.maxSpeed) }}</span>
                  <span class="stat-label">最高速度</span>
                </div>
              </div>
            </div>

            <!-- 轨迹点数量 -->
            <div class="control-section" v-if="playbackData">
              <el-tag type="info">轨迹点: {{ playbackData.pointCount }} 个</el-tag>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧地图和播放控制 -->
        <el-col :span="18">
          <el-card class="map-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">轨迹地图</span>
                <el-tag v-if="playbackData" type="success">
                  {{ selectedCarName || '未选择车辆' }}
                </el-tag>
              </div>
            </template>

            <!-- 地图容器 -->
            <div ref="mapContainer" class="map-container"></div>

            <!-- 当前点信息 -->
            <div class="current-point-info" v-if="currentTrack">
              <div class="info-row">
                <el-icon><Location /></el-icon>
                <span>{{ currentTrack.latitude.toFixed(5) }}, {{ currentTrack.longitude.toFixed(5) }}</span>
              </div>
              <div class="info-row">
                <el-icon><Odometer /></el-icon>
                <span>{{ formatSpeed(currentTrack.speed) }}</span>
              </div>
              <div class="info-row">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(currentTrack.recordTime) }}</span>
              </div>
            </div>
          </el-card>

          <!-- 播放控制栏 -->
          <el-card class="player-card" shadow="never">
            <div class="player-controls">
              <!-- 播放按钮 -->
              <el-button-group class="play-buttons">
                <el-button @click="skipBackward" :disabled="!playbackData">
                  <el-icon><DArrowLeft /></el-icon>
                </el-button>
                <el-button 
                  :type="isPlaying ? 'danger' : 'primary'" 
                  @click="togglePlay"
                  :disabled="!playbackData"
                >
                  <el-icon>
                    <component :is="isPlaying ? 'VideoPause' : 'VideoPlay'" />
                  </el-icon>
                  {{ isPlaying ? '暂停' : '播放' }}
                </el-button>
                <el-button @click="skipForward" :disabled="!playbackData">
                  <el-icon><DArrowRight /></el-icon>
                </el-button>
              </el-button-group>

              <!-- 进度条 -->
              <div class="progress-section">
                <span class="time-display">{{ currentTimeDisplay }}</span>
                <el-slider
                  v-model="progressValue"
                  :max="100"
                  :format-tooltip="formatProgressTooltip"
                  @change="onProgressChange"
                  class="progress-slider"
                />
                <span class="time-display">{{ totalTimeDisplay }}</span>
              </div>

              <!-- 倍速选择 -->
              <div class="speed-control">
                <span class="speed-label">倍速:</span>
                <el-select v-model="playbackSpeed" size="small" style="width: 80px">
                  <el-option label="0.5x" :value="0.5" />
                  <el-option label="1x" :value="1" />
                  <el-option label="2x" :value="2" />
                  <el-option label="4x" :value="4" />
                  <el-option label="8x" :value="8" />
                </el-select>
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
 * 脚本部分 - 轨迹回放组件核心逻辑
 * 
 * 实现功能：
 * 1. 从后端加载轨迹数据
 * 2. 在地图上绘制轨迹线
 * 3. 动画播放轨迹
 * 4. 播放控制（播放/暂停/进度/倍速）
 */

import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { 
  VideoPlay, VideoPause, Location, Odometer, Clock,
  DArrowLeft, DArrowRight
} from '@element-plus/icons-vue';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';
import axios from 'axios';

// Mapbox访问令牌
mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

// ==================== 响应式状态 ====================

/** 车辆名称列表 */
const carNames = ref([]);

/** 当前选中的车辆 */
const selectedCarName = ref('');

/** 时间范围 */
const timeRange = ref([]);

/** 回放数据 */
const playbackData = ref(null);

/** 当前轨迹点索引 */
const currentIndex = ref(0);

/** 是否正在播放 */
const isPlaying = ref(false);

/** 播放速度 */
const playbackSpeed = ref(1);

/** 进度值 (0-100) */
const progressValue = ref(0);

/** 地图容器引用 */
const mapContainer = ref(null);

/** 地图实例 */
let map = null;

/** 车辆标记 */
let vehicleMarker = null;

/** 动画定时器 */
let animationTimer = null;

/** 基础动画间隔（毫秒） */
const BASE_INTERVAL = 1000;

// ==================== 计算属性 ====================

/** 当前轨迹点 */
const currentTrack = computed(() => {
  if (!playbackData.value || !playbackData.value.tracks) return null;
  return playbackData.value.tracks[currentIndex.value];
});

/** 当前时间显示 */
const currentTimeDisplay = computed(() => {
  if (!currentTrack.value) return '--:--:--';
  return formatTime(currentTrack.value.recordTime);
});

/** 总时间显示 */
const totalTimeDisplay = computed(() => {
  if (!playbackData.value || !playbackData.value.startTime) return '--:--:--';
  return formatTime(playbackData.value.startTime);
});

// ==================== 生命周期钩子 ====================

onMounted(() => {
  initMap();
  loadCarNames();
  setQuickTime(1); // 默认最近1小时
});

onUnmounted(() => {
  stopPlayback();
  if (map) {
    map.remove();
  }
});

// ==================== 方法 ====================

/**
 * 初始化地图
 */
const initMap = () => {
  if (mapContainer.value) {
    map = new mapboxgl.Map({
      container: mapContainer.value,
      style: 'mapbox://styles/mapbox/streets-v11',
      center: [118.7634, 32.0617],
      zoom: 13,
      pitch: 0
    });

    map.addControl(new mapboxgl.NavigationControl(), 'top-right');
    map.addControl(new mapboxgl.ScaleControl({ unit: 'metric' }));

    map.on('load', () => {
      // 添加轨迹线数据源
      map.addSource('track-source', {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] }
      });

      // 添加轨迹线图层
      map.addLayer({
        id: 'track-layer',
        type: 'line',
        source: 'track-source',
        paint: {
          'line-color': '#1890ff',
          'line-width': 4,
          'line-opacity': 0.8
        }
      });

      // 添加已行驶轨迹线数据源
      map.addSource('passed-track-source', {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] }
      });

      // 添加已行驶轨迹线图层
      map.addLayer({
        id: 'passed-track-layer',
        type: 'line',
        source: 'passed-track-source',
        paint: {
          'line-color': '#52c41a',
          'line-width': 5,
          'line-opacity': 1
        }
      });
    });
  }
};

/**
 * 加载车辆名称列表
 */
const loadCarNames = async () => {
  try {
    const response = await axios.get('http://localhost:8081/api/tracks/cars');
    carNames.value = response.data.data || [];
  } catch (error) {
    console.error('加载车辆列表失败:', error);
  }
};

/**
 * 加载轨迹数据
 */
const loadTrackData = async () => {
  if (!selectedCarName.value) return;

  try {
    let url = `http://localhost:8081/api/tracks/car/${selectedCarName.value}/playback?hours=24`;
    
    // 如果选择了时间范围
    if (timeRange.value && timeRange.value.length === 2) {
      const startTime = timeRange.value[0].toISOString();
      const endTime = timeRange.value[1].toISOString();
      url = `http://localhost:8081/api/tracks/car/${selectedCarName.value}/playback?startTime=${startTime}&endTime=${endTime}`;
    }

    const response = await axios.get(url);
    playbackData.value = response.data.data;

    if (playbackData.value && playbackData.value.tracks.length > 0) {
      // 重置播放状态
      currentIndex.value = 0;
      progressValue.value = 0;
      isPlaying.value = false;

      // 绘制轨迹
      drawTrack();
      
      // 创建车辆标记
      createVehicleMarker();

      // 调整地图视野
      fitMapToTrack();

      ElMessage.success(`加载了 ${playbackData.value.tracks.length} 个轨迹点`);
    } else {
      ElMessage.warning('该时间段内没有轨迹数据');
    }
  } catch (error) {
    console.error('加载轨迹数据失败:', error);
    ElMessage.error('加载轨迹数据失败');
  }
};

/**
 * 绘制轨迹线
 */
const drawTrack = () => {
  if (!map || !playbackData.value) return;

  const tracks = playbackData.value.tracks;
  const coordinates = tracks.map(t => [t.longitude, t.latitude]);

  // 设置完整轨迹线
  const source = map.getSource('track-source');
  if (source) {
    source.setData({
      type: 'Feature',
      geometry: {
        type: 'LineString',
        coordinates: coordinates
      }
    });
  }
};

/**
 * 更新已行驶轨迹
 */
const updatePassedTrack = () => {
  if (!map || !playbackData.value) return;

  const tracks = playbackData.value.tracks;
  const passedCoordinates = tracks.slice(0, currentIndex.value + 1).map(t => [t.longitude, t.latitude]);

  const source = map.getSource('passed-track-source');
  if (source && passedCoordinates.length >= 2) {
    source.setData({
      type: 'Feature',
      geometry: {
        type: 'LineString',
        coordinates: passedCoordinates
      }
    });
  }
};

/**
 * 创建车辆标记
 */
const createVehicleMarker = () => {
  if (!map || !playbackData.value || playbackData.value.tracks.length === 0) return;

  // 移除旧标记
  if (vehicleMarker) {
    vehicleMarker.remove();
  }

  const firstTrack = playbackData.value.tracks[0];

  // 创建标记元素
  const el = document.createElement('div');
  el.className = 'vehicle-marker';
  el.innerHTML = `
    <svg viewBox="0 0 24 24" width="32" height="32">
      <circle cx="12" cy="12" r="10" fill="#1890ff" stroke="#fff" stroke-width="2"/>
      <path d="M12 2 L14 8 L12 6 L10 8 Z" fill="#fff" transform="rotate(${firstTrack.direction || 0}, 12, 12)"/>
    </svg>
  `;

  vehicleMarker = new mapboxgl.Marker({
    element: el,
    anchor: 'center'
  })
    .setLngLat([firstTrack.longitude, firstTrack.latitude])
    .addTo(map);
};

/**
 * 更新车辆标记位置
 */
const updateVehicleMarker = () => {
  if (!vehicleMarker || !currentTrack.value) return;

  const track = currentTrack.value;
  vehicleMarker.setLngLat([track.longitude, track.latitude]);

  // 更新方向
  const svg = vehicleMarker.getElement().querySelector('svg');
  if (svg && track.direction) {
    const path = svg.querySelector('path');
    if (path) {
      path.setAttribute('transform', `rotate(${track.direction}, 12, 12)`);
    }
  }
};

/**
 * 调整地图视野以适应轨迹
 */
const fitMapToTrack = () => {
  if (!map || !playbackData.value || playbackData.value.tracks.length === 0) return;

  const coordinates = playbackData.value.tracks.map(t => [t.longitude, t.latitude]);
  const bounds = coordinates.reduce((bounds, coord) => {
    return bounds.extend(coord);
  }, new mapboxgl.LngLatBounds(coordinates[0], coordinates[0]));

  map.fitBounds(bounds, { padding: 50, animate: true });
};

/**
 * 切换播放/暂停
 */
const togglePlay = () => {
  if (isPlaying.value) {
    stopPlayback();
  } else {
    startPlayback();
  }
};

/**
 * 开始播放
 */
const startPlayback = () => {
  if (!playbackData.value || playbackData.value.tracks.length === 0) return;

  isPlaying.value = true;

  const interval = BASE_INTERVAL / playbackSpeed.value;

  animationTimer = setInterval(() => {
    if (currentIndex.value < playbackData.value.tracks.length - 1) {
      currentIndex.value++;
      progressValue.value = (currentIndex.value / (playbackData.value.tracks.length - 1)) * 100;
      updateVehicleMarker();
      updatePassedTrack();
    } else {
      // 播放结束
      stopPlayback();
      ElMessage.info('轨迹回放完成');
    }
  }, interval);
};

/**
 * 停止播放
 */
const stopPlayback = () => {
  isPlaying.value = false;
  if (animationTimer) {
    clearInterval(animationTimer);
    animationTimer = null;
  }
};

/**
 * 快退
 */
const skipBackward = () => {
  if (!playbackData.value) return;
  const newIndex = Math.max(0, currentIndex.value - 10);
  currentIndex.value = newIndex;
  progressValue.value = (newIndex / (playbackData.value.tracks.length - 1)) * 100;
  updateVehicleMarker();
  updatePassedTrack();
};

/**
 * 快进
 */
const skipForward = () => {
  if (!playbackData.value) return;
  const newIndex = Math.min(playbackData.value.tracks.length - 1, currentIndex.value + 10);
  currentIndex.value = newIndex;
  progressValue.value = (newIndex / (playbackData.value.tracks.length - 1)) * 100;
  updateVehicleMarker();
  updatePassedTrack();
};

/**
 * 进度条变化处理
 */
const onProgressChange = (value) => {
  if (!playbackData.value) return;
  const newIndex = Math.round((value / 100) * (playbackData.value.tracks.length - 1));
  currentIndex.value = newIndex;
  updateVehicleMarker();
  updatePassedTrack();
};

/**
 * 设置快捷时间
 */
const setQuickTime = (hours) => {
  const end = new Date();
  const start = new Date(end.getTime() - hours * 60 * 60 * 1000);
  timeRange.value = [start, end];
  loadTrackData();
};

/**
 * 格式化进度提示
 */
const formatProgressTooltip = (value) => {
  if (!playbackData.value) return '';
  const index = Math.round((value / 100) * (playbackData.value.tracks.length - 1));
  const track = playbackData.value.tracks[index];
  if (track) {
    return formatTime(track.recordTime);
  }
  return '';
};

/**
 * 格式化距离
 */
const formatDistance = (km) => {
  if (!km) return '0 km';
  if (km < 1) {
    return `${(km * 1000).toFixed(0)} m`;
  }
  return `${km.toFixed(2)} km`;
};

/**
 * 格式化时长
 */
const formatDuration = (minutes) => {
  if (!minutes) return '0分钟';
  if (minutes < 60) {
    return `${minutes}分钟`;
  }
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return `${hours}小时${mins}分钟`;
};

/**
 * 格式化速度
 */
const formatSpeed = (speed) => {
  if (!speed) return '0 km/h';
  return `${speed.toFixed(1)} km/h`;
};

/**
 * 格式化时间
 */
const formatTime = (datetime) => {
  if (!datetime) return '--:--:--';
  const date = new Date(datetime);
  return date.toLocaleTimeString('zh-CN', { hour12: false });
};

// 监听播放速度变化
watch(playbackSpeed, () => {
  if (isPlaying.value) {
    stopPlayback();
    startPlayback();
  }
});
</script>

<style scoped>
.track-playback-page {
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

.content-wrapper {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.control-card, .map-card, .player-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
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

.control-section {
  margin-bottom: 20px;
}

.section-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  margin-bottom: 8px;
}

.quick-time-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.stat-label {
  font-size: 12px;
  color: #8c8c8c;
}

.map-container {
  height: 450px;
  border-radius: 8px;
  overflow: hidden;
  background: #1a1a2e;
  position: relative;
}

.current-point-info {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #333;
  margin-bottom: 4px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.player-controls {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px 0;
}

.play-buttons {
  flex-shrink: 0;
}

.progress-section {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-slider {
  flex: 1;
}

.time-display {
  font-size: 13px;
  color: #666;
  min-width: 80px;
  text-align: center;
}

.speed-control {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.speed-label {
  font-size: 13px;
  color: #666;
}

:deep(.vehicle-marker) {
  cursor: pointer;
}

:deep(.vehicle-marker svg) {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}
</style>
