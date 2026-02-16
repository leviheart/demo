<template>
  <div class="vehicle-groups-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-title">
          <el-icon class="title-icon"><Collection /></el-icon>
          <h1>车辆分组管理</h1>
        </div>
        <el-button type="primary" @click="showAddDialog = true" class="add-btn">
          <el-icon><Plus /></el-icon>新建分组
        </el-button>
      </div>
    </div>

    <div class="content-wrapper">
      <el-card class="data-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">分组列表</span>
            <div class="header-actions">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索分组名称"
                clearable
                style="width: 200px; margin-right: 12px;"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button @click="refreshData">
                <el-icon><Refresh /></el-icon>刷新
              </el-button>
            </div>
          </div>
        </template>

        <el-table 
          :data="filteredGroups" 
          stripe 
          style="width: 100%" 
          v-loading="loading"
          row-class-name="table-row"
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="groupName" label="分组名称" min-width="200">
            <template #default="scope">
              <span class="group-name">{{ scope.row.groupName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="250">
            <template #default="scope">
              <span class="description-text">{{ scope.row.description || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag 
                :type="scope.row.status === 'active' ? 'success' : 'info'" 
                class="status-tag"
              >
                {{ scope.row.status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="180" align="center">
            <template #default="scope">
              <span class="time-text">{{ scope.row.createdTime ? formatDate(scope.row.createdTime) : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="scope">
              <el-button 
                size="small" 
                type="danger" 
                @click="deleteGroup(scope.row.id)"
                class="action-btn delete-btn"
              >
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 新建分组对话框 -->
    <el-dialog 
      v-model="showAddDialog" 
      title="新建车辆分组" 
      width="520px"
      class="custom-dialog"
    >
      <el-form 
        :model="newGroup" 
        :rules="rules" 
        ref="formRef" 
        label-width="90px"
        class="group-form"
      >
        <el-form-item label="分组名称" prop="groupName">
          <el-input 
            v-model="newGroup.groupName" 
            placeholder="请输入分组名称" 
            clearable
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="newGroup.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入分组描述"
            resize="none"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch 
            v-model="newGroup.status" 
            active-value="active" 
            inactive-value="inactive"
            active-text="启用" 
            inactive-text="禁用"
            inline-prompt
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelAdd">取消</el-button>
          <el-button type="primary" @click="saveGroup" :loading="saving">
            {{ saving ? '保存中...' : '保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Collection, Plus, Search, Refresh, Delete 
} from '@element-plus/icons-vue';

const groups = ref([]);
const loading = ref(false);
const saving = ref(false);
const showAddDialog = ref(false);
const formRef = ref();
const searchKeyword = ref('');

const newGroup = reactive({
  groupName: '',
  description: '',
  status: 'active'
});

const rules = {
  groupName: [
    { required: true, message: '请输入分组名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ]
};

const filteredGroups = computed(() => {
  if (!searchKeyword.value) return groups.value;
  return groups.value.filter(group => 
    group.groupName.toLowerCase().includes(searchKeyword.value.toLowerCase())
  );
});

const loadGroups = async () => {
  loading.value = true;
  try {
    const response = await fetch('http://localhost:8081/api/vehicle-groups');
    groups.value = await response.json();
  } catch (error) {
    ElMessage.error('加载数据失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const saveGroup = async () => {
  if (!formRef.value) return;
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      saving.value = true;
      try {
        const response = await fetch('http://localhost:8081/api/vehicle-groups', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(newGroup)
        });

        if (response.ok) {
          const savedGroup = await response.json();
          groups.value.push(savedGroup);
          showAddDialog.value = false;
          resetForm();
          ElMessage.success('分组创建成功');
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

const deleteGroup = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个分组吗？此操作不可撤销。',
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    );
    
    const response = await fetch(`http://localhost:8081/api/vehicle-groups/${id}`, { 
      method: 'DELETE' 
    });
    
    if (response.ok) {
      groups.value = groups.value.filter(group => group.id !== id);
      ElMessage.success('删除成功');
    } else {
      ElMessage.error('删除失败');
    }
  } catch {
    // 用户取消删除
  }
};

const refreshData = () => {
  loadGroups();
  ElMessage.success('数据已刷新');
};

const cancelAdd = () => {
  showAddDialog.value = false;
  resetForm();
};

const resetForm = () => {
  Object.assign(newGroup, { groupName: '', description: '', status: 'active' });
  if (formRef.value) {
    formRef.value.resetFields();
  }
};

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

onMounted(() => {
  loadGroups();
});
</script>

<style scoped>
.vehicle-groups-page {
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

.header-actions {
  display: flex;
  align-items: center;
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

.group-name {
  font-weight: 500;
  color: #303133;
}

.description-text {
  color: #606266;
  font-size: 13px;
}

.status-tag {
  border-radius: 12px;
  padding: 4px 12px;
  font-size: 12px;
}

.time-text {
  color: #909399;
  font-size: 13px;
}

.action-btn {
  border-radius: 6px;
  padding: 6px 12px;
  font-size: 12px;
}

.delete-btn {
  background: linear-gradient(90deg, #f56c6c, #ff7875);
  border: none;
}

.delete-btn:hover {
  background: linear-gradient(90deg, #f78989, #ff9999);
  transform: translateY(-1px);
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

.group-form {
  padding: 8px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
}

@media (max-width: 768px) {
  .vehicle-groups-page {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
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