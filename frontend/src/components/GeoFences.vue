<template>
  <div class="geo-fences-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><Location /></el-icon>
          <h1>地理围栏管理</h1>
        </div>
        <el-button type="primary" @click="showAddDialog = true" class="add-btn">
          <el-icon><Plus /></el-icon>新建围栏
        </el-button>
      </div>
    </div>

    <div class="content-wrapper">
      <el-row :gutter="24">
        <!-- 地图区域 -->
        <el-col :span="16">
          <el-card class="map-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">围栏地图</span>
                <el-tag :type="mapLoaded ? 'success' : 'warning'">
                  {{ mapLoaded ? '地图已加载' : '地图加载中...' }}
                </el-tag>
              </div>
            </template>
            <div ref="mapContainer" class="map-container"></div>
          </el-card>
        </el-col>

        <!-- 围栏列表 -->
        <el-col :span="8">
          <el-card class="list-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">围栏列表</span>
                <el-badge :value="fences.length" type="primary">
                  <el-button size="small" @click="refreshData">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </el-badge>
              </div>
            </template>
            
            <div class="fence-list" v-loading="loading">
              <transition-group name="fence-list" tag="div">
                <el-card 
                  v-for="fence in fences" 
                  :key="fence.id" 
                  class="fence-item"
                  :class="{ 'fence-inactive': fence.status !== 'active' }"
                  shadow="hover"
                >
                  <div class="fence-info">
                    <div class="fence-header">
                      <h3 class="fence-name">{{ fence.fenceName }}</h3>
                      <el-tag 
                        :type="fence.status === 'active' ? 'success' : 'info'" 
                        size="small"
                        class="status-tag"
                      >
                        {{ fence.status === 'active' ? '启用' : '禁用' }}
                      </el-tag>
                    </div>
                    
                    <div class="fence-details">
                      <div class="detail-item">
                        <el-icon class="detail-icon"><Position /></el-icon>
                        <span class="coordinates">
                          {{ fence.centerLatitude?.toFixed(6) }}, {{ fence.centerLongitude?.toFixed(6) }}
                        </span>
                      </div>
                      <div class="detail-item">
                        <el-icon class="detail-icon"><Compass /></el-icon>
                        <span class="radius">半径: {{ fence.radius }}米</span>
                      </div>
                      <div class="detail-item" v-if="fence.description">
                        <el-icon class="detail-icon"><Document /></el-icon>
                        <span class="description">{{ fence.description }}</span>
                      </div>
                    </div>
                    
                    <div class="fence-footer">
                      <el-button 
                        size="small" 
                        type="danger" 
                        @click="deleteFence(fence.id)"
                        class="action-btn delete-btn"
                      >
                        <el-icon><Delete /></el-icon>删除
                      </el-button>
                      <el-button 
                        size="small" 
                        :type="fence.status === 'active' ? 'warning' : 'success'"
                        @click="toggleFenceStatus(fence.id, fence.status)"
                        class="action-btn"
                      >
                        <el-icon>
                          <VideoPause v-if="fence.status === 'active'" />
                          <VideoPlay v-else />
                        </el-icon>
                        {{ fence.status === 'active' ? '禁用' : '启用' }}
                      </el-button>
                    </div>
                  </div>
                </el-card>
              </transition-group>
              
              <div v-if="fences.length === 0" class="empty-state">
                <el-empty description="暂无围栏数据" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 新建围栏对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      title="新建地理围栏" 
      width="550px"
      class="custom-dialog"
    >
      <el-form 
        :model="newFence" 
        :rules="rules" 
        ref="formRef" 
        label-width="100px"
        class="fence-form"
      >
        <el-form-item label="围栏名称" prop="fenceName">
          <el-input 
            v-model="newFence.fenceName" 
            placeholder="请输入围栏名称" 
            clearable
          />
        </el-form-item>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="中心纬度" prop="centerLatitude">
              <el-input-number 
                v-model="newFence.centerLatitude" 
                :precision="6" 
                :step="0.000001" 
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="中心经度" prop="centerLongitude">
              <el-input-number 
                v-model="newFence.centerLongitude" 
                :precision="6" 
                :step="0.000001" 
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="半径(米)" prop="radius">
          <el-slider 
            v-model="newFence.radius" 
            :min="100" 
            :max="5000" 
            show-input
            show-input-controls
          />
        </el-form-item>
        
        <el-form-item label="报警类型" prop="alertType">
          <el-select v-model="newFence.alertType" style="width: 100%">
            <el-option label="进入报警" value="entry" />
            <el-option label="离开报警" value="exit" />
            <el-option label="进出报警" value="both" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch 
            v-model="newFence.status" 
            active-value="active" 
            inactive-value="inactive"
            active-text="启用" 
            inactive-text="禁用"
            inline-prompt
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="newFence.description" 
            type="textarea" 
            :rows="2" 
            placeholder="请输入围栏描述"
            resize="none"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelAdd">取消</el-button>
          <el-button type="primary" @click="saveFence" :loading="saving">
            {{ saving ? '创建中...' : '创建围栏' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Location, Plus, Refresh, Delete, Position, Compass, Document,
  VideoPause, VideoPlay 
} from '@element-plus/icons-vue';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

const fences = ref([]);
const loading = ref(false);
const saving = ref(false);
const showAddDialog = ref(false);
const formRef = ref();
const mapContainer = ref(null);
const mapLoaded = ref(false);

let map = null;
let markers = {};

const newFence = reactive({
  fenceName: '',
  centerLatitude: 32.0617,
  centerLongitude: 118.7634,
  radius: 1000,
  alertType: 'entry',
  status: 'active',
  description: ''
});

const rules = {
  fenceName: [
    { required: true, message: '请输入围栏名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  centerLatitude: [
    { required: true, message: '请输入纬度', trigger: 'blur' },
    { type: 'number', min: -90, max: 90, message: '纬度范围 -90 到 90', trigger: 'blur' }
  ],
  centerLongitude: [
    { required: true, message: '请输入经度', trigger: 'blur' },
    { type: 'number', min: -180, max: 180, message: '经度范围 -180 到 180', trigger: 'blur' }
  ],
  radius: [
    { required: true, message: '请选择半径', trigger: 'change' },
    { type: 'number', min: 100, max: 5000, message: '半径范围 100-5000米', trigger: 'change' }
  ]
};

const initMap = () => {
  if (mapContainer.value) {
    try {
      
      map = new mapboxgl.Map({
        container: mapContainer.value,
        style: 'mapbox://styles/mapbox/streets-v11',
        center: [118.7634, 32.0617],
        zoom: 13
      });

      // 添加控件
      map.addControl(new mapboxgl.NavigationControl(), 'top-right');
      map.addControl(new mapboxgl.ScaleControl({ unit: 'metric' }));
      
      mapLoaded.value = true;
    } catch (error) {
      ElMessage.error('地图初始化失败');
    }
  }
};

const loadFences = async () => {
  loading.value = true;
  try {
    const response = await fetch('http://localhost:8081/api/geo-fences');
    const result = await response.json();
    fences.value = result.data || [];
    renderFencesOnMap();
  } catch (error) {
    ElMessage.error('加载围栏失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const renderFencesOnMap = () => {
  // 清除现有标记
  Object.values(markers).forEach(marker => marker.remove());
  markers = {};

  fences.value.forEach(fence => {
    if (fence.centerLatitude && fence.centerLongitude && fence.status === 'active') {
      // 创建圆形容器
      const circleElement = document.createElement('div');
      circleElement.className = 'fence-circle';
      circleElement.style.width = `${Math.max(fence.radius / 10, 50)}px`;
      circleElement.style.height = `${Math.max(fence.radius / 10, 50)}px`;
      circleElement.style.border = '2px solid #409eff';
      circleElement.style.borderRadius = '50%';
      circleElement.style.backgroundColor = 'rgba(64, 158, 255, 0.2)';
      circleElement.style.boxShadow = '0 0 10px rgba(64, 158, 255, 0.5)';
      
      const marker = new mapboxgl.Marker({
        element: circleElement,
        anchor: 'center'
      })
      .setLngLat([fence.centerLongitude, fence.centerLatitude])
      .setPopup(
        new mapboxgl.Popup({ offset: 25 }).setHTML(`
          <div class="popup-content">
            <h4>${fence.fenceName}</h4>
            <p><strong>类型:</strong> ${fence.fenceType}</p>
            <p><strong>报警:</strong> ${fence.alertType}</p>
            <p><strong>描述:</strong> ${fence.description || '无'}</p>
          </div>
        `)
      )
      .addTo(map);

      markers[fence.id] = marker;
    }
  });
};

const saveFence = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true;
      try {
        const response = await fetch('http://localhost:8081/api/geo-fences', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            ...newFence,
            fenceType: 'circle'
          })
        });

        if (response.ok) {
          const result = await response.json();
          fences.value.push(result.data);
          showAddDialog.value = false;
          resetForm();
          renderFencesOnMap();
          ElMessage.success('围栏创建成功');
        } else {
          ElMessage.error('创建失败');
        }
      } catch (error) {
        ElMessage.error('保存失败，请检查网络连接');
      } finally {
        saving.value = false;
      }
    }
  });
};

const deleteFence = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个围栏吗？此操作不可撤销。',
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );
    
    const response = await fetch(`http://localhost:8081/api/geo-fences/${id}`, { method: 'DELETE' });
    
    if (response.ok) {
      fences.value = fences.value.filter(fence => fence.id !== id);
      if (markers[id]) {
        map.removeLayer(markers[id]);
        delete markers[id];
      }
      ElMessage.success('删除成功');
    } else {
      ElMessage.error('删除失败');
    }
  } catch {
    // 用户取消删除
  }
};

const toggleFenceStatus = async (id, currentStatus) => {
  const newStatus = currentStatus === 'active' ? 'inactive' : 'active';
  try {
    const response = await fetch(`http://localhost:8081/api/geo-fences/${id}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: newStatus })
    });

    if (response.ok) {
      const result = await response.json();
      const index = fences.value.findIndex(f => f.id === result.data.id);
      if (index !== -1) {
        fences.value[index] = result.data;
      }
      renderFencesOnMap();
      ElMessage.success('状态更新成功');
    } else {
      ElMessage.error('更新失败');
    }
  } catch (error) {
    ElMessage.error('更新失败，请检查网络连接');
  }
};

const refreshData = () => {
  loadFences();
  ElMessage.success('数据已刷新');
};

const cancelAdd = () => {
  showAddDialog.value = false;
  resetForm();
};

const resetForm = () => {
  Object.assign(newFence, {
    fenceName: '',
    centerLatitude: 32.0617,
    centerLongitude: 118.7634,
    radius: 1000,
    alertType: 'entry',
    status: 'active',
    description: ''
  });
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

onMounted(() => {
  initMap();
  loadFences();
});

onUnmounted(() => {
  if (map) {
    map.remove();
  }
});
</script>

<style scoped>
.geo-fences-page {
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
  gap: 12px;
}

.title-icon {
  font-size: 28px;
  color: #409eff;
}

.header-title h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(90deg, #409eff, #64b5f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.add-btn {
  height: 40px;
  padding: 0 24px;
  font-size: 14px;
  border-radius: 20px;
  background: linear-gradient(90deg, #409eff, #64b5f6);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s ease;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

.content-wrapper {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.map-card, .list-card {
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
  height: 500px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

/* Mapbox围栏圆圈样式 */
.fence-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); opacity: 0.8; }
}

.fence-list {
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

.fence-item {
  margin-bottom: 16px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #ebeef5;
}

.fence-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.fence-inactive {
  opacity: 0.7;
  filter: grayscale(30%);
}

.fence-info {
  padding: 20px;
}

.fence-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.fence-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.status-tag {
  border-radius: 12px;
}

.fence-details {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.detail-icon {
  margin-right: 8px;
  color: #909399;
  font-size: 14px;
}

.coordinates, .radius, .description {
  flex: 1;
}

.fence-footer {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 12px;
  transition: all 0.2s ease;
}

.delete-btn {
  background: linear-gradient(90deg, #f56c6c, #ff7875);
  border: none;
}

.delete-btn:hover {
  background: linear-gradient(90deg, #f78989, #ff9999);
  transform: translateY(-1px);
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.custom-dialog {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(90deg, #409eff, #64b5f6);
  padding: 20px 24px;
}

:deep(.el-dialog__title) {
  color: white;
  font-weight: 500;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

.fence-form {
  padding: 8px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px 24px;
}

.popup-content h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.popup-content p {
  margin: 6px 0;
  font-size: 13px;
  color: #606266;
}

.fence-list-enter-active,
.fence-list-leave-active {
  transition: all 0.3s ease;
}

.fence-list-enter-from,
.fence-list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

@media (max-width: 1200px) {
  .content-wrapper {
    padding: 16px;
  }
  
  :deep(.el-col-16), :deep(.el-col-8) {
    width: 100%;
    margin-bottom: 24px;
  }
  
  .map-container {
    height: 400px;
  }
  
  .fence-list {
    max-height: 400px;
  }
}

@media (max-width: 768px) {
  .geo-fences-page {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }
  
  .fence-footer {
    flex-direction: column;
  }
  
  .action-btn {
    width: 100%;
  }
}
</style>