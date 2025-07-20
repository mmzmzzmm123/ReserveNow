<template>
  <div class="restaurant-detail-container">
    <!-- Top Navigation Bar -->
    <header class="detail-header">
      <div class="header-actions">
        <el-button class="back-button" @click="goBack" circle>
          <el-icon><ArrowLeft /></el-icon>
        </el-button>
        <div class="actions-right">
          <el-button
            class="favorite-button"
            @click="toggleFavorite"
            circle
            :class="{ 'is-favorite': restaurant.isFavorite }"
          >
            <el-icon><Star /></el-icon>
          </el-button>
          <el-button class="share-button" circle>
            <el-icon><Share /></el-icon>
          </el-button>
        </div>
      </div>
    </header>

    <!-- Restaurant Photo Gallery -->
    <div class="restaurant-gallery" v-loading="loading">
      <el-carousel
        v-if="restaurant.photos && restaurant.photos.length"
        height="450px"
        indicator-position="outside"
        arrow="always"
        :autoplay="true"
        :interval="5000"
      >
        <el-carousel-item
          v-for="(photo, index) in restaurant.photos"
          :key="index"
        >
          <div
            class="gallery-item"
            :style="{ backgroundImage: `url(${photo})` }"
          ></div>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="no-photos">
        <el-icon><Picture /></el-icon>
        <p>No restaurant photos</p>
      </div>

      <!-- Restaurant Title Overlay -->
      <div class="restaurant-title-overlay">
        <h1>{{ restaurant.name || "Loading..." }}</h1>
        <div class="quick-info">
          <div class="info-item cuisine">
            <el-icon><Food /></el-icon>
            <span>{{ restaurant.cuisine || "Mixed Cuisine" }}</span>
          </div>
          <div class="rating-display">
            <span class="rating-value">{{ restaurant.rating || "-" }}</span>
            <el-rate
              v-model="restaurant.rating"
              disabled
              text-color="#ffcc00"
            />
            <span class="review-count"
              >({{ restaurant.reviewCount || 0 }})</span
            >
          </div>
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>{{
              restaurant.address
                ? restaurant.address.substring(0, 15) + "..."
                : "No address"
            }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Restaurant Basic Information -->
    <div class="restaurant-info-card" v-loading="loading">
      <div class="info-section">
        <div class="info-row">
          <el-icon><Location /></el-icon>
          <span>{{ restaurant.address || "No address information" }}</span>
        </div>
        <div class="info-row">
          <el-icon><Phone /></el-icon>
          <span>{{ restaurant.phone || "No contact number" }}</span>
        </div>
        <div class="info-row">
          <el-icon><Clock /></el-icon>
          <span>{{ formatBusinessHours(restaurant.businessHours) }}</span>
        </div>
      </div>

      <div class="description-section">
        <h3>Restaurant Description</h3>
        <p>{{ restaurant.description || "No restaurant description" }}</p>
      </div>
    </div>

    <!-- Table Information -->
    <div class="tables-section" v-loading="loading">
      <h2 class="section-title">Select Table</h2>
      
      <!-- Table type classification panel -->
      <div class="table-types-container">
        <div 
          v-for="type in tableTypes" 
          :key="type.name" 
          class="table-type-card"
          :class="{ 'vip-table': type.name.includes('VIP') }"
          :style="{ '--type-color': type.color }"
          @click="showTablesByType(type.name)"
        >
          <div class="table-type-icon" :style="{ backgroundColor: `${type.color}1a` }">
            <el-icon v-if="type.icon === 'TrophyBase'" class="type-icon"><TrophyBase /></el-icon>
            <el-icon v-else-if="type.icon === 'SetUp'" class="type-icon"><SetUp /></el-icon>
            <el-icon v-else class="type-icon"><Menu /></el-icon>
          </div>
          <div class="table-type-info">
            <h3 class="type-name">{{ type.name }}</h3>
            <p class="table-count" :style="{ backgroundColor: `${type.color}0a` }">{{ type.count }} Tables Available</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Table type list dialog -->
    <el-dialog
      v-model="tableListVisible"
      :title="selectedTableType"
      width="850px"
      class="table-list-dialog"
      destroy-on-close
    >
      <div class="table-filter-bar">
        <div class="filter-title">
          <el-icon><Filter /></el-icon>
          <span>Table Capacity</span>
        </div>
        <div class="capacity-filter">
          <el-radio-group v-model="capacityFilter" size="large">
            <el-radio-button label="all">All</el-radio-button>
            <el-radio-button label="1-2">1-2 people</el-radio-button>
            <el-radio-button label="3-4">3-4 people</el-radio-button>
            <el-radio-button label="5-8">5-8 people</el-radio-button>
            <el-radio-button label="9+">9+ people</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="tables-container">
        <div 
          v-for="table in filteredTablesByCapacity" 
          :key="table.id" 
          class="table-card-modern"
          :class="{
            'vip-table': table.type.includes('VIP'),
            'standard-table': table.type === 'Standard' || table.type === 'Standard Table'
          }"
          @click="selectTable(table)"
        >
          <div class="table-backdrop"></div>
          
          <div class="table-status-tag">
            <span class="status-badge available">Available</span>
          </div>
          
          <div class="table-header-modern">
            <div class="table-icon-container-modern">
              <el-icon v-if="table.type.includes('VIP')" class="table-icon-modern vip">
                <TrophyBase />
              </el-icon>
              <el-icon v-else-if="table.type === 'Standard' || table.type === 'Standard Table'" class="table-icon-modern standard">
                <Menu />
              </el-icon>
              <el-icon v-else class="table-icon-modern">
                <Menu />
              </el-icon>
            </div>
            <div class="table-id">Table No: {{ table.tableNumber || table.id }}</div>
          </div>
          
          <div class="table-content-modern">
            <h3 class="table-type-modern">{{ table.type }}</h3>
            
            <div class="table-info-modern">
              <div class="info-row">
                <el-icon><User /></el-icon>
                <span>{{ table.capacity }} person table</span>
              </div>
              <div class="info-row">
                <el-icon><Clock /></el-icon>
                <span>Available all day</span>
              </div>
            </div>
            
            <div class="table-features-modern">
              <div class="feature-tag" v-if="table.type.includes('VIP')">Premium Booking</div>
              <div class="feature-tag" v-if="table.type.includes('VIP')">Private Space</div>
              <div class="feature-tag" v-if="table.type.includes('Large')">Group Dining</div>
              <div class="feature-tag" v-if="table.type.includes('Standard')">Comfortable Environment</div>
            </div>
          </div>
          
          <div class="table-footer-modern">
            <el-button 
              :type="table.type.includes('VIP') ? 'warning' : 'primary'" 
              class="select-btn"
            >
              Select This Table
            </el-button>
          </div>
        </div>
        
        <div v-if="filteredTablesByCapacity.length === 0" class="no-tables-modern">
          <el-empty description="No tables found matching your criteria" :image-size="200">
            <template #image>
              <div class="empty-icon-container">
                <el-icon class="empty-icon"><DishDot /></el-icon>
              </div>
            </template>
          </el-empty>
        </div>
      </div>
    </el-dialog>

    <!-- Time Selection Dialog -->
    <el-dialog
      v-model="timeSelectionVisible"
      title="Select Reservation Time"
      width="660px"
      destroy-on-close
      :close-on-click-modal="false"
      class="time-selection-dialog"
      :show-close="true"
    >
      <div v-if="selectedTable" class="time-selection-content">
        <div class="selected-table-info">
          <div class="table-icon-wrapper">
            <div
              class="table-icon"
              :class="{
                'vip-icon': selectedTable.type.includes('VIP'),
                'large-icon': selectedTable.type.includes('Large'),
                'standard-icon': selectedTable.type.includes('Standard'),
              }"
            >
              <el-icon v-if="selectedTable.type.includes('VIP')"
                ><TrophyBase
              /></el-icon>
              <el-icon v-else-if="selectedTable.type.includes('Large')"
                ><SetUp
              /></el-icon>
              <el-icon v-else><Menu /></el-icon>
            </div>
          </div>
          <div class="table-details">
            <h3>{{ selectedTable.type }}</h3>
            <p>
              <el-icon><User /></el-icon> {{ selectedTable.capacity }} person table
            </p>
            <div class="table-tags">
              <span
                v-if="selectedTable.type.includes('VIP')"
                class="tag vip-tag"
                >Premium Booking</span
              >
              <span
                v-if="selectedTable.type.includes('VIP')"
                class="tag vip-tag"
                >Privacy</span
              >
              <span
                v-if="selectedTable.type.includes('Large')"
                class="tag large-tag"
                >Group Dining</span
              >
              <span
                v-if="selectedTable.type.includes('Standard')"
                class="tag standard-tag"
                >Comfortable Environment</span
              >
              <span
                v-if="
                  !selectedTable.type.includes('VIP') &&
                  !selectedTable.type.includes('Large') &&
                  !selectedTable.type.includes('Standard')
                "
                class="tag standard-tag"
                >Regular Seating</span
              >
            </div>
          </div>
        </div>

        <div class="selection-container">
          <div class="date-selection">
            <h4>
              <el-icon><Calendar /></el-icon> Select Date
            </h4>
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="Select date"
              format="YYYY-MM-DD"
              :disabled-date="disabledDate"
              :default-value="selectedDate"
              @change="handleDateChange"
            />
            <p class="note">
              <el-icon><InfoFilled /></el-icon> Reservations can only be made for tomorrow and later
            </p>
          </div>

          <div class="time-slots" v-loading="loadingTimeSlots">
            <h4>
              <el-icon><Timer /></el-icon> Select Time
              <span v-if="selectedTimeRange" class="selected-time-range">
                <el-icon><Check /></el-icon> {{ selectedTimeRange }}
              </span>
            </h4>
            <p class="time-hint">
              <el-icon><Clock /></el-icon> Business Hours:
              {{ formatBusinessHours(restaurant.businessHours) }}
            </p>
            <p class="day-type-hint">
              <el-icon><Calendar /></el-icon> Current Selection:
              {{ formatSelectedDay(selectedDate) }}
            </p>
            <p class="selection-hint">
              <el-icon><Warning /></el-icon> Tip: You can select multiple consecutive time slots
            </p>

            <div class="time-grid">
              <el-button
                v-for="time in availableTimeSlots"
                :key="time.value"
                :disabled="time.disabled"
                :class="{
                  selected: selectedTimeSlots.some(
                    (slot) => slot.value === time.value
                  ),
                }"
                @click="handleTimeSelection(time)"
                size="large"
              >
                {{ time.label }}
              </el-button>
            </div>
          </div>
        </div>

        <div class="reservation-details">
          <h4>
            <el-icon><Memo /></el-icon> Reservation Details
          </h4>

          <div class="details-grid">
            <div class="detail-item">
              <label for="person-count">Number of Diners</label>
              <el-input-number
                id="person-count"
                v-model="personCount"
                :min="1"
                :max="selectedTable.capacity || 20"
                size="large"
                :step="1"
                step-strictly
              />
              <span class="capacity-info"
                >Maximum capacity: {{ selectedTable.capacity }} people</span
              >
            </div>

            <div class="detail-item remarks-item">
              <label for="remarks">Notes</label>
              <el-input
                id="remarks"
                v-model="reservationRemarks"
                type="textarea"
                placeholder="Please enter special requests, such as: window seats, child chairs, etc."
                :rows="3"
                maxlength="200"
                show-word-limit
              />
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <div class="reservation-summary" v-if="selectedTimeSlots.length > 0">
            <div class="summary-item">
              <span class="label">Reservation Date:</span>
              <span class="value">{{ selectedDate.toLocaleDateString() }}</span>
            </div>
            <div class="summary-item">
              <span class="label">Reservation Time:</span>
              <span class="value">{{ selectedTimeRange }}</span>
            </div>
            <div class="summary-item">
              <span class="label">Number of Diners:</span>
              <span class="value">{{ personCount }} people</span>
            </div>
          </div>

          <div class="action-buttons">
            <el-button @click="timeSelectionVisible = false" class="cancel-btn"
              >Cancel</el-button
            >
            <el-button
              type="primary"
              :disabled="selectedTimeSlots.length === 0"
              @click="confirmReservation"
              class="confirm-btn"
            >
              Confirm Reservation
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- Restaurant Map Location -->
    <div class="map-section">
      <h2>Location</h2>
      <div class="map-container">
        <div class="google-map-container">
          <iframe
            v-if="restaurant.latitude && restaurant.longitude"
            :src="`https://www.google.com/maps/embed/v1/place?key=AIzaSyCdLhgzrZnEU9ZHO3r5RbzVprvSSpFxBPE&q=${restaurant.latitude},${restaurant.longitude}&zoom=15`"
            width="100%"
            height="300"
            style="border: 0"
            allowfullscreen=""
            loading="lazy"
            referrerpolicy="no-referrer-when-downgrade"
          >
          </iframe>
          <div v-else class="map-placeholder">
            <el-icon><MapLocation /></el-icon>
            <p>Loading map...</p>
          </div>
        </div>
        <div class="address-detail">
          <p>
            <el-icon><Location /></el-icon> {{ restaurant.address }}
          </p>
          <el-button type="primary" plain @click="openGoogleMapsNavigation">
            Navigate to here
          </el-button>
        </div>
      </div>
    </div>

    <!-- Restaurant Reviews -->
    <div class="reviews-section">
      <div class="section-header">
        <h2>Customer Reviews </h2>
      </div>

      <div class="rating-filter">
        <span class="filter-label">Filter by Rating:</span>
        <div class="rating-buttons">
          <el-button
            v-for="rating in 5"
            :key="rating"
            size="small"
            :type="reviewsRating === rating ? 'primary' : 'default'"
            :class="{ 'active-rating': reviewsRating === rating }"
            @click="filterReviewsByRating(rating)"
          >
            {{ rating }} <el-icon><Star /></el-icon>
          </el-button>
          <el-button
            size="small"
            :type="reviewsRating === null ? 'primary' : 'default'"
            :class="{ 'active-rating': reviewsRating === null }"
            @click="filterReviewsByRating(null)"
          >
            All
          </el-button>
        </div>
      </div>

      <div class="reviews-container" v-loading="reviewsLoading">
        <div v-if="reviewsList.length > 0" class="reviews-list">
          <div
            v-for="review in reviewsList"
            :key="review.id"
            class="review-item"
          >
            <div class="review-header">
              <div class="user-info">
                <el-avatar :src="review.userAvatar" :size="48">
                  {{ review.userName ? review.userName.charAt(0) : "User" }}
                </el-avatar>
                <div class="user-details">
                  <div class="user-name">
                    {{ review.userName || "Anonymous User" }}
                  </div>
                  <div class="review-time">
                    {{ formatReviewTime(review.createdAt) }}
                  </div>
                </div>
              </div>
              <div class="review-rating">
                <span class="rating-value">{{ review.rating }}</span>
                <el-rate
                  v-model="review.rating"
                  disabled
                  text-color="#ffcc00"
                />
              </div>
            </div>

            <div class="review-content">
              <p>{{ review.content }}</p>

              <div
                v-if="
                  (review.photos && review.photos.length) ||
                  (review.videos && review.videos.length)
                "
                class="review-media"
              >
                <el-image
                  v-for="(photo, index) in review.photos"
                  :key="`photo-${index}`"
                  :src="photo"
                  :preview-src-list="review.photos"
                  :initial-index="index"
                  fit="cover"
                  class="review-media-item"
                  :preview-teleported="true"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                      <span>Failed to load</span>
                    </div>
                  </template>
                </el-image>

                <div
                  v-for="(video, index) in review.videos"
                  :key="`video-${index}`"
                  class="review-media-item video-container"
                  @click.stop="playVideo($event)"
                >
                  <video
                    :src="video"
                    controls
                    class="review-video"
                    preload="metadata"
                    @click.stop
                  />
                </div>
              </div>
            </div>
          </div>

          <div v-if="reviewsList.length < reviewsTotal" class="load-more">
            <el-button
              type="primary"
              plain
              @click="loadMoreReviews"
              :loading="reviewsLoading"
            >
              Load More
            </el-button>
          </div>
        </div>

        <div v-else-if="!reviewsLoading" class="no-reviews">
          <el-empty description="No reviews yet, be the first to write a review!"></el-empty>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import {
  ArrowLeft,
  Star,
  Share,
  Location,
  Phone,
  Clock,
  Calendar,
  DeskLamp,
  User,
  TrophyBase,
  MapLocation,
  Picture,
  Food,
  Timer,
  SetUp,
  InfoFilled,
  Check,
  Warning,
  Memo,
  Menu,
  VideoPlay,
  Filter,
  DishDot,
} from "@element-plus/icons-vue";
import request from "@/utils/request";
import AppFooter from "@/components/AppFooter.vue";

const router = useRouter();
const route = useRoute();
const restaurantId = route.params.id;

// Restaurant data
const restaurant = ref({
  photos: [],
  tables: [],
});
const loading = ref(true);

// Review related data
const reviewsLoading = ref(false);
const reviewsList = ref([]);
const reviewsTotal = ref(0);
const reviewsPage = ref(1);
const reviewsPageSize = ref(5);
const reviewsRating = ref(null);

// Get user info
const userInfo = ref(JSON.parse(localStorage.getItem("userInfo") || "{}"));
const isUserLoggedIn = computed(() => {
  return !!userInfo.value && !!userInfo.value.token;
});

// Get restaurant details
const fetchRestaurantDetail = async () => {
  loading.value = true;
  try {
    const headers = {};
    if (isUserLoggedIn.value) {
      headers.Authorization = `Bearer ${userInfo.value.token}`;
    }

    const res = await request({
      url: `/restaurants/${restaurantId}`,
      method: "GET",
      headers,
    });

    restaurant.value = res.data;

    // Ensure photos is an array
    if (!restaurant.value.photos) {
      restaurant.value.photos = [];
    }

    // Ensure tables is an array
    if (!restaurant.value.tables) {
      restaurant.value.tables = [];
    }

    // Ensure latitude and longitude are numeric
    if (restaurant.value.latitude && restaurant.value.longitude) {
      restaurant.value.latitude = parseFloat(restaurant.value.latitude);
      restaurant.value.longitude = parseFloat(restaurant.value.longitude);
    }
  } catch (error) {
    console.error("Failed to get restaurant details", error);
    ElMessage.error("Failed to get restaurant information, please try again later");
  } finally {
    loading.value = false;
  }
};

// Go back to previous page
const goBack = () => {
  router.push("/");
};

// Toggle favorite
const toggleFavorite = async () => {
  if (!isUserLoggedIn.value) {
    ElMessage.warning("Please login first to favorite a restaurant");
    router.push("/login");
    return;
  }

  try {
    if (restaurant.value.isFavorite) {
      // Remove from favorites
      await request({
        url: `/favorites/${restaurantId}`,
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${userInfo.value.token}`,
        },
      });

      ElMessage.success("Removed from favorites");
      restaurant.value.isFavorite = false;
    } else {
      // Add to favorites
      await request({
        url: "/favorites",
        method: "POST",
        headers: {
          Authorization: `Bearer ${userInfo.value.token}`,
        },
        data: {
          restaurantId: parseInt(restaurantId),
        },
      });

      ElMessage.success("Added to favorites");
      restaurant.value.isFavorite = true;
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "Operation failed, please try again later");
    console.error("Favorite operation failed", error);
  }
};

// Filter by rating
const filterReviewsByRating = (rating) => {
  // If clicking on the currently selected rating, set to null (all)
  // Otherwise set to the selected rating
  const newRating = rating === reviewsRating.value ? null : rating;

  // If rating hasn't changed, don't do anything
  if (newRating === reviewsRating.value) return;

  // 获取评论容器并保存当前高度
  const reviewsContainer = document.querySelector(".reviews-container");
  if (reviewsContainer) {
    const currentHeight = reviewsContainer.offsetHeight;
    reviewsContainer.style.minHeight = `${currentHeight}px`;
  }

  // Set loading to true first, but don't clear the current list
  reviewsLoading.value = true;

  // Then update the filter rating
  reviewsRating.value = newRating;

  // Delay a bit before fetching data to allow UI to show loading state
  setTimeout(() => {
    // Reset page number and get new data
    reviewsPage.value = 1;
    fetchReviews(false, true); // Pass the second parameter to indicate this is a filter operation
  }, 100);
};

// Get restaurant reviews
const fetchReviews = async (loadMore = false, isFilter = false) => {
  // If loading more, increment the page number
  if (loadMore) {
    reviewsPage.value++;
  } else if (!isFilter) {
    // Only reset page number and set loading state in non-filter situations
    // In filter situations, page number has already been reset in filterReviewsByRating, and loading state has been set
    reviewsPage.value = 1;
    reviewsLoading.value = true;
  }

  try {
    const res = await request({
      url: `/restaurants/${restaurantId}/reviews`,
      method: "GET",
      params: {
        page: reviewsPage.value,
        pageSize: reviewsPageSize.value,
        rating: reviewsRating.value || undefined,
      },
    });

    // If loading more, add new data to the end of the existing list
    if (loadMore) {
      reviewsList.value = [...reviewsList.value, ...res.data.list];
    } else {
      // Otherwise, replace the existing list with new data
      // To avoid flickering, use a short delay
      const newReviews = res.data.list || [];

      if (isFilter) {
        const reviewsContainer = document.querySelector(".reviews-list");
        if (reviewsContainer) {
          reviewsContainer.style.opacity = "0.3";
          reviewsContainer.style.transition = "opacity 0.3s ease";
          const currentHeight = reviewsContainer.offsetHeight;
          reviewsContainer.style.minHeight = `${currentHeight}px`;
          setTimeout(() => {
            reviewsList.value = newReviews;
            setTimeout(() => {
              if (reviewsContainer) {
                reviewsContainer.style.opacity = "1";
                setTimeout(() => {
                  reviewsContainer.style.minHeight = '';
                }, 30000);
              }
              reviewsLoading.value = false;
            }, 50);
          }, 300);
        } else {
          reviewsList.value = newReviews;
          reviewsLoading.value = false;
        }
      } else {
        reviewsList.value = newReviews;
        reviewsLoading.value = false;
      }
    }

    reviewsTotal.value = res.data.total || 0;
  } catch (error) {
    console.error("Failed to fetch reviews", error);
    ElMessage.error("Failed to load review information, please try again later");
    reviewsLoading.value = false;
  }
};

// Load more reviews
const loadMoreReviews = () => {
  if (reviewsList.value.length < reviewsTotal.value) {
    fetchReviews(true);
  }
};

// Format review time
const formatReviewTime = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

// Format business hours
const formatBusinessHours = (hours) => {
  if (!hours) return "No business hours information";

  const time = [];
  hours.split("|").map((item, index) => {
    time[index] = item;
  });
  return time.join(" / ");
};

// Format selected date and type
const formatSelectedDay = (date) => {
  if (!date) return "";

  const dayOfWeek = date.getDay(); // 0 is Sunday, 6 is Saturday
  const isWeekend = dayOfWeek === 0 || dayOfWeek === 6;
  const dayNames = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  const formattedDate = `${date.getFullYear()}年${
    date.getMonth() + 1
  }月${date.getDate()}日`;

  return `${formattedDate} ${dayNames[dayOfWeek]}（${
    isWeekend ? "Weekend" : "Weekday"
  }）`;
};

// Get status code
const getStatusCode = (status) => {
  const statusMap = {
    待审核: "pending",
    审核通过: "approved",
    已关闭: "closed",
    运营中: "operating",
    运营: "operating",
  };

  return statusMap[status] || "unknown";
};

// Open Google Maps navigation
const openGoogleMapsNavigation = () => {
  if (restaurant.value.latitude && restaurant.value.longitude) {
    const url = `https://www.google.com/maps/dir/?api=1&destination=${restaurant.value.latitude},${restaurant.value.longitude}`;
    window.open(url, "_blank");
  } else {
    ElMessage.warning("Unable to retrieve restaurant location information");
  }
};

// Reservation-related state
const timeSelectionVisible = ref(false);
const selectedTable = ref(null);
const selectedDate = ref(new Date());
const selectedTimeSlots = ref([]);
const availableTimeSlots = ref([]);
const loadingTimeSlots = ref(false);
const bookedTimeSlots = ref([]);
const personCount = ref(1);
const reservationRemarks = ref("");

// Select table
const selectTable = (table) => {
  if (!isUserLoggedIn.value) {
    ElMessage.warning("Please log in before reserving a table");
    router.push("/login");
    return;
  }

  selectedTable.value = table;
  // Default to tomorrow instead of today
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  selectedDate.value = tomorrow;
  selectedTimeSlots.value = [];
  personCount.value = Math.min(table.capacity || 4, 4); // Default to 4 or smaller value of table capacity
  reservationRemarks.value = "";
  timeSelectionVisible.value = true;

  // Load reservation details for this table
  fetchTableReservations();
};

// Disable dates before today
const disabledDate = (time) => {
  // Get the time at midnight of tomorrow (excluding hours, minutes, seconds)
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  tomorrow.setHours(0, 0, 0, 0);

  // Disable all dates before tomorrow (including today)
  return time.getTime() < tomorrow.getTime();
};

// Handle date change
const handleDateChange = () => {
  selectedTimeSlots.value = []; // Clear selected times
  generateTimeSlots(); // Regenerate times for today
  fetchTableReservations(); // Get reservations for today
};

// Generate time slots
const generateTimeSlots = () => {
  availableTimeSlots.value = [];

  // Default business hours are 11:00-22:00, if restaurant has business hours, parse them
  let startHour = 11;
  let endHour = 22;

  if (restaurant.value.businessHours) {
    // Parse business hours string, format is "Monday-Friday 11:00-22:00|Weekend 10:00-23:00"
    const businessHoursParts = restaurant.value.businessHours.split("|");

    // Check if the selected date is a weekend
    const selectedDay = selectedDate.value.getDay(); // 0 is Sunday, 6 is Saturday
    const isWeekend = selectedDay === 0 || selectedDay === 6;

    // Depending on whether it's a weekend, choose the corresponding business hours
    let todayHours;
    if (isWeekend) {
      // Find a time period that includes "weekend", "Saturday", or "Sunday"
      todayHours = businessHoursParts.find(
        (hours) =>
          hours.includes("weekend") ||
          hours.includes("Saturday") ||
          hours.includes("Sunday") ||
          hours.includes("Saturday-Sunday")
      );
    } else {
      // Find a time period that includes "Monday-Friday" or weekdays
      todayHours = businessHoursParts.find(
        (hours) =>
          hours.includes("Monday-Friday") ||
          hours.includes("weekdays") ||
          (hours.includes("week") &&
            !hours.includes("weekend") &&
            !hours.includes("Saturday") &&
            !hours.includes("Sunday"))
      );
    }

    // If no matching time period is found, use the first time period as default
    if (!todayHours && businessHoursParts.length > 0) {
      todayHours = businessHoursParts[0];
    }

    if (todayHours) {
      // Extract time part, e.g., "11:00-22:00"
      const timeMatch = todayHours.match(/(\d{1,2}):(\d{2})-(\d{1,2}):(\d{2})/);
      if (timeMatch) {
        startHour = parseInt(timeMatch[1]);
        endHour = parseInt(timeMatch[3]);

        console.log(
          `Current selected date type: ${
            isWeekend ? "weekend" : "weekday"
          }, business hours: ${startHour}:00-${endHour}:00`
        );
      }
    }
  }

  // Generate time slots, one per hour
  for (let hour = startHour; hour < endHour; hour++) {
    const timeValue = `${hour.toString().padStart(2, "0")}:00`;
    availableTimeSlots.value.push({
      label: `${hour}:00-${hour + 1}:00`,
      value: timeValue,
      hour: hour,
      disabled: false, // Initially all are available, will be disabled later in fetchTableReservations
    });
  }
};

// Get table reservation details
const fetchTableReservations = async () => {
  if (!selectedTable.value || !selectedDate.value) return;

  loadingTimeSlots.value = true;

  try {
    // Get selected date in string format, format is YYYY-MM-DD
    const selectedDateString = selectedDate.value.toISOString().split("T")[0];
    console.log(selectedDateString);

    const response = await request({
      url: `/reservations/table/${selectedTable.value.id}/future`,
      method: "GET",
      headers: {
        Authorization: `Bearer ${userInfo.value.token}`,
      },
      params: {
        date: selectedDateString,
      },
    });

    // Update reserved time slots
    bookedTimeSlots.value = response.data || [];

    // Initialize all time slots as available
    availableTimeSlots.value.forEach((slot) => {
      slot.disabled = false;
    });

    // Loop through all reserved time periods and disable corresponding time slots
    bookedTimeSlots.value.forEach((booking) => {
      if (!booking.reservationTime) return;

      // Parse reservation time and date
      const bookingDate = new Date(booking.reservationTime);
      const bookingDateString = bookingDate.toISOString().split("T")[0];

      // Only handle reservations for the current day
      if (bookingDateString === selectedDateString) {
        const reservationHour = bookingDate.getUTCHours();
        const duration = booking.reservationDate || 1; // Use reservation duration, default is 1 hour

        // Disable all time slots occupied by the current reservation
        availableTimeSlots.value.forEach((slot) => {
          if (
            slot.hour >= reservationHour &&
            slot.hour < reservationHour + duration
          ) {
            slot.disabled = true;
          }
        });
      }
    });
  } catch (error) {
    console.error("Failed to fetch reservation information", error);
    ElMessage.error("Failed to load reservation information, please try again later");
  } finally {
    loadingTimeSlots.value = false;
  }
};

// Handle time selection
const handleTimeSelection = (timeSlot) => {
  const index = selectedTimeSlots.value.findIndex(
    (slot) => slot.value === timeSlot.value
  );

  if (index > -1) {
    // If already selected, remove selection
    selectedTimeSlots.value.splice(index, 1);
  } else {
    // If not selected, add to selection list
    selectedTimeSlots.value.push(timeSlot);
  }

  // Sort selected time slots
  selectedTimeSlots.value.sort((a, b) => a.hour - b.hour);

  // Check if they are consecutive
  validateTimeSelection();
};

// Validate time selection
const validateTimeSelection = () => {
  if (selectedTimeSlots.value.length <= 1) {
    return true;
  }

  // Copy and sort by hour
  const sortedSlots = [...selectedTimeSlots.value].sort(
    (a, b) => a.hour - b.hour
  );

  // Check if times are consecutive
  for (let i = 1; i < sortedSlots.length; i++) {
    if (sortedSlots[i].hour !== sortedSlots[i - 1].hour + 1) {
      ElMessage.warning("Please select consecutive time slots");
      // Remove the last added time slot that is not consecutive
      selectedTimeSlots.value = selectedTimeSlots.value.filter(
        (slot) => slot.value !== sortedSlots[i].value
      );
      return false;
    }
  }

  return true;
};

// Get selected time range display
const selectedTimeRange = computed(() => {
  if (!selectedTimeSlots.value.length) return "";

  // Sort by hour
  const sortedSlots = [...selectedTimeSlots.value].sort(
    (a, b) => a.hour - b.hour
  );

  if (sortedSlots.length === 1) {
    return sortedSlots[0].label;
  }

  const startHour = sortedSlots[0].hour;
  const endHour = sortedSlots[sortedSlots.length - 1].hour + 1;

  return `${startHour}:00-${endHour}:00`;
});

// Confirm reservation
const confirmReservation = async () => {
  if (
    !selectedTable.value ||
    !selectedDate.value ||
    selectedTimeSlots.value.length === 0
  ) {
    ElMessage.warning("Please select a reservation time");
    return;
  }

  if (
    personCount.value <= 0 ||
    personCount.value > selectedTable.value.capacity
  ) {
    ElMessage.warning(`Number of diners must be between 1 and ${selectedTable.value.capacity}`);
    return;
  }

  try {
    const dateStr = selectedDate.value.toISOString().split("T")[0];

    // Sort selected time slots by hour
    const sortedSlots = [...selectedTimeSlots.value].sort(
      (a, b) => a.hour - b.hour
    );
    const startTime = sortedSlots[0].value;

    // Calculate reservation duration (in hours)
    const reservationDuration = sortedSlots.length;

    // Assemble ISO format time string "2023-04-20T18:30:00Z"
    const reservationTime = `${dateStr}T${startTime}:00Z`;

    await request({
      url: "/reservations",
      method: "POST",
      headers: {
        Authorization: `Bearer ${userInfo.value.token}`,
      },
      data: {
        restaurantId: parseInt(restaurantId),
        tableId: parseInt(selectedTable.value.id),
        reservationTime: reservationTime,
        reservationDate: reservationDuration, // Use reservationDate as a duration attribute
        personCount: personCount.value,
        remarks: reservationRemarks.value,
      },
    });

    ElMessage.success("Reservation successful");
    timeSelectionVisible.value = false;

    // Clear selection
    selectedTimeSlots.value = [];
    personCount.value = 1;
    reservationRemarks.value = "";

    // After successful reservation, you can redirect to my reservations page or stay on the current page
    // router.push('/my-reservations');
  } catch (error) {
    ElMessage.error(error.response?.data?.message || "Failed to make reservation, please try again later");
    console.error("Failed to make reservation", error);
  }
};

// Listen for date change to generate time slots
watch(selectedDate, () => {
  // Clear previous selection
  selectedTimeSlots.value = [];
  generateTimeSlots();
});

// Generate default time slots when mounted
onMounted(() => {
  fetchRestaurantDetail();
  generateTimeSlots();
  fetchReviews();
});

// Video control methods
const playVideo = (event) => {
  event.preventDefault();
  const videoContainer = event.currentTarget;
  const video = videoContainer.querySelector("video");

  if (video) {
    // Handle video play/pause logic
    if (video.paused) {
      // If video is paused, play video
      videoContainer.classList.add("playing");
      video.play().catch((error) => {
        console.error("Failed to play video:", error);
        videoContainer.classList.remove("playing");
      });

      // Add event listener for video end
      video.onended = () => {
        videoContainer.classList.remove("playing");
      };
    } else {
      // If video is playing, pause video
      video.pause();
      videoContainer.classList.remove("playing");
    }
  }
};

// Update tableTypes computed property to handle English table types
const tableTypes = computed(() => {
  const types = {};
  
  // Group tables by type and count
  if (restaurant.value?.tables && restaurant.value.tables.length > 0) {
    // First find all available table types
    const availableTypes = new Set();
    restaurant.value.tables.forEach(table => {
      availableTypes.add(table.type);
    });
    
    // Then count by type
    restaurant.value.tables.forEach(table => {
      // Get table type
      let typeName, typeColor, typeIcon;
      
      if (table.type.includes('VIP')) {
        typeName = 'VIP Table';
        typeColor = '#ff9800';
        typeIcon = 'TrophyBase';
      } else if (table.type === 'Standard' || table.type === 'Standard Table') {
        typeName = 'Standard Table';
        typeColor = '#26a69a';
        typeIcon = 'Menu';
      } else {
        // For other unrecognized types, use the type name
        typeName = table.type || 'Regular Table';
        typeColor = '#5e6687';
        typeIcon = 'Menu';
      }
      
      if (!types[typeName]) {
        types[typeName] = {
          name: typeName,
          count: 1,
          color: typeColor,
          icon: typeIcon,
          cssVar: typeColor // Store CSS variable
        };
      } else {
        types[typeName].count++;
      }
    });
  }
  
  // Convert to array
  return Object.values(types);
});

const tableListVisible = ref(false);
const selectedTableType = ref('');

// Filtered table list
const filteredTables = computed(() => {
  if (!selectedTableType.value || !restaurant.value?.tables) return [];
  
  // Filter tables by selected type
  return restaurant.value.tables.filter(table => {
    if (selectedTableType.value === 'VIP Table' && table.type.includes('VIP')) {
      return true;
    } else if (selectedTableType.value === 'Standard Table' && 
              (table.type === 'Standard' || table.type === 'Standard Table')) {
      return true;
    } else if (selectedTableType.value === table.type) {
      // Direct match for type name
      return true;
    }
    return false;
  });
});

// Show tables by type
const showTablesByType = (typeName) => {
  selectedTableType.value = typeName;
  tableListVisible.value = true;
};

// Table capacity filter
const capacityFilter = ref('all');

// Filter tables by capacity
const filteredTablesByCapacity = computed(() => {
  if (capacityFilter.value === 'all') {
    return filteredTables.value;
  }
  
  return filteredTables.value.filter(table => {
    const capacity = table.capacity || 0;
    
    if (capacityFilter.value === '1-2') {
      return capacity >= 1 && capacity <= 2;
    } else if (capacityFilter.value === '3-4') {
      return capacity >= 3 && capacity <= 4;
    } else if (capacityFilter.value === '5-8') {
      return capacity >= 5 && capacity <= 8;
    } else if (capacityFilter.value === '9+') {
      return capacity >= 9;
    }
    
    return true;
  });
});
</script>

<style lang="scss" scoped>
// Variable definitions
$primary-color: #e02020;
$secondary-color: #fff0f0;
$text-primary: #333;
$text-secondary: #666;
$text-light: #999;
$border-radius: 8px;
$box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
$gradient-primary: linear-gradient(135deg, $primary-color, #ff6b6b);

.restaurant-detail-container {
  position: relative;
  background-color: #f8f9fa;
  min-height: 100vh;
}

// Top Navigation Bar
.detail-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  padding: 20px;

  .header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .back-button,
    .favorite-button,
    .share-button {
      width: 44px;
      height: 44px;
      background-color: rgba(255, 255, 255, 0.8);
      backdrop-filter: blur(10px);
      border: none;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
      color: $text-primary;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      &.is-favorite {
        background-color: $primary-color;
        color: white;
      }
    }

    .actions-right {
      display: flex;
      gap: 10px;
    }
  }
}

// Restaurant Photo Gallery
.restaurant-gallery {
  position: relative;
  height: 450px;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.3);
    z-index: 1;
  }

  .el-carousel {
    height: 100%;
    --el-carousel-indicator-width: 30px;
    --el-carousel-indicator-height: 3px;

    :deep(.el-carousel__container) {
      height: 100%;
    }

    :deep(.el-carousel__indicator--active) {
      .el-carousel__button {
        background-color: #fff;
      }
    }

    :deep(.el-carousel__arrow) {
      background-color: rgba(255, 255, 255, 0.8);
      backdrop-filter: blur(10px);
      color: $text-primary;
      border-radius: 50%;
      width: 50px;
      height: 50px;

      &:hover {
        background-color: white;
      }
    }

    :deep(.el-carousel__indicators--outside) {
      bottom: 20px;
      z-index: 10;
    }
  }

  .gallery-item {
    width: 100%;
    height: 100%;
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    transition: transform 0.8s ease;

    &:hover {
      transform: scale(1.05);
    }
  }

  .restaurant-title-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 40px 30px 50px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
    color: #fff;
    z-index: 5;

    h1 {
      font-size: 42px;
      font-weight: 800;
      margin: 0 0 10px;
      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
      animation: fadeInUp 0.5s ease-out forwards;
    }

    .quick-info {
      display: flex;
      align-items: center;
      gap: 15px;
      animation: fadeInUp 0.5s 0.1s ease-out forwards;
      opacity: 0;

      .info-item {
        display: flex;
        align-items: center;
        gap: 5px;

        .el-icon {
          font-size: 16px;
        }

        &.cuisine {
          background-color: rgba(255, 255, 255, 0.2);
          padding: 4px 12px;
          border-radius: 20px;
          backdrop-filter: blur(5px);
        }
      }

      .rating-display {
        display: flex;
        align-items: center;
        gap: 8px;
        background-color: rgba(255, 255, 255, 0.2);
        padding: 4px 12px;
        border-radius: 20px;
        backdrop-filter: blur(5px);

        .rating-value {
          font-weight: 700;
          color: #ffcc00;
          font-size: 18px;
        }

        .review-count {
          font-size: 14px;
          opacity: 0.8;
        }
      }
    }
  }

  .no-photos {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #eee;
    color: $text-light;

    .el-icon {
      font-size: 48px;
      margin-bottom: 10px;
    }
  }
}

// Restaurant Basic Information Card
.restaurant-info-card {
  background-color: white;
  border-radius: $border-radius;
  margin: 30px 20px 20px;
  padding: 30px;
  position: relative;
  z-index: 5;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);

  &::before {
    content: "";
    position: absolute;
    top: -15px;
    left: 40px;
    width: 80px;
    height: 5px;
    background: $gradient-primary;
    border-radius: 5px;
  }

  .info-section {
    margin-bottom: 30px;

    .info-row {
      display: flex;
      gap: 15px;
      margin-bottom: 20px;
      color: $text-secondary;
      padding: 10px 15px;
      background-color: #f9f9fa;
      border-radius: 10px;
      transition: all 0.3s;

      &:hover {
        background-color: $secondary-color;
        transform: translateX(5px);
      }

      .el-icon {
        color: $primary-color;
        flex-shrink: 0;
        font-size: 20px;
      }
    }
  }

  .description-section {
    position: relative;

    h3 {
      font-size: 22px;
      margin: 0 0 15px;
      color: $text-primary;
      font-weight: 600;
      display: inline-flex;
      position: relative;

      &::after {
        content: "";
        position: absolute;
        bottom: -5px;
        left: 0;
        width: 100%;
        height: 2px;
        background: $gradient-primary;
        border-radius: 2px;
      }
    }

    p {
      color: $text-secondary;
      line-height: 1.8;
      margin: 0;
      font-size: 16px;
      padding: 0 0 0 15px;
      border-left: 3px solid $secondary-color;
    }
  }
}

// Table Information Section
.tables-section {
  background-color: white;
  border-radius: $border-radius;
  margin: 0 20px 30px;
  padding: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    right: 0;
    width: 150px;
    height: 150px;
    background: radial-gradient(
      circle,
      $secondary-color 0%,
      rgba(255, 255, 255, 0) 70%
    );
    border-radius: 50%;
    opacity: 0.7;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30px;

    h2 {
      margin: 0;
      font-size: 24px;
      color: $text-primary;
      font-weight: 700;
      position: relative;
      padding-left: 15px;

      &::before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        width: 5px;
        height: 100%;
        background: $gradient-primary;
        border-radius: 5px;
      }
    }

    .reserve-button {
      background: $gradient-primary;
      border: none;
      padding: 12px 25px;
      border-radius: 30px;
      font-size: 16px;
      font-weight: 500;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba($primary-color, 0.3);
      }
    }
  }

  .tables-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 25px;

    .table-card {
      border: none;
      border-radius: 16px;
      overflow: visible;
      position: relative;
      cursor: pointer;
      box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
      transition: all 0.4s cubic-bezier(0.215, 0.61, 0.355, 1);
      padding: 0;

      &::before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: radial-gradient(
          circle at top right,
          rgba(255, 255, 255, 0.8),
          transparent 70%
        );
        border-radius: 16px;
        opacity: 0.6;
        z-index: 0;
      }

      &:hover {
        transform: translateY(-12px) scale(1.03);
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);

        .table-actions {
          opacity: 1;
          transform: translateY(0);
        }

        .table-header {
          .table-icon-container {
            transform: translateY(-5px) scale(1.1);
          }
        }

        .table-type-tag {
          background: $gradient-primary;
          color: white;
        }
      }

      &.vip-table {
        border: 2px solid #ffc107;
        background: linear-gradient(145deg, #fffaf0, #fff5e6);
        box-shadow: 0 10px 25px rgba(255, 153, 0, 0.15);

        &::before {
          background: radial-gradient(
            circle at top right,
            rgba(255, 246, 220, 0.8),
            transparent 80%
          );
        }

        .vip-ribbon {
          position: absolute;
          top: 20px;
          right: -35px;
          background: linear-gradient(45deg, #ffc107, #ff9800);
          color: white;
          font-weight: bold;
          padding: 5px 40px;
          transform: rotate(45deg);
          z-index: 10;
          font-size: 14px;
          box-shadow: 0 2px 10px rgba(255, 153, 0, 0.3);
          letter-spacing: 2px;
        }

        .table-actions .el-button {
          background: linear-gradient(45deg, #ffc107, #ff9800);
          border-color: #ff9800;
          color: white;

          &:hover {
            box-shadow: 0 5px 15px rgba(255, 153, 0, 0.3);
            background: linear-gradient(45deg, #ff9800, #ffc107);
          }
        }
      }

      &.large-table {
        border: 2px solid #3f51b5;
        background: linear-gradient(145deg, #f0f3ff, #e6ebff);
        box-shadow: 0 10px 25px rgba(63, 81, 181, 0.15);

        &::before {
          background: radial-gradient(
            circle at top right,
            rgba(220, 230, 255, 0.8),
            transparent 80%
          );
        }

        .large-ribbon {
          position: absolute;
          top: 12px;
          right: -30px;
          background: linear-gradient(45deg, #3f51b5, #5c6bc0);
          color: white;
          font-weight: bold;
          padding: 3px 30px;
          transform: rotate(45deg);
          z-index: 10;
          font-size: 13px;
          box-shadow: 0 2px 10px rgba(63, 81, 181, 0.3);
        }

        .table-actions .el-button {
          background: linear-gradient(45deg, #3f51b5, #5c6bc0);
          border-color: #3f51b5;
          color: white;

          &:hover {
            box-shadow: 0 5px 15px rgba(63, 81, 181, 0.3);
            background: linear-gradient(45deg, #5c6bc0, #3f51b5);
          }
        }
      }

      &.standard-table {
        border: 2px solid #26c6da;
        background: linear-gradient(145deg, #f0fafb, #e6f7f9);
        box-shadow: 0 10px 25px rgba(38, 198, 218, 0.1);

        &::before {
          background: radial-gradient(
            circle at top right,
            rgba(220, 245, 255, 0.8),
            transparent 80%
          );
        }
      }

      .table-status {
        position: absolute;
        top: 15px;
        left: 15px;
        z-index: 5;

        .status-badge {
          padding: 4px 12px;
          border-radius: 20px;
          font-size: 12px;
          font-weight: 600;
          box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);

          &.available {
            background: linear-gradient(45deg, #4caf50, #8bc34a);
            color: white;
          }

          &.unavailable {
            background: linear-gradient(45deg, #f44336, #ff5252);
            color: white;
          }
        }
      }

      .table-header {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 30px 20px 15px;
        position: relative;
        z-index: 2;

        .table-icon-container {
          width: 70px;
          height: 70px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: 15px;
          transition: all 0.3s ease;
          background: rgba(255, 255, 255, 0.9);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);

          .table-icon {
            font-size: 30px;

            &.vip {
              color: #ffc107;
            }

            &.large {
              color: #3f51b5;
            }

            &.standard {
              color: #26c6da;
            }
          }
        }

        .table-type-tag {
          background-color: #f5f5f5;
          color: #333;
          padding: 5px 15px;
          border-radius: 20px;
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 10px;
          transition: all 0.3s ease;
        }
      }

      .table-body {
        padding: 0 20px 20px;
        position: relative;
        z-index: 2;

        .table-features {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          margin-bottom: 15px;

          .feature {
            background-color: #f0f0f0;
            padding: 3px 10px;
            border-radius: 15px;
            font-size: 12px;
            color: #555;

            span {
              display: block;
            }
          }
        }

        .table-info {
          background-color: rgba(255, 255, 255, 0.7);
          border-radius: 10px;
          padding: 12px;

          .table-capacity,
          .table-availability {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 8px;
            color: $text-secondary;
            font-size: 14px;

            &:last-child {
              margin-bottom: 0;
            }

            .el-icon {
              color: $primary-color;
              width: 28px;
              height: 28px;
              border-radius: 50%;
              display: flex;
              align-items: center;
              justify-content: center;
              flex-shrink: 0;
            }
          }
        }
      }

      .table-actions {
        padding: 0 20px 20px;
        opacity: 0.9;
        transform: translateY(5px);
        transition: all 0.3s ease;
        position: relative;
        z-index: 2;

        .el-button {
          width: 100%;
          height: 44px;
          font-size: 16px;
          font-weight: 500;
          border-radius: 10px;
          transition: all 0.3s ease;

          &:hover {
            transform: translateY(-3px);
            box-shadow: 0 8px 15px rgba($primary-color, 0.2);
          }
        }
      }
    }

    .no-tables {
      grid-column: 1 / -1;
      padding: 40px 0;
    }
  }
}

// 桌子类型分类面板
.table-types-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 25px;
  margin-bottom: 30px;

  .table-type-card {
    background-color: white;
    border-radius: $border-radius;
    padding: 25px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.07);
    transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
    cursor: pointer;
    position: relative;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    border: 1px solid rgba(0, 0, 0, 0.03);
    --type-color: #5e6687; /* 默认颜色，会被动态覆盖 */

    &::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(180deg, rgba(255, 255, 255, 0) 70%, rgba(255, 255, 255, 0.8) 100%);
      opacity: 0;
      transition: opacity 0.4s ease;
      z-index: 1;
    }

    &:hover {
      transform: translateY(-10px);
      box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);

      &::after {
        opacity: 1;
      }
      
      .table-type-icon {
        transform: scale(1.1);
      }
    }

    &.vip-table {
      background: linear-gradient(to bottom, #fff9f0, #fff);
      border: 1px solid rgba(255, 193, 7, 0.2);
      
      &::before {
        content: 'VIP';
        position: absolute;
        top: 10px;
        right: 10px;
        background: linear-gradient(45deg, #ffd700, #ffaa00);
        color: white;
        font-size: 12px;
        font-weight: bold;
        padding: 3px 10px;
        border-radius: 20px;
        box-shadow: 0 3px 6px rgba(255, 170, 0, 0.3);
        z-index: 2;
      }
      
      &:hover {
        box-shadow: 0 15px 30px rgba(255, 193, 7, 0.15);
      }
    }

    .table-type-icon {
      width: 80px;
      height: 80px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f7fa;
      border-radius: 50%;
      margin-bottom: 20px;
      transition: all 0.3s ease;
      position: relative;
      z-index: 2;
      
      .type-icon {
        font-size: 40px;
        color: var(--type-color);
      }
    }

    .table-type-info {
      position: relative;
      z-index: 2;
      
      .type-name {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 10px;
        color: var(--type-color);
      }
      
      .table-count {
        font-size: 16px;
        color: $text-secondary;
        background-color: #f5f7fa;
        padding: 8px 15px;
        border-radius: 20px;
        display: inline-block;
      }
    }
  }
}

// Restaurant Map Location
.map-section {
  background-color: white;
  border-radius: $border-radius;
  margin: 0 20px 20px;
  padding: 25px;
  box-shadow: $box-shadow;

  h2 {
    margin: 0 0 20px;
    font-size: 22px;
    color: $text-primary;
    font-weight: 600;
  }

  .map-container {
    display: flex;
    flex-direction: column;
    gap: 15px;

    .google-map-container {
      height: 300px;
      border-radius: $border-radius;
      overflow: hidden;
      position: relative;
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);

      iframe {
        width: 100%;
        height: 100%;
        border: none;
        position: relative;
        z-index: 1;
        transition: all 0.3s ease;

        &:hover {
          box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15);
        }
      }

      &::before {
        content: "";
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4px;
        background: $gradient-primary;
        z-index: 2;
        border-radius: $border-radius $border-radius 0 0;
      }
    }

    .map-placeholder {
      height: 300px;
      background-color: #f5f5f5;
      border-radius: $border-radius;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: $text-light;

      .el-icon {
        font-size: 36px;
        margin-bottom: 10px;
      }
    }

    .address-detail {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 15px;
      background-color: #f9f9fa;
      border-radius: 10px;

      p {
        display: flex;
        align-items: center;
        gap: 8px;
        color: $text-secondary;
        margin: 0;
        font-weight: 500;

        .el-icon {
          color: $primary-color;
        }
      }

      .el-button {
        display: flex;
        align-items: center;
        gap: 8px;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 5px 15px rgba($primary-color, 0.2);
        }
      }
    }
  }
}

// Reviews Section
.reviews-section {
  background-color: white;
  border-radius: $border-radius;
  margin: 0 20px 80px;
  padding: 25px;
  box-shadow: $box-shadow;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      font-size: 22px;
      color: $text-primary;
      font-weight: 600;
    }
  }

  .rating-filter {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    background-color: #f8f9fa;
    padding: 12px 15px;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;

    .filter-label {
      margin-right: 10px;
      font-weight: 600;
    }

    .rating-buttons {
      display: flex;
      gap: 5px;
      flex-wrap: wrap;

      .el-button {
        padding: 5px 10px;
        border-radius: 15px;
        transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        position: relative;
        overflow: hidden;

        &::before {
          content: "";
          position: absolute;
          top: 0;
          left: -100%;
          width: 100%;
          height: 100%;
          background: linear-gradient(
            90deg,
            transparent,
            rgba(255, 255, 255, 0.2),
            transparent
          );
          transition: all 0.5s;
        }

        &.active-rating {
          background-color: $primary-color;
          color: white;
          box-shadow: 0 3px 10px rgba($primary-color, 0.3);
          font-weight: 500;
          transform: translateY(-2px);

          .el-icon {
            animation: pulse 1.5s infinite;
          }
        }

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 5px 10px rgba(0, 0, 0, 0.1);

          &::before {
            left: 100%;
          }
        }
      }
    }
  }

  .reviews-container {
    min-height: 300px; // 添加最小高度，确保在加载过程中不会收缩

    .no-reviews {
      padding: 30px 0;
    }

    .reviews-list {
      transition: opacity 0.3s ease, transform 0.3s ease;
      min-height: 300px; /* 增加最小高度，防止内容变化时的闪烁 */

      .review-item {
        margin-bottom: 30px;
        padding-bottom: 25px;
        border-bottom: 1px solid #eee;
        transition: transform 0.3s ease, opacity 0.3s ease;

        &:last-child {
          margin-bottom: 20px;
        }

        .review-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 15px;

          .user-info {
            display: flex;
            align-items: center;
            gap: 12px;

            .el-avatar {
              border: 2px solid #f5f5f5;
              box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
            }

            .user-details {
              .user-name {
                font-size: 16px;
                font-weight: 600;
                color: $text-primary;
                margin-bottom: 4px;
              }

              .review-time {
                font-size: 12px;
                color: $text-light;
              }
            }
          }

          .review-rating {
            display: flex;
            align-items: center;
            gap: 8px;

            .rating-value {
              font-size: 18px;
              font-weight: 700;
              color: #ffcc00;
            }
          }
        }

        .review-content {
          p {
            margin: 0 0 15px;
            line-height: 1.6;
            color: $text-secondary;
            font-size: 15px;
            position: relative;
            padding-left: 15px;

            &::before {
              content: "";
              position: absolute;
              left: 0;
              top: 0;
              bottom: 0;
              width: 3px;
              background-color: #f0f0f0;
              border-radius: 3px;
            }
          }

          .review-media {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 15px;

            .review-media-item {
              width: 150px;
              height: 150px;
              border-radius: 8px;
              overflow: hidden;
              position: relative;
              box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
              transition: all 0.3s ease;

              &:hover {
                transform: scale(1.03);
                box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
              }

              .image-error {
                height: 100%;
                width: 100%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                background-color: #f5f5f5;
                color: #999;

                .el-icon {
                  font-size: 40px;
                  margin-bottom: 10px;
                }

                span {
                  font-size: 14px;
                }
              }

              &.video-container {
                background-color: #000;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                position: relative;

                .review-video {
                  width: 100%;
                  height: 100%;
                  object-fit: cover;
                  z-index: 1;
                }

                /* Enhance video control visibility */
                video::-webkit-media-controls {
                  z-index: 10;
                }
              }
            }
          }

          .review-videos {
            display: none; // No longer used separately
          }
        }
      }

      .load-more {
        display: flex;
        justify-content: center;
        margin-top: 20px;

        .el-button {
          padding: 10px 30px;
          transition: all 0.3s;

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
          }
        }
      }
    }
  }
}

// Bottom Action Bar
.bottom-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 -5px 20px rgba(0, 0, 0, 0.08);
  padding: 15px 20px;
  z-index: 50;
  display: flex;
  justify-content: center;

  .reserve-action-btn {
    background: $gradient-primary;
    border: none;
    width: 80%;
    max-width: 400px;
    height: 52px;
    font-size: 18px;
    font-weight: 600;
    border-radius: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
    position: relative;
    overflow: hidden;

    &::before {
      content: "";
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(
        90deg,
        transparent,
        rgba(255, 255, 255, 0.2),
        transparent
      );
      transition: all 0.5s;
    }

    &:hover {
      transform: translateY(-2px) scale(1.02);
      box-shadow: 0 10px 25px rgba($primary-color, 0.3);

      &::before {
        left: 100%;
      }
    }

    .el-icon {
      font-size: 20px;
    }
  }
}

// Responsive adjustments
@media (max-width: 768px) {
  .restaurant-gallery {
    height: 250px;
  }

  .restaurant-info-card {
    .restaurant-meta {
      .restaurant-name {
        font-size: 24px;
      }
    }
  }

  .tables-section {
    .tables-container {
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
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

// Time Selection Dialog Style
.time-selection-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1), 0 8px 20px rgba(0, 0, 0, 0.08);
  }

  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #f5f5f5;
    margin-right: 0;
    position: relative;
    background: linear-gradient(to right, lighten($primary-color, 40%), white);

    &::after {
      content: "";
      position: absolute;
      bottom: -1px;
      left: 0;
      width: 100px;
      height: 3px;
      background: $gradient-primary;
      border-radius: 3px 3px 0 0;
    }

    .el-dialog__title {
      font-size: 22px;
      color: $text-primary;
      font-weight: 700;
      position: relative;

      &::before {
        content: "";
        position: absolute;
        left: -12px;
        top: 0;
        height: 100%;
        width: 4px;
        background: $primary-color;
        border-radius: 2px;
      }
    }

    .el-dialog__headerbtn {
      top: 23px;
      right: 24px;

      .el-dialog__close {
        color: $text-secondary;
        font-size: 22px;
        transition: all 0.3s;

        &:hover {
          color: $primary-color;
          transform: rotate(90deg);
        }
      }
    }
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 20px 24px;
    border-top: 1px solid #f5f5f5;
    background-color: rgba(249, 250, 251, 0.8);
    backdrop-filter: blur(10px);
  }

  .time-selection-content {
    .selected-table-info {
      display: flex;
      gap: 24px;
      padding: 24px;
      position: relative;
      border-bottom: 1px dashed #eaeaea;
      background: linear-gradient(to right, #fcfcfc, white);

      .table-icon-wrapper {
        position: relative;

        &::after {
          content: "";
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 100px;
          height: 100px;
          background: radial-gradient(
            circle,
            rgba($primary-color, 0.08) 0%,
            rgba(255, 255, 255, 0) 70%
          );
          border-radius: 50%;
          z-index: 0;
        }
      }

      .table-icon {
        background-color: #f8f9fa;
        width: 80px;
        height: 80px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
        border: 5px solid white;
        transition: all 0.3s;
        z-index: 1;

        &:hover {
          transform: scale(1.05);
        }

        .el-icon {
          font-size: 36px;
          color: $primary-color;
        }

        &.vip-icon {
          background: linear-gradient(135deg, #fff9e6, #fff5e5);
          border-color: #fffbf0;

          .el-icon {
            color: #ffc107;
          }

          &::after {
            content: "VIP";
            position: absolute;
            bottom: -10px;
            right: -10px;
            background: linear-gradient(45deg, #ffc107, #ff9800);
            color: white;
            font-size: 12px;
            font-weight: bold;
            padding: 3px 8px;
            border-radius: 10px;
            box-shadow: 0 3px 8px rgba(255, 152, 0, 0.2);
          }
        }

        &.large-icon {
          background: linear-gradient(135deg, #f0f3ff, #ebeeff);
          border-color: #f7f8ff;

          .el-icon {
            color: #3f51b5;
          }
        }

        &.standard-icon {
          background: linear-gradient(135deg, #e8f9fb, #ecfeff);
          border-color: #f0fdff;

          .el-icon {
            color: #26c6da;
          }
        }
      }

      .table-details {
        flex: 1;

        h3 {
          margin: 0 0 8px;
          font-size: 22px;
          color: $text-primary;
          font-weight: 700;
          position: relative;
          display: inline-block;

          &::after {
            content: "";
            position: absolute;
            bottom: -2px;
            left: 0;
            width: 100%;
            height: 2px;
            background: $gradient-primary;
            border-radius: 2px;
          }
        }

        p {
          margin: 0 0 12px;
          color: $text-secondary;
          font-size: 16px;
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            color: $primary-color;
            font-size: 18px;
          }
        }

        .table-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;

          .tag {
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 500;

            &.vip-tag {
              background-color: rgba(255, 193, 7, 0.15);
              color: #ffab00;
            }

            &.large-tag {
              background-color: rgba(63, 81, 181, 0.15);
              color: #3949ab;
            }

            &.standard-tag {
              background-color: rgba(38, 198, 218, 0.15);
              color: #00acc1;
            }
          }
        }
      }
    }

    .selection-container {
      display: grid;
      grid-template-columns: 240px 1fr;
      gap: 0;
      border-bottom: 1px dashed #eaeaea;

      .date-selection {
        padding: 24px;
        border-right: 1px dashed #eaeaea;
        background-color: #fcfcfc;

        h4 {
          font-size: 17px;
          margin: 0 0 16px;
          color: $text-primary;
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            color: $primary-color;
          }
        }

        .el-date-picker {
          width: 100%;
        }

        .note {
          margin: 16px 0 0;
          color: $primary-color;
          font-size: 13px;
          padding: 8px 12px;
          border-radius: 8px;
          background-color: $secondary-color;
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            color: $primary-color;
            font-size: 16px;
            flex-shrink: 0;
          }
        }
      }

      .time-slots {
        padding: 24px;

        h4 {
          font-size: 17px;
          margin: 0 0 16px;
          color: $text-primary;
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            color: $primary-color;
          }

          .selected-time-range {
            font-size: 14px;
            background: $gradient-primary;
            color: white;
            padding: 4px 12px;
            border-radius: 20px;
            margin-left: 10px;
            display: inline-flex;
            align-items: center;
            gap: 5px;
            font-weight: 500;
            box-shadow: 0 3px 8px rgba($primary-color, 0.2);
          }
        }

        .time-hint,
        .selection-hint {
          margin: 0 0 8px;
          color: $text-secondary;
          font-size: 14px;
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            color: $primary-color;
            font-size: 16px;
            flex-shrink: 0;
          }
        }

        .day-type-hint {
          margin: 0 0 8px;
          color: $primary-color;
          font-size: 14px;
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 5px 12px;
          background-color: rgba($primary-color, 0.05);
          border-radius: 8px;

          .el-icon {
            color: $primary-color;
            font-size: 16px;
            flex-shrink: 0;
          }
        }

        .selection-hint {
          margin-bottom: 16px;
          color: #ff9800;
          background-color: rgba(255, 152, 0, 0.08);
          padding: 8px 12px;
          border-radius: 8px;

          .el-icon {
            color: #ff9800;
          }
        }

        .time-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
          gap: 12px;

          .el-button {
            padding: 14px 10px;
            border-radius: 10px;
            transition: all 0.3s ease;

            &.selected {
              background: $gradient-primary;
              color: white;
              border-color: $primary-color;
              transform: translateY(-3px);
              box-shadow: 0 8px 15px rgba($primary-color, 0.2);
              position: relative;
              font-weight: 600;

              &:not(:last-of-type) {
                border-right: 2px solid white;
              }

              &:not(:first-of-type) {
                border-left: 2px solid white;
              }

              &::after {
                content: "";
                position: absolute;
                bottom: -5px;
                left: 50%;
                transform: translateX(-50%);
                width: 8px;
                height: 8px;
                background-color: $primary-color;
                border-radius: 50%;
              }
            }

            &:hover:not(.is-disabled):not(.selected) {
              background-color: $secondary-color;
              border-color: $primary-color;
              transform: translateY(-3px);
              box-shadow: 0 5px 10px rgba(0, 0, 0, 0.05);
            }

            &.is-disabled {
              opacity: 0.5;
              text-decoration: line-through;
              background-color: #f5f5f5;
              color: #aaa;
            }
          }
        }
      }
    }

    .reservation-details {
      padding: 24px;

      h4 {
        font-size: 17px;
        margin: 0 0 16px;
        color: $text-primary;
        display: flex;
        align-items: center;
        gap: 8px;
        position: relative;

        .el-icon {
          color: $primary-color;
        }

        &::after {
          content: "";
          position: absolute;
          bottom: -8px;
          left: 0;
          width: 100px;
          height: 2px;
          background: $gradient-primary;
          border-radius: 2px;
        }
      }

      .details-grid {
        display: grid;
        grid-template-columns: 1fr 2fr;
        gap: 20px;

        .detail-item {
          position: relative;

          label {
            display: block;
            margin-bottom: 10px;
            color: $text-secondary;
            font-weight: 500;
            font-size: 15px;
          }

          .el-input-number {
            width: 100%;
          }

          .capacity-info {
            display: block;
            margin-top: 8px;
            font-size: 13px;
            color: #9e9e9e;
          }

          :deep(.el-input__wrapper) {
            border-radius: 8px;
            box-shadow: 0 0 0 1px #e0e0e0;
            padding: 8px 12px;
            transition: all 0.3s;

            &.is-focus {
              box-shadow: 0 0 0 2px rgba($primary-color, 0.2);
            }
          }

          :deep(.el-textarea__inner) {
            border-radius: 10px;
            border: 1px solid #e0e0e0;
            padding: 12px;
            transition: all 0.3s;
            resize: none;

            &:focus {
              border-color: $primary-color;
              box-shadow: 0 0 0 2px rgba($primary-color, 0.1);
            }
          }

          :deep(.el-input-number__decrease),
          :deep(.el-input-number__increase) {
            border-color: #e0e0e0;
            color: $text-secondary;

            &:hover {
              color: $primary-color;
            }
          }

          &.remarks-item {
            grid-column: span 2;
          }
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .reservation-summary {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      max-width: 65%;

      .summary-item {
        font-size: 14px;

        .label {
          color: $text-secondary;
          margin-right: 5px;
        }

        .value {
          color: $text-primary;
          font-weight: 600;
        }
      }
    }

    .action-buttons {
      display: flex;
      gap: 12px;

      .el-button {
        border-radius: 8px;
        padding: 12px 24px;
        font-size: 16px;

        &.cancel-btn {
          color: $text-secondary;
          border-color: #e0e0e0;

          &:hover {
            color: $text-primary;
            border-color: #ccc;
            background-color: #f5f5f5;
          }
        }

        &.confirm-btn {
          background: $gradient-primary;
          border: none;
          padding: 12px 32px;
          box-shadow: 0 4px 10px rgba($primary-color, 0.2);

          &:hover:not(.is-disabled) {
            transform: translateY(-2px);
            box-shadow: 0 6px 15px rgba($primary-color, 0.3);
          }

          &.is-disabled {
            opacity: 0.7;
            background: #ccc;
          }
        }
      }
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

// Style for loading state of reviews section
.reviews-section {
  :deep(.el-loading-mask) {
    background-color: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(3px);
    border-radius: $border-radius;
    transition: opacity 0.3s ease;

    .el-loading-spinner {
      .circular {
        animation: rotate 1.5s linear infinite;
      }

      .el-loading-text {
        color: $primary-color;
        font-weight: 500;
        margin-top: 8px;
      }
    }
  }
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: 20px;
  position: relative;
  padding-left: 15px;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 5px;
    height: 24px;
    background: $gradient-primary;
    border-radius: 3px;
  }
}

.table-list-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #f0f0f0;
    margin-right: 0;

    
    .el-dialog__title {
      font-size: 20px;
      font-weight: 700;
      color: $text-primary;
    }
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
    max-height: 70vh;
    overflow-y: auto;
  }
  
  .tables-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 25px;
  }
}

// 桌子列表弹窗样式优化
.table-list-dialog {
  :deep(.el-dialog) {
    border-radius: 20px;
    overflow: hidden;
    background: #f9fafc;
    box-shadow: 0 24px 48px rgba(0, 0, 0, 0.1);
  }
  
  :deep(.el-dialog__header) {
    padding: 20px 30px;
    border-bottom: 1px solid #eaedf2;
    background-color: white;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      left: 0;
      bottom: 0;
      width: 100px;
      height: 3px;
      background: $gradient-primary;
    }
    
    .el-dialog__title {
      font-size: 22px;
      font-weight: 800;
      background: $gradient-primary;
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
      display: inline-block;
    }
    
    .el-dialog__headerbtn {
      top: 20px;
      right: 20px;
      
      .el-dialog__close {
        font-size: 20px;
        color: #5e6687;
        transition: all 0.3s;
        
        &:hover {
          transform: rotate(90deg);
          color: $primary-color;
        }
      }
    }
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
    background-color: #f9fafc;
  }
}

// 过滤栏样式
.table-filter-bar {
  padding: 20px 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: white;
  border-bottom: 1px solid #eaedf2;
  
  .filter-title {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #5e6687;
    font-weight: 600;
    font-size: 16px;
    
    .el-icon {
      color: $primary-color;
      font-size: 18px;
    }
  }
  
  .capacity-filter {
    :deep(.el-radio-group) {
      .el-radio-button__inner {
        border-color: #e4e7f0;
        border-left-color: #e4e7f0;
        color: #5e6687;
        padding: 8px 15px;
        transition: all 0.3s;
        
        &:hover:not(.is-active) {
          color: $primary-color;
          background-color: rgba($primary-color, 0.05);
        }
      }
      
      .el-radio-button__original-radio:checked + .el-radio-button__inner {
        background: $gradient-primary;
        border-color: $primary-color;
        box-shadow: -1px 0 0 0 $primary-color;
        color: white;
      }
      
      .el-radio-button:first-child .el-radio-button__inner {
        border-radius: 10px 0 0 10px;
      }
      
      .el-radio-button:last-child .el-radio-button__inner {
        border-radius: 0 10px 10px 0;
      }
    }
  }
}

// 桌子网格容器
.tables-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 25px;
  padding: 30px;
}

// 现代桌子卡片样式
.table-card-modern {
  position: relative;
  background-color: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  display: flex;
  flex-direction: column;
  height: 320px;
  cursor: pointer;
  transform-origin: center bottom;
  
  &:hover {
    transform: translateY(-10px) scale(1.02);
    box-shadow: 0 20px 30px rgba(0, 0, 0, 0.1);
    
    .table-backdrop {
      opacity: 1;
    }
    
    .table-icon-container-modern {
      transform: scale(1.1);
    }
    
    .select-btn {
      transform: translateY(-5px);
      box-shadow: 0 8px 15px rgba($primary-color, 0.3);
    }
  }
  
  .table-backdrop {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle at top right, rgba(255, 255, 255, 0.8) 0%, rgba(255, 255, 255, 0) 70%);
    opacity: 0.5;
    transition: opacity 0.4s ease;
    z-index: 0;
  }
  
  &.vip-table {
    background: linear-gradient(165deg, #fff8e8, white);
    border: 1px solid rgba(255, 193, 7, 0.2);
    
    &::before {
      content: 'VIP';
      position: absolute;
      top: 20px;
      right: 20px;
      background: linear-gradient(45deg, #ffd700, #ff9800);
      color: white;
      font-size: 12px;
      font-weight: bold;
      padding: 4px 12px;
      border-radius: 20px;
      box-shadow: 0 3px 6px rgba(255, 152, 0, 0.3);
      z-index: 3;
      letter-spacing: 1px;
    }
    
    .table-icon-container-modern {
      background-color: rgba(255, 193, 7, 0.1);
      
      &::after {
        background: radial-gradient(circle, rgba(255, 193, 7, 0.2) 0%, rgba(255, 255, 255, 0) 70%);
      }
    }
    
    .table-type-modern {
      color: #ff9800;
    }
    
    .feature-tag {
      background-color: rgba(255, 193, 7, 0.1);
      color: #ff9800;
    }
  }
  
  &.large-table {
    background: linear-gradient(165deg, #f0f5ff, white);
    border: 1px solid rgba(63, 81, 181, 0.2);
    
    .table-icon-container-modern {
      background-color: rgba(63, 81, 181, 0.1);
      
      &::after {
        background: radial-gradient(circle, rgba(63, 81, 181, 0.2) 0%, rgba(255, 255, 255, 0) 70%);
      }
    }
    
    .table-type-modern {
      color: #3f51b5;
    }
    
    .feature-tag {
      background-color: rgba(63, 81, 181, 0.1);
      color: #3f51b5;
    }
  }
  
  &.standard-table {
    background: linear-gradient(165deg, #e8f8f5, white);
    border: 1px solid rgba(38, 166, 154, 0.2);
    
    .table-icon-container-modern {
      background-color: rgba(38, 166, 154, 0.1);
      
      &::after {
        background: radial-gradient(circle, rgba(38, 166, 154, 0.2) 0%, rgba(255, 255, 255, 0) 70%);
      }
    }
    
    .table-type-modern {
      color: #26a69a;
    }
    
    .feature-tag {
      background-color: rgba(38, 166, 154, 0.1);
      color: #26a69a;
    }
  }
  
  .table-status-tag {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 3;
    
    .status-badge {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      
      &.available {
        background-color: rgba(76, 175, 80, 0.1);
        color: #4caf50;
      }
    }
  }
  
  .table-header-modern {
    padding: 20px;
    position: relative;
    display: flex;
    justify-content: space-between;
    align-items: center;
    z-index: 1;
    
    .table-icon-container-modern {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      transition: all 0.3s ease;
      z-index: 2;
      
      &::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 100px;
        height: 100px;
        border-radius: 50%;
        z-index: -1;
      }
      
      .table-icon-modern {
        font-size: 30px;
        color: $primary-color;
        
        &.vip {
          color: #ff9800;
        }
        
        &.large {
          color: #3f51b5;
        }
        
        &.standard {
          color: #26a69a;
        }
      }
    }
    
    .table-id {
      color: #8792a8;
      font-size: 14px;
      font-weight: 600;
    }
  }
  
  .table-content-modern {
    padding: 0 20px;
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    position: relative;
    z-index: 1;
    
    .table-type-modern {
      font-size: 18px;
      font-weight: 700;
      margin: 0 0 15px;
      color: $text-primary;
    }
    
    .table-info-modern {
      margin-bottom: 20px;
      
      .info-row {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 10px;
        color: $text-secondary;
        font-size: 15px;
        
        .el-icon {
          color: $primary-color;
          font-size: 18px;
        }
      }
    }
    
    .table-features-modern {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      
      .feature-tag {
        padding: 5px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
        background-color: rgba($primary-color, 0.1);
        color: $primary-color;
      }
    }
  }
  
  .table-footer-modern {
    padding: 20px;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
    position: relative;
    z-index: 1;
    
    .select-btn {
      width: 100%;
      padding: 12px;
      border-radius: 12px;
      border: none;
      font-weight: 600;
      transition: all 0.3s ease;
      
      &:hover {
        opacity: 0.9;
      }
    }
  }
}

// 无桌子时的样式
.no-tables-modern {
  grid-column: 1 / -1;
  padding: 50px 0;
  
  :deep(.el-empty) {
    padding: 40px;
  }
  
  .empty-icon-container {
    width: 120px;
    height: 120px;
    background-color: rgba($primary-color, 0.1);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 20px;
    
    .empty-icon {
      font-size: 60px;
      color: $primary-color;
    }
  }
}
</style>
