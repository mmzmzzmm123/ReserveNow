<template>
  <div class="user-manage-container">
    <!-- Search and operation area -->
    <div class="search-section neomorphic-card">
      <div class="search-form">
        <el-input
          v-model="searchForm.keyword"
          placeholder="Search username or email"
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <div class="select-container">
          <select 
            v-model="searchForm.role" 
            class="neomorphic-select"
          >
            <option value="">Filter by Role</option>
            <option value="0">Admin</option>
            <option value="1">Restaurant Manager</option>
            <option value="2">User</option>
            <option value="3">Staff</option>
          </select>
          <div class="select-arrow">
            <el-icon><ArrowDown /></el-icon>
          </div>
        </div>
        
        <div class="select-container">
          <select 
            v-model="searchForm.status" 
            class="neomorphic-select"
          >
            <option value="">Filter by Status</option>
            <option value="1">Enabled</option>
            <option value="0">Disabled</option>
          </select>
          <div class="select-arrow">
            <el-icon><ArrowDown /></el-icon>
          </div>
        </div>
        
        <el-button type="primary" @click="handleSearch" class="search-btn">
          <el-icon><Search /></el-icon>Search
        </el-button>
        
        <el-button type="success" @click="handleAdd" class="add-btn">
          <el-icon><Plus /></el-icon>Add User
        </el-button>
      </div>
    </div>

    <!-- User list -->
    <div class="table-section neomorphic-card">
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column label="Avatar" width="100">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="Username" />
        <el-table-column prop="email" label="Email" />
        <el-table-column label="Role" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.roleValue)">{{ getRoleName(row.roleValue) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Status" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.statusValue"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="Created Time" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-button-group>
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

    <!-- Add/Edit user dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? 'Add User' : 'Edit User'"
      width="550px"
      class="neomorphic-dialog"
      destroy-on-close
    >
      <div class="custom-form">
        <div class="form-group">
          <label for="name">Username</label>
          <div class="input-container">
            <input 
              id="name"
              type="text" 
              v-model="userForm.name" 
              placeholder="Enter username"
              :class="{ 'error': nameError }"
            />
            <span class="error-message" v-if="nameError">{{ nameError }}</span>
          </div>
        </div>
        
        <div class="form-group">
          <label for="email">Email Address</label>
          <div class="input-container">
            <input 
              id="email"
              type="email" 
              v-model="userForm.email" 
              placeholder="Enter email address"
              :class="{ 'error': emailError }"
            />
            <span class="error-message" v-if="emailError">{{ emailError }}</span>
          </div>
        </div>
        
        <div class="form-group" v-if="dialogType === 'add'">
          <label for="password">Password</label>
          <div class="input-container">
            <input 
              id="password"
              type="password" 
              v-model="userForm.password" 
              placeholder="Enter password"
              :class="{ 'error': passwordError }"
            />
            <span class="error-message" v-if="passwordError">{{ passwordError }}</span>
          </div>
        </div>
        
        <div class="form-group">
          <label for="role">User Role</label>
          <div class="select-container">
            <select 
              id="role" 
              v-model="userForm.roleValue"
              class="neomorphic-select"
            >
              <option value="0">System Admin</option>
              <option value="1">Restaurant Manager</option>
              <option value="2">Regular User</option>
              <option value="3">Restaurant Staff</option>
            </select>
            <div class="select-arrow">
              <el-icon><ArrowDown /></el-icon>
            </div>
          </div>
        </div>
        
        <div class="form-group">
          <label>User Status</label>
          <div class="toggle-switch-container">
            <div class="toggle-switch">
              <input 
                type="checkbox" 
                id="status-toggle" 
                :checked="userForm.statusValue === 1"
                @change="userForm.statusValue = $event.target.checked ? 1 : 0"
              />
              <label for="status-toggle"></label>
            </div>
            <span class="toggle-label">{{ userForm.statusValue === 1 ? 'Enabled' : 'Disabled' }}</span>
          </div>
        </div>
        
        <div class="form-group">
          <label>User Avatar</label>
          <div class="avatar-upload-container">
            <div class="avatar-preview" @click="triggerFileInput">
              <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar-image" />
              <div v-else class="avatar-placeholder">
                <i class="el-icon-plus"></i>
              </div>
            </div>
            <input 
              type="file" 
              ref="fileInput" 
              style="display: none" 
              accept="image/*"
              @change="handleFileChange"
            />
            <p class="avatar-hint">Click to upload avatar (Supports JPG, PNG, GIF, max 2MB)</p>
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
import { ref, reactive, onMounted } from 'vue'
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

// Search form
const searchForm = reactive({
  keyword: '',
  role: '',
  status: ''
})

// Table data
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Dialog related
const dialogVisible = ref(false)
const dialogType = ref('add')
const userFormRef = ref(null)
const userForm = reactive({
  name: '',
  email: '',
  password: '',
  roleValue: 2,
  statusValue: 1,
  avatar: ''
})

// Form validation rules
const userRules = {
  name: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 2, max: 20, message: 'Length should be between 2 and 20 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter email address', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 6, message: 'Password length cannot be less than 6 characters', trigger: 'blur' }
  ],
  roleValue: [
    { required: true, message: 'Please select a role', trigger: 'change' }
  ]
}

// Get user list
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    const res = await request({
      url: '/admin/users',
      method: 'get',
      params
    })
    userList.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('Failed to fetch user list:', error)
  } finally {
    loading.value = false
  }
}

// Search
const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

// Add user
const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(userForm, {
    name: '',
    email: '',
    password: '',
    roleValue: 2,
    statusValue: 1,
    avatar: ''
  })
  dialogVisible.value = true
}

// Edit user
const handleEdit = (row) => {
  dialogType.value = 'edit'
  Object.assign(userForm, row)
  dialogVisible.value = true
}

// Delete user
const handleDelete = (row) => {
  ElMessageBox.confirm(
    'Are you sure you want to delete this user?',
    'Warning',
    {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/admin/users/${row.userId}`,
        method: 'delete'
      })
      ElMessage.success('Deleted successfully')
      fetchUserList()
    } catch (error) {
      console.error('Failed to delete user:', error)
      ElMessage.error('Failed to delete user')
    }
  }).catch((error) => {
    // Handle when user clicks cancel button
    if (error === 'cancel') {
      return
    }
    console.error('Operation cancelled:', error)
  })
}

// Update user status
const handleStatusChange = async (row) => {
  try {
    await request({
      url: `/admin/users/${row.userId}/status`,
      method: 'put',
      params: { status: row.statusValue }
    })
    ElMessage.success('Status updated successfully')
  } catch (error) {
    console.error('Failed to update status:', error)
    row.statusValue = row.statusValue === 1 ? 0 : 1 // Restore original status
  }
}

// Add file upload related reference
const fileInput = ref(null)

// Add error state variables
const nameError = ref('')
const emailError = ref('')
const passwordError = ref('')

// Trigger file selection
const triggerFileInput = () => {
  fileInput.value.click()
}

// Handle file selection change
const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  // Validate file type and size
  const isValidImageType = /^image\/(jpeg|png|gif|webp|bmp|svg\+xml)$/.test(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isValidImageType) {
    ElMessage.error('Please upload a valid image format (JPG, PNG, GIF, WEBP, BMP, SVG)')
    return
  }
  
  if (!isLt2M) {
    ElMessage.error('Image size cannot exceed 2MB')
    return
  }
  
  // Create a FormData object
  const formData = new FormData()
  formData.append('file', file)
  
  // Upload file
  request({
    url: '/files/images',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }).then(res => {
    userForm.avatar = res.data
    ElMessage.success('Avatar uploaded successfully')
  }).catch(() => {
    ElMessage.error('Failed to upload avatar')
  })
}

// Validate and submit form
const validateAndSubmit = () => {
  // Reset error messages
  nameError.value = ''
  emailError.value = ''
  passwordError.value = ''
  
  let isValid = true
  
  // Validate username
  if (!userForm.name) {
    nameError.value = 'Please enter username'
    isValid = false
  } else if (userForm.name.length < 2 || userForm.name.length > 20) {
    nameError.value = 'Username length should be between 2-20 characters'
    isValid = false
  }
  
  // Validate email
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!userForm.email) {
    emailError.value = 'Please enter email address'
    isValid = false
  } else if (!emailPattern.test(userForm.email)) {
    emailError.value = 'Please enter a valid email address'
    isValid = false
  }
  
  // Validate password (only in add mode)
  if (dialogType.value === 'add') {
    if (!userForm.password) {
      passwordError.value = 'Please enter password'
      isValid = false
    } else if (userForm.password.length < 6) {
      passwordError.value = 'Password length cannot be less than 6 characters'
      isValid = false
    }
  }
  
  // If validation passes, submit form
  if (isValid) {
    if (dialogType.value === 'add') {
      addUser()
    } else {
      updateUser()
    }
  }
}

// Add user
const addUser = async () => {
  try {
    await request({
      url: '/admin/users',
      method: 'post',
      data: userForm
    })
    ElMessage.success('Added successfully')
    dialogVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error('Failed to add user:', error)
  }
}

// Update user
const updateUser = async () => {
  try {
    await request({
      url: `/admin/users/${userForm.userId}`,
      method: 'put',
      data: userForm
    })
    ElMessage.success('Updated successfully')
    dialogVisible.value = false
    fetchUserList()
  } catch (error) {
    console.error('Failed to update user:', error)
  }
}

// Pagination related
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchUserList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUserList()
}

// Get role tag type
const getRoleType = (roleValue) => {
  const typeMap = {
    0: 'danger',
    1: 'warning',
    2: 'success',
    3: 'info'
  }
  return typeMap[roleValue] || 'info'
}

// Get role name
const getRoleName = (roleValue) => {
  const nameMap = {
    0: 'Admin',
    1: 'Restaurant Manager',
    2: 'User',
    3: 'Staff'
  }
  return nameMap[roleValue] || 'Unknown Role'
}

// Initialize
onMounted(() => {
  fetchUserList()
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

/* Dark mode adaptation */
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

/* Responsive adaptation */
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

/* Add dropdown styles */
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

/* Update dropdown styles */
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

/* Add custom form styles */
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

/* Dark mode adaptation */
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
</style> 