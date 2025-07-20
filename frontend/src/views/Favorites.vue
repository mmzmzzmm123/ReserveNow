<template>
    <AppNavbar />
  <div class="favorites-page">
    <!-- Page Header -->
    <div class="page-header" :class="{ 'with-parallax': favorites.length > 0 }">
      <div class="header-content">
        <h1 class="page-title">My Favorites</h1>
        <p class="page-subtitle">Explore Your Treasured Restaurants</p>
      </div>
      <div class="header-decoration"></div>
    </div>

    <!-- Content Area -->
    <div class="page-content">
      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner">
          <div class="spinner"></div>
        </div>
        <p>Loading your favorites...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="favorites.length === 0" class="empty-state">
        <div class="empty-illustration">
          <svg width="200" height="200" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h2>No Favorite Restaurants Yet</h2>
        <p>Browse restaurants and click the favorite button to start building your collection</p>
        <el-button type="primary" class="explore-button" @click="goToExplore">
          <span>Start Exploring</span>
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <!-- Favorites List -->
      <div v-else class="favorites-container">
        <div class="favorites-header">
          <div class="favorites-info">
            <h2>Total <span>{{ total }}</span> Restaurants</h2>
            <p>These are your carefully selected restaurants</p>
          </div>
        </div>

        <!-- Card Grid -->
        <div class="favorites-grid">
          <div 
            v-for="restaurant in favorites" 
            :key="restaurant.restaurantId" 
            class="favorite-card"
            :style="{
              '--delay': `${favorites.indexOf(restaurant) * 0.05}s`
            }"
          >
            <div class="card-remove" @click.stop="removeFromFavorites(restaurant.restaurantId)">
              <el-icon><Close /></el-icon>
            </div>
            <div class="card-content" @click="viewRestaurant(restaurant.restaurantId)">
              <div class="card-image" :style="{ backgroundImage: `url(${getMainPhoto(restaurant.photos)})` }">
                <div class="card-badges">
                  <div class="card-rating">
                    <el-icon><Star /></el-icon>
                    <span>{{ restaurant.rating.toFixed(1) }}</span>
                  </div>
                  <div class="card-cuisine">{{ restaurant.cuisine }}</div>
                </div>
              </div>
              <div class="card-info">
                <h3 class="card-title">{{ restaurant.restaurantName }}</h3>
                <p class="card-address">
                  <el-icon><Location /></el-icon>
                  <span>{{ restaurant.address }}</span>
                </p>
                <p class="card-description">{{ truncateText(restaurant.description, 80) }}</p>
                <div class="card-footer">
                  <span class="saved-date">Saved on {{ formatDate(restaurant.createdAt) }}</span>
                  <el-button type="primary" size="small" class="view-button" @click.stop="viewRestaurant(restaurant.restaurantId)">
                    View Details
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination-container" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
  <AppFooter />
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import AppNavbar from '@/components/AppNavbar.vue';
import { ArrowRight, Star, Location, Close } from '@element-plus/icons-vue';
import request from '@/utils/request';
import AppFooter from '@/components/AppFooter.vue';

const router = useRouter();

// State Variables
const loading = ref(true);
const favorites = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// Fetch Favorites List
const fetchFavorites = async () => {
  loading.value = true;
  
  try {
    const userinfo = localStorage.getItem('userInfo');
    const token = JSON.parse(userinfo).token;

    if (!token) {
      ElMessage.error('You are not logged in or your session has expired. Please login again');
      router.push('/login');
      loading.value = false;
      return;
    }
    
    const headers = {
      'Authorization': `Bearer ${token}`
    };
    
    const response = await request.get('/favorites', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value
      },
      headers
    });
    
    if (response.code === 200) {
      favorites.value = response.data.list;
      total.value = response.data.list.length;
    } else {
      ElMessage.error(response.message || 'Failed to load favorites');
    }
  } catch (error) {
    console.error('Failed to load favorites:', error);
    ElMessage.error('Failed to load favorites, please try again later');
  } finally {
    loading.value = false;
  }
};

// Handle Page Change
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchFavorites();
  
  // 滚动到页面顶部
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
};

// Remove from Favorites
const removeFromFavorites = async (restaurantId) => {
  try {
    await ElMessageBox.confirm(
      'Are you sure you want to remove this restaurant from your favorites?',
      'Remove from Favorites',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    );
    
    const response = await request.delete(`/favorites/${restaurantId}`);
    
    if (response.code === 200) {
      ElMessage.success('Removed from favorites');
      
      if (favorites.value.length === 1 && currentPage.value > 1) {
        currentPage.value--;
      }

      fetchFavorites();
    } else {
      ElMessage.error(response.message || 'Failed to remove from favorites');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to remove from favorites:', error);
      ElMessage.error('Failed to remove from favorites, please try again later');
    }
  }
};

// View Restaurant Details
const viewRestaurant = (restaurantId) => {
  router.push(`/restaurant/${restaurantId}`);
};

// Go to Explore Page
const goToExplore = () => {
  router.push('/explore');
};

// Get Restaurant Main Photo
const getMainPhoto = (photos) => {
  if (!photos || photos.length === 0) {
    return 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=2070&auto=format&fit=crop';
  }
  return photos[0];
};

// Format Date
const formatDate = (dateString) => {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// Truncate Text
const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length > maxLength 
    ? `${text.substring(0, maxLength)}...` 
    : text;
};

// Load Favorites List on Component Mount
onMounted(() => {
  fetchFavorites();
});
</script>

<style lang="scss" scoped>
// Variables
$primary-color: #ff4757;
$secondary-color: #f8f1ff;
$accent-color: #6c5ce7;
$dark-color: #2d3436;
$light-color: #f9f9f9;
$gray-color: #636e72;
$border-radius: 16px;
$card-shadow: 0 10px 30px rgba(0, 0, 0, 0.07);
$transition-normal: all 0.3s ease;
$transition-bounce: all 0.4s cubic-bezier(0.68, -0.55, 0.27, 1.55);

// Page Container
.favorites-page {
  padding-top: 70px;
  min-height: 100vh;
  background-color: #fcfcfc;
}

// Page Header
.page-header {
  background-color: $secondary-color;
  background-image: 
    radial-gradient(circle at 10% 20%, rgba($primary-color, 0.05) 0%, transparent 30%),
    radial-gradient(circle at 90% 80%, rgba($accent-color, 0.05) 0%, transparent 30%);
  height: 220px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  margin-bottom: 40px;
  border-radius: 0 0 30px 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.03);
  transition: $transition-normal;
  
  &.with-parallax {
    background-attachment: fixed;
    
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
  }
  
  .header-content {
    position: relative;
    z-index: 2;
    text-align: center;
    max-width: 800px;
    padding: 0 20px;
  }
  
  .page-title {
    margin: 0;
    font-size: 36px;
    font-weight: 800;
    color: $dark-color;
    background: linear-gradient(135deg, $dark-color, $primary-color);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    margin-bottom: 10px;
    position: relative;
    display: inline-block;
    
    &::after {
      content: '';
      position: absolute;
      bottom: -5px;
      left: 50%;
      transform: translateX(-50%);
      width: 40px;
      height: 4px;
      background: linear-gradient(to right, $primary-color, $accent-color);
      border-radius: 2px;
    }
  }
  
  .page-subtitle {
    margin: 0;
    font-size: 16px;
    color: $gray-color;
    font-weight: 500;
  }
  
  .header-decoration {
    position: absolute;
    bottom: -5px;
    left: 0;
    right: 0;
    height: 10px;
    background-image: 
      radial-gradient(circle at 10% 0, $primary-color 0%, transparent 30%),
      radial-gradient(circle at 90% 0, $accent-color 0%, transparent 30%);
    opacity: 0.2;
  }
}

// Page Content
.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 50px;
}

// Loading State
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  
  .loading-spinner {
    margin-bottom: 20px;
    position: relative;
    width: 60px;
    height: 60px;
    
    .spinner {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      border-radius: 50%;
      border: 3px solid transparent;
      border-top-color: $primary-color;
      animation: spin 1.2s linear infinite;
      
      &::after {
        content: '';
        position: absolute;
        top: 5px;
        left: 5px;
        right: 5px;
        bottom: 5px;
        border-radius: 50%;
        border: 3px solid transparent;
        border-top-color: $accent-color;
        animation: spin 0.8s linear reverse infinite;
      }
    }
  }
  
  p {
    font-size: 16px;
    color: $gray-color;
    margin: 0;
  }
}

// Empty State
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
  background-color: white;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  
  .empty-illustration {
    margin-bottom: 30px;
    color: #e0e0e0;
    animation: pulse 2s infinite ease-in-out;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      width: 150px;
      height: 150px;
      border-radius: 50%;
      background: radial-gradient(circle, rgba($primary-color, 0.1) 0%, rgba($primary-color, 0) 70%);
      z-index: -1;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
    
    svg {
      width: 120px;
      height: 120px;
      stroke-width: 1;
    }
  }
  
  h2 {
    margin: 0 0 15px;
    font-size: 26px;
    color: $dark-color;
    font-weight: 700;
  }
  
  p {
    margin: 0 0 30px;
    font-size: 16px;
    color: $gray-color;
    line-height: 1.6;
    max-width: 400px;
  }
}

// Favorites List
.favorites-container {
  animation: fadeIn 0.5s ease;
}

// Favorites Header
.favorites-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
  
  .favorites-info {
    h2 {
      margin: 0 0 5px;
      font-size: 22px;
      color: $dark-color;
      font-weight: 700;
      
      span {
        color: $primary-color;
        font-weight: 800;
      }
    }
    
    p {
      margin: 0;
      font-size: 14px;
      color: $gray-color;
    }
  }
}

// Favorites Card Grid
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}

// Favorite Card
.favorite-card {
  background-color: white;
  border-radius: $border-radius;
  overflow: hidden;
  box-shadow: $card-shadow;
  transition: $transition-normal;
  position: relative;
  animation: cardFadeIn 0.5s ease forwards;
  animation-delay: var(--delay);
  opacity: 0;
  transform: translateY(20px);
  border: 1px solid rgba(0, 0, 0, 0.05);
  height: 100%;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
    
    .card-image {
      &::before {
        opacity: 0.7;
      }
      
      .card-rating {
        transform: translateY(-3px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
      }
      
      .card-cuisine {
        transform: translateY(-3px);
        box-shadow: 0 5px 15px rgba($accent-color, 0.4);
      }
    }
    
    .card-title {
      color: $primary-color;
    }
  }
  
  .card-remove {
    position: absolute;
    top: 15px;
    right: 15px;
    width: 30px;
    height: 30px;
    background-color: rgba(255, 255, 255, 0.9);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 2;
    transition: $transition-normal;
    opacity: 0;
    transform: scale(0.8);
    box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
    color: #e74c3c;
    
    &:hover {
      background-color: #e74c3c;
      color: white;
      transform: scale(1.1);
      box-shadow: 0 5px 15px rgba(#e74c3c, 0.3);
    }
  }
  
  &:hover .card-remove {
    opacity: 1;
    transform: scale(1);
  }
  
  .card-content {
    display: flex;
    flex-direction: column;
    height: 100%;
    cursor: pointer;
  }
  
  .card-image {
    height: 200px;
    background-size: cover;
    background-position: center;
    position: relative;
    transition: all 0.4s ease;
    
    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0) 50%);
      opacity: 0.6;
      transition: $transition-normal;
    }
    
    .card-badges {
      position: absolute;
      bottom: 15px;
      left: 15px;
      right: 15px;
      display: flex;
      justify-content: space-between;
      z-index: 1;
    }
    
    .card-rating {
      background-color: rgba(255, 255, 255, 0.95);
      border-radius: 20px;
      padding: 5px 12px;
      display: flex;
      align-items: center;
      gap: 5px;
      font-weight: 600;
      font-size: 14px;
      color: $dark-color;
      box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
      
      .el-icon {
        color: gold;
        animation: starPulse 2s infinite;
      }
    }
    
    .card-cuisine {
      background-color: rgba($accent-color, 0.95);
      color: white;
      border-radius: 20px;
      padding: 5px 12px;
      font-weight: 500;
      font-size: 13px;
      box-shadow: 0 3px 10px rgba($accent-color, 0.3);
      backdrop-filter: blur(4px);
    }
  }
  
  .card-info {
    padding: 20px;
    display: flex;
    flex-direction: column;
    flex: 1;
  }
  
  .card-title {
    margin: 0 0 10px;
    font-size: 18px;
    font-weight: 700;
    color: $dark-color;
    transition: $transition-normal;
    line-height: 1.3;
    
    &::after {
      content: '';
      display: block;
      width: 30px;
      height: 2px;
      background: linear-gradient(to right, $primary-color, transparent);
      margin-top: 5px;
      border-radius: 2px;
      transition: all 0.3s;
    }
  }
  
  &:hover .card-title::after {
    width: 50px;
  }
  
  .card-address {
    display: flex;
    align-items: center;
    gap: 7px;
    margin: 0 0 15px;
    font-size: 14px;
    color: $gray-color;
    
    .el-icon {
      color: $primary-color;
      flex-shrink: 0;
    }
    
    span {
      line-height: 1.4;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .card-description {
    margin: 0 0 20px;
    font-size: 14px;
    color: $gray-color;
    line-height: 1.5;
    flex: 1;
  }
  
  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;
    
    .saved-date {
      font-size: 12px;
      color: $gray-color;
      font-style: italic;
    }
    
    .view-button {
      padding: 8px 15px;
      font-size: 12px;
      border-radius: 20px;
      background: $secondary-color;
      border: none;
      color: $primary-color;
      font-weight: 600;
      transition: $transition-normal;
      
      &:hover {
        background: $primary-color;
        color: white;
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba($primary-color, 0.2);
      }
    }
  }
}

// Pagination
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  
  :deep(.el-pagination) {
    --el-pagination-button-bg-color: white;
    --el-pagination-hover-color: $primary-color;
    --el-pagination-button-height: 40px;
    --el-pagination-button-width: 40px;
    --el-pagination-font-size: 14px;
    
    .el-pager li {
      border-radius: 8px;
      margin: 0 3px;
      transition: all 0.3s;
      
      &.is-active {
        background-color: $primary-color;
        color: white;
        font-weight: 600;
        box-shadow: 0 5px 15px rgba($primary-color, 0.3);
      }
    }
    
    .btn-prev, .btn-next {
      background-color: white;
      border-radius: 8px;
      box-shadow: 0 3px 10px rgba(0, 0, 0, 0.05);
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      }
    }
  }
}

// Animations
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes cardFadeIn {
  from { 
    opacity: 0;
    transform: translateY(20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.7; }
  50% { transform: scale(1.05); opacity: 1; }
  100% { transform: scale(1); opacity: 0.7; }
}

@keyframes starPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

@keyframes headerShine {
  0% { transform: translateX(-100%) rotate(30deg); }
  100% { transform: translateX(200%) rotate(30deg); }
}

// Responsive Styles
@media (max-width: 768px) {
  .page-header {
    height: 180px;
    
    .page-title {
      font-size: 28px;
    }
    
    .page-subtitle {
      font-size: 14px;
    }
  }
  
  .favorites-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .favorites-grid {
    grid-template-columns: 1fr;
  }
  
  .favorite-card .card-remove {
    opacity: 1;
    transform: scale(1);
  }
}
</style> 