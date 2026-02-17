<!--
================================================================================
  DrivingAnalysis.vue - 驾驶行为分析组件
================================================================================

  【功能概述】
  提供驾驶行为分析功能，展示异常驾驶行为记录、驾驶评分、行为统计等。
  支持按车辆和时间范围查询分析。

  【组件结构】
  ┌─────────────────────────────────────────────────────────────────────────┐
  │ 控制面板                                                                 │
  │ ├── 车辆选择器                                                          │
  │ └── 时间范围选择                                                        │
  ├─────────────────────────────────────────────────────────────────────────┤
  │ 驾驶评分卡片                                                             │
  │ ├── 评分环形图                                                          │
  │ ├── 评级标签                                                            │
  │ └── 行为统计                                                            │
  ├─────────────────────────────────────────────────────────────────────────┤
  │ 行为记录列表                                                             │
  │ └── 行为卡片（类型、时间、位置、风险等级）                                │
  └─────────────────────────────────────────────────────────────────────────┘

  【关联文件】
  - DrivingBehaviorController.java: 后端API
  - DrivingBehaviorService.java: 业务逻辑
================================================================================
-->

<template>
  <div class="driving-analysis-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><DataAnalysis /></el-icon>
          <h1>驾驶行为分析</h1>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="analyzeBehavior" :loading="analyzing">
            <el-icon><Search /></el-icon>
            开始分析
          </el-button>
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
                <span class="card-title">分析设置</span>
              </div>
            </template>

            <!-- 车辆选择 -->
            <div class="control-section">
              <label class="section-label">选择车辆</label>
              <el-select v-model="selectedCarName" placeholder="请选择车辆" 
                style="width: 100%">
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
              />
            </div>

            <!-- 快捷时间选择 -->
            <div class="control-section">
              <div class="quick-time-buttons">
                <el-button size="small" @click="setQuickTime(1)">今天</el-button>
                <el-button size="small" @click="setQuickTime(7)">近7天</el-button>
                <el-button size="small" @click="setQuickTime(30)">近30天</el-button>
              </div>
            </div>
          </el-card>

          <!-- 驾驶评分卡片 -->
          <el-card class="score-card" shadow="never" v-if="drivingScore">
            <template #header>
              <div class="card-header">
                <span class="card-title">驾驶评分</span>
                <el-tag :type="getScoreTagType(drivingScore.grade)">
                  {{ drivingScore.grade }}
                </el-tag>
              </div>
            </template>

            <div class="score-display">
              <div class="score-circle" :style="{ borderColor: getScoreColor(drivingScore.score) }">
                <span class="score-number">{{ drivingScore.score }}</span>
                <span class="score-label">分</span>
              </div>
            </div>

            <div class="score-stats">
              <div class="stat-item">
                <span class="stat-value">{{ drivingScore.totalBehaviors }}</span>
                <span class="stat-label">异常行为</span>
              </div>
            </div>

            <!-- 行为类型统计 -->
            <div class="behavior-counts" v-if="drivingScore.behaviorCounts">
              <div class="count-item" v-for="(count, type) in drivingScore.behaviorCounts" :key="type">
                <el-tag :type="getBehaviorTagType(type)" size="small">
                  {{ getBehaviorLabel(type) }}
                </el-tag>
                <span class="count-value">{{ count }}次</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧行为记录列表 -->
        <el-col :span="18">
          <el-card class="behaviors-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">行为记录</span>
                <el-tag type="info">共 {{ behaviors.length }} 条记录</el-tag>
              </div>
            </template>

            <div class="behaviors-list" v-loading="loading">
              <el-empty v-if="behaviors.length === 0 && !loading" description="暂无行为记录" />
              
              <div 
                v-for="behavior in behaviors" 
                :key="behavior.id"
                class="behavior-item"
                :class="'risk-' + behavior.riskLevel?.toLowerCase()"
              >
                <div class="behavior-header">
                  <div class="behavior-type">
                    <el-icon :class="'icon-' + behavior.behaviorType?.toLowerCase()">
                      <component :is="getBehaviorIcon(behavior.behaviorType)" />
                    </el-icon>
                    <span class="type-name">{{ getBehaviorLabel(behavior.behaviorType) }}</span>
                  </div>
                  <el-tag :type="getRiskTagType(behavior.riskLevel)" size="small">
                    {{ getRiskLabel(behavior.riskLevel) }}
                  </el-tag>
                </div>
                
                <div class="behavior-body">
                  <p class="behavior-desc">{{ behavior.description }}</p>
                  <div class="behavior-details">
                    <div class="detail-item">
                      <el-icon><Clock /></el-icon>
                      <span>{{ formatTime(behavior.eventTime) }}</span>
                    </div>
                    <div class="detail-item" v-if="behavior.speed">
                      <el-icon><Odometer /></el-icon>
                      <span>{{ behavior.speed.toFixed(1) }} km/h</span>
                    </div>
                    <div class="detail-item" v-if="behavior.acceleration">
                      <el-icon><TrendCharts /></el-icon>
                      <span>{{ Math.abs(behavior.acceleration).toFixed(1) }} m/s²</span>
                    </div>
                  </div>
                </div>
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
 * 驾驶行为分析组件 - 核心逻辑
 */

import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { 
  DataAnalysis, Search, Clock, Odometer, TrendCharts,
  Warning, Timer, Promotion, VideoPause
} from '@element-plus/icons-vue';
import axios from 'axios';

// ==================== 响应式状态 ====================

/** 车辆名称列表 */
const carNames = ref([]);

/** 当前选中的车辆 */
const selectedCarName = ref('');

/** 时间范围 */
const timeRange = ref([]);

/** 行为记录列表 */
const behaviors = ref([]);

/** 驾驶评分 */
const drivingScore = ref(null);

/** 加载状态 */
const loading = ref(false);

/** 分析中状态 */
const analyzing = ref(false);

// ==================== 生命周期钩子 ====================

onMounted(() => {
  loadCarNames();
  setQuickTime(7);
});

// ==================== 方法 ====================

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
 * 分析驾驶行为
 */
const analyzeBehavior = async () => {
  if (!selectedCarName.value) {
    ElMessage.warning('请选择车辆');
    return;
  }
  if (!timeRange.value || timeRange.value.length !== 2) {
    ElMessage.warning('请选择时间范围');
    return;
  }

  analyzing.value = true;
  loading.value = true;

  try {
    const startTime = timeRange.value[0].toISOString();
    const endTime = timeRange.value[1].toISOString();

    // 分析行为
    const analyzeResponse = await axios.get(
      `http://localhost:8081/api/behaviors/car/${selectedCarName.value}/analyze`,
      { params: { startTime, endTime } }
    );
    behaviors.value = analyzeResponse.data.data || [];

    // 获取评分
    const scoreResponse = await axios.get(
      `http://localhost:8081/api/behaviors/car/${selectedCarName.value}/score`,
      { params: { startTime, endTime } }
    );
    drivingScore.value = scoreResponse.data.data;

    ElMessage.success(`分析完成，检测到 ${behaviors.value.length} 个异常行为`);
  } catch (error) {
    console.error('分析失败:', error);
    ElMessage.error('分析失败');
  } finally {
    analyzing.value = false;
    loading.value = false;
  }
};

/**
 * 设置快捷时间
 */
const setQuickTime = (days) => {
  const end = new Date();
  const start = new Date(end.getTime() - days * 24 * 60 * 60 * 1000);
  timeRange.value = [start, end];
};

/**
 * 获取行为类型标签
 */
const getBehaviorLabel = (type) => {
  const labels = {
    'RAPID_ACCEL': '急加速',
    'RAPID_BRAKE': '急刹车',
    'OVERSPEED': '超速',
    'FATIGUE': '疲劳驾驶',
    'SHARP_TURN': '急转弯',
    'IDLE_LONG': '长时间怠速'
  };
  return labels[type] || type;
};

/**
 * 获取行为图标
 */
const getBehaviorIcon = (type) => {
  const icons = {
    'RAPID_ACCEL': Promotion,
    'RAPID_BRAKE': VideoPause,
    'OVERSPEED': TrendCharts,
    'FATIGUE': Timer,
    'SHARP_TURN': TrendCharts,
    'IDLE_LONG': Clock
  };
  return icons[type] || Warning;
};

/**
 * 获取行为标签类型
 */
const getBehaviorTagType = (type) => {
  const types = {
    'RAPID_ACCEL': 'warning',
    'RAPID_BRAKE': 'danger',
    'OVERSPEED': 'danger',
    'FATIGUE': 'danger',
    'SHARP_TURN': 'warning',
    'IDLE_LONG': 'info'
  };
  return types[type] || 'info';
};

/**
 * 获取风险等级标签
 */
const getRiskLabel = (level) => {
  const labels = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险',
    'CRITICAL': '严重'
  };
  return labels[level] || level;
};

/**
 * 获取风险标签类型
 */
const getRiskTagType = (level) => {
  const types = {
    'LOW': 'success',
    'MEDIUM': 'warning',
    'HIGH': 'danger',
    'CRITICAL': 'danger'
  };
  return types[level] || 'info';
};

/**
 * 获取评分颜色
 */
const getScoreColor = (score) => {
  if (score >= 90) return '#52c41a';
  if (score >= 80) return '#1890ff';
  if (score >= 60) return '#faad14';
  return '#ff4d4f';
};

/**
 * 获取评分标签类型
 */
const getScoreTagType = (grade) => {
  const types = {
    '优秀': 'success',
    '良好': 'primary',
    '一般': 'warning',
    '较差': 'danger'
  };
  return types[grade] || 'info';
};

/**
 * 格式化时间
 */
const formatTime = (datetime) => {
  if (!datetime) return '--';
  const date = new Date(datetime);
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};
</script>

<style scoped>
.driving-analysis-page {
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

.control-card, .score-card, .behaviors-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
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

.score-display {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 8px solid #1890ff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
}

.score-number {
  font-size: 36px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1;
}

.score-label {
  font-size: 14px;
  color: #8c8c8c;
}

.score-stats {
  display: flex;
  justify-content: center;
  padding: 10px 0;
  border-top: 1px solid #e8e8e8;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}

.stat-label {
  font-size: 12px;
  color: #8c8c8c;
}

.behavior-counts {
  padding-top: 15px;
  border-top: 1px solid #e8e8e8;
}

.count-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.count-value {
  font-weight: 600;
  color: #1a1a2e;
}

.behaviors-list {
  max-height: 600px;
  overflow-y: auto;
}

.behavior-item {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #f8f9fa;
  border-left: 4px solid #1890ff;
}

.behavior-item.risk-low {
  border-left-color: #52c41a;
}

.behavior-item.risk-medium {
  border-left-color: #faad14;
}

.behavior-item.risk-high {
  border-left-color: #ff7875;
}

.behavior-item.risk-critical {
  border-left-color: #ff4d4f;
  background: #fff2f0;
}

.behavior-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.behavior-type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-name {
  font-weight: 600;
  font-size: 15px;
  color: #1a1a2e;
}

.behavior-desc {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #595959;
}

.behavior-details {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #8c8c8c;
}

.icon-rapid_accel { color: #faad14; }
.icon-rapid_brake { color: #ff7875; }
.icon-overspeed { color: #ff4d4f; }
.icon-fatigue { color: #722ed1; }
</style>
