<template>
  <div id="app">
    <nav class="navbar navbar-expand-lg navbar-dark bg-gradient">
      <div class="container-fluid">
        <a class="navbar-brand" href="#">
          <i class="bi bi-car-front-fill me-2"></i>
          智能车辆监控系统
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav ms-auto">
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentView === 'dashboard' }" @click="currentView = 'dashboard'">
                <i class="bi bi-speedometer2 me-1"></i>仪表板
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentView === 'map' }" @click="currentView = 'map'">
                <i class="bi bi-map me-1"></i>地图监控
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentView === 'groups' }" @click="currentView = 'groups'">
                <i class="bi bi-collection me-1"></i>车辆分组
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentView === 'alerts' }" @click="currentView = 'alerts'">
                <i class="bi bi-bell me-1"></i>报警记录
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentView === 'fences' }" @click="currentView = 'fences'">
                <i class="bi bi-geo-alt me-1"></i>地理围栏
              </a>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <main class="main-content">
      <TechDashboard v-if="currentView === 'dashboard'" />
      <MapView v-else-if="currentView === 'map'" />
      <VehicleGroups v-else-if="currentView === 'groups'" />
      <FenceAlerts v-else-if="currentView === 'alerts'" />
      <GeoFences v-else-if="currentView === 'fences'" />
    </main>
  </div>
</template>

<script>
import { ref } from 'vue';
import TechDashboard from './components/TechDashboard.vue';
import MapView from './components/MapView.vue';
import VehicleGroups from './components/VehicleGroups.vue';
import FenceAlerts from './components/FenceAlerts.vue';
import GeoFences from './components/GeoFences.vue';

export default {
  name: 'App',
  components: {
    TechDashboard,
    MapView,
    VehicleGroups,
    FenceAlerts,
    GeoFences
  },
  setup() {
    const currentView = ref('dashboard');

    return {
      currentView
    };
  }
};
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.navbar {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  backdrop-filter: blur(10px);
  background: rgba(0,0,0,0.85) !important;
  padding: 1rem 0;
}

.navbar-brand {
  font-weight: 600;
  font-size: 1.5rem;
  color: #fff !important;
  transition: all 0.3s ease;
}

.navbar-brand:hover {
  color: #4dabf7 !important;
  transform: scale(1.05);
}

.nav-link {
  color: rgba(255,255,255,0.8) !important;
  font-weight: 500;
  padding: 0.75rem 1.25rem !important;
  margin: 0 0.25rem;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
}

.nav-link:hover {
  color: #fff !important;
  background: rgba(255,255,255,0.1);
  transform: translateY(-2px);
}

.nav-link.active {
  color: #fff !important;
  background: linear-gradient(45deg, #4dabf7, #3bc9db);
  box-shadow: 0 4px 12px rgba(77, 171, 247, 0.4);
}

.nav-link i {
  font-size: 1.1em;
}

.main-content {
  padding: 0;
  min-height: calc(100vh - 76px);
}

.bg-gradient {
  background: linear-gradient(90deg, #2c3e50, #34495e) !important;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .navbar-nav {
    background: rgba(0,0,0,0.9);
    border-radius: 8px;
    margin-top: 1rem;
    padding: 1rem;
  }
  
  .nav-link {
    margin: 0.25rem 0;
  }
}

/* 滚动条美化 */
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
</style>