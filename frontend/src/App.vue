<!--
================================================================================
  App.vue - 智能车辆监控系统主入口组件
================================================================================

  【功能概述】
  这是Vue3应用的主入口组件，负责整体页面布局和导航菜单。
  采用单页应用(SPA)架构，通过条件渲染切换不同功能模块。

  【组件结构】
  ┌─────────────────────────────────────────────────────────────────────────┐
  │ 顶部导航栏 (el-menu)                                                    │
  │ ├── 品牌标识区                                                          │
  │ └── 导航菜单项                                                          │
  │     ├── 仪表板 (dashboard)                                              │
  │     ├── 地图监控 (map)                                                  │
  │     ├── 轨迹回放 (playback)                                             │
  │     ├── 车辆分组 (groups)                                               │
  │     ├── 报警记录 (alerts)                                               │
  │     └── 地理围栏 (fences)                                               │
  ├─────────────────────────────────────────────────────────────────────────┤
  │ 主内容区 (main-content)                                                 │
  │ └── 动态组件切换 (根据currentView显示不同组件)                           │
  └─────────────────────────────────────────────────────────────────────────┘

  【技术栈】
  - Vue 3 Composition API (script setup语法)
  - Element Plus UI组件库
  - 响应式设计，支持移动端

  【关联组件】
  - TechDashboard.vue: 技术仪表板
  - MapView.vue: 地图监控视图
  - TrackPlayback.vue: 轨迹回放视图
  - VehicleGroups.vue: 车辆分组管理
  - FenceAlerts.vue: 围栏告警记录
  - GeoFences.vue: 地理围栏管理

  【状态管理】
  使用ref管理当前视图状态，无全局状态管理器(可扩展使用Pinia)

  【样式特点】
  - 渐变背景色
  - 毛玻璃效果(backdrop-filter)
  - 响应式布局适配移动端
================================================================================
-->

<template>
  <div id="app">
    <!-- 顶部导航栏 -->
    <el-menu
      :default-active="currentView"
      mode="horizontal"
      class="navbar"
      @select="handleMenuSelect"
    >
      <!-- 品牌标识区域 -->
      <div class="navbar-brand">
        <el-icon class="brand-icon"><Van /></el-icon>
        <span>智能车辆监控系统</span>
      </div>
      
      <!-- 导航菜单项 -->
      <div class="navbar-menu">
        <el-menu-item index="dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表板</span>
        </el-menu-item>
        <el-menu-item index="map">
          <el-icon><MapLocation /></el-icon>
          <span>地图监控</span>
        </el-menu-item>
        <el-menu-item index="playback">
          <el-icon><VideoPlay /></el-icon>
          <span>轨迹回放</span>
        </el-menu-item>
        <el-menu-item index="analysis">
          <el-icon><DataAnalysis /></el-icon>
          <span>驾驶分析</span>
        </el-menu-item>
        <el-menu-item index="groups">
          <el-icon><Collection /></el-icon>
          <span>车辆分组</span>
        </el-menu-item>
        <el-menu-item index="alerts">
          <el-icon><Bell /></el-icon>
          <span>报警记录</span>
        </el-menu-item>
        <el-menu-item index="fences">
          <el-icon><Location /></el-icon>
          <span>地理围栏</span>
        </el-menu-item>
      </div>
    </el-menu>

    <!-- 主内容区域 - 根据currentView动态渲染组件 -->
    <main class="main-content">
      <TechDashboard v-if="currentView === 'dashboard'" />
      <MapView v-else-if="currentView === 'map'" />
      <TrackPlayback v-else-if="currentView === 'playback'" />
      <VehicleGroups v-else-if="currentView === 'groups'" />
      <FenceAlerts v-else-if="currentView === 'alerts'" />
      <GeoFences v-else-if="currentView === 'fences'" />
      <DrivingAnalysis v-else-if="currentView === 'analysis'" />
    </main>
  </div>
</template>

<script setup>
/**
 * 脚本部分 - 使用Vue 3 Composition API
 * 
 * 导入说明:
 * - ref: Vue响应式引用，用于创建响应式状态
 * - 子组件: 各功能模块的Vue组件
 */
import { ref } from 'vue';
import TechDashboard from './components/TechDashboard.vue';
import MapView from './components/MapView.vue';
import TrackPlayback from './components/TrackPlayback.vue';
import VehicleGroups from './components/VehicleGroups.vue';
import FenceAlerts from './components/FenceAlerts.vue';
import GeoFences from './components/GeoFences.vue';
import DrivingAnalysis from './components/DrivingAnalysis.vue';
import { VideoPlay, DataAnalysis } from '@element-plus/icons-vue';

/**
 * 当前视图状态
 * 用于控制显示哪个功能模块
 * 默认值: 'dashboard' (仪表板)
 */
const currentView = ref('dashboard');

/**
 * 处理菜单选择事件
 * 当用户点击导航菜单项时调用
 * 
 * @param {string} index - 菜单项的索引值，对应组件名称
 */
const handleMenuSelect = (index) => {
  currentView.value = index;
};
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 页面基础样式 */
body {
  font-family: 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 应用容器样式 */
#app {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 导航栏样式 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(0, 0, 0, 0.85) !important;
  backdrop-filter: blur(10px);
  border-bottom: none !important;
  padding: 0 24px;
  height: 64px;
}

/* 品牌标识样式 */
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
}

.brand-icon {
  font-size: 28px;
  color: #409eff;
}

/* 导航菜单样式 */
.navbar-menu {
  display: flex;
  background: transparent !important;
  border: none !important;
}

.navbar-menu .el-menu-item {
  color: rgba(255, 255, 255, 0.8) !important;
  border-bottom: none !important;
  height: 64px;
  line-height: 64px;
  padding: 0 20px;
  transition: all 0.3s ease;
}

.navbar-menu .el-menu-item:hover {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.1) !important;
}

.navbar-menu .el-menu-item.is-active {
  color: #fff !important;
  background: linear-gradient(45deg, #409eff, #64b5f6) !important;
  border-bottom: none !important;
}

/* 主内容区域样式 */
.main-content {
  min-height: calc(100vh - 64px);
}

/* 自定义滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式布局 - 平板设备 */
@media (max-width: 992px) {
  .navbar {
    flex-direction: column;
    height: auto;
    padding: 12px;
  }
  
  .navbar-brand {
    margin-bottom: 12px;
  }
  
  .navbar-menu {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .navbar-menu .el-menu-item {
    height: 48px;
    line-height: 48px;
  }
}
</style>
