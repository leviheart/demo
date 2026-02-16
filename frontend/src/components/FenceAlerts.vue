<template>
  <div class="fence-alerts-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><Bell /></el-icon>
          <h1>报警记录管理</h1>
        </div>
        <div class="header-actions">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="margin-right: 12px;"
          />
          <el-button type="primary" @click="filterAlarms">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button type="success" @click="loadAlarms">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </div>
    </div>

    <div class="content-wrapper">
      <el-card class="data-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">报警列表</span>
            <el-badge :value="alerts.length" type="danger">
              <el-tag type="info">总记录数</el-tag>
            </el-badge>
          </div>
        </template>

        <el-table 
          :data="alerts" 
          stripe 
          style="width: 100%" 
          v-loading="loading"
          row-class-name="table-row"
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="createdTime" label="时间" width="180" align="center">
            <template #default="scope">
              <span class="time-text">{{ scope.row.createdTime ? new Date(scope.row.createdTime).toLocaleString() : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="carName" label="车牌号" width="120">
            <template #default="scope">
              <span class="car-name">{{ scope.row.carName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="alertType" label="报警类型" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getAlertTypeColor(scope.row.alertType)" size="small">
                {{ getAlertTypeText(scope.row.alertType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="severity" label="严重程度" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getSeverityColor(scope.row.severity)" size="small">
                {{ scope.row.severity || '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="latitude" label="位置" min-width="180">
            <template #default="scope">
              <span class="location-text">
                {{ scope.row.latitude && scope.row.longitude ? 
                  `${scope.row.latitude.toFixed(6)}, ${scope.row.longitude.toFixed(6)}` : '未知位置' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="isHandled" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.isHandled ? 'success' : 'warning'" size="small">
                {{ scope.row.isHandled ? '已处理' : '待处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="scope">
              <el-button 
                v-if="!scope.row.isHandled"
                size="small" 
                type="success" 
                @click="resolveAlert(scope.row.id)"
                class="action-btn"
              >
                <el-icon><Check /></el-icon>处理
              </el-button>
              <span v-else class="handled-text">-</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Bell, Search, Refresh, Check } from '@element-plus/icons-vue';

const alerts = ref([]);
const loading = ref(false);
const dateRange = ref([]);

const loadAlarms = async () => {
  loading.value = true;
  try {
    const response = await fetch('http://localhost:8081/api/fence-alerts');
    const result = await response.json();
    alerts.value = result.data || [];
  } catch (error) {
    ElMessage.error('加载数据失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const filterAlarms = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    loadAlarms();
    return;
  }
  
  loading.value = true;
  try {
    const [start, end] = dateRange.value;
    const response = await fetch(`http://localhost:8081/api/fence-alerts/range?startTime=${start}T00:00:00&endTime=${end}T23:59:59`);
    const result = await response.json();
    alerts.value = result.data || [];
  } catch (error) {
    ElMessage.error('查询失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const resolveAlert = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要标记此报警为已处理吗？',
      '处理确认',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    );
    
    const response = await fetch(`http://localhost:8081/api/fence-alerts/${id}/resolve`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        handler: 'system',
        handleTime: new Date().toISOString()
      })
    });

    if (response.ok) {
      const result = await response.json();
      const index = alerts.value.findIndex(a => a.id === result.data.id);
      if (index !== -1) {
        alerts.value[index] = result.data;
      }
      ElMessage.success('处理成功');
    } else {
      ElMessage.error('处理失败');
    }
  } catch {
    // 用户取消
  }
};

const getAlertTypeText = (type) => {
  const typeMap = {
    'entry': '进入',
    'exit': '离开',
    'both': '进出'
  };
  return typeMap[type] || type || '-';
};

const getAlertTypeColor = (type) => {
  const colorMap = {
    'entry': 'primary',
    'exit': 'danger',
    'both': 'warning'
  };
  return colorMap[type] || 'info';
};

const getSeverityColor = (severity) => {
  if (!severity) return 'info';
  const colorMap = {
    'high': 'danger',
    'medium': 'warning',
    'low': 'info'
  };
  return colorMap[severity.toLowerCase()] || 'info';
};

onMounted(() => {
  loadAlarms();
});
</script>

<style scoped>
.fence-alerts-page {
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
  flex-wrap: wrap;
  gap: 16px;
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

.header-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.content-wrapper {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.data-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
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

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background: #f5f7fa;
}

:deep(.el-table__header th) {
  background: #f5f7fa !important;
  color: #606266;
  font-weight: 500;
  padding: 16px 12px;
}

:deep(.el-table__body td) {
  padding: 14px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.table-row:hover {
  background: #f5f7fa !important;
}

.time-text,
.location-text {
  color: #606266;
  font-size: 13px;
}

.car-name {
  font-weight: 500;
  color: #303133;
}

.action-btn {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
}

.handled-text {
  color: #909399;
}

@media (max-width: 992px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .fence-alerts-page {
    padding: 16px;
  }
  
  .header-content {
    padding: 16px;
  }
  
  .content-wrapper {
    padding: 16px;
  }
  
  :deep(.el-table) {
    font-size: 12px;
  }
}
</style>
