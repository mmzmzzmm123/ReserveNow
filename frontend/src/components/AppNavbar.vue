<template>
  <header class="app-navbar" :class="{ 'scrolled': isScrolled }">
    <div class="navbar-container">
      <!-- Left Logo Area -->
      <div class="navbar-logo" @click="goToHome">
        <div class="logo-container">
          <span class="logo-icon"><el-icon><KnifeFork /></el-icon></span>
          <div class="logo-text">
            <h1 class="logo-title">ReserveNow</h1>
            <span class="logo-tagline">Explore Flavors, Book Experience</span>
          </div>
        </div>
      </div>
      
      <!-- Center Navigation Links -->
      <nav class="navbar-links">
        <ul class="nav-list">
          <li class="nav-item" :class="{ 'active': isActivePath('/') }">
            <router-link to="/" @click="handleNavLinkClick('/')">
              <el-icon><HomeFilled /></el-icon>
              <span>Home</span>
            </router-link>
          </li>
          <li class="nav-item" :class="{ 'active': isActivePath('/explore') }">
            <router-link to="/explore" @click="handleNavLinkClick('/explore')">
              <el-icon><Compass /></el-icon>
              <span>Explore</span>
            </router-link>
          </li>
          <li class="nav-item" :class="{ 'active': isActivePath('/reservations') }">
            <router-link to="/reservations" @click="handleNavLinkClick('/reservations')">
              <el-icon><Calendar /></el-icon>
              <span>My Reservations</span>
            </router-link>
          </li>
          <li class="nav-item" :class="{ 'active': isActivePath('/favorites') }">
            <router-link to="/favorites" @click="handleNavLinkClick('/favorites')">
              <el-icon><Star /></el-icon>
              <span>Favorites</span>
            </router-link>
          </li>
        </ul>
      </nav>
      
      <!-- Right Search and User Area -->
      <div class="navbar-right">
        <!-- User Information -->
        <div class="user-container" v-if="isUserLoggedIn" ref="userDropdownRef">
          <div class="user-info" @click="toggleUserDropdown">
            <div class="user-avatar" :style="userAvatar ? `background-image: url(${userAvatar})` : ''">
              <span v-if="!userAvatar">{{ userInitial }}</span>
            </div>
            <div class="user-meta">
              <span class="user-name">{{ userName }}</span>
              <span class="user-role">{{ userRoleText }}</span>
            </div>
            <div class="dropdown-icon" :class="{ 'active': isUserDropdownOpen }">
              <svg xmlns="http://www.w3.org/2000/svg" width="10" height="6" viewBox="0 0 10 6" fill="none">
                <path d="M1 1L5 5L9 1" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
          
          <div class="user-dropdown" v-if="isUserDropdownOpen" @click.stop>
            <div class="dropdown-user-info">
              <div class="dropdown-user-details">
                <h3>{{ userName }}</h3>
                <p>{{ userInfo.email || 'Email not set' }}</p>
              </div>
            </div>
            
            <div class="dropdown-separator"></div>
            
            <ul class="dropdown-menu">
              <li class="dropdown-item" @click="navigateTo('profile')">
                <div class="item-icon profile-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 12C14.7614 12 17 9.76142 17 7C17 4.23858 14.7614 2 12 2C9.23858 2 7 4.23858 7 7C7 9.76142 9.23858 12 12 12Z" fill="currentColor" fill-opacity="0.2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M3.41421 20.4142C2.52331 19.5233 2 18.2761 2 17V16.5C2 15.0494 2.81582 13.7102 4.10557 12.9933C5.84238 12.0647 8.67996 11 12 11C15.32 11 18.1576 12.0647 19.8944 12.9933C21.1842 13.7102 22 15.0494 22 16.5V17C22 18.2761 21.4767 19.5233 20.5858 20.4142L20 21" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <span>Profile</span>
              </li>
              <li class="dropdown-item" @click="navigateTo('reservations')">
                <div class="item-icon reservations-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="2" y="4" width="20" height="18" rx="2" fill="currentColor" fill-opacity="0.2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M8 2V6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M16 2V6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    <path d="M2 10H22" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M7 14L9 16L12 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <span>My Reservations</span>
              </li>
              <li class="dropdown-item" @click="navigateTo('favorites')">
                <div class="item-icon favorites-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor" fill-opacity="0.2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <span>My Favorites</span>
              </li>
              <div class="dropdown-separator"></div>
              <li class="dropdown-item logout-item" @click="handleLogout">
                <div class="item-icon logout-icon">
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M18 8L22 12M22 12L18 16M22 12H9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M15 4V3C15 2.44772 14.5523 2 14 2H3C2.44772 2 2 2.44772 2 3V21C2 21.5523 2.44772 22 3 22H14C14.5523 22 15 21.5523 15 21V20" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </div>
                <span>Logout</span>
              </li>
            </ul>
          </div>
        </div>
        
        <!-- Login/Register Buttons -->
        <div class="auth-buttons" v-else>
          <el-button class="login-button" @click="goToLogin">Login</el-button>
          <el-button type="primary" class="register-button" @click="goToRegister">Register</el-button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { 
  KnifeFork, HomeFilled, Compass, Calendar, Star, 
  Search, Bell, User, Setting, SwitchButton, 
  Tickets, CaretBottom, ArrowRight
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();

// Search related
const searchKeyword = ref('');
const isSearchActive = ref(false);

// Navbar scroll state
const isScrolled = ref(false);

// Notification related
const unreadCount = ref(0);

// User data
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'));
const userName = ref('Guest');
const userAvatar = ref('');

// Computed properties
const userInitial = computed(() => {
  return userName.value ? userName.value.charAt(0).toUpperCase() : 'G';
});

const userRoleText = computed(() => {
  const roleMap = {
    'customer': 'Customer',
    'owner': 'Owner',
    'admin': 'Admin'
  };
  return roleMap[userInfo.value.role] || 'Customer';
});

const isUserLoggedIn = computed(() => {
  return !!userInfo.value?.token;
});

const interval = ref(null);
const debounceTimer = ref(null);

// Debounce function
const debounce = (fn, delay = 300) => {
  return (...args) => {
    if (debounceTimer.value) clearTimeout(debounceTimer.value);
    debounceTimer.value = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
};

// Determine if current path is active - completely rewrite this function, avoid recursive updates
const isActivePath = (path) => {
  // No longer use timer, directly return path matching result
  return route.path === path || route.path.startsWith(`${path}/`);
};

// Create a separate message prompt function instead of calling it in isActivePath
const showLoginRequiredMessage = debounce(() => {
  if (!isUserLoggedIn.value) {
    ElMessage.error("You need to login to access");
  }
}, 300);

// Handle navigation link click
const handleNavLinkClick = (path) => {
  // If login is required but not logged in, show prompt
  if (!isUserLoggedIn.value && path !== '/') {
    showLoginRequiredMessage();
  }
};

// Handle search
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/search',
      query: { keyword: searchKeyword.value }
    });
  }
};

const handleSearchBlur = () => {
  if (!searchKeyword.value.trim()) {
    isSearchActive.value = false;
  }
};

// Listen for scroll events
const handleScroll = () => {
  isScrolled.value = window.scrollY > 20;
};

// Navigation method
const goToHome = () => {
  router.push('/');
};

const goToLogin = () => {
  router.push('/login');
};

const goToRegister = () => {
  router.push('/login');
};

// User dropdown menu state
const isUserDropdownOpen = ref(false);
const userDropdownRef = ref(null);

// Toggle user dropdown menu
const toggleUserDropdown = () => {
  isUserDropdownOpen.value = !isUserDropdownOpen.value;
};

// Navigate to specified page
const navigateTo = (path) => {
  router.push(`/${path}`);
  isUserDropdownOpen.value = false;
};

// Handle logout
const handleLogout = () => {
  // Clear user data
  localStorage.removeItem('userInfo');
  localStorage.removeItem('token');
  // Clear token from cookies
  document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
  
  // Reset user information
  userInfo.value = {};
  userName.value = 'Guest';
  userAvatar.value = '';
  
  ElMessage.success('Logout successful');
  isUserDropdownOpen.value = false;
  
  // Navigate to home page
  router.push('/');
};

// Click outside to close dropdown menu
const handleClickOutside = (event) => {
  if (userDropdownRef.value && !userDropdownRef.value.contains(event.target)) {
    isUserDropdownOpen.value = false;
  }
};

// Component mounted get user information
onMounted(() => {
  // Get user info from local storage
  const storedUserInfo = localStorage.getItem('userInfo');
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo);
    userName.value = userInfo.value.name || 'Guest';
    userAvatar.value = userInfo.value.avatar || '';
    
    // Simulate unread notifications count
    unreadCount.value = Math.floor(Math.random() * 5);
  }
  
  // Add scroll listener
  window.addEventListener('scroll', handleScroll);
  
  // Add click outside listener
  document.addEventListener('click', handleClickOutside);
});

// Component unmount clear listeners
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  document.removeEventListener('click', handleClickOutside);
  
  // Clear all timers
  if (interval.value) {
    clearInterval(interval.value);
    interval.value = null;
  }
  
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value);
    debounceTimer.value = null;
  }
});

// Watch local storage changes
watch(
  () => localStorage.getItem('userInfo'),
  (newValue) => {
    if (newValue) {
      userInfo.value = JSON.parse(newValue);
      userName.value = userInfo.value.name || 'Guest';
      userAvatar.value = userInfo.value.avatar || '';
    } else {
      userInfo.value = {};
      userName.value = 'Guest';
      userAvatar.value = '';
    }
  }
);
</script>

<style lang="scss" scoped>
// Variables
$primary-color: #ff4757;
$secondary-color: #f8f1ff;
$accent-color: #6c5ce7;
$dark-color: #2d3436;
$light-color: #f9f9f9;
$gray-color: #636e72;
$border-radius: 12px;
$transition-speed: 0.25s;
$box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
$gradient-primary: linear-gradient(135deg, #ff4757, #ff7675);
$gradient-secondary: linear-gradient(135deg, #6c5ce7, #a29bfe);
$blur-effect: blur(10px);

// Navbar global styles
.app-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: $blur-effect;
  -webkit-backdrop-filter: $blur-effect;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
  height: 70px;
  transition: all $transition-speed ease;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: $gradient-primary;
    opacity: 0.85;
  }
  
  &::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 0;
    right: 0;
    height: 10px;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.05), transparent);
    pointer-events: none;
  }
  
  &.scrolled {
    height: 60px;
    background-color: rgba(255, 255, 255, 0.98);
    box-shadow: 0 5px 30px rgba(0, 0, 0, 0.12);
    
    .navbar-container {
      .navbar-logo {
        .logo-container {
          transform: scale(0.95);
          
          .logo-text {
            .logo-title {
              font-size: 20px;
            }
            
            .logo-tagline {
              font-size: 11px;
            }
          }
        }
      }
      
      .navbar-links {
        .nav-list {
          .nav-item {
            a {
              font-size: 14px;
              padding: 8px 16px;
            }
          }
        }
      }
    }
  }
  
  .navbar-container {
    max-width: 1440px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 30px;
  }
  
  // Logo styles
  .navbar-logo {
    cursor: pointer;
    
    .logo-container {
      display: flex;
      align-items: center;
      gap: 12px;
      transition: all $transition-speed;
      
      .logo-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 42px;
        height: 42px;
        background: $secondary-color;
        border-radius: 14px;
        font-size: 22px;
        color: $primary-color;
        box-shadow: 0 5px 15px rgba($primary-color, 0.2);
        transition: all $transition-speed;
        transform-origin: center;
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: -10px;
          left: -10px;
          right: -10px;
          bottom: -10px;
          background: radial-gradient(circle at center, rgba($primary-color, 0.2) 0%, rgba($primary-color, 0) 70%);
          opacity: 0;
          transition: opacity $transition-speed;
        }
      }
      
      .logo-text {
        display: flex;
        flex-direction: column;
        transition: all $transition-speed;
        
        .logo-title {
          margin: 0;
          font-size: 22px;
          font-weight: 800;
          line-height: 1.2;
          background: $gradient-primary;
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          letter-spacing: 0.5px;
          text-shadow: 0 2px 10px rgba($primary-color, 0.1);
          transition: all $transition-speed;
        }
        
        .logo-tagline {
          font-size: 12px;
          color: $gray-color;
          letter-spacing: 0.5px;
          opacity: 0.9;
          font-weight: 500;
          transition: all $transition-speed;
        }
      }
    }
    
    &:hover {
      .logo-icon {
        transform: scale(1.05) rotate(5deg);
        box-shadow: 0 8px 20px rgba($primary-color, 0.3);
        
        &::before {
          opacity: 1;
        }
      }
      
      .logo-text {
        .logo-title {
          text-shadow: 0 2px 15px rgba($primary-color, 0.2);
        }
      }
    }
  }
  
  // Navigation link styles
  .navbar-links {
    margin: 0 20px;
    
    .nav-list {
      display: flex;
      list-style: none;
      margin: 0;
      padding: 0;
      gap: 12px;
      
      .nav-item {
        position: relative;
        
        a {
          display: flex;
          align-items: center;
          padding: 9px 18px;
          color: $dark-color;
          text-decoration: none;
          font-weight: 500;
          font-size: 15px;
          border-radius: 25px;
          transition: all $transition-speed;
          gap: 8px;
          position: relative;
          overflow: hidden;
          
          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: linear-gradient(to right, rgba($primary-color, 0.1), rgba($primary-color, 0));
            opacity: 0;
            transition: opacity $transition-speed;
            z-index: -1;
            border-radius: 25px;
          }
          
          .el-icon {
            font-size: 16px;
            transition: all $transition-speed;
          }
          
          &:hover {
            color: $primary-color;
            transform: translateY(-1px);
            
            &::before {
              opacity: 1;
            }
            
            .el-icon {
              transform: translateY(-2px) scale(1.1);
              color: $primary-color;
            }
          }
        }
        
        &.active {
          a {
            color: $primary-color;
            background-color: $secondary-color;
            font-weight: 600;
            box-shadow: 0 5px 15px rgba($primary-color, 0.15);
            
            .el-icon {
              transform: translateY(-2px) scale(1.1);
              color: $primary-color;
            }
            
            &::after {
              content: '';
              position: absolute;
              bottom: 5px;
              left: 50%;
              transform: translateX(-50%);
              width: 4px;
              height: 4px;
              border-radius: 50%;
              background-color: $primary-color;
            }
          }
        }
      }
    }
  }
  
  // Right area styles
  .navbar-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    // Search box styles
    .search-container {
      position: relative;
      display: flex;
      align-items: center;
      width: 220px;
      transition: width $transition-speed cubic-bezier(0.175, 0.885, 0.32, 1.275);
      
      &.search-active {
        width: 320px;
        
        .search-button {
          opacity: 1;
          transform: translateX(0);
          pointer-events: auto;
        }
      }
      
      .search-input {
        width: 100%;
        transition: all $transition-speed;
        
        :deep(.el-input__wrapper) {
          border-radius: 25px;
          padding-left: 14px;
          padding-right: 10px;
          background-color: #f5f7fa;
          box-shadow: none;
          border: 1px solid transparent;
          transition: all $transition-speed;
          
          &:hover {
            background-color: white;
            border-color: #e0e0e0;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
          }
          
          &.is-focus {
            background-color: white;
            border-color: rgba($primary-color, 0.3);
            box-shadow: 0 5px 20px rgba($primary-color, 0.1);
          }
        }
        
        .search-icon {
          color: $gray-color;
        }
      }
      
      .search-button {
        position: absolute;
        right: 5px;
        height: 34px;
        padding: 0 16px;
        border-radius: 17px;
        background: $gradient-primary;
        border: none;
        color: white;
        font-size: 13px;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 6px;
        opacity: 0;
        transform: translateX(10px);
        transition: all $transition-speed;
        pointer-events: none;
        box-shadow: 0 4px 10px rgba($primary-color, 0.2);
        
        &.btn-hidden {
          display: none;
        }
        
        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 6px 15px rgba($primary-color, 0.3);
        }
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
    
    // Notification button styles
    .notification-button {
      position: relative;
      
      .icon-button {
        width: 40px;
        height: 40px;
        background-color: #f5f7fa;
        border: none;
        border-radius: 50%;
        transition: all $transition-speed;
        color: $dark-color;
        
        &:hover {
          background-color: $secondary-color;
          color: $primary-color;
          transform: translateY(-2px);
          box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
        }
        
        .el-icon {
          font-size: 18px;
        }
      }
      
      :deep(.notification-badge) {
        .el-badge__content {
          background-color: $primary-color;
          box-shadow: 0 0 0 2px white;
        }
      }
    }
    
    // User information styles
    .user-container {
      position: relative;
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 10px;
        cursor: pointer;
        padding: 4px 12px 4px 4px;
        border-radius: 40px;
        transition: all $transition-speed;
        border: 1px solid transparent;
        
        &:hover {
          background-color: #f5f7fa;
          border-color: #eee;
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
          
          .dropdown-icon {
            transform: translateY(2px);
          }
          
          .user-avatar {
            transform: scale(1.05);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
          }
        }
        
        .user-avatar {
          border: 2px solid white;
          box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
          width: 38px;
          height: 38px;
          background-color: $secondary-color;
          color: $primary-color;
          font-weight: 600;
          transition: all $transition-speed;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          background-size: cover;
          background-position: center;
          position: relative;
          overflow: hidden;
          
          &::after {
            content: '';
            position: absolute;
            inset: 0;
            border-radius: 50%;
            box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.2);
            background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
          }
          
          span {
            font-size: 16px;
            font-weight: 700;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
          }
        }
        
        .user-meta {
          display: flex;
          flex-direction: column;
          line-height: 1.1;
          
          .user-name {
            font-weight: 600;
            color: $dark-color;
            font-size: 14px;
          }
          
          .user-role {
            font-size: 12px;
            color: $gray-color;
          }
        }
        
        .dropdown-icon {
          color: $gray-color;
          transition: transform $transition-speed;
          display: flex;
          align-items: center;
          justify-content: center;
          
          &.active {
            transform: rotate(180deg);
          }
        }
      }
      
      // User dropdown menu styles - re-designed native version
      .user-dropdown {
        position: absolute;
        top: calc(100% + 15px);
        right: 0;
        width: 300px;
        background: white;
        border-radius: 20px;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.18);
        z-index: 1000;
        overflow: hidden;
        animation: dropdownFadeIn 0.3s cubic-bezier(0.68, -0.55, 0.27, 1.55);
        border: 1px solid rgba(0, 0, 0, 0.05);
        transform-origin: top right;
        
        &::before {
          content: '';
          position: absolute;
          top: -8px;
          right: 20px;
          width: 16px;
          height: 16px;
          background: white;
          transform: rotate(45deg);
          border-left: 1px solid rgba(0, 0, 0, 0.05);
          border-top: 1px solid rgba(0, 0, 0, 0.05);
        }
        
        .dropdown-user-info {
          background: linear-gradient(135deg, #f8f9fa, #f0f0f0);
          padding: 24px;
          display: flex;
          gap: 18px;
          align-items: center;
          position: relative;
          overflow: hidden;
          
          &::before {
            content: '';
            position: absolute;
            top: -20px;
            right: -20px;
            width: 120px;
            height: 120px;
            background: radial-gradient(circle at center, rgba($primary-color, 0.1) 0%, rgba($primary-color, 0) 70%);
            border-radius: 50%;
          }
          
          .user-avatar-large {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            background-color: $secondary-color;
            color: $primary-color;
            font-weight: 600;
            font-size: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 3px solid white;
            box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
            background-size: cover;
            background-position: center;
            position: relative;
            overflow: hidden;
            
            &::after {
              content: '';
              position: absolute;
              inset: 0;
              border-radius: 50%;
              box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.3);
              background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.2), transparent);
            }
            
            span {
              font-size: 28px;
              font-weight: 700;
              text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
            }
          }
          
          .dropdown-user-details {
            h3 {
              margin: 0 0 6px;
              font-size: 17px;
              color: $dark-color;
              font-weight: 600;
              letter-spacing: 0.2px;
            }
            
            p {
              margin: 0;
              font-size: 13px;
              color: $gray-color;
              display: flex;
              align-items: center;
              gap: 5px;
              
              &::before {
                content: '✉️';
                font-size: 12px;
              }
            }
          }
        }
        
        .dropdown-separator {
          height: 1px;
          background: linear-gradient(to right, rgba(0, 0, 0, 0.05), rgba(0, 0, 0, 0.08), rgba(0, 0, 0, 0.05));
          margin: 6px 0;
        }
        
        .dropdown-menu {
          list-style: none;
          padding: 8px 0;
          margin: 0;
          
          .dropdown-item {
            padding: 12px 20px;
            display: flex;
            align-items: center;
            gap: 15px;
            cursor: pointer;
            transition: all 0.2s;
            position: relative;
            overflow: hidden;
            
            &::before {
              content: '';
              position: absolute;
              left: 0;
              top: 0;
              height: 100%;
              width: 3px;
              background: $gradient-primary;
              transform: scaleY(0);
              transition: transform 0.2s;
            }
            
            &:hover {
              background-color: $secondary-color;
              
              &::before {
                transform: scaleY(1);
              }
              
              .item-icon {
                transform: translateY(-2px) scale(1.1);
                color: $primary-color;
                box-shadow: 0 5px 15px rgba($primary-color, 0.15);
              }
              
              span {
                color: $primary-color;
                transform: translateX(3px);
              }
            }
            
            &:active {
              background-color: rgba($primary-color, 0.1);
            }
            
            .item-icon {
              width: 36px;
              height: 36px;
              border-radius: 12px;
              background-color: rgba($primary-color, 0.05);
              display: flex;
              align-items: center;
              justify-content: center;
              color: $dark-color;
              transition: all 0.25s;
              
              svg {
                width: 20px;
                height: 20px;
              }
              
              &.profile-icon {
                background-color: rgba(#4e89fd, 0.05);
                color: #4e89fd;
              }
              
              &.reservations-icon {
                background-color: rgba(#7e6cf3, 0.05);
                color: #7e6cf3;
              }
              
              &.favorites-icon {
                background-color: rgba(#f39c6c, 0.05);
                color: #f39c6c;
              }
            }
            
            span {
              font-size: 14px;
              color: $dark-color;
              font-weight: 500;
              transition: all 0.25s;
            }
            
            &.logout-item {
              color: #e23c3c;
              
              .item-icon {
                background-color: rgba(#e23c3c, 0.05);
                color: #e23c3c;
              }
              
              span {
                color: #e23c3c;
              }
              
              &:hover {
                background-color: #fff2f2;
                
                &::before {
                  background: linear-gradient(to bottom, #ff9a9e, #e23c3c);
                }
              }
            }
          }
        }
      }
    }
    
    // Login/Register button styles
    .auth-buttons {
      display: flex;
      gap: 12px;
      
      .login-button, .register-button {
        height: 40px;
        border-radius: 20px;
        font-weight: 600;
        transition: all $transition-speed;
        padding: 0 20px;
      }
      
      .login-button {
        color: $dark-color;
        border: 1px solid transparent;
        
        &:hover {
          background-color: $light-color;
          color: $primary-color;
          border-color: #eee;
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
        }
      }
      
      .register-button {
        background: $gradient-primary;
        border: none;
        box-shadow: 0 5px 15px rgba($primary-color, 0.2);
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 20px rgba($primary-color, 0.3);
        }
      }
    }
  }
}

// Animation
@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

// Responsive design
@media (max-width: 991px) {
  .app-navbar {
    .navbar-container {
      padding: 0 20px;
    }
    
    .navbar-links {
      display: none; // Hide navigation links on small screens
    }
    
    .navbar-right {
      .search-container {
        width: 180px;
        
        &.search-active {
          width: 240px;
        }
      }
    }
  }
}

@media (max-width: 767px) {
  .app-navbar {
    .navbar-right {
      .search-container {
        position: static;
        width: 40px;
        overflow: hidden;
        
        &.search-active {
          width: 100%;
          position: absolute;
          top: 70px;
          left: 0;
          right: 0;
          padding: 12px 20px;
          background: white;
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
          z-index: 1000;
        }
        
        .search-input {
          :deep(.el-input__wrapper) {
            padding: 0;
            background: transparent;
            
            .el-input__inner {
              display: none;
            }
          }
          
          &.search-active {
            :deep(.el-input__wrapper) {
              padding: 0 10px;
              background: #f5f7fa;
              
              .el-input__inner {
                display: block;
              }
            }
          }
        }
        
        .search-button {
          display: none;
        }
      }
      
      .user-container {
        .user-info {
          .user-meta {
            display: none;
          }
          
          padding: 4px;
        }
      }
      
      .auth-buttons {
        .login-button {
          span {
            display: none;
          }
          
          .el-icon {
            margin: 0;
          }
        }
      }
    }
  }
}
</style> 