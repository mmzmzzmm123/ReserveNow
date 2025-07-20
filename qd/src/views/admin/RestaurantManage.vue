<template>
  <div class="restaurant-manage-container">
    <!-- Search and operation area -->
    <div class="search-section neomorphic-card">
      <div class="search-form">
        <el-input
          v-model="searchForm.keyword"
          placeholder="Search restaurant name or address"
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <div class="select-container">
          <select v-model="searchForm.status" class="status-select">
            <option value="">All statuses</option>
            <option :value="0">Pending Review</option>
            <option :value="1">Approved</option>
            <option :value="2">Closed</option>
            <option :value="3">Operating</option>
          </select>
          <div class="select-arrow">
            <el-icon><ArrowDown /></el-icon>
          </div>
        </div>
        
        <el-button type="primary" @click="handleSearch" class="search-btn">
          <el-icon><Search /></el-icon>Search
        </el-button>
        
        <el-button type="success" @click="handleAdd" class="add-btn">
          <el-icon><Plus /></el-icon>Add Restaurant
        </el-button>
      </div>
    </div>

    <!-- Restaurant list -->
    <div class="table-section neomorphic-card">
      <el-table
        v-loading="loading"
        :data="restaurantList"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column label="Image" width="100">
          <template #default="{ row }">
            <el-image 
              :src="getFirstPhotoUrl(row.photos)" 
              fit="cover"
              style="width: 70px; height: 70px; border-radius: 8px;"
              :preview-src-list="getPhotoArray(row.photos)"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="Restaurant Name" />
        <el-table-column prop="address" label="Address" show-overflow-tooltip />
        <el-table-column prop="phone" label="Phone" width="120" />
        <el-table-column prop="cuisine" label="Cuisine" width="100" />
        <el-table-column label="Status" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Created Time" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="240" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
              </el-button>
              <el-button 
                v-if="userRole !== 1 || (row.status == 2 || row.status == 3)" 
                type="warning" 
                size="small" 
                @click="handleStatusChange(row)"
              >
                <el-icon><Setting /></el-icon>
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

    <!-- Add/Edit restaurant dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? 'Add Restaurant' : 'Edit Restaurant'"
      width="650px"
      class="neomorphic-dialog"
      destroy-on-close
    >
      <div class="custom-form">
        <div class="form-group">
          <label for="name">Restaurant Name</label>
          <div class="input-container">
            <input 
              id="name"
              type="text" 
              v-model="restaurantForm.name" 
              placeholder="Enter restaurant name"
              :class="{ 'error': nameError }"
            />
            <span class="error-message" v-if="nameError">{{ nameError }}</span>
          </div>
        </div>
        
        <div class="form-group">
          <label for="description">Description</label>
          <div class="input-container">
            <textarea 
              id="description"
              v-model="restaurantForm.description" 
              placeholder="Enter restaurant description"
              :class="{ 'error': descriptionError }"
              rows="4"
            ></textarea>
            <span class="error-message" v-if="descriptionError">{{ descriptionError }}</span>
          </div>
        </div>
        
        <div class="form-group">
          <label for="address">Address</label>
          <div class="input-container">
            <input 
              id="address"
              type="text" 
              v-model="restaurantForm.address" 
              placeholder="Enter restaurant address"
              :class="{ 'error': addressError }"
            />
            <span class="error-message" v-if="addressError">{{ addressError }}</span>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group half">
            <label for="longitude">Longitude</label>
            <div class="input-container">
              <input 
                id="longitude"
                type="number" 
                step="0.000001"
                v-model="restaurantForm.longitude" 
                placeholder="Longitude"
                :class="{ 'error': longitudeError }"
              />
              <span class="error-message" v-if="longitudeError">{{ longitudeError }}</span>
            </div>
          </div>
          
          <div class="form-group half">
            <label for="latitude">Latitude</label>
            <div class="input-container">
              <input 
                id="latitude"
                type="number" 
                step="0.000001"
                v-model="restaurantForm.latitude" 
                placeholder="Latitude"
                :class="{ 'error': latitudeError }"
              />
              <span class="error-message" v-if="latitudeError">{{ latitudeError }}</span>
            </div>
          </div>
        </div>
        
        <div class="form-row">
          <div class="form-group half">
            <label for="phone">Phone</label>
            <div class="input-container">
              <input 
                id="phone"
                type="text" 
                v-model="restaurantForm.phone" 
                placeholder="Phone"
                :class="{ 'error': phoneError }"
              />
              <span class="error-message" v-if="phoneError">{{ phoneError }}</span>
            </div>
          </div>
          
          <div class="form-group half">
            <label for="businessLicense">Business License</label>
            <div class="input-container">
              <input 
                id="businessLicense"
                type="text" 
                v-model="restaurantForm.businessLicense" 
                placeholder="Business License"
                :class="{ 'error': businessLicenseError }"
              />
              <span class="error-message" v-if="businessLicenseError">{{ businessLicenseError }}</span>
            </div>
          </div>
        </div>
        
        <div class="form-group">
          <label for="businessHours">Business Hours</label>
          <div class="input-container">
            <input 
              id="businessHours"
              type="text" 
              v-model="restaurantForm.businessHours" 
              placeholder="e.g.: Mon-Fri 11:00-22:00|Weekend 10:00-23:00"
              :class="{ 'error': businessHoursError }"
            />
            <span class="error-message" v-if="businessHoursError">{{ businessHoursError }}</span>
          </div>
        </div>
        
        <div class="form-group">
          <label for="cuisine">Cuisine</label>
          <div class="input-container">
            <input 
              id="cuisine" 
              type="text"
              v-model="restaurantForm.cuisine"
              placeholder="Enter cuisine type"
            />
          </div>
        </div>
        
        <div class="form-group">
          <label>Restaurant Photos</label>
          <div class="photos-upload-container">
            <div class="photos-preview">
              <div 
                v-for="(photo, index) in restaurantForm.photos" 
                :key="index" 
                class="photo-item"
              >
                <img :src="photo" class="photo-image" />
                <div class="photo-actions">
                  <button type="button" class="photo-delete" @click="removePhoto(index)">
                    <i class="el-icon-delete"></i>
                  </button>
                </div>
              </div>
              <div class="photo-add" @click="triggerFileInput">
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
            <p class="photos-hint">Click to add restaurant photos (support JPG, PNG, GIF, etc., max 2MB)</p>
          </div>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="cancel-btn" @click="dialogVisible = false">Cancel</button>
        <button class="submit-btn" @click="validateAndSubmit">{{ dialogType === 'add' ? 'Add' : 'Save' }}</button>
      </div>
    </el-dialog>

    <!-- Status change dialog -->
    <el-dialog
      v-model="statusDialogVisible"
      title="Change Restaurant Status"
      width="450px"
      class="neomorphic-dialog"
      destroy-on-close
    >
      <div class="custom-form">
        <div class="form-group">
          <label for="status">Select Status</label>
          <div class="status-select-container">
            <select 
              id="status" 
              v-model="currentStatus"
              class="status-select"
            >
              <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
            <div class="status-select-arrow">
              <el-icon><ArrowDown /></el-icon>
            </div>
          </div>
        </div>
      </div>
      
      <div class="dialog-footer">
        <button class="cancel-btn" @click="statusDialogVisible = false">Cancel</button>
        <button class="submit-btn" @click="submitStatusChange">Confirm</button>
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
  Picture,
  Setting,
  ArrowDown
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatDate } from '@/utils/format'

// Get user role
const userRole = ref(0)  // Initialize with number 0, not empty string

// Get available status options based on role
const statusOptions = computed(() => {
  // Role: 0: Admin, 1: Restaurant Manager, 2: User, 3: Staff
  if (userRole.value === 1) { // Restaurant Manager
    return [
      { label: 'Closed', value: 2 },
      { label: 'Operating', value: 3 }
    ]
  } else {
    return [
      { label: 'Pending Review', value: 0 },
      { label: 'Approved', value: 1 },
      { label: 'Closed', value: 2 },
      { label: 'Operating', value: 3 }
    ]
  }
})

// Determine if status change button should be shown
const showStatusButton = (row) => {
  // Admin can see status change button for all statuses
  if (userRole.value !== 1) {
    return true
  }
  
  // Restaurant Manager can only see status change button for Operating(3) and Closed(2) statuses
  return row.status === 2 || row.status === 3
}

// Search form
const searchForm = reactive({
  keyword: '',
  status: ''
})

// Table data
const loading = ref(false)
const restaurantList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Dialog related
const dialogVisible = ref(false)
const dialogType = ref('add')
const restaurantFormRef = ref(null)
const restaurantForm = reactive({
  name: '',
  description: '',
  address: '',
  longitude: '',
  latitude: '',
  phone: '',
  businessLicense: '',
  businessHours: '',
  cuisine: '',
  photos: []
})

// Form validation rules
const restaurantRules = {
  name: [
    { required: true, message: 'Please enter restaurant name', trigger: 'blur' },
    { min: 2, max: 50, message: 'Length should be between 2 and 50 characters', trigger: 'blur' }
  ],
  description: [
    { required: true, message: 'Please enter restaurant description', trigger: 'blur' },
    { min: 10, max: 200, message: 'Length should be between 10 and 200 characters', trigger: 'blur' }
  ],
  address: [
    { required: true, message: 'Please enter restaurant address', trigger: 'blur' },
    { min: 10, max: 100, message: 'Length should be between 10 and 100 characters', trigger: 'blur' }
  ],
  longitude: [
    { required: true, message: 'Please enter longitude', trigger: 'blur' },
    { type: 'number', message: 'Please enter valid longitude', trigger: 'blur' }
  ],
  latitude: [
    { required: true, message: 'Please enter latitude', trigger: 'blur' },
    { type: 'number', message: 'Please enter valid latitude', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: 'Please enter phone number', trigger: 'blur' },
    { pattern: /^\+?\d{1,3}[-.\s]?\(?\d{1,3}\)?[-.\s]?\d{1,4}[-.\s]?\d{1,4}[-.\s]?\d{1,9}$/, message: 'Please enter valid phone number', trigger: 'blur' }
  ],
  businessLicense: [
    { required: true, message: 'Please enter business license number', trigger: 'blur' },
    { min: 10, max: 20, message: 'Length should be between 10 and 20 characters', trigger: 'blur' }
  ],
  businessHours: [
    { required: true, message: 'Please enter business hours', trigger: 'blur' },
  ],
  cuisine: [
    { required: true, message: 'Please select cuisine type', trigger: 'change' }
  ]
}

// Add status change dialog related variables
const statusDialogVisible = ref(false)
const currentStatus = ref(0)
const currentRestaurantId = ref(null)

// Get restaurant list
const fetchRestaurantList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    
    // Choose different API based on user role
    const apiUrl = userRole.value === 1 // Use strict equality
      ? '/admin/restaurants/my-restaurants' 
      : '/admin/restaurants'               
    
    console.log('apiUrl:', apiUrl)
    console.log('userRole:', userRole.value, typeof userRole.value)
    const res = await request({
      url: apiUrl,
      method: 'get',
      params
    })
    restaurantList.value = res.data.list
    // Ensure photos is an array
    restaurantList.value = restaurantList.value.map(item => {
      if (item.photos && typeof item.photos === 'string') {
        item.photos = item.photos.split('|').filter(url => url)
      } else if (!Array.isArray(item.photos)) {
        item.photos = []
      }
      return item
    })
    total.value = res.data.total
  } catch (error) {
    console.error('Failed to get restaurant list:', error)
  } finally {
    loading.value = false
  }
}

// Add helper methods to get first photo and photo array
const getFirstPhotoUrl = (photos) => {
  if (!photos) return ''
  if (typeof photos === 'string') {
    const photoArray = photos.split('|').filter(url => url)
    return photoArray.length > 0 ? photoArray[0] : ''
  }
  if (Array.isArray(photos) && photos.length > 0) {
    return photos[0]
  }
  return ''
}

const getPhotoArray = (photos) => {
  if (!photos) return []
  if (typeof photos === 'string') {
    return photos.split('|').filter(url => url)
  }
  if (Array.isArray(photos)) {
    return photos
  }
  return []
}

// Search
const handleSearch = () => {
  currentPage.value = 1
  fetchRestaurantList()
}

// Add restaurant
const handleAdd = () => {
  dialogType.value = 'add'
  Object.assign(restaurantForm, {
    name: '',
    description: '',
    address: '',
    longitude: '',
    latitude: '',
    phone: '',
    businessLicense: '',
    businessHours: '',
    cuisine: '',
    photos: []
  })
  dialogVisible.value = true
}

// Edit restaurant
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  // Print to check row data, confirm ID field name
  console.log('Editing restaurant data:', row)
  
  // Get ID
  const id = row.id || row.restaurantId
  if (!id) {
    console.error('Restaurant ID not found:', row)
    ElMessage.error('Failed to get restaurant ID, cannot edit')
    return
  }
  
  // Get detailed info
  const detailData = await fetchRestaurantDetail(id)
  if (!detailData) {
    return
  }
  
  // Deep copy object to avoid direct reference
  const formData = JSON.parse(JSON.stringify(detailData))
  // Ensure ID field exists
  if (!formData.restaurantId) {
    formData.restaurantId = formData.id
  }
  
  // Handle photos field, ensure it's an array type
  if (formData.photos && typeof formData.photos === 'string') {
    formData.photos = formData.photos.split('|').filter(url => url)
  } else if (!Array.isArray(formData.photos)) {
    formData.photos = []
  }
  
  Object.assign(restaurantForm, formData)
  dialogVisible.value = true
}

// Delete restaurant
const handleDelete = (row) => {
  ElMessageBox.confirm(
    'Are you sure you want to delete this restaurant?',
    'Warning',
    {
      confirmButtonText: 'Confirm',
      cancelButtonText: 'Cancel',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request({
        url: `/admin/restaurants/${row.restaurantId}`,
        method: 'delete'
      })
      ElMessage.success('Deleted successfully')
      fetchRestaurantList()
    } catch (error) {
      console.error('Failed to delete restaurant:', error)
    }
  })
}

// Add file upload related refs
const fileInput = ref(null)

// Add error status variables
const nameError = ref('')
const descriptionError = ref('')
const addressError = ref('')
const longitudeError = ref('')
const latitudeError = ref('')
const phoneError = ref('')
const businessLicenseError = ref('')
const businessHoursError = ref('')

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
    ElMessage.error('Please upload valid image format (JPG, PNG, GIF, WEBP, BMP, SVG)')
    return
  }
  
  if (!isLt2M) {
    ElMessage.error('Image size cannot exceed 2MB')
    return
  }
  
  // Create FormData object
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
    restaurantForm.photos.push(res.data)
    ElMessage.success('Restaurant photo uploaded successfully')
  }).catch(() => {
    ElMessage.error('Failed to upload restaurant photo')
  })
}

// Validate and submit form
const validateAndSubmit = () => {
  // Reset error messages
  nameError.value = ''
  descriptionError.value = ''
  addressError.value = ''
  longitudeError.value = ''
  latitudeError.value = ''
  phoneError.value = ''
  businessLicenseError.value = ''
  businessHoursError.value = ''
  
  let isValid = true
  
  // Validate restaurant name
  if (!restaurantForm.name) {
    nameError.value = 'Please enter restaurant name'
    isValid = false
  } else if (restaurantForm.name.length < 2 || restaurantForm.name.length > 50) {
    nameError.value = 'Restaurant name should be between 2-50 characters'
    isValid = false
  }
  
  // Validate restaurant description
  if (!restaurantForm.description) {
    descriptionError.value = 'Please enter restaurant description'
    isValid = false
  } else if (restaurantForm.description.length < 10 || restaurantForm.description.length > 200) {
    descriptionError.value = 'Description should be between 10-200 characters'
    isValid = false
  }
  
  // Validate restaurant address
  if (!restaurantForm.address) {
    addressError.value = 'Please enter restaurant address'
    isValid = false
  } else if (restaurantForm.address.length < 10 || restaurantForm.address.length > 100) {
    addressError.value = 'Address should be between 10-100 characters'
    isValid = false
  }
  
  // Validate longitude
  if (!restaurantForm.longitude) {
    longitudeError.value = 'Please enter longitude'
    isValid = false
  } else if (!/^-?\d{1,3}(\.\d+)?$/.test(restaurantForm.longitude)) {
    longitudeError.value = 'Please enter valid longitude format'
    isValid = false
  }
  
  // Validate latitude
  if (!restaurantForm.latitude) {
    latitudeError.value = 'Please enter latitude'
    isValid = false
  } else if (!/^-?\d{1,3}(\.\d+)?$/.test(restaurantForm.latitude)) {
    latitudeError.value = 'Please enter valid latitude format'
    isValid = false
  }
  
  // Validate phone number
  if (!restaurantForm.phone) {
    phoneError.value = 'Please enter phone number'
    isValid = false
  } else if (!/^\+?\d{1,3}[-.\s]?\(?\d{1,3}\)?[-.\s]?\d{1,4}[-.\s]?\d{1,4}[-.\s]?\d{1,9}$/.test(restaurantForm.phone)) {
    phoneError.value = 'Please enter valid phone number format'
    isValid = false
  }
  
  // Validate business license
  if (!restaurantForm.businessLicense) {
    businessLicenseError.value = 'Please enter business license number'
    isValid = false
  } else if (restaurantForm.businessLicense.length < 10 || restaurantForm.businessLicense.length > 20) {
    businessLicenseError.value = 'Business license number should be between 10-20 characters'
    isValid = false
  }
  
  // Validate business hours
  if (!restaurantForm.businessHours) {
    businessHoursError.value = 'Please enter business hours'
    isValid = false
  } else if (!validateBusinessHours(restaurantForm.businessHours)) {
    businessHoursError.value = 'Please enter valid business hours format, e.g.: Mon-Fri 11:00-22:00|Weekend 10:00-23:00'
    isValid = false
  }
  
  // Validate cuisine
  if (!restaurantForm.cuisine) {
    ElMessage.error('Please select cuisine type')
    isValid = false
  }
  
  // If validation passes, submit form
  if (isValid) {
    if (dialogType.value === 'add') {
      addRestaurant()
    } else {
      updateRestaurant()
    }
  }
}

// Add business hours validation method
const validateBusinessHours = (value) => {
  // Split into different time segments by |
  const segments = value.split('|')
  if (segments.length === 0) return false

  // Validate each time segment format
  for (const segment of segments) {
    // Check if each segment contains date description and time range
    const parts = segment.trim().split(' ')
    if (parts.length < 2) return false

    // Validate time range part (the last part should be time range like "11:00-22:00")
    const timeRange = parts[parts.length - 1]
    const times = timeRange.split('-')
    
    // Time range should have start and end time
    if (times.length !== 2) return false
    
    // Validate time format HH:MM
    const timePattern = /^([01]?\d|2[0-3]):([0-5]\d)$/
    if (!timePattern.test(times[0].trim()) || !timePattern.test(times[1].trim())) {
      return false
    }
  }
  
  return true
}

// Add restaurant
const addRestaurant = async () => {
  try {
    // Submit directly in array format, no need to convert photos to string
    await request({
      url: '/admin/restaurants',
      method: 'post',
      data: restaurantForm
    })
    ElMessage.success('Added successfully')
    dialogVisible.value = false
    fetchRestaurantList()
  } catch (error) {
    console.error('Failed to add restaurant:', error)
  }
}

// Update restaurant
const updateRestaurant = async () => {
  // Check if ID exists
  if (!restaurantForm.restaurantId && restaurantForm.id) {
    restaurantForm.restaurantId = restaurantForm.id
  }
  
  if (!restaurantForm.restaurantId) {
    console.error('Failed to update restaurant: Missing restaurant ID', restaurantForm)
    ElMessage.error('Update failed: Missing restaurant ID')
    return
  }
  
  console.log('Updating restaurant ID:', restaurantForm.restaurantId)
  
  try {
    // Submit directly in array format, no need to convert photos to string
    await request({
      url: `/admin/restaurants/${restaurantForm.restaurantId}`,
      method: 'put',
      data: restaurantForm
    })
    ElMessage.success('Updated successfully')
    dialogVisible.value = false
    fetchRestaurantList()
  } catch (error) {
    console.error('Failed to update restaurant:', error)
  }
}

// Pagination related
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRestaurantList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRestaurantList()
}

// Get status tag type
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'warning'
  }
  return typeMap[status] || 'info'
}

// Get status text
const getStatusText = (status) => {
  const textMap = {
    0: 'Pending Review',
    1: 'Approved',
    2: 'Closed',
    3: 'Operating'
  }
  return textMap[status] || 'Unknown Status'
}

// Add photo removal method
const removePhoto = (index) => {
  restaurantForm.photos.splice(index, 1)
}

// Get restaurant details
const fetchRestaurantDetail = async (id) => {
  try {
    const res = await request({
      url: `/admin/restaurants/${id}`,
      method: 'get'
    })
    
    // Process returned data, ensure photos field is an array
    const data = res.data
    
    if (data && data.photos && typeof data.photos === 'string') {
      data.photos = data.photos.split('|').filter(url => url)
    } else if (data && !Array.isArray(data.photos)) {
      data.photos = []
    }
    
    return data
  } catch (error) {
    console.error('Failed to get restaurant details:', error)
    ElMessage.error('Failed to get restaurant details')
    return null
  }
}

// Handle status change
const handleStatusChange = async (row) => {
  console.log('Restaurant data for status change:', row)
  // Get restaurant details to ensure latest data
  const id = row.id || row.restaurantId
  if (!id) {
    ElMessage.error('Restaurant ID does not exist, cannot change status')
    return
  }
  
  currentRestaurantId.value = id
  currentStatus.value = row.status.toString() // Ensure it's a string
  statusDialogVisible.value = true
}

// Submit status change
const submitStatusChange = async () => {
  if (!currentRestaurantId.value) {
    ElMessage.error('Restaurant ID does not exist, cannot change status')
    return
  }
  
  try {
    await request({
      url: `/admin/restaurants/${currentRestaurantId.value}/status`,
      method: 'put',
      params: { status: parseInt(currentStatus.value) }
    })
    ElMessage.success('Status updated successfully')
    statusDialogVisible.value = false
    fetchRestaurantList() // Refresh list
  } catch (error) {
    console.error('Failed to update restaurant status:', error)
    ElMessage.error('Failed to update restaurant status')
  }
}

// Initialize
onMounted(() => {
  // Get user info from localStorage
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr) {
      const userInfo = JSON.parse(userInfoStr)
      userRole.value = Number(userInfo.role || 0) // Ensure conversion to number
      console.log('Current user role:', userRole.value, typeof userRole.value)
    }
  } catch (error) {
    console.error('Failed to get user info:', error)
  }
  
  // Ensure restaurant list is fetched after getting user role
  fetchRestaurantList()
})
</script>

<style scoped>
.restaurant-manage-container {
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

.photos-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.photos-preview {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
  justify-content: flex-start;
}

.photo-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
  border: 3px solid white;
  transition: all 0.3s ease;
}

.photo-item:hover {
  transform: scale(1.05);
  box-shadow: 8px 8px 16px #c3c4ca,
              -8px -8px 16px #ffffff;
}

.photo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-actions {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.photo-item:hover .photo-actions {
  opacity: 1;
}

.photo-delete {
  background-color: rgba(255, 255, 255, 0.8);
  color: #f56c6c;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.photo-delete:hover {
  transform: scale(1.1);
  background-color: white;
  color: #ff4d4f;
}

.photo-add {
  width: 120px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  border: 3px dashed rgba(64, 158, 255, 0.5);
  background-color: #e6e7ee;
  color: #409eff;
  font-size: 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 4px 4px 8px #c3c4ca, 
              -4px -4px 8px #ffffff;
}

.photo-add:hover {
  transform: translateY(-3px);
  box-shadow: 6px 6px 12px #c3c4ca, 
              -6px -6px 12px #ffffff;
  border-color: #409eff;
}

.photos-hint {
  margin-top: 10px;
  font-size: 14px;
  color: #909399;
  text-align: center;
  max-width: 400px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 20px;
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
  .restaurant-manage-container {
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

  .photos-preview {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
    border-color: #2d3748;
  }

  .photos-preview:hover {
    box-shadow: 8px 8px 16px #202632,
                -8px -8px 16px #3a485e;
  }

  .photo-item {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
    border-color: #2d3748;
  }

  .photo-actions {
    background-color: rgba(255, 255, 255, 0.5);
  }

  .photo-delete {
    background-color: rgba(255, 255, 255, 0.5);
  }

  .photo-delete:hover {
    transform: scale(1.2);
  }

  .photos-hint {
    color: #e2e8f0;
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

  .form-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .photo-item, .photo-add {
    width: 80px;
    height: 80px;
  }
}

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
  
  .photos-hint {
    color: #e2e8f0;
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

.input-container input,
.input-container textarea {
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

.input-container input::placeholder,
.input-container textarea::placeholder {
  color: #a0aec0;
}

.input-container input:focus,
.input-container textarea:focus {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border: 1px solid #409eff;
}

.input-container input.error,
.input-container textarea.error {
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

.status-select {
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

.status-select:focus {
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

.photos-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.photos-preview {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
  justify-content: flex-start;
}

.photo-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
  border: 3px solid white;
  transition: all 0.3s ease;
}

.photo-item:hover {
  transform: scale(1.05);
  box-shadow: 8px 8px 16px #c3c4ca,
              -8px -8px 16px #ffffff;
}

.photo-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-actions {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.photo-item:hover .photo-actions {
  opacity: 1;
}

.photo-delete {
  background-color: rgba(255, 255, 255, 0.8);
  color: #f56c6c;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.photo-delete:hover {
  transform: scale(1.1);
  background-color: white;
  color: #ff4d4f;
}

.photo-add {
  width: 120px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  border: 3px dashed rgba(64, 158, 255, 0.5);
  background-color: #e6e7ee;
  color: #409eff;
  font-size: 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 4px 4px 8px #c3c4ca, 
              -4px -4px 8px #ffffff;
}

.photo-add:hover {
  transform: translateY(-3px);
  box-shadow: 6px 6px 12px #c3c4ca, 
              -6px -6px 12px #ffffff;
  border-color: #409eff;
}

.photos-hint {
  margin-top: 10px;
  font-size: 14px;
  color: #909399;
  text-align: center;
  max-width: 400px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 20px;
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
  .input-container textarea,
  .select-container select {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: inset 3px 3px 7px #202632,
                inset -3px -3px 7px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .input-container input::placeholder,
  .input-container textarea::placeholder {
    color: #718096;
  }
  
  .input-container input:focus,
  .input-container textarea:focus,
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
  
  .photos-preview {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
    border-color: #2d3748;
  }
  
  .photos-preview:hover {
    box-shadow: 8px 8px 16px #202632,
                -8px -8px 16px #3a485e;
  }
  
  .photo-item {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
    border-color: #2d3748;
  }
  
  .photo-actions {
    background-color: rgba(255, 255, 255, 0.5);
  }
  
  .photo-delete {
    background-color: rgba(255, 255, 255, 0.5);
  }
  
  .photo-delete:hover {
    transform: scale(1.2);
  }
  
  .photos-hint {
    color: #e2e8f0;
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

.cuisine-select-container {
  position: relative;
  width: 100%;
}

.cuisine-select {
  width: 100%;
  padding: 14px 18px;
  font-size: 16px;
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
  background-image: none;
}

.cuisine-select:focus {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border-color: #409eff;
}

.cuisine-select option {
  background-color: #e6e7ee;
  color: #5e6687;
  padding: 12px;
  font-size: 16px;
}

.cuisine-select-arrow {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #5e6687;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background-color: #e6e7ee;
  border-radius: 50%;
  box-shadow: 2px 2px 4px #c3c4ca,
              -2px -2px 4px #ffffff;
}

@media (prefers-color-scheme: dark) {
  .cuisine-select {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: inset 3px 3px 7px #202632,
                inset -3px -3px 7px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .cuisine-select:focus {
    box-shadow: inset 4px 4px 8px #202632,
                inset -4px -4px 8px #3a485e;
    border-color: #409eff;
  }

  .cuisine-select option {
    background-color: #2d3748;
    color: #e2e8f0;
  }

  .cuisine-select-arrow {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: 2px 2px 4px #202632,
                -2px -2px 4px #3a485e;
  }
}

.status-select-container {
  position: relative;
  width: 100%;
}

.status-select {
  width: 100%;
  padding: 14px 18px;
  font-size: 16px;
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
  background-image: none;
}

.status-select:focus {
  box-shadow: inset 4px 4px 8px #c3c4ca,
              inset -4px -4px 8px #ffffff;
  border-color: #409eff;
}

.status-select option {
  background-color: #e6e7ee;
  color: #5e6687;
  padding: 12px;
  font-size: 16px;
}

.status-select-arrow {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #5e6687;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background-color: #e6e7ee;
  border-radius: 50%;
  box-shadow: 2px 2px 4px #c3c4ca,
              -2px -2px 4px #ffffff;
}

@media (prefers-color-scheme: dark) {
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
    border-color: #409eff;
  }

  .status-select option {
    background-color: #2d3748;
    color: #e2e8f0;
  }

  .status-select-arrow {
    background-color: #2d3748;
    color: #e2e8f0;
    box-shadow: 2px 2px 4px #202632,
                -2px -2px 4px #3a485e;
  }
}
</style> 