<template>
  <div class="reservation-manage-container">
    <!-- Search and operation area -->
    <div class="search-section neomorphic-card">
      <div class="search-form">
        <!-- Reservation status filter dropdown -->
        <div class="custom-select">
          <div 
            class="select-selected" 
            :class="{ 'active': statusSelectVisible }"
            @click="toggleStatusSelect"
          >
            <span v-if="searchForm.status.length === 0">Filter by Status</span>
            <div v-else>
              <span 
                v-for="(status, index) in searchForm.status" 
                :key="status" 
                class="select-tag"
              >
                {{ getStatusText(status) }}
                <span class="select-tag-close" @click.stop="removeStatus(index)">×</span>
              </span>
              <span v-if="searchForm.status.length > 2">...</span>
            </div>
            <div class="select-arrow"></div>
          </div>
          <div class="select-items" :class="{ 'show': statusSelectVisible }">
            <div 
              class="select-item" 
              :class="{ 'selected': searchForm.status.includes(0) }"
              @click="toggleStatus(0)"
            >Cancelled</div>
            <div 
              class="select-item" 
              :class="{ 'selected': searchForm.status.includes(1) }"
              @click="toggleStatus(1)"
            >Pending</div>
            <div 
              class="select-item" 
              :class="{ 'selected': searchForm.status.includes(2) }"
              @click="toggleStatus(2)"
            >Confirmed</div>
            <div 
              class="select-item" 
              :class="{ 'selected': searchForm.status.includes(3) }"
              @click="toggleStatus(3)"
            >Completed</div>
            <div 
              class="select-item" 
              :class="{ 'selected': searchForm.status.includes(4) }"
              @click="toggleStatus(4)"
            >Rejected</div>
          </div>
        </div>
        
        <!-- Restaurant selection dropdown -->
        <div class="custom-select" v-if="userRole === 0">
          <div 
            class="select-selected" 
            :class="{ 'active': restaurantSelectVisible }"
            @click="toggleRestaurantSelect"
          >
            <span v-if="!searchForm.restaurantId">Select Restaurant</span>
            <span v-else>{{ getRestaurantName(searchForm.restaurantId) }}</span>
            <div class="select-arrow"></div>
          </div>
          <div class="select-items" :class="{ 'show': restaurantSelectVisible }">
            <div 
              class="select-item"
              @click="selectRestaurant(''); toggleRestaurantSelect()"
            >All Restaurants</div>
            <div 
              v-for="restaurant in restaurantOptions" 
              :key="restaurant.id"
              class="select-item" 
              :class="{ 'selected': restaurant.id === searchForm.restaurantId }"
              @click="selectRestaurant(restaurant.id); toggleRestaurantSelect()"
            >{{ restaurant.name }}</div>
          </div>
        </div>
        
        <!-- Search button -->
        <button class="search-btn" @click="handleSearch">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          Search
        </button>
        
        <!-- Reset button -->
        <button class="reset-btn" @click="handleReset">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="1 4 1 10 7 10"></polyline>
            <polyline points="23 20 23 14 17 14"></polyline>
            <path d="M20.49 9A9 9 0 0 0 5.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 0 1 3.51 15"></path>
          </svg>
          Reset
        </button>
      </div>
    </div>

    <!-- Reservation list -->
    <div class="table-section neomorphic-card">
      <!-- Loading -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
      </div>
      
      <!-- Custom table -->
      <table v-else class="custom-table">
        <thead>
          <tr>
            <th>Reservation ID</th>
            <th>Restaurant Name</th>
            <th>Table Type</th>
            <th>Reservation Time</th>
            <th>Guests</th>
            <th>Status</th>
            <th>Remarks</th>
            <th>Created Time</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in reservationList" :key="row.id">
            <td>{{ row.id }}</td>
            <td>{{ row.restaurantName }}</td>
            <td>{{ row.tableType }}</td>
            <td>{{ formatReservationTime(row.reservationTime) }}</td>
            <td>{{ row.personCount }} guests</td>
            <td>
              <span class="status-tag" :class="getStatusType(row.statusValue)">
                {{ row.status }}
              </span>
            </td>
            <td :title="row.remarks">{{ row.remarks || 'None' }}</td>
            <td>{{ formatCreatedTime(row.createdAt) }}</td>
            <td>
              <div class="operation-buttons">
                <!-- Confirm button -->
                <button 
                  v-if="row.statusValue === 1" 
                  class="action-btn confirm" 
                  @click="handleConfirm(row)"
                  title="Confirm Reservation"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                </button>
                
                <!-- Reject button -->
                <button 
                  v-if="row.statusValue === 1" 
                  class="action-btn reject" 
                  @click="handleReject(row)"
                  title="Reject Reservation"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
                
                <!-- Complete button -->
                <button 
                  v-if="row.statusValue === 2" 
                  class="action-btn complete" 
                  @click="handleComplete(row)"
                  title="Mark as Completed"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                  </svg>
                </button>
                
                <!-- View button -->
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
          <!-- No data message -->
          <tr v-if="reservationList.length === 0">
            <td colspan="9" style="text-align: center; padding: 30px;">No reservation data</td>
          </tr>
        </tbody>
      </table>

      <!-- Custom pagination -->
      <div class="pagination">
        <div class="pagination-info">
          Total {{ total }} records, Current {{ currentPage }}/{{ Math.ceil(total / pageSize) }} page
        </div>
        <!-- Previous page -->
        <button 
          class="pagination-btn" 
          :disabled="currentPage === 1"
          @click="currentPage > 1 && handleCurrentChange(currentPage - 1)"
        >
          &lt;
        </button>
        
        <!-- Page number buttons -->
        <button 
          v-for="page in displayedPages" 
          :key="page"
          class="pagination-btn" 
          :class="{ 'active': currentPage === page }"
          @click="handleCurrentChange(page)"
        >
          {{ page }}
        </button>
        
        <!-- Next page -->
        <button 
          class="pagination-btn" 
          :disabled="currentPage === Math.ceil(total / pageSize)"
          @click="currentPage < Math.ceil(total / pageSize) && handleCurrentChange(currentPage + 1)"
        >
          &gt;
        </button>
      </div>
    </div>

    <!-- Reservation details dialog -->
    <div v-if="detailDialogVisible" class="detail-dialog-overlay" @click="closeDetailDialog">
      <div class="detail-dialog" @click.stop>
        <div class="detail-dialog-header">
          <h3 class="detail-dialog-title">Reservation Details</h3>
          <button class="detail-dialog-close" @click="detailDialogVisible = false">×</button>
        </div>
        
        <div class="detail-dialog-body">
          <div v-if="selectedReservation" class="reservation-detail">
            <div class="detail-row">
              <div class="detail-label">Reservation ID:</div>
              <div class="detail-value">{{ selectedReservation.id }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Restaurant Name:</div>
              <div class="detail-value">{{ selectedReservation.restaurantName }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Table Type:</div>
              <div class="detail-value">{{ selectedReservation.tableType }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Reservation Time:</div>
              <div class="detail-value">{{ formatReservationTime(selectedReservation.reservationTime) }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Guests:</div>
              <div class="detail-value">{{ selectedReservation.personCount }} guests</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Status:</div>
              <div class="detail-value">
                <span class="status-tag" :class="getStatusType(selectedReservation.statusValue)">
                  {{ selectedReservation.status }}
                </span>
              </div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Remarks:</div>
              <div class="detail-value">{{ selectedReservation.remarks || 'None' }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Created Time:</div>
              <div class="detail-value">{{ formatCreatedTime(selectedReservation.createdAt) }}</div>
            </div>
            <div class="detail-row">
              <div class="detail-label">Updated Time:</div>
              <div class="detail-value">{{ formatCreatedTime(selectedReservation.updatedAt) }}</div>
            </div>
          </div>
        </div>
        
        <div class="detail-dialog-footer">
          <button class="dialog-btn cancel" @click="detailDialogVisible = false">
            Close
          </button>
          <button 
            v-if="selectedReservation && selectedReservation.statusValue === 1"
            class="dialog-btn confirm" 
            @click="confirmReservation"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
            Confirm Reservation
          </button>
          <button 
            v-if="selectedReservation && selectedReservation.statusValue === 1"
            class="dialog-btn reject" 
            @click="rejectReservation"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
            Reject Reservation
          </button>
          <button 
            v-if="selectedReservation && selectedReservation.statusValue === 2"
            class="dialog-btn complete" 
            @click="completeReservation"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
            </svg>
            Mark as Completed
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { formatDate } from '@/utils/format'

// Get user role
const userRole = ref(0)

// Search form
const searchForm = reactive({
  status: [],
  restaurantId: ''
})

// Table data
const loading = ref(false)
const reservationList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Restaurant options list (for admin)
const restaurantOptions = ref([])

// Reservation details dialog
const detailDialogVisible = ref(false)
const selectedReservation = ref(null)

// Dropdown states
const statusSelectVisible = ref(false)
const restaurantSelectVisible = ref(false)

// Pagination display page numbers calculation
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

// Get restaurant list (admin only)
const fetchRestaurantOptions = async () => {
  try {
    const res = await request({
      url: '/admin/restaurants',
      method: 'get',
      params: { page: 1, pageSize: 100 } // Get enough restaurant options
    })
    restaurantOptions.value = res.data.list
  } catch (error) {
    console.error('Failed to fetch restaurant list:', error)
  }
}

// Get reservation list
const fetchReservationList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    // Add status filter parameters
    if (searchForm.status && searchForm.status.length > 0) {
      params.status = searchForm.status.join(',')
    }
    
    // Add restaurant filter parameters (admin only)
    if (userRole.value === 0 && searchForm.restaurantId) {
      params.restaurantId = searchForm.restaurantId
    }
    
    let url = ''
    // Select different API URL based on user role
    if (userRole.value === 0) { // Admin
      url = '/admin/reservations'
    } else if (userRole.value === 1) { // Restaurant Manager
      url = '/admin/reservations/manager'
      
      // If restaurant manager selected specific restaurant for filtering
      if (searchForm.restaurantId) {
        params.restaurantId = searchForm.restaurantId
      }
    } else if (userRole.value === 3) { // Staff
      url = '/admin/staff/reservations'
    }else {
      ElMessage.error('You do not have permission to access this page')
      loading.value = false
      return
    }
    
    console.log('Request reservation list URL:', url, 'Parameters:', params)
    const res = await request({
      url,
      method: 'get',
      params
    })
    
    reservationList.value = res.data.list
    total.value = res.data.total
    
    console.log('Fetched reservation list:', reservationList.value)
  } catch (error) {
    console.error('Failed to fetch reservation list:', error)
  } finally {
    loading.value = false
  }
}

// Search
const handleSearch = () => {
  currentPage.value = 1
  fetchReservationList()
  
  // Close all dropdowns
  statusSelectVisible.value = false
  restaurantSelectVisible.value = false
}

// Reset search conditions
const handleReset = () => {
  searchForm.status = []
  searchForm.restaurantId = ''
  currentPage.value = 1
  fetchReservationList()
  
  // Close all dropdowns
  statusSelectVisible.value = false
  restaurantSelectVisible.value = false
}

// Pagination related
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchReservationList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchReservationList()
}

// Get status tag type
const getStatusType = (statusValue) => {
  const typeMap = {
    0: 'info',    // Cancelled
    1: 'warning', // Pending
    2: 'success', // Confirmed
    3: 'primary', // Completed
    4: 'danger'   // Rejected
  }
  return typeMap[statusValue] || 'info'
}

// Get status text
const getStatusText = (statusValue) => {
  const textMap = {
    0: 'Cancelled',
    1: 'Pending',
    2: 'Confirmed',
    3: 'Completed',
    4: 'Rejected'
  }
  return textMap[statusValue] || 'Unknown Status'
}

// Get restaurant name
const getRestaurantName = (id) => {
  const restaurant = restaurantOptions.value.find(item => item.id === id)
  return restaurant ? restaurant.name : 'Unknown Restaurant'
}

const formatReservationTime = (dateStr) => {
  if (!dateStr) return '';
  
  const [datePart, timePart] = dateStr.split('+')[0].split('.')[0].split('T');
  if (!datePart || !timePart) return dateStr;
  
  const [year, month, day] = datePart.split('-');
  const [hours, minutes] = timePart.split(':');
  
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

const formatCreatedTime = (dateStr) => {
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

// Confirm reservation
const handleConfirm = (row) => {
  ElMessageBox.confirm(
    'Are you sure you want to confirm this reservation?',
    'Confirm Operation',
    {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'success'
    }
  ).then(async () => {
    try {
      await request({
        url: `/admin/reservations/${row.id}/confirm`,
        method: 'put'
      })
      ElMessage.success('Reservation confirmed')
      fetchReservationList()
    } catch (error) {
      console.error('Failed to confirm reservation:', error)
    }
  }).catch(() => {
    // User cancelled operation, no action needed
  })
}

// Reject reservation
const handleReject = (row) => {
  ElMessageBox.prompt(
    'Please enter rejection reason',
    'Reject Reservation',
    {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      inputPlaceholder: 'Please enter rejection reason',
      type: 'warning'
    }
  ).then(async ({ value }) => {
    try {
      await request({
        url: `/admin/reservations/${row.id}/reject`,
        method: 'put',
        data: { reason: value }
      })
      ElMessage.success('Reservation rejected')
      fetchReservationList()
    } catch (error) {
      console.error('Failed to reject reservation:', error)
    }
  }).catch(() => {
    // User cancelled input, no action needed
  })
}

// Mark reservation as completed
const handleComplete = (row) => {
  ElMessageBox.confirm(
    'Are you sure you want to mark this reservation as completed?',
    'Complete Operation',
    {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'info'
    }
  ).then(async () => {
    try {
      await request({
        url: `/admin/reservations/${row.id}/complete`,
        method: 'put'
      })
      ElMessage.success('Reservation marked as completed')
      fetchReservationList()
    } catch (error) {
      console.error('Failed to mark reservation as completed:', error)
    }
  }).catch(() => {
    // User cancelled operation, no action needed
  })
}

// View reservation details
const handleViewDetails = (row) => {
  selectedReservation.value = { ...row }
  detailDialogVisible.value = true
}

// Details dialog actions
const confirmReservation = () => {
  handleConfirm(selectedReservation.value)
  detailDialogVisible.value = false
}

const rejectReservation = () => {
  handleReject(selectedReservation.value)
  detailDialogVisible.value = false
}

const completeReservation = () => {
  handleComplete(selectedReservation.value)
  detailDialogVisible.value = false
}

// Close details dialog
const closeDetailDialog = (e) => {
  // Only close if clicking background
  if (e.target.classList.contains('detail-dialog-overlay')) {
    detailDialogVisible.value = false
  }
}

// Dropdown actions
const toggleStatusSelect = () => {
  statusSelectVisible.value = !statusSelectVisible.value
  restaurantSelectVisible.value = false
}

const toggleRestaurantSelect = () => {
  restaurantSelectVisible.value = !restaurantSelectVisible.value
  statusSelectVisible.value = false
}

const toggleStatus = (status) => {
  const index = searchForm.status.indexOf(status)
  if (index === -1) {
    searchForm.status.push(status)
  } else {
    searchForm.status.splice(index, 1)
  }
}

const removeStatus = (index) => {
  searchForm.status.splice(index, 1)
}

const selectRestaurant = (id) => {
  searchForm.restaurantId = id
}

// Close dropdown click outside listener
const handleOutsideClick = (e) => {
  const statusSelect = document.querySelector('.custom-select')
  const restaurantSelect = document.querySelectorAll('.custom-select')[1]
  
  if (statusSelect && !statusSelect.contains(e.target)) {
    statusSelectVisible.value = false
  }
  
  if (restaurantSelect && !restaurantSelect.contains(e.target)) {
    restaurantSelectVisible.value = false
  }
}

// Initialize
onMounted(() => {
  // Get user info from localStorage
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      userRole.value = Number(userInfo.role || 0)
    }
  } catch (error) {
    console.error('Failed to get user info:', error)
  }
  
  // If admin, get restaurant options
  if (userRole.value === 0) {
    fetchRestaurantOptions()
  }
  
  // Get reservation list
  fetchReservationList()
  
  // Add click outside to close dropdown listener
  document.addEventListener('click', handleOutsideClick)
})

// Component unmount remove event listener
onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<style scoped>
.reservation-manage-container {
  padding: 20px;
  min-height: 100vh;
  background-color: #e6e7ee;
  background-image: linear-gradient(135deg, #e6e7ee 0%, #e6e7ee 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* Neomorphic card */
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

/* Search part */
.search-section {
  margin-bottom: 28px;
}

.search-form {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
}

/* Custom buttons */
.search-btn, .reset-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-radius: 16px;
  padding: 12px 28px;
  font-weight: 600;
  font-size: 15px;
  letter-spacing: 0.5px;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: none;
  cursor: pointer;
  color: #fff;
  box-shadow: 6px 6px 12px #c1c7d0,
              -6px -6px 12px #ffffff;
}

.search-btn {
  background: linear-gradient(135deg, #4d78ef, #5a6bed);
}

.reset-btn {
  background: linear-gradient(135deg, #f0ad4e, #f39c12);
}

.search-btn:hover, .reset-btn:hover {
  transform: translateY(-3px);
  box-shadow: 8px 8px 16px #c1c7d0,
              -8px -8px 16px #ffffff;
}

.search-btn:active, .reset-btn:active {
  transform: translateY(1px);
  box-shadow: inset 4px 4px 8px rgba(0, 0, 0, 0.1),
              inset -4px -4px 8px rgba(255, 255, 255, 0.5);
}

/* Table part styles */
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

/* Pagination styles */
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

/* Operation buttons */
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

.action-btn.complete {
  background: linear-gradient(135deg, #ffb74d, #ff9800);
  color: white;
}

.action-btn.view {
  background: linear-gradient(135deg, #64b5f6, #2196f3);
  color: white;
}

/* Status tag */
.status-tag {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 13px;
  box-shadow: 2px 2px 5px #c1c7d0,
              -2px -2px 5px #ffffff;
}

.status-tag.info {
  background: linear-gradient(135deg, #90caf9, #64b5f6);
  color: white;
}

.status-tag.warning {
  background: linear-gradient(135deg, #ffe082, #ffc107);
  color: white;
}

.status-tag.success {
  background: linear-gradient(135deg, #a5d6a7, #66bb6a);
  color: white;
}

.status-tag.primary {
  background: linear-gradient(135deg, #81d4fa, #29b6f6);
  color: white;
}

.status-tag.danger {
  background: linear-gradient(135deg, #ef9a9a, #e57373);
  color: white;
}

/* Details dialog */
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

.dialog-btn.complete {
  background: linear-gradient(135deg, #ffb74d, #ff9800);
  color: white;
}

.dialog-btn:hover {
  transform: translateY(-3px);
  box-shadow: 6px 6px 12px #c1c7d0,
              -6px -6px 12px #ffffff;
}

/* Reservation details styles */
.reservation-detail {
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

/* Custom form elements */
.custom-select {
  position: relative;
  min-width: 200px;
}

.select-selected {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: #e6e7ee;
  border-radius: 16px;
  cursor: pointer;
  box-shadow: inset 2px 2px 5px #c1c7d0,
              inset -2px -2px 5px #ffffff;
  color: #5e6687;
  border: 1px solid transparent;
  transition: all 0.3s ease;
}

.select-selected.active {
  box-shadow: inset 4px 4px 8px #c1c7d0,
              inset -4px -4px 8px #ffffff;
  border-color: #4d78ef;
}

.select-selected:hover {
  box-shadow: inset 3px 3px 6px #c1c7d0,
              inset -3px -3px 6px #ffffff;
}

.select-arrow {
  border: solid #5e6687;
  border-width: 0 2px 2px 0;
  display: inline-block;
  padding: 3px;
  margin-left: 10px;
  transform: rotate(45deg);
  transition: transform 0.3s;
}

.select-selected.active .select-arrow {
  transform: rotate(-135deg);
}

.select-items {
  position: absolute;
  background-color: #e6e7ee;
  top: calc(100% + 10px);
  left: 0;
  right: 0;
  z-index: 99;
  border-radius: 16px;
  box-shadow: 6px 6px 12px #c1c7d0,
              -6px -6px 12px #ffffff;
  max-height: 250px;
  opacity: 0;
  overflow-x: auto;
  transform: translateY(-10px);
  transition: all 0.3s ease;
}

.select-items.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.select-item {
  padding: 12px 16px;
  cursor: pointer;
  color: #5e6687;
  transition: all 0.2s;
}

.select-item:hover {
  background-color: #edf2fa;
}

.select-item.selected {
  background-color: rgba(77, 120, 239, 0.1);
  color: #4d78ef;
  font-weight: 600;
}

.select-tag {
  display: inline-flex;
  align-items: center;
  background: rgba(77, 120, 239, 0.1);
  color: #4d78ef;
  padding: 4px 8px;
  border-radius: 8px;
  margin-right: 5px;
  font-size: 13px;
}

.select-tag-close {
  margin-left: 5px;
  cursor: pointer;
  font-weight: bold;
}

.custom-datepicker {
  position: relative;
}

.datepicker-input {
  width: 100%;
  padding: 12px 16px;
  background-color: #e6e7ee;
  border-radius: 16px;
  box-shadow: inset 2px 2px 5px #c1c7d0,
              inset -2px -2px 5px #ffffff;
  border: 1px solid transparent;
  color: #5e6687;
  font-size: 14px;
  transition: all 0.3s ease;
}

.datepicker-input:focus {
  outline: none;
  box-shadow: inset 4px 4px 8px #c1c7d0,
              inset -4px -4px 8px #ffffff;
  border-color: #4d78ef;
}

.datepicker-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #5e6687;
}

/* Loading animation */
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

/* Dark mode adaptation */
@media (prefers-color-scheme: dark) {
  .reservation-manage-container {
    background-color: #2d3748;
    background-image: linear-gradient(135deg, #2d3748 0%, #1a202c 100%);
  }

  .neomorphic-card {
    background: #2d3748;
    box-shadow: 8px 8px 16px #202632,
                -8px -8px 16px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .neomorphic-card:hover {
    box-shadow: 10px 10px 20px #1a202c,
                -10px -10px 20px #3a485e;
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

  .pagination-btn:hover {
    box-shadow: 6px 6px 10px #202632,
                -6px -6px 10px #3a485e;
  }

  .action-btn {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }

  .action-btn:hover {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }

  .detail-dialog {
    background-color: #2d3748;
    box-shadow: 10px 10px 20px #202632,
                -10px -10px 20px #3a485e;
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
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }

  .detail-dialog-close:hover {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }

  .detail-dialog-footer {
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }

  .dialog-btn {
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }

  .dialog-btn.cancel {
    background-color: #2d3748;
    color: #e2e8f0;
  }

  .dialog-btn:hover {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }

  .detail-row {
    background: #2a3243;
    box-shadow: inset 2px 2px 5px #202632,
                inset -2px -2px 5px #3a485e;
  }

  .detail-label {
    color: #e2e8f0;
  }

  .detail-value {
    color: #e2e8f0;
  }

  .select-selected {
    background-color: #2d3748;
    box-shadow: inset 2px 2px 5px #202632,
                inset -2px -2px 5px #3a485e;
    color: #e2e8f0;
  }

  .select-selected.active {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
  }

  .select-selected:hover {
    box-shadow: inset 3px 3px 6px #202632,
                inset -3px -3px 6px #3a485e;
  }

  .select-arrow {
    border-color: #e2e8f0;
  }

  .select-items {
    background-color: #2d3748;
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }

  .select-item {
    color: #e2e8f0;
  }

  .select-item:hover {
    background-color: #323c52;
  }

  .datepicker-input {
    background-color: #2d3748;
    box-shadow: inset 2px 2px 5px #202632,
                inset -2px -2px 5px #3a485e;
    color: #e2e8f0;
  }

  .datepicker-input:focus {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
  }

  .datepicker-icon {
    color: #e2e8f0;
  }

  .loading-spinner {
    border-color: rgba(77, 120, 239, 0.2);
    border-top-color: #4d78ef;
  }
}

/* Responsive adaptation */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-btn, .reset-btn {
    width: 100%;
    justify-content: center;
  }
  
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