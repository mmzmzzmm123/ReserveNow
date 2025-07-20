<template>
  <div class="review-manage-container">
    <!-- Search and filter area -->
    <div class="search-section neomorphic-card">
      <div class="search-form">
        <div class="select-container">
          <select v-model="searchForm.restaurantId" class="status-select">
            <option value="">All Restaurants</option>
            <option v-for="item in restaurantOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
          <div class="select-arrow">
            <el-icon><ArrowDown /></el-icon>
          </div>
        </div>
        
        <div class="select-container">
          <select v-model="searchForm.rating" class="status-select">
            <option value="">All Ratings</option>
            <option v-for="rating in 5" :key="rating" :value="rating">
              {{ rating }} Stars
            </option>
          </select>
          <div class="select-arrow">
            <el-icon><ArrowDown /></el-icon>
          </div>
        </div>
        
        <el-button type="primary" @click="handleSearch" class="search-btn">
          <el-icon><Search /></el-icon>Search
        </el-button>
      </div>
    </div>

    <!-- Review list -->
    <div class="table-section neomorphic-card">
      <el-table
        v-loading="loading"
        :data="reviewList"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column label="User Info" width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="40" :src="row.userAvatar">
                {{ row.userName?.charAt(0) }}
              </el-avatar>
              <span class="user-name">{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="restaurantName" label="Restaurant Name" width="150" />
        
        <el-table-column label="Rating" width="170">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        
        <el-table-column width="200" prop="content" label="Review Content" show-overflow-tooltip />
        
        <el-table-column label="Media Content" width="200">
          <template #default="{ row }">
            <div class="media-preview">
              <!-- Image preview -->
              <el-image 
                v-if="row.photos"
                class="preview-image"
                :src="row.photos"
                :preview-src-list="[row.photos]"
                fit="cover"
                :preview-teleported="true"
                :initial-index="0"
                :zoom-rate="1.2"
                :preview-options="{
                  closeOnPressEscape: true,
                  closeOnClickModal: true
                }"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <!-- Video preview -->
              <el-button 
                v-if="row.videos"
                type="primary" 
                link
                @click="handleVideoPreview(row.videos)"
              >
                View Video
              </el-button>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="Created Time" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="Updated Time" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="Actions" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- Video preview dialog -->
    <el-dialog
      v-model="videoDialogVisible"
      title="Video Preview"
      width="640px"
      destroy-on-close
      class="neomorphic-dialog"
    >
      <video 
        v-if="currentVideo"
        :src="currentVideo"
        controls
        style="width: 100%"
      ></video>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Delete,
  Picture,
  ArrowDown
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatDate } from '@/utils/format'

// Search form
const searchForm = reactive({
  restaurantId: '',
  rating: ''
})

// Table data
const loading = ref(false)
const reviewList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Restaurant options
const restaurantOptions = ref([])

// Video preview
const videoDialogVisible = ref(false)
const currentVideo = ref('')

// Get restaurant list (for filtering)
const fetchRestaurantOptions = async () => {
  try {
    const res = await request({
      url: '/admin/restaurants',
      method: 'get',
      params: {
        page: 1,
        pageSize: 100 // Get enough restaurants for filtering
      }
    })
    restaurantOptions.value = res.data.list.map(item => ({
      value: item.id,
      label: item.name
    }))
  } catch (error) {
    console.error('Failed to fetch restaurant list:', error)
  }
}

// Get review list
const fetchReviewList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    
    const res = await request({
      url: '/admin/reviews',
      method: 'get',
      params
    })
    
    if (res.success && res.data) {
      reviewList.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || 'Failed to fetch review list')
    }
  } catch (error) {
    console.error('Failed to fetch review list:', error)
    ElMessage.error('Failed to fetch review list')
  } finally {
    loading.value = false
  }
}

// Search
const handleSearch = () => {
  currentPage.value = 1
  fetchReviewList()
}

// Delete review
const handleDelete = (row) => {
  ElMessageBox.confirm(
    'Are you sure you want to delete this review?',
    'Warning',
    {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/admin/reviews/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('Deleted successfully')
      fetchReviewList()
    } catch (error) {
      console.error('Failed to delete review:', error)
    }
  })
}

// Video preview
const handleVideoPreview = (videoUrl) => {
  currentVideo.value = videoUrl
  videoDialogVisible.value = true
}

// Pagination related
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchReviewList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchReviewList()
}

// Initialize
onMounted(() => {
  fetchRestaurantOptions()
  fetchReviewList()
})
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
  min-height: 100%;
  background-color: #e6e7ee;
  background-image: linear-gradient(135deg, #e6e7ee 0%, #f5f7fa 100%);
}

.neomorphic-card {
  background: #e6e7ee;
  border-radius: 24px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 10px 10px 20px #c3c4ca,
              -10px -10px 20px #ffffff;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.neomorphic-card:hover {
  box-shadow: 12px 12px 24px #c3c4ca,
              -12px -12px 24px #ffffff;
  transform: translateY(-2px);
}

.search-section {
  margin-bottom: 24px;
}

.search-form {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.select-container {
  position: relative;
  width: 180px;
}

.status-select {
  appearance: none;
  width: 100%;
  padding: 12px 15px;
  font-size: 14px;
  border: none;
  border-radius: 16px;
  background-color: #e6e7ee;
  color: #5e6687;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: inset 3px 3px 7px #c3c4ca,
              inset -3px -3px 7px #ffffff;
  outline: none;
  border: 1px solid rgba(255, 255, 255, 0.7);
}

.status-select:focus {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border: 1px solid #409eff;
}

.select-arrow {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #5e6687;
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 16px;
  padding: 12px 24px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
}

.search-btn:active {
  transform: translateY(1px);
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
}

.table-section {
  min-height: 500px;
  overflow: hidden;
}

.table-section :deep(.el-table) {
  border-radius: 16px;
  border: none;
  overflow: hidden;
  box-shadow: inset 2px 2px 5px #c3c4ca,
              inset -2px -2px 5px #ffffff;
}

.table-section :deep(.el-table th) {
  background-color: #e6e7ee;
  border-bottom: 2px solid rgba(195, 196, 202, 0.3);
  font-weight: 600;
  color: #5e6687;
}

.table-section :deep(.el-table tr) {
  background-color: #e6e7ee;
}

.table-section :deep(.el-table td) {
  border-bottom: 1px solid rgba(195, 196, 202, 0.2);
}

.table-section :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: rgba(245, 247, 250, 0.5);
}

.table-section :deep(.el-table__body tr:hover > td) {
  background-color: rgba(235, 238, 245, 0.7) !important;
}

.pagination-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.pagination-container :deep(.el-pagination.is-background .el-pager li:not(.is-disabled)) {
  background-color: #e6e7ee;
  border-radius: 8px;
  box-shadow: 2px 2px 5px #c3c4ca,
              -2px -2px 5px #ffffff;
  margin: 0 4px;
  transition: all 0.3s ease;
}

.pagination-container :deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  transform: translateY(-2px);
  box-shadow: 3px 3px 6px #c3c4ca,
              -3px -3px 6px #ffffff;
}

.pagination-container :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #409eff;
  box-shadow: inset 2px 2px 5px rgba(0, 0, 0, 0.1),
              inset -2px -2px 5px rgba(255, 255, 255, 0.1);
  color: #fff;
}

.neomorphic-dialog {
  border-radius: 24px;
  overflow: hidden;
}

.neomorphic-dialog :deep(.el-dialog) {
  border-radius: 30px;
  overflow: hidden;
  box-shadow: 16px 16px 32px #c3c4ca,
              -16px -16px 32px #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
}

.neomorphic-dialog :deep(.el-dialog__header) {
  background-color: #e6e7ee;
  padding: 30px 30px 20px;
  border-bottom: 1px solid rgba(195, 196, 202, 0.2);
  position: relative;
}

.neomorphic-dialog :deep(.el-dialog__title) {
  font-weight: 700;
  color: #5e6687;
  font-size: 1.4rem;
  letter-spacing: 0.5px;
}

.neomorphic-dialog :deep(.el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
  width: 36px;
  height: 36px;
  background-color: #e6e7ee;
  border-radius: 12px;
  box-shadow: 3px 3px 6px #c3c4ca,
              -3px -3px 6px #ffffff;
  transition: all 0.3s ease;
}

.neomorphic-dialog :deep(.el-dialog__headerbtn:hover) {
  transform: rotate(90deg);
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
}

.neomorphic-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #5e6687;
  font-weight: 700;
  font-size: 20px;
}

.neomorphic-dialog :deep(.el-dialog__body) {
  padding: 30px;
  background-color: #e6e7ee;
}

.neomorphic-dialog :deep(.el-dialog__footer) {
  background-color: #e6e7ee;
  padding: 20px 30px 30px;
  border-top: 1px solid rgba(195, 196, 202, 0.2);
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.neomorphic-dialog :deep(.el-form-item) {
  margin-bottom: 25px;
}

.neomorphic-dialog :deep(.el-form-item__label) {
  font-weight: 600;
  color: #5e6687;
  font-size: 16px;
  padding-bottom: 8px;
}

.neomorphic-dialog :deep(.el-form-item__content) {
  position: relative;
}

.neomorphic-dialog :deep(.el-input__wrapper),
.neomorphic-dialog :deep(.el-select .el-input__wrapper) {
  border-radius: 16px;
  background-color: #e6e7ee;
  box-shadow: inset 3px 3px 7px #c3c4ca,
              inset -3px -3px 7px #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
  padding: 12px 15px;
}

.neomorphic-dialog :deep(.el-input__wrapper.is-focus),
.neomorphic-dialog :deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border-color: #409eff;
}

.neomorphic-dialog :deep(.el-form-item__error) {
  padding-top: 5px;
  font-size: 12px;
  color: #f56c6c;
  font-weight: 500;
}

.neomorphic-dialog :deep(.el-button) {
  border-radius: 16px;
  padding: 12px 24px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  border: none;
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
}

.neomorphic-dialog :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
}

.neomorphic-dialog :deep(.el-button:active) {
  transform: translateY(1px);
  box-shadow: inset 3px 3px 6px #c3c4ca,
              inset -3px -3px 6px #ffffff;
}

.neomorphic-dialog :deep(.el-button--primary) {
  color: white;
  background: linear-gradient(145deg, #3b8fe7, #4da5ff);
}

.neomorphic-dialog :deep(.el-button--primary:hover) {
  background: linear-gradient(145deg, #4da5ff, #3b8fe7);
}

.avatar-uploader {
  text-align: center;
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

.avatar-uploader .avatar {
  width: 150px;
  height: 150px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
  border: 5px solid white;
  transition: all 0.3s ease;
}

.avatar-uploader .avatar:hover {
  transform: scale(1.05);
  box-shadow: 8px 8px 16px #c3c4ca,
              -8px -8px 16px #ffffff;
}

.avatar-uploader .el-upload {
  border: 2px dashed rgba(64, 158, 255, 0.5);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  background-color: #e6e7ee;
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
  width: 150px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
}

.avatar-uploader-icon {
  font-size: 32px;
  color: #409eff;
  width: 150px;
  height: 150px;
  text-align: center;
  line-height: 150px;
}

/* Role tag styles */
.table-section :deep(.el-tag) {
  border-radius: 12px;
  padding: 6px 12px;
  font-weight: 600;
  border: none;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1),
              -2px -2px 5px rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.table-section :deep(.el-tag:hover) {
  transform: translateY(-2px);
}

/* Switch styles */
.table-section :deep(.el-switch__core) {
  border-radius: 12px;
  box-shadow: 2px 2px 5px #c3c4ca,
              -2px -2px 5px #ffffff;
}

/* Button group styles */
.table-section :deep(.el-button-group .el-button) {
  box-shadow: 2px 2px 5px #c3c4ca,
              -2px -2px 5px #ffffff;
  border-radius: 8px;
  margin: 0 4px;
  transition: all 0.3s ease;
}

.table-section :deep(.el-button-group .el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 3px 3px 6px #c3c4ca,
              -3px -3px 6px #ffffff;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .user-manage-container {
    background-color: #2d3748;
    background-image: linear-gradient(135deg, #2d3748 0%, #1a202c 100%);
  }

  .neomorphic-card {
    background: #2d3748;
    box-shadow: 10px 10px 20px #202632,
                -10px -10px 20px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .neomorphic-card:hover {
    box-shadow: 12px 12px 24px #202632,
                -12px -12px 24px #3a485e;
  }

  .status-select {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: inset 3px 3px 7px #202632,
                inset -3px -3px 7px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .status-select:focus {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
    border: 1px solid #409eff;
  }
  
  .select-arrow {
    color: #e2e8f0;
  }
  
  .search-btn {
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }
  
  .search-btn:hover {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }
  
  .search-btn:active {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
  }

  .table-section :deep(.el-table) {
    background-color: #2d3748;
    box-shadow: inset 2px 2px 5px #202632,
                inset -2px -2px 5px #3a485e;
  }

  .table-section :deep(.el-table th),
  .table-section :deep(.el-table tr) {
    background-color: #2d3748;
    color: #e2e8f0;
  }

  .table-section :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
    background-color: rgba(26, 32, 44, 0.5);
  }

  .table-section :deep(.el-table__body tr:hover > td) {
    background-color: rgba(45, 55, 72, 0.8) !important;
  }

  .pagination-container :deep(.el-pagination.is-background .el-pager li:not(.is-disabled)) {
    background-color: #2d3748;
    box-shadow: 2px 2px 5px #202632,
                -2px -2px 5px #3a485e;
    color: #e2e8f0;
  }

  .pagination-container :deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
    background-color: #409eff;
  }

  .neomorphic-dialog :deep(.el-dialog) {
    background-color: #2d3748;
    box-shadow: 16px 16px 32px #202632,
                -16px -16px 32px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .neomorphic-dialog :deep(.el-dialog__header),
  .neomorphic-dialog :deep(.el-dialog__body),
  .neomorphic-dialog :deep(.el-dialog__footer) {
    background-color: #2d3748;
    color: #e2e8f0;
  }

  .neomorphic-dialog :deep(.el-dialog__title) {
    color: #e2e8f0;
  }

  .neomorphic-dialog :deep(.el-form-item__label) {
    color: #e2e8f0;
  }

  .neomorphic-dialog :deep(.el-input__wrapper),
  .neomorphic-dialog :deep(.el-select .el-input__wrapper) {
    background-color: #2d3748;
    box-shadow: inset 3px 3px 7px #202632,
                inset -3px -3px 7px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .neomorphic-dialog :deep(.el-input__wrapper.is-focus),
  .neomorphic-dialog :deep(.el-select .el-input__wrapper.is-focus) {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
    border-color: #409eff;
  }

  .neomorphic-dialog :deep(.el-button) {
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }

  .neomorphic-dialog :deep(.el-button:hover) {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }

  .neomorphic-dialog :deep(.el-button:active) {
    box-shadow: inset 3px 3px 6px #202632,
                inset -3px -3px 6px #3a485e;
  }

  .neomorphic-dialog :deep(.el-button--primary) {
    background: linear-gradient(145deg, #3685d7, #3b97f7);
  }

  .neomorphic-dialog :deep(.el-button--primary:hover) {
    background: linear-gradient(145deg, #3b97f7, #3685d7);
  }

  .avatar-uploader .avatar {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
    border-color: #2d3748;
  }

  .avatar-uploader .avatar:hover {
    box-shadow: 8px 8px 16px #202632,
                -8px -8px 16px #3a485e;
  }

  .avatar-uploader .el-upload {
    background-color: #2d3748;
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }

  .avatar-uploader .el-upload:hover {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .select-container {
    width: 100%;
  }

  .search-btn {
    width: 100%;
    justify-content: center;
  }
  
  .neomorphic-card {
    padding: 16px;
    border-radius: 16px;
  }
  
  .table-section {
    overflow-x: auto;
  }
}

/* 添加自定义表单样式 */
.custom-form {
  padding: 10px 20px;
}

.form-group {
  margin-bottom: 28px;
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #5e6687;
}

.input-container {
  position: relative;
}

.input-container input {
  width: 100%;
  padding: 14px 18px;
  font-size: 16px;
  border: none;
  border-radius: 16px;
  background-color: #e6e7ee;
  color: #5e6687;
  transition: all 0.3s ease;
  box-shadow: inset 3px 3px 7px #c3c4ca,
              inset -3px -3px 7px #ffffff;
  outline: none;
  border: 1px solid rgba(255, 255, 255, 0.7);
}

.input-container input::placeholder {
  color: #a0aec0;
}

.input-container input:focus {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border: 1px solid #409eff;
}

.input-container input.error {
  border: 1px solid #f56c6c;
  box-shadow: inset 3px 3px 7px rgba(245, 108, 108, 0.2),
              inset -3px -3px 7px #ffffff;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 6px;
  display: block;
}

.select-container {
  position: relative;
}

.select-container select {
  appearance: none;
  width: 100%;
  padding: 14px 18px;
  font-size: 16px;
  border: none;
  border-radius: 16px;
  background-color: #e6e7ee;
  color: #5e6687;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: inset 3px 3px 7px #c3c4ca,
              inset -3px -3px 7px #ffffff;
  outline: none;
  border: 1px solid rgba(255, 255, 255, 0.7);
}

.select-container select:focus {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border: 1px solid #409eff;
}

.select-arrow {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #5e6687;
}

.toggle-switch-container {
  display: flex;
  align-items: center;
}

.toggle-switch {
  position: relative;
  width: 60px;
  height: 32px;
  margin-right: 12px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-switch label {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #e6e7ee;
  border-radius: 34px;
  cursor: pointer;
  box-shadow: inset 3px 3px 5px #c3c4ca,
              inset -3px -3px 5px #ffffff;
  transition: 0.4s;
  border: 1px solid rgba(255, 255, 255, 0.7);
  margin: 0;
}

.toggle-switch label:before {
  position: absolute;
  content: "";
  height: 24px;
  width: 24px;
  left: 4px;
  bottom: 3px;
  background-color: white;
  transition: 0.4s;
  border-radius: 50%;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1),
              -2px -2px 5px rgba(255, 255, 255, 0.5);
}

.toggle-switch input:checked + label {
  background-color: #409eff;
  box-shadow: inset 3px 3px 5px rgba(0, 0, 0, 0.2),
              inset -3px -3px 5px rgba(255, 255, 255, 0.1);
}

.toggle-switch input:checked + label:before {
  transform: translateX(28px);
}

.toggle-label {
  font-size: 16px;
  color: #5e6687;
}

.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-preview {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
  border: 5px solid white;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-preview:hover {
  transform: scale(1.05);
  box-shadow: 8px 8px 16px #c3c4ca,
              -8px -8px 16px #ffffff;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 40px;
  color: #a0aec0;
  background-color: #e6e7ee;
}

.avatar-hint {
  margin-top: 12px;
  font-size: 14px;
  color: #a0aec0;
}

.dialog-footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

.cancel-btn, .submit-btn {
  padding: 12px 24px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;
}

.cancel-btn {
  background-color: #e6e7ee;
  color: #5e6687;
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
}

.cancel-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
}

.cancel-btn:active {
  transform: translateY(1px);
  box-shadow: inset 3px 3px 6px #c3c4ca,
              inset -3px -3px 6px #ffffff;
}

.submit-btn {
  background: linear-gradient(145deg, #3b8fe7, #4da5ff);
  color: white;
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
}

.submit-btn:active {
  transform: translateY(1px);
  box-shadow: inset 3px 3px 6px rgba(0, 0, 0, 0.2),
              inset -3px -3px 6px rgba(255, 255, 255, 0.1);
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .form-group label {
    color: #e2e8f0;
  }
  
  .input-container input,
  .select-container select {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: inset 3px 3px 7px #202632,
                inset -3px -3px 7px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .input-container input::placeholder {
    color: #718096;
  }
  
  .input-container input:focus,
  .select-container select:focus {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
    border: 1px solid #409eff;
  }
  
  .select-arrow {
    color: #e2e8f0;
  }
  
  .toggle-switch label {
    background-color: #2d3748;
    box-shadow: inset 3px 3px 5px #202632,
                inset -3px -3px 5px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .toggle-switch label:before {
    background-color: #e2e8f0;
  }
  
  .toggle-label {
    color: #e2e8f0;
  }
  
  .avatar-preview {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
    border-color: #2d3748;
  }
  
  .avatar-preview:hover {
    box-shadow: 8px 8px 16px #202632,
                -8px -8px 16px #3a485e;
  }
  
  .avatar-placeholder {
    color: #718096;
    background-color: #2d3748;
  }
  
  .cancel-btn {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }
  
  .cancel-btn:hover {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }
  
  .cancel-btn:active {
    box-shadow: inset 3px 3px 6px #202632,
                inset -3px -3px 6px #3a485e;
  }
  
  .submit-btn {
    background: linear-gradient(145deg, #3685d7, #3b97f7);
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }
  
  .submit-btn:hover {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }
}

.media-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 2px 2px 5px #c3c4ca,
              -2px -2px 5px #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.preview-image :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
}

.preview-image:hover {
  transform: translateY(-2px);
  box-shadow: 3px 3px 6px #c3c4ca,
              -3px -3px 6px #ffffff;
}

.preview-image:hover :deep(img) {
  transform: scale(1.1);
}

.image-error {
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #e6e7ee;
  border-radius: 8px;
  color: #909399;
  box-shadow: inset 2px 2px 5px #c3c4ca,
              inset -2px -2px 5px #ffffff;
}

/* Dark mode adaptations */
@media (prefers-color-scheme: dark) {
  .preview-image {
    box-shadow: 2px 2px 5px #202632,
                -2px -2px 5px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .preview-image:hover {
    box-shadow: 3px 3px 6px #202632,
                -3px -3px 6px #3a485e;
  }

  .image-error {
    background-color: #2d3748;
    color: #a0aec0;
    box-shadow: inset 2px 2px 5px #202632,
                inset -2px -2px 5px #3a485e;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  font-size: 14px;
  color: #5e6687;
  font-weight: 500;
}

@media (prefers-color-scheme: dark) {
  .user-name {
    color: #e2e8f0;
  }
}
</style> 