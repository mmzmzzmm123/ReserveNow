<template>
  <div class="table-manage-container">
    <!-- Search and operation area -->
    <div class="search-section neomorphic-card">
      <div class="search-form">
        <el-input
          v-model="searchForm.keyword"
          placeholder="Search table type"
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <div class="select-container" v-if="userRole === 0">
          <select 
            v-model="searchForm.restaurantId" 
            class="restaurant-select"
          >
            <option value="">All Restaurants</option>
            <option 
              v-for="restaurant in restaurantOptions" 
              :key="restaurant.id" 
              :value="restaurant.id"
            >
              {{ restaurant.name }}
            </option>
          </select>
          <div class="select-arrow">
            <el-icon><ArrowDown /></el-icon>
          </div>
        </div>
        
        <el-button type="primary" @click="handleSearch" class="search-btn">
          <el-icon><Search /></el-icon>Search
        </el-button>
        
        <el-button type="success" @click="handleAdd" class="add-btn">
          <el-icon><Plus /></el-icon>Add Table
        </el-button>
      </div>
    </div>

    <!-- Table list -->
    <div class="table-section neomorphic-card">
      <el-table
        v-loading="loading"
        :data="tableList"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column prop="id" label="Table ID" width="100" align="center" />
        <el-table-column label="Restaurant" width="150" align="center">
          <template #default="{ row }">
            <span>{{ row.restaurantName || getRestaurantName(row.restaurantId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="Table Type" width="150" align="center" />
        <el-table-column label="Capacity" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.capacity }} people</span>
          </template>
        </el-table-column>
        <el-table-column label="Created Time" min-width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <div class="operation-buttons">
              <el-button type="primary" size="small" circle @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" size="small" circle @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
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

    <!-- Add/Edit table dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? 'Add Table' : 'Edit Table'"
      width="550px"
      class="neomorphic-dialog"
      destroy-on-close
    >
      <div class="custom-form">
        <div class="form-group" v-if="(userRole === 0 || userRole === 1) && dialogType === 'add'">
          <label for="restaurantId">Restaurant</label>
          <div class="select-container">
            <select 
              id="restaurantId" 
              v-model="tableForm.restaurantId"
              class="neomorphic-select"
            >
              <option value="">Select Restaurant</option>
              <option 
                v-for="restaurant in userRole === 0 ? restaurantOptions : managerRestaurants" 
                :key="restaurant.id" 
                :value="restaurant.id"
              >
                {{ restaurant.name }}
              </option>
            </select>
            <div class="select-arrow">
              <el-icon><ArrowDown /></el-icon>
            </div>
          </div>
          <span class="error-message" v-if="restaurantIdError">{{ restaurantIdError }}</span>
        </div>
        
        <div class="form-group">
          <label for="type">Table Type</label>
          <div class="select-container">
            <select 
              id="type" 
              v-model="tableForm.type"
              class="neomorphic-select"
            >
              <option value="Standard">Standard Table</option>
              <option value="Large">Large Table</option>
              <option value="VIP">VIP Room</option>
            </select>
            <div class="select-arrow">
              <el-icon><ArrowDown /></el-icon>
            </div>
          </div>
          <span class="error-message" v-if="typeError">{{ typeError }}</span>
        </div>
        
        <div class="form-group">
          <label for="capacity">Capacity</label>
          <div class="input-container">
            <input 
              id="capacity"
              type="number" 
              v-model="tableForm.capacity" 
              placeholder="Enter table capacity (number of people)"
              :class="{ 'error': capacityError }"
              min="1"
              max="50"
            />
            <span class="error-message" v-if="capacityError">{{ capacityError }}</span>
          </div>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="cancel-btn" @click="dialogVisible = false">Cancel</button>
        <button class="submit-btn" @click="validateAndSubmit">{{ dialogType === 'add' ? 'Add' : 'Save' }}</button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Edit,
  Delete,
  ArrowDown
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatDate } from '@/utils/format'

// Get user role
const userRole = ref(0)

// Search form
const searchForm = reactive({
  keyword: '',
  restaurantId: ''
})

// Table data
const loading = ref(false)
const tableList = ref([])
const originalTableList = ref([]) // Save original data for frontend filtering
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Restaurant options list (for admin)
const restaurantOptions = ref([])

// Restaurant list managed by restaurant manager
const managerRestaurants = ref([])

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
    console.error('Failed to get restaurant list:', error)
  }
}

// Get restaurant list managed by restaurant manager
const fetchManagerRestaurants = async () => {
  try {
    const res = await request({
      url: '/admin/restaurants/my-restaurants',
      method: 'get',
      params: { page: 1, pageSize: 100 } // Get enough restaurant options
    })
    managerRestaurants.value = res.data.list
    
    // If there are restaurants, select the first one by default
    if (managerRestaurants.value.length > 0) {
      tableForm.restaurantId = managerRestaurants.value[0].id
    }
  } catch (error) {
    console.error('Failed to get manager restaurant list:', error)
  }
}

// Dialog related
const dialogVisible = ref(false)
const dialogType = ref('add')
const tableForm = reactive({
  restaurantId: '',
  type: 'Standard',
  capacity: 4
})

// Form validation errors
const restaurantIdError = ref('')
const typeError = ref('')
const capacityError = ref('')

// Get table list
const fetchTableList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
      // Remove keyword parameter, handle filtering on frontend
    }
    
    let url = ''
    // Determine API URL based on user role and whether a specific restaurant is selected
    if (userRole.value === 0) { // Admin
      url = '/admin/tables'
      // Only add restaurantId to params if it's not empty
      if (searchForm.restaurantId) {
        params.restaurantId = searchForm.restaurantId
      }
    } else if (userRole.value === 1) { // Restaurant Manager
      // Use new API endpoint to get all tables under manager, this endpoint directly returns restaurantName
      url = '/admin/tables/my-tables'
    } else {
      ElMessage.error('You do not have permission to access this page')
      loading.value = false
      return
    }
    
    console.log('Requesting table list URL:', url)
    const res = await request({
      url,
      method: 'get',
      params
    })
    
    // Save original data
    originalTableList.value = res.data.list
    
    // If there's a search keyword, apply local filtering
    if (searchForm.keyword) {
      applySearchFilter()
    } else {
      tableList.value = originalTableList.value
    }
    
    total.value = res.data.total
    
  } catch (error) {
    console.error('Failed to fetch table list:', error)
  } finally {
    loading.value = false
  }
}

// Local search filtering
const applySearchFilter = () => {
  if (!searchForm.keyword) {
    // If no search keyword, show all data
    tableList.value = originalTableList.value
    total.value = originalTableList.value.length
    return
  }
  
  // Execute local filtering, match records where table type includes search keyword
  const keyword = searchForm.keyword.toLowerCase()
  tableList.value = originalTableList.value.filter(item => 
    item.type && item.type.toLowerCase().includes(keyword)
  )
  
  // Update total for pagination component to display correctly
  total.value = tableList.value.length
}

// Search
const handleSearch = () => {
  // If filtering by restaurant, need to fetch data from server again
  if (userRole.value === 0) {
    currentPage.value = 1
    // If All Restaurants is selected, remove restaurantId from params
    if (searchForm.restaurantId === '') {
      fetchTableList()
    } else {
      fetchTableList()
    }
  } else if (originalTableList.value.length > 0) {
    // If we already have data and only searching by keyword, execute local filtering
    applySearchFilter()
  } else {
    // If no data, fetch from server
    currentPage.value = 1
    fetchTableList()
  }
}

// Add table
const handleAdd = () => {
  dialogType.value = 'add'
  
  // Reset form data
  tableForm.type = 'Standard'
  tableForm.capacity = 4
  tableForm.restaurantId = ''

  // Set default restaurant ID based on user role
  if (userRole.value === 0) { // Admin
    if (restaurantOptions.value.length > 0) {
      tableForm.restaurantId = restaurantOptions.value[0].id
    }
  } else if (userRole.value === 1) { // Restaurant Manager
    if (managerRestaurants.value.length > 0) {
      tableForm.restaurantId = managerRestaurants.value[0].id
    }
  }
  
  resetFormErrors()
  dialogVisible.value = true
}

// Edit table
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  // Get details
  try {
    const res = await request({
      url: `/admin/tables/${row.id}`,
      method: 'get'
    })
    
    Object.assign(tableForm, res.data)
    resetFormErrors()
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('Failed to get table details')
  }
}

// Delete table
const handleDelete = (row) => {
  ElMessageBox.confirm(
    'Are you sure you want to delete this table?',
    'Warning',
    {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/admin/tables/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('Deleted successfully')
      fetchTableList()
    } catch (error) {
      console.error('Failed to delete table:', error)
    }
  })
}

// Reset form errors
const resetFormErrors = () => {
  restaurantIdError.value = ''
  typeError.value = ''
  capacityError.value = ''
}

// Validate and submit form
const validateAndSubmit = () => {
  resetFormErrors()
  
  let isValid = true
  
  // Validate restaurant ID (for admin and restaurant manager)
  if ((userRole.value === 0 || userRole.value === 1) && dialogType.value === 'add' && !tableForm.restaurantId) {
    restaurantIdError.value = 'Please select a restaurant'
    isValid = false
  }
  
  // Validate table type
  if (!tableForm.type) {
    typeError.value = 'Please select a table type'
    isValid = false
  }
  
  // Validate table capacity
  if (!tableForm.capacity) {
    capacityError.value = 'Please enter table capacity'
    isValid = false
  } else if (tableForm.capacity < 1 || tableForm.capacity > 50) {
    capacityError.value = 'Table capacity should be between 1-50 people'
    isValid = false
  }
  
  // If validation passes, submit form
  if (isValid) {
    if (dialogType.value === 'add') {
      addTable()
    } else {
      updateTable()
    }
  }
}

// Add table
const addTable = async () => {
  try {
    tableForm.id = ''
    await request({
      url: '/admin/tables',
      method: 'post',
      data: tableForm
    })
    ElMessage.success('Added successfully')
    dialogVisible.value = false
    fetchTableList()
  } catch (error) {
    console.error('Failed to add table:', error)
  }
}

// Update table
const updateTable = async () => {
  try {
    await request({
      url: `/admin/tables/${tableForm.id}`,
      method: 'put',
      data: tableForm
    })
    ElMessage.success('Updated successfully')
    dialogVisible.value = false
    fetchTableList()
  } catch (error) {
    console.error('Failed to update table:', error)
  }
}

// Pagination related
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTableList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTableList()
}

// Date time formatting
const formatDateTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return dateStr;
  
  // Format as YYYY-MM-DD HH:MM:SS
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// Get restaurant name
const getRestaurantName = (restaurantId) => {
  if (!restaurantId) return '';
  const restaurant = restaurantOptions.value.find(item => item.id === restaurantId);
  return restaurant ? restaurant.name : `Restaurant #${restaurantId}`;
}

// Initialize
onMounted(() => {
  // Get user information from localStorage
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      userRole.value = Number(userInfo.role || 0)
    }
  } catch (error) {
    console.error('Failed to get user information:', error)
  }
  
  if (userRole.value === 0) {
    fetchRestaurantOptions()
  } else if (userRole.value === 1) { 
    fetchManagerRestaurants()
  }

  fetchTableList()
})
</script>

<style scoped>
.table-manage-container {
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

.search-input {
  width: 320px;
  border-radius: 16px;
}

.search-input :deep(.el-input__wrapper) {
  background-color: #e6e7ee;
  border-radius: 16px;
  box-shadow: inset 3px 3px 7px #c3c4ca,
              inset -3px -3px 7px #ffffff;
  padding: 12px 15px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border-color: #409eff;
}

.search-input :deep(.el-input__prefix) {
  margin-right: 10px;
}

.search-input :deep(.el-input__inner) {
  color: #5e6687;
  font-weight: 500;
}

.search-btn, .add-btn {
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

.search-btn:hover, .add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
}

.search-btn:active, .add-btn:active {
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

/* 角色标签样式 */
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

/* 开关样式 */
.table-section :deep(.el-switch__core) {
  border-radius: 12px;
  box-shadow: 2px 2px 5px #c3c4ca,
              -2px -2px 5px #ffffff;
}

/* 按钮组样式 */
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
  .table-manage-container {
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

  .search-btn, .add-btn {
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }

  .search-btn:hover, .add-btn:hover {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }

  .search-btn:active, .add-btn:active {
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

  .search-input {
    width: 100%;
  }

  .search-btn, .add-btn {
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

/* 添加下拉选择框的样式 */
.el-select {
  width: 180px;
}

.el-select :deep(.el-input__wrapper) {
  background-color: #e6e7ee;
  border-radius: 16px;
  box-shadow: inset 3px 3px 7px #c3c4ca,
              inset -3px -3px 7px #ffffff;
  padding: 12px 15px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
}

.el-select :deep(.el-input__wrapper.is-focus) {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border-color: #409eff;
}

.el-select :deep(.el-input__inner) {
  color: #5e6687;
  font-weight: 500;
}

.el-select :deep(.el-select__tags) {
  margin-top: 2px;
}

/* 更新下拉弹出样式 */
:deep(.el-popper.is-light) {
  background-color: #e6e7ee;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  box-shadow: 5px 5px 10px #c3c4ca,
              -5px -5px 10px #ffffff;
}

:deep(.el-select-dropdown__item) {
  color: #5e6687;
  transition: all 0.3s ease;
}

:deep(.el-select-dropdown__item.hover),
:deep(.el-select-dropdown__item:hover) {
  background-color: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

:deep(.el-select-dropdown__item.selected) {
  color: #409eff;
  font-weight: 600;
}

@media (prefers-color-scheme: dark) {
  .search-input :deep(.el-input__wrapper) {
    background-color: #2d3748;
    box-shadow: inset 3px 3px 7px #202632,
                inset -3px -3px 7px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .search-input :deep(.el-input__wrapper.is-focus) {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
    border-color: #409eff;
  }
  
  .search-input :deep(.el-input__inner) {
    color: #e2e8f0;
  }
  
  .el-select :deep(.el-input__wrapper) {
    background-color: #2d3748;
    box-shadow: inset 3px 3px 7px #202632,
                inset -3px -3px 7px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .el-select :deep(.el-input__wrapper.is-focus) {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
    border-color: #409eff;
  }
  
  .el-select :deep(.el-input__inner) {
    color: #e2e8f0;
  }
  
  :deep(.el-popper.is-light) {
    background-color: #2d3748;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 5px 5px 10px #202632,
                -5px -5px 10px #3a485e;
  }
  
  :deep(.el-select-dropdown__item) {
    color: #e2e8f0;
  }
  
  :deep(.el-select-dropdown__item.hover),
  :deep(.el-select-dropdown__item:hover) {
    background-color: rgba(64, 158, 255, 0.2);
  }
  
  .neomorphic-dialog :deep(.el-input__inner) {
    color: #e2e8f0;
  }
  
  .avatar-uploader-icon {
    color: #63b3ed;
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
  width: 180px;
}

.restaurant-select {
  width: 100%;
  padding: 12px 15px;
  font-size: 14px;
  color: #5e6687;
  background-color: #e6e7ee;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  appearance: none;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: inset 3px 3px 7px #c3c4ca,
              inset -3px -3px 7px #ffffff;
  outline: none;
  padding-right: 40px;
}

.restaurant-select:focus {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border-color: #409eff;
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
}

/* 操作按钮布局 */
.operation-buttons {
  display: flex;
  justify-content: center;
  gap: 8px;
}

/* 按钮组样式修改 */
.table-section :deep(.el-button.is-circle) {
  box-shadow: 3px 3px 6px #c3c4ca,
              -3px -3px 6px #ffffff;
  margin: 0 4px;
  transition: all 0.3s ease;
}

.table-section :deep(.el-button.is-circle:hover) {
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
}

@media (prefers-color-scheme: dark) {
  .table-section :deep(.el-button.is-circle) {
    box-shadow: 3px 3px 6px #202632,
                -3px -3px 6px #3a485e;
  }
  
  .table-section :deep(.el-button.is-circle:hover) {
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }
}

.neomorphic-select {
  width: 100%;
  padding: 12px 15px;
  font-size: 14px;
  color: #5e6687;
  background-color: #e6e7ee;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  appearance: none;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: inset 3px 3px 7px #c3c4ca,
              inset -3px -3px 7px #ffffff;
  outline: none;
  padding-right: 40px;
  -webkit-appearance: none;
  -moz-appearance: none;
}

.neomorphic-select:focus {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border-color: #409eff;
}

.neomorphic-select option {
  background-color: #e6e7ee;
  color: #5e6687;
  padding: 12px;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .neomorphic-select {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: inset 3px 3px 7px #202632,
                inset -3px -3px 7px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .neomorphic-select:focus {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
    border-color: #409eff;
  }

  .neomorphic-select option {
    background-color: #2d3748;
    color: #e2e8f0;
  }
}
</style> 