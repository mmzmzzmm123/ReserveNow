<template>
    <div class="staff-manage-container">
      <!-- Staff List -->
      <div class="table-section neomorphic-card">
        <!-- Loading -->
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
        </div>
        
        <!-- Custom Table -->
        <table v-else class="custom-table">
          <thead>
            <tr>
              <th>Staff ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Status</th>
              <th>Created At</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in staffList" :key="row.id">
              <td>{{ row.id }}</td>
              <td>{{ row.userName }}</td>
              <td>{{ row.userEmail }}</td>
              <td>
                <span class="status-tag" :class="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </span>
              </td>
              <td>{{ formatDateTime(row.createdAt) }}</td>
              <td>
                <div class="operation-buttons">
                  <!-- Approve Button -->
                  <button 
                    v-if="row.status === 0" 
                    class="action-btn confirm" 
                    @click="handleApprove(row)"
                    title="Approve"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                  </button>
                  
                  <!-- Delete Button -->
                  <button 
                    class="action-btn reject" 
                    @click="handleDelete(row)"
                    title="Delete Staff"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"></line>
                      <line x1="6" y1="6" x2="18" y2="18"></line>
                    </svg>
                  </button>
                  
                  <!-- View Button -->
                  <button 
                    class="action-btn view" 
                    @click="handleViewDetails(row)"
                    title="View Details"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
            <!-- No Data Message -->
            <tr v-if="staffList.length === 0">
              <td colspan="6" style="text-align: center; padding: 30px;">No staff data available</td>
            </tr>
          </tbody>
        </table>
  
        <!-- Custom Pagination -->
        <div class="pagination">
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
  
      <!-- Staff Details Dialog -->
      <div v-if="detailDialogVisible" class="detail-dialog-overlay" @click="closeDetailDialog">
        <div class="detail-dialog" @click.stop>
          <div class="detail-dialog-header">
            <h3 class="detail-dialog-title">Staff Details</h3>
            <button class="detail-dialog-close" @click="detailDialogVisible = false">×</button>
          </div>
          
          <div class="detail-dialog-body">
            <div v-if="selectedStaff" class="staff-detail">
              <div class="detail-row">
                <div class="detail-label">Staff ID:</div>
                <div class="detail-value">{{ selectedStaff.id }}</div>
              </div>
              <div class="detail-row">
                <div class="detail-label">Name:</div>
                <div class="detail-value">{{ selectedStaff.userName }}</div>
              </div>
              <div class="detail-row">
                <div class="detail-label">Email:</div>
                <div class="detail-value">{{ selectedStaff.userEmail }}</div>
              </div>
              <div class="detail-row">
                <div class="detail-label">Status:</div>
                <div class="detail-value">
                  <span class="status-tag" :class="getStatusType(selectedStaff.status)">
                    {{ getStatusText(selectedStaff.status) }}
                  </span>
                </div>
              </div>
              <div class="detail-row">
                <div class="detail-label">Created At:</div>
                <div class="detail-value">{{ formatDateTime(selectedStaff.createdAt) }}</div>
              </div>
              <div class="detail-row">
                <div class="detail-label">Updated At:</div>
                <div class="detail-value">{{ formatDateTime(selectedStaff.updatedAt) }}</div>
              </div>
            </div>
          </div>
          
          <div class="detail-dialog-footer">
            <button class="dialog-btn cancel" @click="detailDialogVisible = false">
              Close
            </button>
            <button 
              v-if="selectedStaff && selectedStaff.status === 0"
              class="dialog-btn confirm" 
              @click="approveStaff"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
              Approve
            </button>
            <button 
              class="dialog-btn reject" 
              @click="deleteStaff"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
              Delete Staff
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import request from '@/utils/request'
  
  // Table data
  const loading = ref(false)
  const staffList = ref([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)
  
  // Detail dialog
  const detailDialogVisible = ref(false)
  const selectedStaff = ref(null)
  
  // Pagination display calculation
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
  
  // Get staff list
  const fetchStaffList = async () => {
    loading.value = true
    try {
      const res = await request({
        url: '/admin/staff',
        method: 'get',
        params: {
          page: currentPage.value,
          pageSize: pageSize.value
        }
      })
      
      staffList.value = res.data.list
      total.value = res.data.total
    } catch (error) {
      console.error('Failed to get staff list:', error)
    } finally {
      loading.value = false
    }
  }
  
  // Get status tag type
  const getStatusType = (status) => {
    const typeMap = {
      0: 'warning', // Pending
      1: 'success'  // Approved
    }
    return typeMap[status] || 'info'
  }
  
  // Get status text
  const getStatusText = (status) => {
    const textMap = {
      0: 'Pending',
      1: 'Approved'
    }
    return textMap[status] || 'Unknown'
  }
  
  // Format date time
  const formatDateTime = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return dateStr;
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  };
  
  // Approve staff
  const handleApprove = (row) => {
    ElMessageBox.confirm(
      'Are you sure you want to approve this staff member?',
      'Approval Confirmation',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'success'
      }
    ).then(async () => {
      try {
        await request({
          url: `/admin/staff/${row.id}/status`,
          method: 'put',
          params: { status: 1 }
        })
        ElMessage.success('Staff approved successfully')
        fetchStaffList()
      } catch (error) {
        console.error('Approval failed:', error)
      }
    })
  }
  
  // Delete staff
  const handleDelete = (row) => {
    ElMessageBox.confirm(
      'Are you sure you want to delete this staff member? This action cannot be undone!',
      'Delete Confirmation',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    ).then(async () => {
      try {
        await request({
          url: `/admin/staff/${row.id}`,
          method: 'delete'
        })
        ElMessage.success('Staff deleted successfully')
        fetchStaffList()
      } catch (error) {
        console.error('Delete failed:', error)
      }
    })
  }
  
  // View details
  const handleViewDetails = (row) => {
    selectedStaff.value = { ...row }
    detailDialogVisible.value = true
  }
  
  // Actions in detail dialog
  const approveStaff = () => {
    handleApprove(selectedStaff.value)
    detailDialogVisible.value = false
  }
  
  const deleteStaff = () => {
    handleDelete(selectedStaff.value)
    detailDialogVisible.value = false
  }
  
  // Pagination related
  const handleCurrentChange = (val) => {
    currentPage.value = val
    fetchStaffList()
  }
  
  // Close detail dialog
  const closeDetailDialog = (e) => {
    if (e.target.classList.contains('detail-dialog-overlay')) {
      detailDialogVisible.value = false
    }
  }
  
  // Initialize
  onMounted(() => {
    fetchStaffList()
  })
  </script>
  
  <style scoped>
  .staff-manage-container {
    padding: 20px;
    min-height: 100vh;
    background-color: #e6e7ee;
    background-image: linear-gradient(135deg, #e6e7ee 0%, #e6e7ee 100%);
    font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  }
  
  /* Neomorphic Card */
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
  
  .neomorphic-card:hover {
    transform: translateY(-3px);
  }
  
  /* Table Section Styles */
  .table-section {
    min-height: 500px;
    overflow: hidden;
  }
  
  .custom-table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0;
    margin-bottom: 20px;
  }
  
  .custom-table thead th {
    background-color: #e6e7ee;
    color: #5e6687;
    font-weight: 600;
    padding: 16px;
    text-align: center;
    border-bottom: 2px solid rgba(195, 196, 202, 0.3);
    letter-spacing: 0.5px;
    font-size: 15px;
  }
  
  .custom-table tbody tr {
    transition: all 0.3s ease;
  }
  
  .custom-table tbody tr:nth-child(odd) {
    background-color: #e6e7ee;
  }
  
  .custom-table tbody tr:nth-child(even) {
    background-color: #edf2fa;
  }
  
  .custom-table tbody tr:hover {
    background-color: #f0f4fa;
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
  }
  
  .custom-table tbody td {
    padding: 14px;
    border-bottom: 1px solid rgba(195, 196, 202, 0.2);
    color: #5e6687;
    text-align: center;
    font-size: 14px;
  }
  
  /* Pagination Styles */
  .pagination {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    margin-top: 24px;
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
    background-color: #e6e7ee;
    color: #5e6687;
    font-weight: 600;
    box-shadow: 4px 4px 8px #c1c7d0,
                -4px -4px 8px #ffffff;
    border: none;
    cursor: pointer;
    transition: all 0.3s ease;
  }
  
  .pagination-btn:hover {
    transform: translateY(-2px);
    box-shadow: 6px 6px 10px #c1c7d0,
                -6px -6px 10px #ffffff;
  }
  
  .pagination-btn.active {
    background: linear-gradient(135deg, #4d78ef, #5a6bed);
    color: white;
    box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.1),
                inset -2px -2px 4px rgba(255, 255, 255, 0.1);
  }
  
  .pagination-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
  
  /* Operation Buttons */
  .operation-buttons {
    display: flex;
    justify-content: center;
    gap: 12px;
  }
  
  .action-btn {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #e6e7ee;
    color: #5e6687;
    border: none;
    cursor: pointer;
    box-shadow: 4px 4px 8px #c1c7d0,
                -4px -4px 8px #ffffff;
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  }
  
  .action-btn:hover {
    transform: translateY(-3px);
    box-shadow: 6px 6px 12px #c1c7d0,
                -6px -6px 12px #ffffff;
  }
  
  .action-btn.confirm {
    background: linear-gradient(135deg, #66bb6a, #4caf50);
    color: white;
  }
  
  .action-btn.reject {
    background: linear-gradient(135deg, #ef5350, #f44336);
    color: white;
  }
  
  .action-btn.view {
    background: linear-gradient(135deg, #64b5f6, #2196f3);
    color: white;
  }
  
  /* Status Tags */
  .status-tag {
    display: inline-block;
    padding: 6px 12px;
    border-radius: 12px;
    font-weight: 600;
    font-size: 13px;
    box-shadow: 2px 2px 5px #c1c7d0,
                -2px -2px 5px #ffffff;
  }
  
  .status-tag.warning {
    background: linear-gradient(135deg, #ffe082, #ffc107);
    color: white;
  }
  
  .status-tag.success {
    background: linear-gradient(135deg, #a5d6a7, #66bb6a);
    color: white;
  }
  
  /* Detail Dialog */
  .detail-dialog-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 999;
    backdrop-filter: blur(6px);
  }
  
  .detail-dialog {
    background-color: #e6e7ee;
    border-radius: 24px;
    padding: 0;
    width: 550px;
    transform: translateY(0);
    transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    max-height: 90vh;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  
  .detail-dialog-header {
    background-color: #e6e7ee;
    padding: 20px 24px;
    border-bottom: 1px solid rgba(195, 196, 202, 0.2);
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .detail-dialog-title {
    font-size: 18px;
    font-weight: 700;
    color: #5e6687;
    margin: 0;
  }
  
  .detail-dialog-close {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #e6e7ee;
    color: #5e6687;
    border: none;
    cursor: pointer;
    box-shadow: 4px 4px 8px #c1c7d0,
                -4px -4px 8px #ffffff;
    transition: all 0.3s ease;
    font-size: 18px;
  }
  
  .detail-dialog-close:hover {
    transform: rotate(90deg);
    box-shadow: 6px 6px 12px #c1c7d0,
                -6px -6px 12px #ffffff;
  }
  
  .detail-dialog-body {
    padding: 24px;
    overflow-y: auto;
    flex: 1;
  }
  
  .detail-dialog-footer {
    padding: 16px 24px 24px;
    display: flex;
    justify-content: flex-end;
    gap: 16px;
    border-top: 1px solid rgba(195, 196, 202, 0.2);
  }
  
  .dialog-btn {
    padding: 12px 24px;
    border-radius: 16px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    border: none;
    box-shadow: 4px 4px 8px #c1c7d0,
                -4px -4px 8px #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }
  
  .dialog-btn.cancel {
    background-color: #e6e7ee;
    color: #5e6687;
  }
  
  .dialog-btn.confirm {
    background: linear-gradient(135deg, #66bb6a, #4caf50);
    color: white;
  }
  
  .dialog-btn.reject {
    background: linear-gradient(135deg, #ef5350, #f44336);
    color: white;
  }
  
  .dialog-btn:hover {
    transform: translateY(-3px);
    box-shadow: 6px 6px 12px #c1c7d0,
                -6px -6px 12px #ffffff;
  }
  
  /* Staff Detail Styles */
  .staff-detail {
    padding: 10px;
  }
  
  .detail-row {
    display: flex;
    margin-bottom: 15px;
    align-items: center;
    background: #edf2fa;
    padding: 12px 16px;
    border-radius: 12px;
    box-shadow: inset 2px 2px 5px #c1c7d0,
                inset -2px -2px 5px #ffffff;
  }
  
  .detail-label {
    width: 110px;
    font-weight: 600;
    color: #5e6687;
  }
  
  .detail-value {
    flex: 1;
    color: #5e6687;
  }
  
  /* Loading Animation */
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
  
  /* Dark Mode Adaptation */
  @media (prefers-color-scheme: dark) {
    .staff-manage-container {
      background-color: #2d3748;
      background-image: linear-gradient(135deg, #2d3748 0%, #1a202c 100%);
    }
  
    .neomorphic-card {
      background: #2d3748;
      box-shadow: 8px 8px 16px #202632,
                  -8px -8px 16px #3a485e;
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
  
    .custom-table thead th {
      background-color: #2d3748;
      color: #e2e8f0;
      border-bottom: 2px solid rgba(255, 255, 255, 0.1);
    }
  
    .custom-table tbody tr:nth-child(odd) {
      background-color: #2d3748;
    }
  
    .custom-table tbody tr:nth-child(even) {
      background-color: #2a3243;
    }
  
    .custom-table tbody tr:hover {
      background-color: #323c52;
    }
  
    .custom-table tbody td {
      color: #e2e8f0;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }
  
    .pagination-info {
      color: #e2e8f0;
    }
  
    .pagination-btn {
      background-color: #2d3748;
      color: #e2e8f0;
      box-shadow: 4px 4px 8px #202632,
                  -4px -4px 8px #3a485e;
    }
  
    .action-btn {
      background-color: #2d3748;
      color: #e2e8f0;
      box-shadow: 4px 4px 8px #202632,
                  -4px -4px 8px #3a485e;
    }
  
    .detail-dialog {
      background-color: #2d3748;
    }
  
    .detail-dialog-header {
      background-color: #2d3748;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }
  
    .detail-dialog-title {
      color: #e2e8f0;
    }
  
    .detail-dialog-close {
      background-color: #2d3748;
      color: #e2e8f0;
    }
  
    .dialog-btn.cancel {
      background-color: #2d3748;
      color: #e2e8f0;
    }
  
    .detail-row {
      background: #2a3243;
      box-shadow: inset 2px 2px 5px #202632,
                  inset -2px -2px 5px #3a485e;
    }
  
    .detail-label,
    .detail-value {
      color: #e2e8f0;
    }
  }
  
  /* 响应式适配 */
  @media (max-width: 768px) {
    .neomorphic-card {
      padding: 16px;
      border-radius: 16px;
    }
    
    .custom-table {
      display: block;
      overflow-x: auto;
      white-space: nowrap;
    }
  
    .custom-table thead th,
    .custom-table tbody td {
      padding: 10px;
    }
  
    .detail-dialog {
      width: 90%;
      max-width: 500px;
    }
    
    .action-btn {
      width: 32px;
      height: 32px;
    }
    
    .operation-buttons {
      gap: 8px;
    }
  }
  </style>