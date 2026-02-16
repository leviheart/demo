<template>
  <div class="fence-alerts-page">
    <div class="page-header">
      <h1>报警记录管理</h1>
      <div class="header-actions">
        <div class="input-group me-2">
          <input type="date" class="form-control" v-model="startDate">
          <input type="date" class="form-control" v-model="endDate">
          <button class="btn btn-outline-secondary" @click="filterAlarms">
            <i class="bi bi-search"></i> 查询
          </button>
        </div>
        <button class="btn btn-success" @click="loadAlarms">
          <i class="bi bi-arrow-clockwise"></i> 刷新
        </button>
      </div>
    </div>

    <div class="content-area">
      <div class="alerts-table">
        <table class="table table-striped">
          <thead>
            <tr>
              <th>ID</th>
              <th>时间</th>
              <th>车牌号</th>
              <th>报警类型</th>
              <th>严重程度</th>
              <th>位置</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="alert in alerts" :key="alert.id" :class="getSeverityClass(alert.severity)">
              <td>{{ alert.id }}</td>
              <td>{{ alert.createdTime ? new Date(alert.createdTime).toLocaleString() : '-' }}</td>
              <td>{{ alert.carName || '-' }}</td>
              <td>{{ alert.alertType || '-' }}</td>
              <td>
                <span class="badge" :class="`bg-${getSeverityColor(alert.severity)}`">
                  {{ alert.severity || '-' }}
                </span>
              </td>
              <td>{{ alert.latitude && alert.longitude ? `${alert.latitude.toFixed(6)},${alert.longitude.toFixed(6)}` : '未知位置' }}</td>
              <td>
                <span class="badge" :class="alert.isHandled ? 'bg-success' : 'bg-warning'">
                  {{ alert.isHandled ? '已处理' : '待处理' }}
                </span>
              </td>
              <td>
                <button v-if="!alert.isHandled" class="btn btn-sm btn-outline-success" @click="resolveAlert(alert.id)">
                  <i class="bi bi-check"></i> 处理
                </button>
                <span v-else>-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';

export default {
  name: 'FenceAlerts',
  setup() {
    const alerts = ref([]);
    const startDate = ref('');
    const endDate = ref('');

    // 加载报警数据
    const loadAlarms = async () => {
      try {
        const response = await fetch('http://localhost:8081/api/fence-alerts');
        alerts.value = await response.json();
      } catch (error) {
        console.error('加载报警记录失败:', error);
        alert('加载数据失败，请检查后端服务是否正常运行');
      }
    };

    // 获取严重程度样式类
    const getSeverityClass = (severity) => {
      if (!severity) return '';
      switch(severity.toLowerCase()) {
        case 'high': return 'alert-high';
        case 'medium': return 'alert-medium';
        case 'low': return 'alert-low';
        default: return '';
      }
    };

    // 获取严重程度颜色
    const getSeverityColor = (severity) => {
      if (!severity) return 'secondary';
      switch(severity.toLowerCase()) {
        case 'high': return 'danger';
        case 'medium': return 'warning';
        case 'low': return 'info';
        default: return 'secondary';
      }
    };

    // 处理报警
    const resolveAlert = async (id) => {
      if (confirm('确定要标记此报警为已处理吗？')) {
        try {
          const response = await fetch(`http://localhost:8081/api/fence-alerts/${id}/resolve`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              handler: 'system',
              handleTime: new Date().toISOString()
            })
          });

          if (response.ok) {
            const updatedAlert = await response.json();
            const index = alerts.value.findIndex(a => a.id === updatedAlert.id);
            if (index !== -1) {
              alerts.value[index] = updatedAlert;
            }
            alert('处理成功！');
          } else {
            alert('处理失败：' + await response.text());
          }
        } catch (error) {
          console.error('处理报警失败:', error);
          alert('处理失败，请检查网络连接');
        }
      }
    };

    // 筛选报警
    const filterAlarms = async () => {
      let url = 'http://localhost:8081/api/fence-alerts';
      if (startDate.value || endDate.value) {
        const params = new URLSearchParams();
        if (startDate.value) params.append('startTime', startDate.value);
        if (endDate.value) params.append('endTime', endDate.value);
        url += `/range?${params.toString()}`;
      }

      try {
        const response = await fetch(url);
        alerts.value = await response.json();
      } catch (error) {
        console.error('筛选报警失败:', error);
        alert('查询失败，请检查网络连接');
      }
    };

    onMounted(() => {
      loadAlarms();
    });

    return {
      alerts,
      startDate,
      endDate,
      getSeverityClass,
      getSeverityColor,
      resolveAlert,
      filterAlarms
    };
  }
};
</script>

<style scoped>
.fence-alerts-page {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #dee2e6;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.input-group {
  display: flex;
  align-items: center;
}

.input-group .form-control {
  margin-right: 5px;
}

.alert-high { background-color: #fff3f3; }
.alert-medium { background-color: #fff8f0; }
.alert-low { background-color: #f8fff8; }

.table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.badge {
  font-size: 0.75em;
}

.btn {
  border-radius: 4px;
}

.form-control {
  border: 1px solid #ced4da;
  border-radius: 4px;
}

.btn-success {
  background-color: #198754;
  border-color: #198754;
}

.btn-success:hover {
  background-color: #157347;
  border-color: #146c43;
}

.btn-outline-secondary {
  border-color: #6c757d;
  color: #6c757d;
}

.btn-outline-secondary:hover {
  background-color: #6c757d;
  color: white;
}
</style>