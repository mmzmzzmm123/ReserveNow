<template>
  <div class="home-container">
    <!-- Top Navigation Bar -->
    <AppNavbar />

    <!-- Banner Section -->
    <section class="banner">
      <div class="banner-overlay"></div>
      <div class="banner-content">
        <h2>Discover and Book the Best Restaurants</h2>
        <p>Browse our carefully curated restaurants and book your table with just a few clicks</p>
        <div class="banner-actions">
          <el-button type="primary" size="large" class="action-btn explore-btn" @click="goToBottom">
            <el-icon><Compass /></el-icon> Restaurants
          </el-button>
          <el-button size="large" class="action-btn learn-btn" @click="router.push('/explore')">
            <el-icon><QuestionFilled /></el-icon> About Us
          </el-button>
        </div>
      </div>
      <div class="banner-decoration banner-decoration-1"></div>
      <div class="banner-decoration banner-decoration-2"></div>
      <div class="banner-decoration banner-decoration-3"></div>
    </section>

    <!-- Filters Section -->
    <section class="filters-section">
      <div class="filters-container">
        <div class="filter-group">
          <h3 class="filter-title">Restaurant Status</h3>
          <el-radio-group v-model="selectedStatus" @change="handleFilterChange" size="large">
            <el-radio-button value="">All Restaurants</el-radio-button>
            <el-radio-button value="0">Upcoming</el-radio-button>
            <el-radio-button value="3">Operating</el-radio-button>
          </el-radio-group>
        </div>
        
        <div class="sort-options">
          <el-select v-model="sortOption" placeholder="Sort By" @change="handleSortChange">
            <el-option label="Rating (High to Low)" value="ratingDesc" />
            <el-option label="Rating (Low to High)" value="ratingAsc" />
            <el-option label="Review Count (High to Low)" value="reviewsDesc" />
            <el-option label="Name (A-Z)" value="nameAsc" />
          </el-select>
        </div>
      </div>
      
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="Search by restaurant name, address or description..."
          class="search-input"
          @keyup.enter="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">Search</el-button>
          </template>
        </el-input>
        
        <div class="active-filters" v-if="searchKeyword || selectedStatus !== ''">
          <span class="filter-label">Active Filters:</span>
          <el-tag 
            v-if="searchKeyword" 
            closable 
            @close="searchKeyword = ''; handleSearch()"
            size="small"
            class="filter-tag"
          >
            Keyword: {{ searchKeyword }}
          </el-tag>
          <el-tag 
            v-if="selectedStatus" 
            closable 
            @close="selectedStatus = ''; handleFilterChange()"
            size="small"
            class="filter-tag"
          >
            Status: {{ statusMap[selectedStatus] || selectedStatus }}
          </el-tag>
          <el-button type="text" @click="resetFilters" class="clear-all-btn">
            Clear All Filters
          </el-button>
        </div>
      </div>
    </section>

    <!-- Restaurant List -->
    <section class="restaurant-list">
      <el-row :gutter="30" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="restaurant in restaurants" :key="restaurant.id">
          <div class="restaurant-card-wrapper">
            <el-card class="restaurant-card" shadow="hover" @click="viewRestaurantDetails(restaurant.id)">
              <div class="restaurant-image">
                <img :src="restaurant.photos[0]" :alt="restaurant.name" onerror="this.src='https://via.placeholder.com/300x200?text=暂无图片'" />
                <div class="rating-badge">
                  <span>{{ restaurant.rating }}</span>
                  <el-rate
                    v-model="restaurant.rating"
                    disabled
                    text-color="#ff9900"
                    score-template="{value}"
                  />
                  <span class="review-count">({{ restaurant.reviewCount }})</span>
                </div>
                <div class="restaurant-tags">
                  <el-tag v-if="restaurant.isNew" size="small" effect="plain" class="tag new-tag">New</el-tag>
                  <el-tag v-if="restaurant.rating >= 4.5" size="small" effect="plain" class="tag popular-tag">Popular</el-tag>
                  <el-tag v-if="restaurant.cuisine" size="small" effect="plain" class="tag cuisine-badge">{{ restaurant.cuisine }}</el-tag>
                </div>
              </div>
              <div class="restaurant-info">
                <h3>{{ restaurant.name }}</h3>
                <div class="cuisine-tags">
                  <span class="cuisine-tag" v-for="(cuisine, idx) in getCuisineTypes(restaurant)" :key="idx">{{ cuisine }}</span>
                </div>
                <p class="address">
                  <el-icon><Location /></el-icon> {{ restaurant.address }}
                </p>
                <p class="description">{{ restaurant.description }}</p>
                <div class="attributes">
                  <div class="attribute" v-if="restaurant.businessHours">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatBusinessHours(restaurant.businessHours) }}</span>
                  </div>
                  <div class="attribute" v-if="restaurant.cuisine">
                    <el-icon><Food /></el-icon>
                    <span>{{ restaurant.cuisine }}</span>
                  </div>
                </div>
                <div class="restaurant-footer">
                  <el-button type="primary" class="reserve-btn" @click.stop="handleReservation(restaurant, $event)">Reserve Now</el-button>
                  <div class="like-button" @click.stop="toggleFavorite(restaurant, $event)">
                    <el-icon :class="{ 'is-active': restaurant.isFavorite }"><Star /></el-icon>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>

      <!-- Infinite Scroll Loader -->
      <div v-if="!noMoreData" class="infinite-scroll-loader" v-intersect="loadMoreRestaurants">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>Loading more restaurants...</span>
      </div>
      
      <!-- No More Data Message -->
      <div v-if="noMoreData && restaurants.length > 0" class="no-more-data">
        <el-divider>All Restaurants Displayed</el-divider>
      </div>

      <!-- No Results Message -->
      <div v-if="noMoreData && restaurants.length === 0 && !loading" class="no-results">
        <el-empty description="No restaurants found matching your criteria" />
        <el-button @click="resetFilters">Reset Filters</el-button>
      </div>
    </section>

    <!-- Footer -->
    <AppFooter />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { 
  Search, Location, KnifeFork, Clock, Loading, ArrowDown,
  Bell, ChatLineSquare, User, Calendar, Setting, SwitchButton,
  Compass, QuestionFilled, Star, Food
} from '@element-plus/icons-vue';
import request from '@/utils/request';
import AppFooter from '@/components/AppFooter.vue';
import AppNavbar from '@/components/AppNavbar.vue';

const router = useRouter();

// User data
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'));
const userName = ref('Guest');
const userAvatar = ref('');
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

// Search and filters
const searchKeyword = ref('');
const selectedStatus = ref('3'); 
const sortOption = ref('ratingDesc'); 

// Restaurant list data
const restaurants = ref([]);
const currentPage = ref(1);
const pageSize = ref(8);
const loading = ref(false);
const noMoreData = ref(false);
const total = ref(0);

// Load restaurants on component mount
onMounted(() => {
  const storedUserInfo = localStorage.getItem('userInfo');
  if (storedUserInfo) {
    userInfo.value = JSON.parse(storedUserInfo);
    userName.value = userInfo.value.name || 'Guest';
    userAvatar.value = userInfo.value.avatar || '';
  }

  fetchRestaurants();
});

// Fetch restaurants from API
const fetchRestaurants = async (reset = false) => {
  if (reset) {
    const row = document.querySelector('.restaurant-list .el-row');
    if (row) {
      row.style.opacity = '0.4';
    }
    setTimeout(() => {
      restaurants.value = [];
      currentPage.value = 1;
      noMoreData.value = false;
      
      setTimeout(() => {
        loading.value = true;
        loadNewData();
      }, 50);
    }, 200);
  } else {
    if (loading.value || noMoreData.value) return;
    loading.value = true;
    loadNewData();
  }
  
  async function loadNewData() {
    try {
      const params = {
        page: currentPage.value,
        pageSize: pageSize.value
      };
      
      if (selectedStatus.value) {
        params.status = selectedStatus.value;
      }
      
      if (searchKeyword.value) {
        params.keyword = searchKeyword.value;
      }
      
      const res = await request({
        url: '/restaurants',
        method: 'GET',
        params
      });
      
      const newRestaurants = res.data?.list || [];
      total.value = res.data?.total || 0;
      
      const processedRestaurants = newRestaurants.map(restaurant => {
        let statusCode = restaurant.status;
        if (typeof restaurant.status === 'string') {
          const statusKeys = Object.keys(statusMap);
          for (const key of statusKeys) {
            if (statusMap[key] === restaurant.status) {
              statusCode = parseInt(key);
              break;
            }
          }
        }
        
        return {
          ...restaurant,
          status: statusCode,
          isNew: statusCode === 1 || new Date(restaurant.createdAt) > new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
          isPromoted: Math.random() > 0.7, 
        };
      });
      
      if (reset) {
        restaurants.value = processedRestaurants;
      } else {
        restaurants.value = [...restaurants.value, ...processedRestaurants];
      }
      
      setTimeout(() => {
        const row = document.querySelector('.restaurant-list .el-row');
        if (row) {
          row.style.opacity = '1';
        }
      }, 100);
      
      if (restaurants.value.length >= total.value) {
        noMoreData.value = true;
      } else {
        currentPage.value += 1;
      }
      
    } catch (error) {
      ElMessage.error('Failed to load restaurant information, please try again later');
    } finally {
      setTimeout(() => {
        loading.value = false;
      }, 300);
    }
  }
};

// Load more restaurants when scrolling
const loadMoreRestaurants = (entries) => {
  const entry = entries[0];
  if (entry && entry.isIntersecting && !loading.value && !noMoreData.value) {
    fetchRestaurants();
  }
};

// Handle search
const handleSearch = () => {
  fetchRestaurants(true);
};

const goToBottom = () => {
  window.scrollTo({
    top: 500,
    behavior: 'smooth'
  });
};

// Handle filter change
const handleFilterChange = () => {
  fetchRestaurants(true);
};

// Handle sort change
const handleSortChange = () => {
  // Client-side sorting since the API doesn't support sorting
  if (sortOption.value === 'ratingDesc') {
    restaurants.value.sort((a, b) => b.rating - a.rating);
  } else if (sortOption.value === 'ratingAsc') {
    restaurants.value.sort((a, b) => a.rating - b.rating);
  } else if (sortOption.value === 'reviewsDesc') {
    restaurants.value.sort((a, b) => b.reviewCount - a.reviewCount);
  } else if (sortOption.value === 'nameAsc') {
    restaurants.value.sort((a, b) => a.name.localeCompare(b.name));
  }
};

// Reset filters
const resetFilters = () => {
  searchKeyword.value = '';
  selectedStatus.value = '3';
  sortOption.value = 'ratingDesc';
  fetchRestaurants(true);
};

// View restaurant details
const viewRestaurantDetails = (id) => {
  router.push(`/restaurant/${id}`);
};

// User dropdown menu handler
const handleCommand = (command) => {
  if (command === 'logout') {
    // Clear user data
    localStorage.removeItem('userInfo');
    localStorage.removeItem('savedCredentials');
    document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    
    // Reset user information
    userInfo.value = {};
    userName.value = 'Guest';
    userAvatar.value = '';
    
    ElMessage.success('Logout successful');
    router.push('/');
  } else if (command === 'profile') {
    // Navigate to profile page
    router.push('/profile');
  } else if (command === 'orders') {
    // Navigate to orders page
    router.push('/reservations');
  } else if (command === 'settings') {
    // Navigate to settings page
    router.push('/settings');
  }
};

// Helper functions
const truncateText = (text, length) => {
  if (!text) return '';
  return text;
};

const formatBusinessHours = (hours) => {
  if (!hours) return 'No business hours available';
  // Split by | for different day groups
  return hours.split('|')[0]; // Show only the first group for simplicity
};

const getCuisineTypes = (restaurant) => {
  if (!restaurant.cuisine) return ['Chinese Cuisine'];
  return restaurant.cuisine.split(',').slice(0, 2);
};

// Status mapping
const statusMap = {
  '0': 'Pending Review',
  '3': 'Operating'
};

// Handle restaurant reservation
const handleReservation = (restaurant, event) => {
  event.stopPropagation(); // Prevent event bubbling, avoid triggering card click
  
  if (!isUserLoggedIn.value) {
    ElMessage.warning('Please login first to make a reservation');
    return;
  }
  
  // Navigate to restaurant details page and show reservation form
  router.push({
    path: `/restaurant/${restaurant.id}`,
    query: { action: 'reserve' }
  });
};

// Handle favorite or unfavorite
const toggleFavorite = async (restaurant, event) => {
  if (!isUserLoggedIn.value) {
    ElMessage.warning('Please login first to favorite restaurants');
    return;
  }
  
  try {
    // Show loading state
    const likeButton = event.currentTarget;
    const icon = likeButton.querySelector('.el-icon');
    if (icon) icon.classList.add('is-loading');
    
    if (restaurant.isFavorite) {
      // Unfavorite
      await request({
        url: `/favorites/${restaurant.id}`,
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${userInfo.value.token}`
        }
      });
      
      ElMessage.success('Removed from favorites');
      // Update UI state
      restaurant.isFavorite = false;
    } else {
      // Add to favorites
      const res = await request({
        url: '/favorites',
        method: 'POST',
        headers: {
          Authorization: `Bearer ${userInfo.value.token}`
        },
        data: {
          restaurantId: restaurant.id
        }
      });
      
      ElMessage.success('Added to favorites');
      // Update UI state
      restaurant.isFavorite = true;
    }
    
    // Add animation effect
    if (icon) {
      icon.classList.remove('is-loading');
      icon.classList.add('animate-heart');
      setTimeout(() => {
        icon.classList.remove('animate-heart');
      }, 600);
    }
  } catch (error) {
    // Restore original state on error
    restaurant.isFavorite = !restaurant.isFavorite;
    ElMessage.error(error.response?.data?.message || 'Operation failed, please try again later');
    console.error('Favorite operation failed', error);
  }
};
</script>

<style lang="scss" scoped>
// Variables
$primary-color: #e02020; // Electric red
$secondary-color: #fff0f0;
$border-radius: 8px;
$box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
$gradient-primary: linear-gradient(135deg, $primary-color, #ff6b6b);
$accent-color: #6c5ce7; 

.home-container {
  min-height: 100vh;
  background-color: #f8f9fa;
}


// Banner - 更新颜色
.banner {
  position: relative;
  background-color: #f8f1ff;
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(255, 71, 87, 0.05) 0%, transparent 30%),
    radial-gradient(circle at 90% 80%, rgba(108, 92, 231, 0.05) 0%, transparent 30%);
  padding: 80px 0 100px;
  text-align: center;
  margin-bottom: 50px;
  overflow: hidden;
  margin-top: 70px;
  
  &::after {
    content: '';
    position: absolute;
    top: -150%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: linear-gradient(to right, rgba(255,255,255,0) 0%, rgba(255,255,255,0.4) 50%, rgba(255,255,255,0) 100%);
    transform: rotate(30deg);
    animation: headerShine 6s infinite linear;
    pointer-events: none;
  }
  
  .banner-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: transparent;
    z-index: 2;
  }
  
  .banner-content {
    position: relative;
    max-width: 900px;
    margin: 0 auto;
    padding: 0 20px;
    z-index: 3;
    
    h2 {
      font-size: 46px;
      font-weight: 800;
      margin-bottom: 20px;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      animation: fadeInUp 0.8s ease-out forwards;
      background: linear-gradient(135deg, #2d3436, #ff4757);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    
    p {
      font-size: 20px;
      max-width: 600px;
      margin: 0 auto 35px;
      line-height: 1.6;
      animation: fadeInUp 0.8s 0.2s ease-out forwards;
      color: #636e72;
    }
    
    .banner-actions {
      display: flex;
      justify-content: center;
      gap: 15px;
      animation: fadeInUp 0.8s 0.4s ease-out forwards;
      
      .action-btn {
        font-weight: 500;
        height: 50px;
        padding: 0 30px;
        border-radius: 25px;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        transition: all 0.3s;
        
        .el-icon {
          font-size: 18px;
        }
      }
      
      .explore-btn {
        background-color: white;
        color: $primary-color;
        border: none;
        
        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
        }
        
        &:active {
          transform: translateY(-1px);
        }
      }
      
      .learn-btn {
        background-color: rgb(51 234 188 / 70%);
        color: white;
        border: 1px solid rgba(255, 255, 255, 0.3);
        
        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
        }
        
        &:active {
          transform: translateY(-1px);
        }
      }
    }
  }
  
  .banner-decoration {
    position: absolute;
    z-index: 3;
    border-radius: 50%;
    
    &-1 {
      width: 300px;
      height: 300px;
      background: radial-gradient(rgba(255, 71, 87, 0.1) 0%, rgba(255, 71, 87, 0) 70%);
      bottom: -150px;
      left: 10%;
      animation: float 15s ease-in-out infinite;
    }
    
    &-2 {
      width: 200px;
      height: 200px;
      right: 15%;
      animation: float 12s ease-in-out infinite;
      animation-delay: 2s;
    }
    
    &-3 {
      width: 150px;
      height: 150px;
      background: radial-gradient(rgba(255, 71, 87, 0.1) 0%, rgba(255, 71, 87, 0) 70%);
      top: 30%;
      left: 20%;
      animation: float 10s ease-in-out infinite;
      animation-delay: 1s;
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
  100% {
    transform: translateY(0);
  }
}

@keyframes headerShine {
  0% { transform: translateX(-100%) rotate(30deg); }
  100% { transform: translateX(200%) rotate(30deg); }
}

// Filters Section
.filters-section {
  margin-bottom: 40px;
  padding: 0 40px;
  
  .filters-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .filter-group {
      .filter-title {
        font-size: 16px;
        color: #333;
        margin-bottom: 12px;
        font-weight: 600;
      }
    }
    
    :deep(.el-radio-group) {
      .el-radio-button__inner {
        border-color: #dcdfe6;
        color: #606266;
        
        &:hover {
          color: $primary-color;
        }
      }
      
      .el-radio-button__original-radio:checked + .el-radio-button__inner {
        background-color: $primary-color;
        border-color: $primary-color;
        box-shadow: -1px 0 0 0 $primary-color;
        color: white;
      }
    }
    
    .sort-options {
      :deep(.el-select) {
        min-width: 180px;
        
        .el-input__wrapper {
          &.is-focus {
            box-shadow: 0 0 0 1px $primary-color inset;
          }
        }
      }
    }
  }
  
  .search-box {
    margin-bottom: 15px;
    
    .search-input {
      max-width: 600px;
      --el-input-focus-border-color: #{$primary-color};
      
      :deep(.el-input__wrapper) {
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04), 0 0 0 1px rgba(0, 0, 0, 0.05) inset;
        
        &:hover {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.08) inset;
        }
        
        &.is-focus {
          box-shadow: 0 2px 8px rgba($primary-color, 0.1), 0 0 0 1px rgba($primary-color, 0.2) inset;
        }
      }
      
      :deep(.el-input-group__append) {
        .el-button {
          background: $gradient-primary;
          border: none;
          color: white;
          font-weight: 500;
          padding: 0 20px;
          
          &:hover {
            background: linear-gradient(135deg, darken($primary-color, 5%), #ff5252);
            transform: translateY(-1px);
            box-shadow: 0 4px 8px rgba($primary-color, 0.3);
          }
          
          &:active {
            transform: translateY(0);
          }
        }
      }
    }
    
    .active-filters {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 10px;
      
      .filter-label {
        color: #606266;
        font-size: 14px;
      }
      
      .filter-tag {
        background-color: #f0f2f5;
        border-color: #e4e7ed;
        color: #606266;
      }
      
      .clear-all-btn {
        color: $primary-color;
        padding: 0;
        height: auto;
        font-size: 14px;
        margin-left: 10px;
        
        &:hover {
          color: darken($primary-color, 10%);
          text-decoration: underline;
        }
      }
    }
  }
}

// Restaurant List
.restaurant-list {
  padding: 30px 40px 80px;
  min-height: 500px; /* 确保列表区域有足够高度 */
  
  .el-row {
    transition: opacity 0.3s ease; /* 添加行容器的过渡效果 */
    min-height: 300px; /* 保持最小高度 */
  }
  
  :deep(.el-loading-mask) {
    background-color: rgba(255, 255, 255, 0.8); /* 半透明背景 */
    transition: opacity 0.3s ease; /* 平滑过渡 */
  }
  
  .restaurant-card-wrapper {
    margin-bottom: 35px;
    transform: perspective(1000px);
    transition: transform 0.4s, opacity 0.3s ease; /* 添加透明度过渡 */
    opacity: 1; /* 默认不透明 */
    
    &:hover {
      transform: perspective(1000px) translateY(-10px);
    }
  }
  
  .restaurant-card {
    position: relative;
    border-radius: 16px;
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
    height: 100%;
    cursor: pointer;
    border: none;
    
    &:hover {
      box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1), 0 5px 15px rgba(0, 0, 0, 0.07);
      transform: translateZ(10px);
      
      .restaurant-image img {
        transform: scale(1.08);
      }
      
      .reserve-btn {
        background: $gradient-primary;
        box-shadow: 0 5px 15px rgba($primary-color, 0.3);
      }
    }
    
    :deep(.el-card__body) {
      padding: 0;
    }
    
    .restaurant-image {
      position: relative;
      height: 220px;
      overflow: hidden;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 60px;
        background: linear-gradient(to top, rgba(0,0,0,0.6), transparent);
        z-index: 1;
      }
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.7s cubic-bezier(0.165, 0.84, 0.44, 1);
      }
      
      .rating-badge {
        position: absolute;
        top: 15px;
        right: 15px;
        background: rgba(0, 0, 0, 0.7);
        color: white;
        padding: 5px 12px;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 600;
        display: flex;
        align-items: center;
        backdrop-filter: blur(5px);
        z-index: 2;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
        
        .el-rate {
          margin-left: 5px;
          height: 20px;
          line-height: 1;
          
          :deep(.el-rate__icon) {
            font-size: 12px;
            margin-right: 2px;
          }
        }
        
        .review-count {
          font-size: 12px;
          opacity: 0.8;
          margin-left: 3px;
        }
      }
      
      .restaurant-tags {
        position: absolute;
        bottom: 15px;
        left: 15px;
        display: flex;
        gap: 8px;
        z-index: 2;
        
        .tag {
          backdrop-filter: blur(5px);
          font-weight: 500;
          font-size: 12px;
          border-radius: 4px;
          padding: 2px 8px;
          
          &.new-tag {
            background-color: rgba($primary-color, 0.9);
            color: white;
            border-color: transparent;
          }
          
          &.popular-tag {
            background-color: rgba(255, 165, 0, 0.9);
            color: white;
            border-color: transparent;
          }
          
          &.cuisine-badge {
            background-color: rgba(0, 128, 0, 0.8);
            color: white;
            border-color: transparent;
          }
        }
      }
    }
    
    .restaurant-info {
      padding: 25px;
      background: white;
      
      h3 {
        font-size: 20px;
        font-weight: 700;
        margin: 0 0 10px;
        color: #222;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .cuisine-tags {
        margin-bottom: 12px;
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .cuisine-tag {
          display: inline-block;
          background-color: #f3f4f6;
          color: #666;
          font-size: 12px;
          font-weight: 500;
          padding: 3px 10px;
          border-radius: 100px;
        }
      }
      
      .address {
        display: flex;
        align-items: center;
        gap: 5px;
        color: #666;
        font-size: 14px;
        margin-bottom: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        
        .el-icon {
          color: $primary-color;
          font-size: 16px;
          flex-shrink: 0;
        }
      }
      
      .description {
        color: #666;
        font-size: 14px;
        line-height: 1.5;
        margin-bottom: 15px;
        min-height: 40px;
        position: relative;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        max-height: 42px; /* 14px(字体大小) * 1.5(行高) * 2(行数) */
        text-overflow: ellipsis;
      }
      
      .attributes {
        display: flex;
        flex-wrap: wrap;
        gap: 20px;
        margin-bottom: 20px;
        
        .attribute {
          display: flex;
          align-items: center;
          gap: 6px;
          color: #666;
          font-size: 14px;
          
          .price-level {
            color: #2c9e5f;
            font-weight: 600;
          }
          
          .attribute-label {
            color: #888;
            margin-left: 2px;
          }
          
          .el-icon {
            color: $primary-color;
            font-size: 16px;
          }
        }
      }
      
      .restaurant-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-top: 15px;
        
        .reserve-btn {
          flex: 1;
          background-color: $primary-color;
          border-color: $primary-color;
          transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
          font-weight: 600;
          font-size: 15px;
          padding: 10px 0;
          border-radius: 8px;
          
          &:hover {
            transform: translateY(-2px);
          }
          
          &:active {
            transform: translateY(0);
          }
        }
        
        .like-button {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: #f3f4f6;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-left: 12px;
          cursor: pointer;
          transition: all 0.3s;
          position: relative;
          overflow: hidden;
          
          &::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            background: rgba($primary-color, 0.1);
            border-radius: 50%;
            transform: translate(-50%, -50%);
            transition: width 0.4s ease, height 0.4s ease;
          }
          
          &:active::before {
            width: 100%;
            height: 100%;
          }
          
          .el-icon {
            font-size: 18px;
            color: #888;
            transition: all 0.3s;
            
            &.is-active {
              color: #ff9900;
            }
            
            &.is-loading {
              animation: spinning 1s infinite linear;
            }
            
            &.animate-heart {
              animation: heartbeat 0.6s ease;
            }
          }
          
          &:hover {
            background: $secondary-color;
            
            .el-icon {
              color: $primary-color;
              transform: scale(1.1);
              
              &.is-active {
                color: #ff9900;
              }
            }
          }
        }
      }
    }
    
    .restaurant-ribbon {
      position: absolute;
      top: 20px;
      left: -30px;
      background: $gradient-primary;
      color: white;
      padding: 5px 30px;
      font-size: 14px;
      font-weight: 600;
      transform: rotate(-45deg);
      z-index: 3;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
    }
  }
  
  .infinite-scroll-loader {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 30px 0;
    color: #777;
    font-size: 15px;
    
    .el-icon {
      margin-right: 10px;
      font-size: 20px;
      color: $primary-color;
      animation: rotating 2s linear infinite;
    }
  }
  
  .no-more-data {
    padding: 20px 0 40px;
    color: #777;
    font-size: 14px;
    text-align: center;
    
    :deep(.el-divider__text) {
      background-color: #f8f9fa;
      color: #777;
      font-size: 14px;
      padding: 0 15px;
    }
  }
  
  .no-results {
    padding: 60px 0;
    text-align: center;
    
    :deep(.el-empty__description) {
      margin-top: 20px;
      color: #666;
    }
    
    .el-button {
      margin-top: 25px;
      background: $gradient-primary;
      border: none;
      color: white;
      border-radius: 8px;
      padding: 12px 25px;
      font-weight: 500;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba($primary-color, 0.3);
      }
    }
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes spinning {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes heartbeat {
  0% { transform: scale(1); }
  15% { transform: scale(1.3); }
  30% { transform: scale(1); }
  45% { transform: scale(1.3); }
  60% { transform: scale(1); }
  100% { transform: scale(1); }
}

// Responsive Adjustments
@media (max-width: 768px) {
  .navbar {
    padding: 15px 20px;
    flex-wrap: wrap;
    
    .nav-left {
      order: 1;
      width: 50%;
    }
    
    .nav-right {
      order: 2;
      width: 50%;
      justify-content: flex-end;
    }
    
    .nav-center {
      order: 3;
      width: 100%;
      margin: 15px 0 0;
      max-width: none;
    }
  }
  
  .filters-section {
    padding: 0 20px;
    
    .filters-container {
      flex-direction: column;
      gap: 15px;
      align-items: stretch;
    }
  }
  
  .restaurant-list {
    padding: 0 20px 40px;
  }
  
  .banner {
    padding: 40px 0;
    
    .banner-content {
      h2 {
        font-size: 28px;
      }
      
      p {
        font-size: 16px;
      }
    }
  }
}
</style> 