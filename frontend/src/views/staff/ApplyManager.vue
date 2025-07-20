<template>
  <div class="apply-container">
    <div class="apply-header neomorphic-card">
      <h2 class="apply-title">Apply to Become a Restaurant Staff</h2>
      <p class="apply-desc">Choose the restaurant manager you want to apply to and wait for approval after submission</p>
    </div>

    <!-- Search Area -->
    <div class="search-section neomorphic-card">
      <div class="search-form">
        <div class="input-container">
          <input 
            type="text"
            v-model="keyword"
            placeholder="Search manager name or email"
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <div class="input-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
          </div>
        </div>
        
        <button class="search-btn" @click="handleSearch">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          Search
        </button>
      </div>
    </div>

    <!-- Manager List -->
    <div class="manager-section neomorphic-card">
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
      </div>
      
      <div v-else-if="managerList.length === 0" class="empty-container">
        <div class="empty-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
          </svg>
        </div>
        <p class="empty-text">No restaurant managers found</p>
      </div>
      
      <div v-else class="manager-list">
        <div v-for="manager in managerList" :key="manager.managerId" class="manager-card neomorphic-inner-card">
          <div class="manager-avatar">
            <img :src="manager.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'" alt="Manager Avatar" />
          </div>
          <div class="manager-info">
            <h3 class="manager-name">{{ manager.name }}</h3>
            <p class="manager-email">{{ manager.email }}</p>
            <p class="manager-date">Registration Time: {{ formatDate(manager.createdAt) }}</p>
          </div>
          <div class="manager-action">
            <button class="apply-btn" @click="handleApply(manager)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="12" y1="5" x2="12" y2="19"></line>
                <line x1="5" y1="12" x2="19" y2="12"></line>
              </svg>
              Apply
            </button>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="managerList.length > 0" class="pagination">
        <div class="pagination-info">
          Total {{ total }} records, Current Page {{ currentPage }}/{{ Math.ceil(total / pageSize) }}
        </div>
        <!-- Previous Page -->
        <button 
          class="pagination-btn" 
          :disabled="currentPage === 1"
          @click="currentPage > 1 && handleCurrentChange(currentPage - 1)"
        >
          &lt;
        </button>
        
        <!-- Page Numbers -->
        <button 
          v-for="page in displayedPages" 
          :key="page"
          class="pagination-btn" 
          :class="{ 'active': currentPage === page }"
          @click="handleCurrentChange(page)"
        >
          {{ page }}
        </button>
        
        <!-- Next Page -->
        <button 
          class="pagination-btn" 
          :disabled="currentPage === Math.ceil(total / pageSize)"
          @click="currentPage < Math.ceil(total / pageSize) && handleCurrentChange(currentPage + 1)"
        >
          &gt;
        </button>
      </div>
    </div>

    <!-- Confirmation Dialog -->
    <div v-if="confirmDialogVisible" class="confirm-dialog-overlay" @click="confirmDialogVisible = false">
      <div class="confirm-dialog neomorphic-card" @click.stop>
        <div class="dialog-header">
          <h3>Confirm Application</h3>
          <button class="close-btn" @click="confirmDialogVisible = false">×</button>
        </div>
        <div class="dialog-body">
          <p>Are you sure you want to apply to be a staff member under <strong>{{ selectedManager.name }}</strong>?</p>
          <p>After submission, you will need to wait for the manager's approval.</p>
        </div>
        <div class="dialog-footer">
          <button class="cancel-btn" @click="confirmDialogVisible = false">Cancel</button>
          <button class="confirm-btn" @click="submitApply">Confirm</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// Search keyword
const keyword = ref('')

// Table data
const loading = ref(false)
const managerList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Selected manager and confirmation dialog
const selectedManager = ref({})
const confirmDialogVisible = ref(false)

// Pagination computation
const displayedPages = computed(() => {
  const totalPages = Math.ceil(total.value / pageSize.value)
  if (totalPages <= 5) {
    return Array.from({ length: totalPages }, (_, i) => i + 1)
  }
  
  if (currentPage.value <= 3) {
    return [1, 2, 3, 4, 5]
  }
  
  if (currentPage.value >= totalPages - 2) {
    return [totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1, totalPages]
  }
  
  return [currentPage.value - 2, currentPage.value - 1, currentPage.value, currentPage.value + 1, currentPage.value + 2]
})

// Get restaurant manager list
const fetchManagerList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    if (keyword.value) {
      params.keyword = keyword.value
    }
    
    const res = await request({
      url: '/staff-apply/managers',
      method: 'get',
      params
    })
    
    managerList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('Failed to get restaurant manager list:', error)
    ElMessage.error('Failed to get restaurant manager list')
  } finally {
    loading.value = false
  }
}

// Search
const handleSearch = () => {
  currentPage.value = 1
  fetchManagerList()
}

// Pagination
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchManagerList()
}

// Apply
const handleApply = (manager) => {
  selectedManager.value = manager
  confirmDialogVisible.value = true
}

// Submit application
const submitApply = async () => {
  try {
    const res = await request({
      url: '/staff-apply',
      method: 'post',
      params: {
        managerId: selectedManager.value.managerId
      }
    })
    
    ElMessage.success('Application submitted successfully, waiting for manager approval')
    confirmDialogVisible.value = false
  } catch (error) {
    console.error('Failed to submit application:', error)
    ElMessage.error('Failed to submit application')
    confirmDialogVisible.value = false
  }
}

// Format date
const formatDate = (dateStr) => {
  if (!dateStr) return 'Unknown'
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

// Initialize
onMounted(() => {
  fetchManagerList()
})
</script>

<style scoped>
.apply-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #e6e7ee;
  background-image: linear-gradient(135deg, #e6e7ee 0%, #e6e7ee 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.neomorphic-card {
  background: #e6e7ee;
  border-radius: 24px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 8px 8px 16px #c8cdd5,
              -8px -8px 16px #ffffff;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.neomorphic-inner-card {
  background: #e6e7ee;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 6px 6px 12px #c8cdd5,
              -6px -6px 12px #ffffff;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.neomorphic-inner-card:hover {
  transform: translateY(-3px);
  box-shadow: 8px 8px 16px #c8cdd5,
              -8px -8px 16px #ffffff;
}

.apply-header {
  text-align: center;
  margin-bottom: 30px;
}

.apply-title {
  font-size: 28px;
  color: #5e6687;
  margin-bottom: 10px;
  font-weight: 700;
}

.apply-desc {
  color: #8e94aa;
  font-size: 16px;
}

/* Search Area */
.search-section {
  margin-bottom: 30px;
}

.search-form {
  display: flex;
  gap: 16px;
  align-items: center;
}

.input-container {
  position: relative;
  flex: 1;
}

.search-input {
  width: 100%;
  padding: 14px 20px 14px 50px;
  border-radius: 16px;
  border: none;
  background-color: #e6e7ee;
  box-shadow: inset 3px 3px 7px #c3c4ca,
              inset -3px -3px 7px #ffffff;
  color: #5e6687;
  font-size: 16px;
  outline: none;
  transition: all 0.3s ease;
}

.search-input:focus {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border: 1px solid #409eff;
}

.input-icon {
  position: absolute;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #8e94aa;
}

.search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 16px;
  padding: 12px 24px;
  font-weight: 600;
  font-size: 16px;
  background: linear-gradient(135deg, #4d78ef, #5a6bed);
  color: white;
  border: none;
  cursor: pointer;
  box-shadow: 4px 4px 8px #c8cdd5,
              -4px -4px 8px #ffffff;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c8cdd5,
              -6px -6px 12px #ffffff;
}

.search-btn:active {
  transform: translateY(1px);
  box-shadow: inset 3px 3px 6px rgba(0, 0, 0, 0.1),
              inset -3px -3px 6px rgba(255, 255, 255, 0.5);
}

/* Manager List */
.manager-section {
  min-height: 500px;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 3px solid rgba(77, 120, 239, 0.2);
  border-top-color: #4d78ef;
  animation: spin 1s infinite linear;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.empty-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: #8e94aa;
}

.empty-icon {
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
}

.manager-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.manager-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.manager-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 20px;
  box-shadow: 4px 4px 8px #c8cdd5,
              -4px -4px 8px #ffffff;
}

.manager-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.manager-info {
  flex: 1;
}

.manager-name {
  font-size: 18px;
  color: #5e6687;
  margin-bottom: 8px;
  font-weight: 600;
}

.manager-email {
  color: #8e94aa;
  margin-bottom: 8px;
  font-size: 14px;
}

.manager-date {
  color: #8e94aa;
  font-size: 14px;
}

.manager-action {
  margin-left: 20px;
}

.apply-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #4caf50, #66bb6a);
  color: white;
  border: none;
  border-radius: 16px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 4px 4px 8px #c8cdd5,
              -4px -4px 8px #ffffff;
  transition: all 0.3s ease;
}

.apply-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c8cdd5,
              -6px -6px 12px #ffffff;
}

.apply-btn:active {
  transform: translateY(1px);
  box-shadow: inset 3px 3px 6px rgba(0, 0, 0, 0.1),
              inset -3px -3px 6px rgba(255, 255, 255, 0.5);
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 30px;
  gap: 10px;
}

.pagination-info {
  margin-right: 20px;
  color: #5e6687;
  font-size: 14px;
}

.pagination-btn {
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 10px;
  border: none;
  background-color: #e6e7ee;
  color: #5e6687;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 4px 4px 8px #c8cdd5,
              -4px -4px 8px #ffffff;
  transition: all 0.3s ease;
}

.pagination-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 10px #c8cdd5,
              -6px -6px 10px #ffffff;
}

.pagination-btn.active {
  background: linear-gradient(135deg, #4d78ef, #5a6bed);
  color: white;
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.1),
              inset -2px -2px 4px rgba(255, 255, 255, 0.1);
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Confirmation Dialog */
.confirm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(5px);
}

.confirm-dialog {
  width: 400px;
  border-radius: 20px;
  overflow: hidden;
  background-color: #e6e7ee;
  box-shadow: 10px 10px 20px #c8cdd5,
              -10px -10px 20px #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.8);
  padding: 0;
}

.dialog-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.dialog-header h3 {
  font-size: 18px;
  color: #5e6687;
  margin: 0;
}

.close-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  border: none;
  background-color: #e6e7ee;
  color: #5e6687;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 3px 3px 6px #c8cdd5,
              -3px -3px 6px #ffffff;
  transition: all 0.3s ease;
}

.close-btn:hover {
  transform: scale(1.1);
  box-shadow: 4px 4px 8px #c8cdd5,
              -4px -4px 8px #ffffff;
}

.dialog-body {
  padding: 30px 20px;
  color: #5e6687;
}

.dialog-body p {
  margin: 10px 0;
}

.dialog-footer {
  padding: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.cancel-btn, .confirm-btn {
  padding: 12px 24px;
  border-radius: 16px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 4px 4px 8px #c8cdd5,
              -4px -4px 8px #ffffff;
}

.cancel-btn {
  background-color: #e6e7ee;
  color: #5e6687;
}

.confirm-btn {
  background: linear-gradient(135deg, #4caf50, #66bb6a);
  color: white;
}

.cancel-btn:hover, .confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c8cdd5,
              -6px -6px 12px #ffffff;
}

.cancel-btn:active, .confirm-btn:active {
  transform: translateY(1px);
  box-shadow: inset 3px 3px 6px rgba(0, 0, 0, 0.1),
              inset -3px -3px 6px rgba(255, 255, 255, 0.5);
}

/* Responsive Adaptation */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    gap: 15px;
  }

  .search-btn {
    width: 100%;
  }

  .manager-card {
    flex-direction: column;
    text-align: center;
  }

  .manager-avatar {
    margin-right: 0;
    margin-bottom: 15px;
  }

  .manager-action {
    margin-left: 0;
    margin-top: 15px;
  }

  .apply-btn {
    width: 100%;
  }

  .confirm-dialog {
    width: 90%;
    max-width: 400px;
  }

  .pagination {
    flex-wrap: wrap;
    justify-content: center;
  }

  .pagination-info {
    width: 100%;
    text-align: center;
    margin-right: 0;
    margin-bottom: 10px;
  }
}

/* Dark Mode */
@media (prefers-color-scheme: dark) {
  .apply-container {
    background-color: #2d3748;
    background-image: linear-gradient(135deg, #2d3748 0%, #1a202c 100%);
  }

  .neomorphic-card, .neomorphic-inner-card {
    background: #2d3748;
    box-shadow: 8px 8px 16px #1a202c,
                -8px -8px 16px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .neomorphic-inner-card:hover {
    box-shadow: 10px 10px 20px #1a202c,
                -10px -10px 20px #3a485e;
  }

  .apply-title {
    color: #e2e8f0;
  }

  .apply-desc {
    color: #a0aec0;
  }

  .search-input {
    background-color: #2d3748;
    box-shadow: inset 3px 3px 7px #1a202c,
                inset -3px -3px 7px #3a485e;
    color: #e2e8f0;
    border: none;
  }

  .search-input:focus {
    box-shadow: inset 4px 4px 8px #1a202c,
                inset -4px -4px 8px #3a485e;
    border: 1px solid #409eff;
  }

  .input-icon {
    color: #a0aec0;
  }

  .search-btn {
    box-shadow: 4px 4px 8px #1a202c,
                -4px -4px 8px #3a485e;
  }

  .search-btn:hover {
    box-shadow: 6px 6px 12px #1a202c,
                -6px -6px 12px #3a485e;
  }

  .manager-avatar {
    box-shadow: 4px 4px 8px #1a202c,
                -4px -4px 8px #3a485e;
  }

  .manager-name {
    color: #e2e8f0;
  }

  .manager-email, .manager-date {
    color: #a0aec0;
  }

  .apply-btn {
    box-shadow: 4px 4px 8px #1a202c,
                -4px -4px 8px #3a485e;
  }

  .apply-btn:hover {
    box-shadow: 6px 6px 12px #1a202c,
                -6px -6px 12px #3a485e;
  }

  .pagination-info {
    color: #e2e8f0;
  }

  .pagination-btn {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: 4px 4px 8px #1a202c,
                -4px -4px 8px #3a485e;
  }

  .pagination-btn:hover {
    box-shadow: 6px 6px 10px #1a202c,
                -6px -6px 10px #3a485e;
  }

  .confirm-dialog {
    background-color: #2d3748;
    box-shadow: 10px 10px 20px #1a202c,
                -10px -10px 20px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .dialog-header h3 {
    color: #e2e8f0;
  }

  .close-btn {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: 3px 3px 6px #1a202c,
                -3px -3px 6px #3a485e;
  }

  .close-btn:hover {
    box-shadow: 4px 4px 8px #1a202c,
                -4px -4px 8px #3a485e;
  }

  .dialog-body {
    color: #e2e8f0;
  }

  .cancel-btn {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: 4px 4px 8px #1a202c,
                -4px -4px 8px #3a485e;
  }

  .cancel-btn:hover, .confirm-btn:hover {
    box-shadow: 6px 6px 12px #1a202c,
                -6px -6px 12px #3a485e;
  }
}
</style> 