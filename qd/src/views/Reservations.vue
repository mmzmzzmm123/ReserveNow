<template>
  <AppNavbar />
  <div class="reservations-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">My Reservations</h1>
        <p class="page-subtitle">Manage your restaurant booking records</p>
      </div>
    </div>

    <!-- Content Area -->
    <div class="page-content">
      <!-- Status Filter -->
      <div class="filter-section">
        <div class="filter-tags">
          <el-tag
            v-for="status in statusOptions"
            :key="status.value"
            :class="['status-tag', { active: selectedStatus.includes(status.value) }]"
            @click="toggleStatus(status.value)"
          >
            {{ status.label }}
            <span class="tag-count" v-if="getStatusCount(status.value)">
              {{ getStatusCount(status.value) }}
            </span>
          </el-tag>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Loading reservation records...</p>
      </div>

      <!-- No Reservation State -->
      <div v-else-if="reservations.length === 0" class="empty-state">
        <div class="empty-illustration">
          <el-icon><Calendar /></el-icon>
        </div>
        <h2>No Reservation Records</h2>
        <p>You haven't made any restaurant reservations yet. Start exploring delicious food now!</p>
        <el-button type="primary" class="explore-button" @click="goToExplore">
          Browse Restaurants
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>

      <!-- Reservation List -->
      <div v-else class="reservations-list">
        <div 
          v-for="reservation in reservations" 
          :key="reservation.id" 
          class="reservation-card"
          :class="getStatusClass(reservation.status)"
        >
          <div class="card-header">
            <div class="restaurant-info">
              <img :src="reservation.restaurantPhoto" :alt="reservation.restaurantName" class="restaurant-photo">
              <div class="info-content">
                <h3>{{ reservation.restaurantName }}</h3>
                <p class="address">
                  <el-icon><Location /></el-icon>
                  Table Type: {{ reservation.address }}
                </p>
              </div>
            </div>
            <div class="status-badge" :class="getStatusClass(reservation.status)">
              {{ reservation.statusText }}
            </div>
          </div>

          <div class="card-content">
            <div class="reservation-details">
              <div class="detail-item">
                <el-icon><Calendar /></el-icon>
                <span>Reservation Date: {{ formatDate(reservation.reservationDate) }}</span>
              </div>
              <div class="detail-item">
                <el-icon><Clock /></el-icon>
                <span>Reservation Time: {{ reservation.reservationTime }}</span>
              </div>
              <div class="detail-item">
                <el-icon><User /></el-icon>
                <span>Number of Guests: {{ reservation.numberOfGuests }}</span>
              </div>
              <div class="detail-item" v-if="reservation.specialRequests">
                <el-icon><Message /></el-icon>
                <span>Special Requests: {{ reservation.specialRequests }}</span>
              </div>
              <div class="detail-item reservation-code">
                <el-icon><Ticket /></el-icon>
                <span>Reservation Code: {{ reservation.reservationCode }}</span>
              </div>
            </div>

            <div class="card-actions">
              <el-button 
                v-if="reservation.status === 1 || reservation.status === 2"
                type="danger" 
                plain
                @click="showCancelDialog(reservation)"
              >
                Cancel Reservation
              </el-button>
              <el-button 
                v-if="reservation.status === 2"
                type="primary"
                @click="viewQRCode(reservation)"
              >
                View QR Code
              </el-button>
              <el-button 
                v-if="reservation.status === 3 && !reservation.reviewed"
                type="success"
                @click="showReviewDialog(reservation)"
              >
                Write Review
              </el-button>
              <el-button 
                v-if="reservation.status === 3 && reservation.reviewed"
                type="info"
                plain
                disabled
              >
                Reviewed
              </el-button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
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

  <!-- Cancel Reservation Dialog -->
  <el-dialog
    v-model="cancelDialogVisible"
    title="Cancel Reservation"
    width="30%"
    class="cancel-dialog"
  >
    <div class="cancel-content">
      <p class="cancel-warning">
        <el-icon class="warning-icon"><Warning /></el-icon>
        Are you sure you want to cancel this reservation? This action cannot be undone.
      </p>
      <el-form :model="cancelForm" label-position="top">
        <el-form-item label="Cancellation Reason">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            rows="3"
            placeholder="Please briefly explain the reason for cancellation (optional)"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelDialogVisible = false">Back</el-button>
        <el-button type="danger" @click="confirmCancel" :loading="cancelling">
          Confirm Cancel
        </el-button>
      </span>
    </template>
  </el-dialog>

  <!-- Reservation Code Dialog -->
  <el-dialog
    v-model="qrCodeDialogVisible"
    title="Reservation QR Code"
    width="30%"
    class="qrcode-dialog"
  >
    <div class="qrcode-content">
      <div class="qrcode-container">
        <div class="qrcode-placeholder">
          Reservation Code: {{ selectedReservation?.reservationCode }}
        </div>
      </div>
      <p class="qrcode-tip">Please show this QR code when you arrive at the restaurant</p>
    </div>
  </el-dialog>

  <!-- Review Dialog -->
  <el-dialog
    v-model="reviewDialogVisible"
    title="Write a Review"
    width="40%"
    class="review-dialog"
  >
    <div class="review-content">
      <h3 class="review-restaurant-name">{{ selectedReservation?.restaurantName }}</h3>
      
      <div class="custom-review-form">
        <div class="form-section">
          <label class="form-label">Rating</label>
          <div class="custom-rating">
            <div 
              v-for="star in 5" 
              :key="star" 
              class="rating-star" 
              :class="{ active: star <= reviewForm.rating }"
              @click="reviewForm.rating = star"
              @mouseover="hoverRating = star"
              @mouseleave="hoverRating = 0"
            >
              <svg viewBox="0 0 24 24" class="star-icon" :class="{ 'hover': star <= hoverRating && star > reviewForm.rating }">
                <path d="M12,17.27L18.18,21L16.54,13.97L22,9.24L14.81,8.62L12,2L9.19,8.62L2,9.24L7.45,13.97L5.82,21L12,17.27Z" />
              </svg>
            </div>
            <span class="rating-text">{{ getRatingText(hoverRating || reviewForm.rating) }}</span>
          </div>
        </div>
        
        <div class="form-section">
          <label class="form-label">Your Review</label>
          <textarea 
            v-model="reviewForm.content" 
            class="custom-textarea" 
            placeholder="Share your dining experience..."
            rows="4"
            :class="{ 'error': contentError }"
          ></textarea>
          <span class="error-message" v-if="contentError">{{ contentError }}</span>
        </div>
        
        <div class="form-section">
          <label class="form-label">Upload Photos (Optional, max 3)</label>
          <div class="custom-upload-area">
            <div class="upload-preview">
              <div 
                v-for="(photo, index) in reviewPhotoPreviews" 
                :key="index" 
                class="preview-item"
              >
                <img :src="photo" class="preview-image" />
                <button type="button" class="remove-btn" @click="removePhoto(index)">
                  <svg viewBox="0 0 24 24" class="remove-icon">
                    <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />
                  </svg>
                </button>
              </div>
              <div 
                v-if="reviewPhotoPreviews.length < 3" 
                class="upload-btn"
                @click="triggerPhotoUpload"
              >
                <svg viewBox="0 0 24 24" class="upload-icon">
                  <path d="M19,13H13V19H11V13H5V11H11V5H13V11H19V13Z" />
                </svg>
                <span>Add Photo</span>
                <input 
                  type="file"
                  ref="photoInput"
                  accept="image/*"
                  style="display: none"
                  @change="uploadPhoto"
                  multiple
                >
              </div>
            </div>
          </div>
        </div>
        
        <div class="form-section">
          <label class="form-label">Upload Video (Optional, max 1)</label>
          <div class="custom-video-upload">
            <div v-if="reviewVideoPreview" class="video-preview">
              <video :src="reviewVideoPreview" controls class="preview-video"></video>
              <button type="button" class="remove-btn" @click="removeVideo">
                <svg viewBox="0 0 24 24" class="remove-icon">
                  <path d="M19,6.41L17.59,5L12,10.59L6.41,5L5,6.41L10.59,12L5,17.59L6.41,19L12,13.41L17.59,19L19,17.59L13.41,12L19,6.41Z" />
                </svg>
              </button>
            </div>
            <div v-else class="video-upload-btn" @click="triggerVideoUpload">
              <svg viewBox="0 0 24 24" class="upload-icon">
                <path d="M17,10.5V7A1,1 0 0,0 16,6H4A1,1 0 0,0 3,7V17A1,1 0 0,0 4,18H16A1,1 0 0,0 17,17V13.5L21,17.5V6.5L17,10.5Z" />
              </svg>
              <span>Add Video</span>
              <input 
                type="file"
                ref="videoInput"
                accept="video/*"
                style="display: none"
                @change="uploadVideo"
              >
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="review-actions">
      <button class="cancel-btn" @click="reviewDialogVisible = false">Cancel</button>
      <button 
        class="submit-btn" 
        @click="submitReview" 
        :disabled="submittingReview"
      >
        <span v-if="submittingReview" class="loading-spinner"></span>
        <span>{{ submittingReview ? 'Submitting...' : 'Submit Review' }}</span>
      </button>
    </div>
  </el-dialog>

  <AppFooter />
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import AppNavbar from '@/components/AppNavbar.vue';
import AppFooter from '@/components/AppFooter.vue';
import request from '@/utils/request';
import { 
  Calendar,
  Clock,
  Location,
  User,
  Message,
  Warning,
  ArrowRight,
  Ticket,
  Plus
} from '@element-plus/icons-vue';

const router = useRouter();
const loading = ref(false);
const reservations = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const selectedStatus = ref(['1', '2']); 
const cancelDialogVisible = ref(false);
const qrCodeDialogVisible = ref(false);
const reviewDialogVisible = ref(false);
const selectedReservation = ref(null);
const cancelling = ref(false);
const submittingReview = ref(false);
const cancelForm = ref({
  reason: ''
});
const reviewForm = ref({
  id: null,
  restaurantId: null,
  rating: 5,
  content: '',
  photos: [],
  videos: []
});
const contentError = ref('');
const hoverRating = ref(0);
const reviewPhotoPreviews = ref([]);
const reviewVideoPreview = ref(null);
const photoInput = ref(null);
const videoInput = ref(null);

// Status options
const statusOptions = [
  { label: 'Pending', value: '1' },
  { label: 'Confirmed', value: '2' },
  { label: 'Completed', value: '3' },
  { label: 'Cancelled', value: '0' },
  { label: 'Rejected', value: '4' }
];

// Get reservation list
const fetchReservations = async () => {
  loading.value = true;
  try {
    const response = await request.get('/reservations', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        status: selectedStatus.value.join(',')
      }
    });
    
    if (response.code === 200) {
      // Process data format
      reservations.value = response.data.list.map(item => ({
        id: item.id,
        restaurantId: item.restaurantId,
        restaurantName: item.restaurantName,
        restaurantPhoto: 'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=2070&auto=format&fit=crop', // Default image
        address: item.tableType, 
        reservationDate: item.reservationTime,
        reservationTime: item.reservationTime.split('T')[1].substring(0, 5),
        numberOfGuests: item.personCount,
        specialRequests: item.remarks,
        status: item.statusValue,
        statusText: item.statusText,
        reservationCode: `RES${String(item.id).padStart(6, '0')}`,
        cancelReason: item.cancelReason,
        reviewed: item.reviewed || false
      }));
      total.value = response.data.total;
    }
  } catch (error) {
    ElMessage.error('Failed to load reservation records');
  } finally {
    loading.value = false;
  }
};

// Toggle status filter
const toggleStatus = (status) => {
  const index = selectedStatus.value.indexOf(status);
  if (index > -1) {
    selectedStatus.value.splice(index, 1);
  } else {
    selectedStatus.value.push(status);
  }
  currentPage.value = 1;
  fetchReservations();
};

// Get status text
const getStatusText = (status) => {
  const statusMap = {
    1: 'Pending',
    2: 'Confirmed',
    3: 'Completed',
    0: 'Cancelled',
    4: 'Rejected'
  };
  return statusMap[status] || 'Unknown Status';
};

// Get status class name
const getStatusClass = (status) => {
  const statusMap = {
    0: 'cancelled',
    1: 'pending',
    2: 'confirmed',
    3: 'completed',
    4: 'rejected'
  };
  return statusMap[status] || '';
};

// Get count for each status
const getStatusCount = (status) => {
  return reservations.value.filter(r => r.status === parseInt(status)).length;
};

// Format date
const formatDate = (dateString) => {
  if (!dateString) return '';
  
  const date = dateString.split('T')[0];
  const [year, month, day] = date.split('-');
  
  const dateObj = new Date(year, month - 1, day);
  const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const weekday = weekdays[dateObj.getDay()];
  
  return `${weekday} ${year}-${month}-${day}`;
};

// Show cancel dialog
const showCancelDialog = (reservation) => {
  selectedReservation.value = reservation;
  cancelDialogVisible.value = true;
};

// Confirm cancel reservation
const confirmCancel = async () => {
  if (!selectedReservation.value) return;
  
  cancelling.value = true;
  try {
    const response = await request.put(
      `/reservations/${selectedReservation.value.id}/cancel`,
      { reason: cancelForm.value.reason }
    );
    
    if (response.code === 200) {
      ElMessage.success('Reservation cancelled successfully');
      cancelDialogVisible.value = false;
      fetchReservations();
    }
  } catch (error) {
    ElMessage.error('Failed to cancel reservation');
  } finally {
    cancelling.value = false;
  }
};

// View reservation code
const viewQRCode = (reservation) => {
  selectedReservation.value = reservation;
  qrCodeDialogVisible.value = true;
};

// Show review dialog
const showReviewDialog = (reservation) => {
  selectedReservation.value = reservation;
  reviewForm.value = {
    id: reservation.id,
    restaurantId: reservation.restaurantId,
    rating: 5,
    content: '',
    photos: [],
    videos: []
  };
  reviewPhotoPreviews.value = [];
  reviewVideoPreview.value = null;
  contentError.value = '';
  reviewDialogVisible.value = true;
};

// Get rating text
const getRatingText = (rating) => {
  const texts = ['Very Poor', 'Poor', 'Fair', 'Good', 'Excellent'];
  return texts[rating - 1] || '';
};

// Trigger photo upload
const triggerPhotoUpload = () => {
  photoInput.value.click();
};

// Trigger video upload
const triggerVideoUpload = () => {
  videoInput.value.click();
};

// Upload photo
const uploadPhoto = async (e) => {
  const files = e.target.files;
  if (!files || files.length === 0) return;
  
  const remainingSlots = 3 - reviewPhotoPreviews.value.length;
  const filesToUpload = Array.from(files).slice(0, remainingSlots);
  
  for (const file of filesToUpload) {
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning('Image size cannot exceed 5MB');
      continue;
    }
    
    // Show temporary preview
    const tempUrl = URL.createObjectURL(file);
    reviewPhotoPreviews.value.push(tempUrl);
    
    // Create FormData object
    const formData = new FormData();
    formData.append('file', file);
    
    try {
      submittingReview.value = true;
      const response = await request.post('/files/images', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      if (response.code === 200) {
        // Replace temporary preview with actual URL
        const index = reviewPhotoPreviews.value.indexOf(tempUrl);
        if (index !== -1) {
          URL.revokeObjectURL(tempUrl);
          reviewPhotoPreviews.value[index] = response.data;
          reviewForm.value.photos.push(response.data);
        }
      } else {
        // Upload failed, remove temporary preview
        removePhoto(reviewPhotoPreviews.value.indexOf(tempUrl));
        ElMessage.error('Failed to upload image');
      }
    } catch (error) {
      // Upload failed, remove temporary preview
      removePhoto(reviewPhotoPreviews.value.indexOf(tempUrl));
      ElMessage.error('Failed to upload image');
    } finally {
      submittingReview.value = false;
    }
  }
  
  // Clear input to allow uploading the same file again
  e.target.value = '';
};

// Remove photo
const removePhoto = (index) => {
  if (index < 0 || index >= reviewPhotoPreviews.value.length) return;
  
  const previewUrl = reviewPhotoPreviews.value[index];
  if (previewUrl.startsWith('blob:')) {
    URL.revokeObjectURL(previewUrl);
  }
  
  reviewPhotoPreviews.value.splice(index, 1);
  reviewForm.value.photos.splice(index, 1);
};

// Upload video
const uploadVideo = async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning('Video size cannot exceed 50MB');
    return;
  }
  
  // Show temporary preview
  const tempUrl = URL.createObjectURL(file);
  reviewVideoPreview.value = tempUrl;
  
  // Create FormData object
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    submittingReview.value = true;
    const response = await request.post('/files/videos', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    if (response.code === 200) {
      // Replace temporary preview with actual URL
      URL.revokeObjectURL(tempUrl);
      reviewVideoPreview.value = response.data;
      reviewForm.value.videos = [response.data];
    } else {
      // Upload failed, remove temporary preview
      removeVideo();
      ElMessage.error('Failed to upload video');
    }
  } catch (error) {
    // Upload failed, remove temporary preview
    removeVideo();
    ElMessage.error('Failed to upload video');
  } finally {
    submittingReview.value = false;
  }
  
  // Clear input to allow uploading the same file again
  e.target.value = '';
};

// Remove video
const removeVideo = () => {
  if (reviewVideoPreview.value && reviewVideoPreview.value.startsWith('blob:')) {
    URL.revokeObjectURL(reviewVideoPreview.value);
  }
  
  reviewVideoPreview.value = null;
  reviewForm.value.videos = [];
};

// Submit review
const submitReview = async () => {
  // Validate content
  contentError.value = '';
  if (!reviewForm.value.content.trim()) {
    contentError.value = 'Please enter your review content';
    return;
  }
  
  submittingReview.value = true;
  try {
    // Convert photo array to string format: url|url|url
    const photosString = reviewForm.value.photos.join('|');
    // Convert video array to string format
    const videosString = reviewForm.value.videos.join('|');
    
    const response = await request.post('/reviews', {
      id: reviewForm.value.id,
      restaurantId: reviewForm.value.restaurantId,
      content: reviewForm.value.content,
      photos: photosString,  // Send as string format
      videos: videosString,  // Send as string format
      rating: reviewForm.value.rating
    });
    
    if (response.code === 200) {
      ElMessage.success('Review submitted successfully');
      reviewDialogVisible.value = false;
      
      // Update current reservation's reviewed status
      const index = reservations.value.findIndex(r => r.id === selectedReservation.value.id);
      if (index !== -1) {
        reservations.value[index].reviewed = true;
      }
    }
  } catch (error) {
    ElMessage.error('Failed to submit review');
  } finally {
    submittingReview.value = false;
  }
};

// Go to explore page
const goToExplore = () => {
  router.push('/explore');
};

// Handle page change
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchReservations();
};

// Get reservation list when component is mounted
onMounted(() => {
  fetchReservations();
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

// Status colors
$status-pending: #f39c12;
$status-confirmed: #2ecc71;
$status-completed: #3498db;
$status-cancelled: #95a5a6;
$status-rejected: #e74c3c;

// Page base styles
.reservations-page {
  padding-top: 70px;
  min-height: 100vh;
  background-color: #fcfcfc;
}

// Page header
.page-header {
  background: linear-gradient(135deg, $secondary-color, #fff);
  padding: 60px 20px;
  text-align: center;
  margin-bottom: 40px;

  .page-title {
    font-size: 36px;
    font-weight: 800;
    margin: 0 0 10px;
    background: linear-gradient(135deg, $dark-color, $primary-color);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .page-subtitle {
    font-size: 16px;
    color: $gray-color;
    margin: 0;
  }
}

// Page content
.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 50px;
}

// Status filter
.filter-section {
  margin-bottom: 30px;

  .filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    .status-tag {
      cursor: pointer;
      padding: 8px 16px;
      border-radius: 20px;
      font-size: 14px;
      transition: all 0.3s ease;
      user-select: none;
      
      &.active {
        background-color: $primary-color;
        color: white;
        border-color: $primary-color;
      }

      .tag-count {
        margin-left: 5px;
        font-size: 12px;
        opacity: 0.8;
      }
    }
  }
}

// Reservation card
.reservation-card {
  background: white;
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }

  .card-header {
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);

    .restaurant-info {
      display: flex;
      align-items: center;
      gap: 15px;

      .restaurant-photo {
        width: 60px;
        height: 60px;
        border-radius: 8px;
        object-fit: cover;
      }

      .info-content {
        h3 {
          margin: 0 0 5px;
          font-size: 18px;
          color: $dark-color;
        }

        .address {
          margin: 0;
          font-size: 14px;
          color: $gray-color;
          display: flex;
          align-items: center;
          gap: 5px;
        }
      }
    }

    .status-badge {
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 500;

      &.pending {
        background-color: rgba($status-pending, 0.1);
        color: $status-pending;
      }

      &.confirmed {
        background-color: rgba($status-confirmed, 0.1);
        color: $status-confirmed;
      }

      &.completed {
        background-color: rgba($status-completed, 0.1);
        color: $status-completed;
      }

      &.cancelled {
        background-color: rgba($status-cancelled, 0.1);
        color: $status-cancelled;
      }

      &.rejected {
        background-color: rgba($status-rejected, 0.1);
        color: $status-rejected;
      }
    }
  }

  .card-content {
    padding: 20px;

    .reservation-details {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
      margin-bottom: 20px;

      .detail-item {
        display: flex;
        align-items: center;
        gap: 8px;
        color: $gray-color;
        font-size: 14px;

        .el-icon {
          color: $primary-color;
        }
      }

      .reservation-code {
        grid-column: 1 / -1;
        background: rgba($primary-color, 0.05);
        padding: 10px;
        border-radius: 8px;
        margin-top: 10px;
        
        .el-icon {
          color: $primary-color;
        }
      }
    }

    .card-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
}

// Loading state
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;

  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid rgba($primary-color, 0.1);
    border-radius: 50%;
    border-top-color: $primary-color;
    animation: spin 1s linear infinite;
    margin-bottom: 20px;
  }

  p {
    color: $gray-color;
    font-size: 14px;
  }
}

// Empty state
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);

  .empty-illustration {
    font-size: 60px;
    color: $primary-color;
    margin-bottom: 20px;
    opacity: 0.5;
  }

  h2 {
    font-size: 24px;
    color: $dark-color;
    margin: 0 0 10px;
  }

  p {
    color: $gray-color;
    margin: 0 0 30px;
  }

  .explore-button {
    padding: 12px 30px;
  }
}

// Cancel dialog
.cancel-dialog {
  .cancel-warning {
    display: flex;
    align-items: center;
    gap: 10px;
    color: $status-rejected;
    margin-bottom: 20px;

    .warning-icon {
      font-size: 20px;
    }
  }
}

// Reservation code dialog
.qrcode-dialog {
  .qrcode-content {
    text-align: center;

    .qrcode-container {
      margin-bottom: 20px;
    }

    .qrcode-tip {
      color: $gray-color;
      font-size: 14px;
      margin: 0;
    }
  }
}

// Review dialog
.review-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
  
  :deep(.el-dialog__footer) {
    display: none;
  }
  
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
    background: linear-gradient(to bottom, #fff5f6, #fff);
  }
  
  .review-content {
    padding: 30px;
    
    .review-restaurant-name {
      font-size: 24px;
      font-weight: 700;
      color: $dark-color;
      margin: 0 0 25px;
      text-align: center;
      position: relative;
      
      &:after {
        content: '';
        position: absolute;
        bottom: -10px;
        left: 50%;
        width: 50px;
        height: 3px;
        background: linear-gradient(to right, $primary-color, lighten($primary-color, 20%));
        transform: translateX(-50%);
        border-radius: 3px;
      }
    }
  }
  
  .custom-review-form {
    .form-section {
      margin-bottom: 25px;
      
      .form-label {
        display: block;
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 12px;
        color: $dark-color;
      }
      
      .custom-rating {
        display: flex;
        align-items: center;
        
        .rating-star {
          cursor: pointer;
          margin-right: 5px;
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          
          .star-icon {
            width: 30px;
            height: 30px;
            fill: #ddd;
            transition: all 0.2s ease;
            
            &.hover {
              fill: #FFD700;
              transform: scale(1.1);
            }
          }
          
          &.active .star-icon {
            fill: #FFD700;
          }
          
          &:hover {
            transform: scale(1.1);
          }
        }
        
        .rating-text {
          margin-left: 15px;
          font-size: 16px;
          color: $dark-color;
          font-weight: 500;
        }
      }
      
      .custom-textarea {
        width: 100%;
        padding: 15px;
        border-radius: 12px;
        border: 2px solid rgba(0,0,0,0.1);
        background-color: rgba(255,255,255,0.8);
        font-size: 16px;
        transition: all 0.3s ease;
        resize: vertical;
        min-height: 120px;
        box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
        
        &:focus {
          border-color: $primary-color;
          box-shadow: 0 0 0 3px rgba($primary-color, 0.2);
          outline: none;
        }
        
        &.error {
          border-color: $status-rejected;
          box-shadow: 0 0 0 3px rgba($status-rejected, 0.2);
        }
      }
      
      .error-message {
        color: $status-rejected;
        font-size: 14px;
        display: block;
        margin-top: 5px;
      }
      
      .custom-upload-area {
        .upload-preview {
          display: flex;
          flex-wrap: wrap;
          gap: 15px;
          
          .preview-item {
            width: 120px;
            height: 120px;
            border-radius: 12px;
            overflow: hidden;
            position: relative;
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
            
            .preview-image {
              width: 100%;
              height: 100%;
              object-fit: cover;
              transition: transform 0.3s ease;
            }
            
            &:hover .preview-image {
              transform: scale(1.05);
            }
            
            .remove-btn {
              position: absolute;
              top: 5px;
              right: 5px;
              width: 24px;
              height: 24px;
              border-radius: 50%;
              background: rgba(0,0,0,0.5);
              border: none;
              display: flex;
              align-items: center;
              justify-content: center;
              cursor: pointer;
              opacity: 0;
              transition: all 0.2s ease;
              
              .remove-icon {
                width: 16px;
                height: 16px;
                fill: white;
              }
            }
            
            &:hover .remove-btn {
              opacity: 1;
            }
          }
          
          .upload-btn {
            width: 120px;
            height: 120px;
            border-radius: 12px;
            border: 2px dashed rgba(0,0,0,0.1);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s ease;
            background-color: rgba(255,255,255,0.5);
            
            .upload-icon {
              width: 30px;
              height: 30px;
              fill: $primary-color;
              margin-bottom: 8px;
            }
            
            span {
              font-size: 14px;
              color: $gray-color;
            }
            
            &:hover {
              border-color: $primary-color;
              background-color: rgba($primary-color, 0.05);
            }
          }
        }
      }
      
      .custom-video-upload {
        .video-preview {
          position: relative;
          width: 100%;
          max-width: 400px;
          border-radius: 12px;
          overflow: hidden;
          box-shadow: 0 4px 15px rgba(0,0,0,0.1);
          
          .preview-video {
            width: 100%;
            display: block;
          }
          
          .remove-btn {
            position: absolute;
            top: 10px;
            right: 10px;
            width: 30px;
            height: 30px;
            border-radius: 50%;
            background: rgba(0,0,0,0.5);
            border: none;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.2s ease;
            
            .remove-icon {
              width: 20px;
              height: 20px;
              fill: white;
            }
            
            &:hover {
              background: rgba(0,0,0,0.7);
              transform: scale(1.1);
            }
          }
        }
        
        .video-upload-btn {
          width: 100%;
          max-width: 400px;
          height: 150px;
          border-radius: 12px;
          border: 2px dashed rgba(0,0,0,0.1);
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          transition: all 0.3s ease;
          background-color: rgba(255,255,255,0.5);
          
          .upload-icon {
            width: 40px;
            height: 40px;
            fill: $primary-color;
            margin-bottom: 10px;
          }
          
          span {
            font-size: 16px;
            color: $gray-color;
          }
          
          &:hover {
            border-color: $primary-color;
            background-color: rgba($primary-color, 0.05);
          }
        }
      }
    }
  }
  
  .review-actions {
    display: flex;
    justify-content: flex-end;
    padding: 20px 30px 30px;
    gap: 15px;
    
    .cancel-btn {
      padding: 12px 25px;
      border-radius: 30px;
      border: none;
      background-color: #f1f1f1;
      color: $gray-color;
      font-size: 16px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background-color: #e5e5e5;
      }
    }
    
    .submit-btn {
      padding: 12px 30px;
      border-radius: 30px;
      border: none;
      background: linear-gradient(135deg, $primary-color, lighten($primary-color, 10%));
      color: white;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      min-width: 120px;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 15px rgba($primary-color, 0.4);
      }
      
      &:disabled {
        opacity: 0.7;
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
      }
      
      .loading-spinner {
        width: 20px;
        height: 20px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-radius: 50%;
        border-top-color: white;
        animation: spin 1s linear infinite;
        margin-right: 10px;
      }
    }
  }
}

// Animation
@keyframes spin {
  to { transform: rotate(360deg); }
}

// Responsive styles
@media (max-width: 768px) {
  .page-header {
    padding: 40px 20px;

    .page-title {
      font-size: 28px;
    }
  }

  .reservation-card {
    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 15px;

      .status-badge {
        align-self: flex-start;
      }
    }

    .card-content {
      .reservation-details {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style> 