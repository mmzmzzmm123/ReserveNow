<template>
  <div class="admin-layout" :class="{ 'layout-collapsed': isCollapsed }">
    <div class="sidebar" :class="{ 'sidebar-collapsed': isCollapsed }">
      <div class="sidebar-header">
        <div class="logo-container" v-if="!isCollapsed">
          <div class="logo-text">ADMIN</div>
        </div>
        <div class="toggle-btn neomorphic-toggle" @click="toggleSidebar">
          <el-icon size="14">
            <ArrowLeft v-if="!isCollapsed" />
            <ArrowRight v-else />
          </el-icon>
        </div>
      </div>
      
      <nav class="sidebar-nav">
        <div v-for="(item, index) in menuItems" :key="index" 
             class="nav-item" 
             :class="{ 'active': currentRoute === item.path, 'neomorphic-nav-item': true }"
             @click="navigate(item.path)">
          <div class="nav-icon">
            <el-icon :size="18">
              <component :is="item.icon"></component>
            </el-icon>
          </div>
          <span v-if="!isCollapsed" class="nav-text">{{ item.title }}</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info neomorphic-user-profile">
          <div class="avatar-wrapper">
            <img :src="userAvatar" alt="User Avatar" class="avatar" />
            <div class="status-dot"></div>
          </div>
          <div v-if="!isCollapsed" class="user-details">
            <span class="username">{{ username }}</span>
            <span class="role">{{ userRoleText }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="main-content">
      <header class="admin-header neomorphic-header">
        <div class="header-left">
          <h2 class="page-title">{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info-header neomorphic-dropdown-trigger">
              <img :src="userAvatar" alt="User Avatar" class="avatar" />
              <span class="username">{{ username }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">Profile</el-dropdown-item>
                <el-dropdown-item divided command="logout">Logout</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="admin-main">
        <router-view />
      </main>
    </div>

    <!-- Add personal information dialog -->
    <div class="profile-dialog" v-if="profileDialogVisible">
      <div class="profile-dialog-content neomorphic-panel">
        <div class="profile-dialog-header">
          <h2>Profile</h2>
          <button class="close-btn neomorphic-btn" @click="profileDialogVisible = false">
            <el-icon><Close /></el-icon>
          </button>
        </div>
        
        <div class="profile-container">
          <!-- Avatar upload section -->
          <div class="avatar-section">
            <div class="avatar-uploader neomorphic-upload" @click="triggerFileInput">
              <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
              <div v-else class="avatar-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
              </div>
            </div>
            <input 
              type="file" 
              ref="fileInput" 
              style="display: none" 
              accept="image/*"
              @change="handleFileChange"
            />
            <p class="upload-hint">Click to Change Avatar</p>
          </div>

          <!-- 选项卡 -->
          <div class="profile-tabs neomorphic-tabs">
            <div class="tab-headers">
              <button 
                class="tab-btn" 
                :class="{ active: activeTab === 'info' }"
                @click="activeTab = 'info'"
              >Basic Info</button>
              <button 
                class="tab-btn" 
                :class="{ active: activeTab === 'password' }"
                @click="activeTab = 'password'"
              >Change Password</button>
            </div>

            <!-- Basic Info Form -->  
            <div v-show="activeTab === 'info'" class="tab-content">
              <div class="form-group">
                <label>Username</label>
                <div class="input-wrapper neomorphic-input">
                  <input type="text" v-model="userForm.name" placeholder="Enter username">
                </div>
              </div>
              <div class="form-group">
                <label>Email</label>
                <div class="input-wrapper neomorphic-input">
                  <input type="text" v-model="userForm.email" disabled>
                </div>
              </div>
              <div class="form-group">
                <label>Registration Time</label>
                <div class="input-wrapper neomorphic-input">
                  <input type="text" :value="formatCreatedTime(userForm.createdAt)" disabled>
                </div>
              </div>
              <div class="form-actions">
                <button class="submit-btn neomorphic-btn" @click="handleUpdateProfile">
                   Save Changes
                </button>
              </div>
            </div>

            <div v-show="activeTab === 'password'" class="tab-content">
              <div class="form-group">
                <label>Current Password</label>
                <div class="input-wrapper neomorphic-input">
                  <input 
                    type="password" 
                    v-model="passwordForm.oldPassword" 
                    placeholder="Enter current password"
                  >
                </div>
              </div>
              <div class="form-group">
                <label>New Password</label>
                <div class="input-wrapper neomorphic-input">
                  <input 
                    type="password" 
                    v-model="passwordForm.newPassword" 
                    placeholder="Enter new password"
                  >
                </div>
              </div>
              <div class="form-group">
                <label>Confirm Password</label>
                <div class="input-wrapper neomorphic-input">
                  <input 
                    type="password" 
                    v-model="passwordForm.confirmPassword" 
                    placeholder="Confirm new password"
                  >
                </div>
              </div>
              <div class="form-actions">
                <button class="submit-btn neomorphic-btn" @click="handleChangePassword">
                   Change Password
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Odometer,
  User,
  DataLine,
  Setting,
  ArrowLeft,
  ArrowRight,
  ArrowDown,
  KnifeFork,
  Calendar,
  ChatDotRound,
  UserFilled,
  Star,
  Tickets,
  Plus,
  Close
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const isCollapsed = ref(false)
const currentRoute = computed(() => route.path)

// Get user information and role
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'));
const userRole = ref(userInfo.value.role || 0);

// Filter menu items based on user role
const menuItems = computed(() => {
  // 0: Admin, 1: Restaurant Manager, 2: User, 3: Staff
  
  // Admin Menu
  if (userRole.value === 0) {
    return [
      { title: 'Dashboard', icon: 'Odometer', path: '/admin/dashboard' },
      { title: 'Restaurant Management', icon: 'KnifeFork', path: '/admin/restaurants' },
      { title: 'Table Management', icon: 'Tickets', path: '/admin/tables' },
      { title: 'User Management', icon: 'User', path: '/admin/users' },
      { title: 'Reservation Management', icon: 'Calendar', path: '/admin/reservations' },
      { title: 'Review Management', icon: 'ChatDotRound', path: '/admin/reviews' }
    ];
  }
  
  // Restaurant Manager Menu
  else if (userRole.value === 1) {
    return [
      { title: 'Dashboard', icon: 'Odometer', path: '/admin/dashboard' },
      { title: 'My Restaurant', icon: 'KnifeFork', path: '/admin/restaurants' },
      { title: 'Table Management', icon: 'Tickets', path: '/admin/tables' },
      { title: 'Reservation Management', icon: 'Calendar', path: '/admin/reservations' },
      { title: 'Staff Management', icon: 'UserFilled', path: '/admin/staff' }
    ];
  }
  
  // Staff Menu
  else if (userRole.value === 3) {
    return [
      { title: 'Dashboard', icon: 'Odometer', path: '/admin/dashboard' },
      { title: 'Reservation Management', icon: 'Calendar', path: '/admin/reservations' },
      { title: 'Apply for Staff', icon: 'UserFilled', path: '/admin/staff-apply' }
    ];
  }
  
  // Default Menu
  return [
    { title: 'Dashboard', icon: 'Odometer', path: '/admin/dashboard' },
    { title: 'Settings', icon: 'Setting', path: '/admin/settings' },
  ];
});

const userAvatar = ref(userInfo.value.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix')
const username = ref(userInfo.value.name || 'Admin')
const userRoleText = computed(() => {
  const roleMap = {
    0: 'System Admin',
    1: 'Restaurant Manager',
    2: 'Regular User',
    3: 'Restaurant Staff'
  };
  return roleMap[userRole.value] || 'Unknown Role';
});

// Page Title
const pageTitle = computed(() => {
  return route.meta.title || 'Admin Console';
});

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const navigate = (path) => {
  router.push(path)
}

// Handle dropdown menu commands
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm(
      'Are you sure you want to logout?',
      'Logout Confirmation',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    ).then(() => {
      localStorage.removeItem('userInfo');
      localStorage.removeItem('savedCredentials');
      document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
      
      ElMessage.success('Logout successful');
      router.push('/login');
    }).catch(() => {});
  } else if (command === 'profile') {
    profileDialogVisible.value = true
    fetchUserInfo()
  } else if (command === 'settings') {
    router.push('/admin/settings');
  }
};

// Profile related
const profileDialogVisible = ref(false)
const activeTab = ref('info')
const fileInput = ref(null)
const userForm = reactive({
  name: '',
  email: '',
  role: '',
  createdAt: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: 'Please enter current password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: 'Please enter new password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm new password', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('Passwords do not match'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const passwordFormRef = ref(null)

// Get user information
const fetchUserInfo = async () => {
  try {
    const res = await request({
      url: '/users/info',
      method: 'get'
    })
    if (res.code === 200) {
      userInfo.value = res.data
      Object.assign(userForm, res.data)
      const token = JSON.parse(localStorage.getItem("userInfo")).token
      res.data.token = token
      localStorage.setItem("userInfo",JSON.stringify(res.data))
    }
  } catch (error) {
    console.error('Failed to get user information:', error)
  }
}

// Update user information
const handleUpdateProfile = async () => {
  try {
    const res = await request({
      url: '/users/update',
      method: 'put',
      data: {
        name: userForm.name,
        avatar: userForm.avatar
      }
    })
    if (res.code === 200) {
      ElMessage.success('Profile updated successfully')
      fetchUserInfo()
    }
    
  } catch (error) {
    console.error('Failed to update user information:', error)
  }
}

// Change password
const handleChangePassword = async () => {
  // Validate password
  if (!passwordForm.oldPassword) {
    ElMessage.error('Please enter current password');
    return;
  }
  
  if (!passwordForm.newPassword) {
    ElMessage.error('Please enter new password');
    return;
  }
  
  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('New password must be at least 6 characters');
    return;
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('Passwords do not match');
    return;
  }
  
  try {
    const res = await request({
      url: '/users/update',
      method: 'put',
      data: {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
        confirmPassword: passwordForm.confirmPassword
      }
    });
    
    if (res.code === 200) {
      ElMessage.success('Password changed successfully');
      activeTab.value = 'info';
      // Clear password form
      passwordForm.oldPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
    }
  } catch (error) {
    console.error('Failed to change password:', error);
    ElMessage.error('Failed to change password');
  }
};

// Add time formatting function
const formatCreatedTime = (timestamp) => {
  if (!timestamp) return 'Unknown Time';
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}`;
};

// Add file selection trigger function
const triggerFileInput = () => {
  fileInput.value.click();
};

// Add file change handler function
const handleFileChange = (e) => {
  const file = e.target.files[0];
  if (!file) return;
  
  const isValidImageType = /^image\/(jpeg|png|gif)$/.test(file.type);
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isValidImageType) {
    ElMessage.error('Please upload JPG/PNG/GIF format images');
    return;
  }
  
  if (!isLt2M) {
    ElMessage.error('Image size cannot exceed 2MB');
    return;
  }
  
  const formData = new FormData();
  formData.append('file', file);
  
  handleAvatarUpload({ file });
};

// Add avatar upload handler function
const handleAvatarUpload = async ({ file }) => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const res = await request({
      url: '/files/images',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    if (res.code === 200) {
      userForm.avatar = res.data;
      ElMessage.success('Avatar uploaded successfully');
    }
  } catch (error) {
    console.error('Failed to upload avatar:', error);
    ElMessage.error('Failed to upload avatar');
  }
};

// Check access permission when component is mounted
onMounted(() => {
  // If path includes admin, check permission
  if (route.path.includes('/admin')) {
    const role = userInfo.value.role;
    // If regular user (role === 2) or not logged in, redirect to home page
    if (role === 2 || !userInfo.value.token) {
      ElMessage.error('You do not have permission to access the admin panel');
      router.push('/');
    }
  }
  fetchUserInfo()
});
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background-color: #e6e7ee;
}

.admin-layout.layout-collapsed .main-content {
  margin-left: 110px;
}

.main-content {
  flex: 1;
  margin-left: 320px;
  transition: margin-left 0.3s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-header {
  height: 70px;
  background-color: #e6e7ee;
  padding: 0 25px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
}

.neomorphic-header {
  box-shadow: 0 4px 10px -6px #c3c4ca;
  background: #e6e7ee;
  position: relative;
  z-index: 10;
}

.header-left .page-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: #44476a;
  margin: 0;
  text-shadow: 1px 1px 1px rgba(255, 255, 255, 0.5);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info-header {
  display: flex;
  align-items: center;
  padding: 6px 14px;
  cursor: pointer;
  border-radius: 12px;
  transition: all 0.3s;
}

.neomorphic-dropdown-trigger {
  background: #e6e7ee;
  box-shadow: 3px 3px 6px #c3c4ca,
              -3px -3px 6px #ffffff;
}

.user-info-header:hover {
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
  transform: translateY(-2px);
}

.user-info-header .avatar {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  margin-right: 10px;
  object-fit: cover;
  border: 3px solid #e6e7ee;
  box-shadow: 2px 2px 4px #c3c4ca,
              -2px -2px 4px #ffffff;
}

.user-info-header .username {
  font-size: 14px;
  color: #44476a;
  margin-right: 6px;
  font-weight: 500;
}

.admin-main {
  flex: 1;
  padding: 20px;
  overflow: auto;
}

.sidebar {
  height: 100vh;
  background: #e6e7ee;
  color: #44476a;
  width: 320px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  padding: 15px;
  border-right: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 5px 0 15px -5px #c3c4ca;
}

.sidebar-collapsed {
  width: 110px;
}

.sidebar-header {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #4a6cf7;
  letter-spacing: 1px;
  text-shadow: 1px 1px 1px rgba(255, 255, 255, 0.8);
}

.neomorphic-toggle {
  cursor: pointer;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #4a6cf7;
  transition: all 0.3s ease;
  background: #e6e7ee;
  box-shadow: 3px 3px 6px #c3c4ca,
              -3px -3px 6px #ffffff;
}

.neomorphic-toggle:hover {
  color: #f74a6c;
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
}

.neomorphic-toggle:active {
  transform: translateY(0px);
  box-shadow: inset 2px 2px 5px #c3c4ca,
              inset -2px -2px 5px #ffffff;
}

.sidebar-nav {
  flex: 1;
  padding: 10px 0;
  overflow-y: auto;
}

.neomorphic-nav-item {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  margin: 10px 5px;
  border-radius: 12px;
  background: #e6e7ee;
  box-shadow: 3px 3px 6px #c3c4ca,
              -3px -3px 6px #ffffff;
}

.nav-item:hover {
  color: #f74a6c;
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
}

.nav-item.active {
  color: #4a6cf7;
  box-shadow: inset 2px 2px 5px #c3c4ca,
              inset -2px -2px 5px #ffffff;
}

.nav-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  margin-right: 10px;
  background: #e6e7ee;
  box-shadow: 2px 2px 4px #c3c4ca,
              -2px -2px 4px #ffffff;
}

.active .nav-icon {
  box-shadow: inset 1px 1px 3px #c3c4ca,
              inset -1px -1px 3px #ffffff;
}

.nav-text {
  font-weight: 500;
  white-space: nowrap;
  opacity: 1;
  transition: opacity 0.3s ease;
}

.sidebar-collapsed .nav-text {
  opacity: 0;
  display: none;
}

.sidebar-footer {
  padding: 16px 8px;
  margin-top: 20px;
}

.sidebar-footer .user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  border-radius: 16px;
  background: #e6e7ee;
  box-shadow: 3px 3px 6px #c3c4ca,
              -3px -3px 6px #ffffff;
}

.sidebar-footer .user-info:hover {
  transform: translateY(-2px);
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
}

.sidebar-footer .avatar-wrapper {
  position: relative;
}

.sidebar-footer .avatar {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  object-fit: cover;
  border: 3px solid #e6e7ee;
  box-shadow: 2px 2px 4px #c3c4ca,
              -2px -2px 4px #ffffff;
}

.sidebar-footer .status-dot {
  width: 12px;
  height: 12px;
  background: #4ade80;
  border-radius: 50%;
  position: absolute;
  bottom: -2px;
  right: -2px;
  border: 2px solid #e6e7ee;
  box-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1);
}

.sidebar-footer .user-details {
  display: flex;
  flex-direction: column;
}

.sidebar-footer .username {
  font-weight: 600;
  font-size: 0.95em;
  color: #44476a;
}

.sidebar-footer .role {
  font-size: 0.8em;
  color: #7f83a2;
}

/* Scrollbar styles */
.sidebar-nav::-webkit-scrollbar {
  width: 6px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}

.sidebar-nav::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}

/* Adapt to collapsed state */
.sidebar-collapsed .nav-item {
  padding: 14px 10px;
  justify-content: center;
}

.sidebar-collapsed .nav-icon {
  margin-right: 0;
}

.sidebar-collapsed .user-info {
  justify-content: center;
  padding: 10px;
}

/* Responsive */
@media (max-width: 768px) {
  .admin-layout .main-content {
    margin-left: 0;
  }
  
  .admin-layout.layout-collapsed .main-content {
    margin-left: 0;
  }
  
  .sidebar {
    transform: translateX(-100%);
    position: fixed;
    z-index: 1001;
  }
  
  .sidebar-collapsed {
    transform: translateX(0);
    width: 80px;
  }
}

/* Dark mode adaptation */
@media (prefers-color-scheme: dark) {
  .admin-layout {
    background-color: #2d3748;
  }
  
  .sidebar {
    background: #2d3748;
    color: #e2e8f0;
    box-shadow: 5px 0 15px -5px #202632;
    border-right: 1px solid rgba(0, 0, 0, 0.2);
  }
  
  .neomorphic-toggle, .neomorphic-nav-item, .neomorphic-user-profile, .nav-icon {
    background: #2d3748;
    box-shadow: 3px 3px 6px #202632,
                -3px -3px 6px #3a485e;
  }
  
  .neomorphic-toggle:hover, .nav-item:hover, .neomorphic-user-profile:hover {
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }
  
  .neomorphic-toggle:active, .nav-item.active {
    box-shadow: inset 2px 2px 5px #202632,
                inset -2px -2px 5px #3a485e;
  }
  
  .active .nav-icon {
    box-shadow: inset 1px 1px 3px #202632,
                inset -1px -1px 3px #3a485e;
  }
  
  .logo-text {
    color: #63b3ed;
    text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.2);
  }
  
  .username {
    color: #e2e8f0;
  }
  
  .role {
    color: #a0aec0;
  }
  
  .avatar {
    border-color: #2d3748;
    box-shadow: 2px 2px 4px #202632,
                -2px -2px 4px #3a485e;
  }
  
  .status-dot {
    border-color: #2d3748;
  }
  
  .admin-header {
    background-color: #2d3748;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    border-bottom: 1px solid rgba(0, 0, 0, 0.2);
  }
  
  .neomorphic-header {
    box-shadow: 0 4px 10px -6px #202632;
  }
  
  .header-left .page-title {
    color: #e2e8f0;
    text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.2);
  }
  
  .user-info-header .username {
    color: #e2e8f0;
  }
  
  .neomorphic-dropdown-trigger {
    background: #2d3748;
    box-shadow: 3px 3px 6px #202632,
                -3px -3px 6px #3a485e;
  }
  
  .user-info-header:hover {
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }
  
  .user-info-header .avatar {
    border-color: #2d3748;
    box-shadow: 2px 2px 4px #202632,
                -2px -2px 4px #3a485e;
  }
  
  .admin-main {
    background-color: #2d3748;
  }

  .sidebar-footer .user-info {
    background: #2d3748;
    box-shadow: 3px 3px 6px #202632,
                -3px -3px 6px #3a485e;
  }

  .sidebar-footer .user-info:hover {
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
  }

  .sidebar-footer .avatar {
    border-color: #2d3748;
    box-shadow: 2px 2px 4px #202632,
                -2px -2px 4px #3a485e;
  }

  .sidebar-footer .status-dot {
    border-color: #2d3748;
  }

  .sidebar-footer .username {
    color: #e2e8f0;
  }

  .sidebar-footer .role {
    color: #a0aec0;
  }
}

.profile-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.profile-dialog-content {
  width: 90%;
  max-width: 460px;
  background: #e6e7ee;
  border-radius: 16px;
  padding: 20px;
  position: relative;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { 
    transform: translateY(-20px);
    opacity: 0;
  }
  to { 
    transform: translateY(0);
    opacity: 1;
  }
}

.neomorphic-panel {
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.profile-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.profile-dialog-header h2 {
  font-size: 20px;
  color: #44476a;
  margin: 0;
  font-weight: 600;
}

.close-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  background: none;
  color: #44476a;
}

.neomorphic-btn {
  background: #e6e7ee;
  box-shadow: 4px 4px 8px #c3c4ca,
              -4px -4px 8px #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.neomorphic-btn:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
}

.neomorphic-btn:active {
  transform: translateY(1px);
  box-shadow: inset 2px 2px 5px #c3c4ca,
              inset -2px -2px 5px #ffffff;
}

.avatar-section {
  text-align: center;
  margin-bottom: 25px;
}

.avatar-uploader {
  width: 90px;
  height: 90px;
  margin: 0 auto;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.neomorphic-upload {
  background: #e6e7ee;
  box-shadow: inset 2px 2px 4px #c3c4ca,
              inset -2px -2px 4px #ffffff;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.avatar-uploader:hover {
  transform: scale(1.05);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-icon {
  font-size: 32px;
  color: #44476a;
}

.upload-hint {
  margin-top: 8px;
  font-size: 13px;
  color: #44476a;
}

.profile-tabs {
  margin-top: 20px;
}

.neomorphic-tabs {
  background: #e6e7ee;
  border-radius: 12px;
  padding: 15px;
  box-shadow: inset 1px 1px 3px #c3c4ca,
              inset -1px -1px 3px #ffffff;
}

.tab-headers {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-btn {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #44476a;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #e6e7ee;
  box-shadow: 2px 2px 4px #c3c4ca,
              -2px -2px 4px #ffffff;
}

.tab-btn:hover {
  transform: translateY(-2px);
}

.tab-btn.active {
  color: #4a6cf7;
  box-shadow: inset 2px 2px 5px #c3c4ca,
              inset -2px -2px 5px #ffffff;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #44476a;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
}

.neomorphic-input {
  background: #e6e7ee;
  border-radius: 8px;
  box-shadow: inset 1px 1px 3px #c3c4ca,
              inset -1px -1px 3px #ffffff;
  padding: 2px;
}

.input-wrapper input {
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  color: #44476a;
  background: #e6e7ee;
  transition: all 0.2s ease;
  outline: none;
}

.input-wrapper input:focus {
  box-shadow: inset 3px 3px 6px #c3c4ca,
              inset -3px -3px 6px #ffffff;
}

.input-wrapper input:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.form-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.submit-btn {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  background: #4a6cf7;
}

/* Dark mode adaptation */
@media (prefers-color-scheme: dark) {
  .profile-dialog-content {
    background: #2d3748;
  }

  .neomorphic-panel {
    box-shadow: 4px 4px 8px #202632,
                -4px -4px 8px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .profile-dialog-header h2 {
    color: #e2e8f0;
  }

  .close-btn {
    color: #e2e8f0;
  }

  .neomorphic-btn {
    background: #2d3748;
    box-shadow: 2px 2px 4px #202632,
                -2px -2px 4px #3a485e;
  }

  .neomorphic-btn:hover {
    box-shadow: 6px 6px 12px #202632,
                -6px -6px 12px #3a485e;
  }

  .neomorphic-btn:active {
    box-shadow: inset 2px 2px 5px #202632,
                inset -2px -2px 5px #3a485e;
  }

  .neomorphic-upload {
    background: #2d3748;
    box-shadow: inset 2px 2px 4px #202632,
                inset -2px -2px 4px #3a485e;
    border: 2px solid rgba(255, 255, 255, 0.1);
  }

  .upload-icon {
    color: #e2e8f0;
  }

  .upload-hint {
    color: #e2e8f0;
  }

  .neomorphic-tabs {
    background: #2d3748;
    box-shadow: inset 1px 1px 3px #202632,
                inset -1px -1px 3px #3a485e;
  }

  .tab-btn {
    background: #2d3748;
    color: #e2e8f0;
    box-shadow: 2px 2px 4px #202632,
                -2px -2px 4px #3a485e;
  }

  .tab-btn.active {
    color: #63b3ed;
    box-shadow: inset 1px 1px 3px #202632,
                inset -1px -1px 3px #3a485e;
  }

  .form-group label {
    color: #e2e8f0;
  }

  .neomorphic-input {
    background: #2d3748;
    box-shadow: inset 1px 1px 3px #202632,
                inset -1px -1px 3px #3a485e;
  }

  .input-wrapper input {
    background: #2d3748;
    color: #e2e8f0;
  }

  .submit-btn {
    background: #4299e1;
  }
}

/* Responsive */
@media (max-width: 768px) {
  .profile-dialog-content {
    width: 95%;
    padding: 20px;
  }

  .tab-headers {
    flex-direction: column;
  }

  .form-actions {
    flex-direction: column;
  }

  .submit-btn {
    width: 100%;
  }
}
</style>