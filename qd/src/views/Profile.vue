<template>
  <div class="profile-container">
    <!-- Background Animation -->
    <div class="animated-background">
      <div class="gradient-sphere"></div>
      <div class="gradient-sphere"></div>
      <div class="gradient-sphere"></div>
            </div>
    
    <!-- Back Button -->
    <div class="back-button" @click="goBack">
      <i class="fas fa-arrow-left"></i>
      <span>Back to Home</span>
          </div>

    <!-- Profile Main Container -->
    <div class="profile-content">
      <!-- Profile Card -->
      <div class="profile-card glass-effect">
        <div class="profile-header">
          <div class="avatar-container">
            <div class="avatar-wrapper">
              <img :src="userInfo.avatar || defaultAvatar" :alt="userInfo.name">
              <div class="avatar-overlay" @click="handleAvatarUpload">
                <i class="fas fa-camera"></i>
                <span>Change Avatar</span>
        </div>
            </div>
            <div class="user-status" :class="userInfo.status === 'Active' ? 'active' : 'inactive'">
              <span class="status-dot"></span>
              {{ userInfo.status }}
            </div>
          </div>
          <div class="user-info">
            <h1>{{ userInfo.name }}</h1>
            <div class="role-badge">
              <i class="fas fa-user-shield"></i>
              {{ userInfo.role }}
            </div>
      </div>
    </div>

        <div class="info-grid">
          <div class="info-card glass-effect-light">
            <i class="fas fa-envelope"></i>
            <div class="info-content">
              <label>Email Account</label>
              <span>{{ userInfo.email }}</span>
          </div>
            <div class="card-decoration"></div>
          </div>

          <div class="info-card glass-effect-light">
            <i class="fas fa-calendar-alt"></i>
            <div class="info-content">
              <label>Registration Date</label>
              <span>{{ formatDate(userInfo.createdAt) }}</span>
            </div>
            <div class="card-decoration"></div>
            </div>

          <div class="info-card glass-effect-light">
            <i class="fas fa-shield-alt"></i>
            <div class="info-content">
              <label>Account Level</label>
              <span>Regular Member</span>
            </div>
            <div class="card-decoration"></div>
            </div>
            </div>

        <div class="action-buttons">
          <button class="btn-edit glass-button" @click="startEdit">
            <i class="fas fa-edit"></i>
            <span>Edit Profile</span>
            <div class="button-effect"></div>
          </button>
          <button class="btn-password glass-button" @click="showPasswordModal">
            <i class="fas fa-key"></i>
            <span>Change Password</span>
            <div class="button-effect"></div>
          </button>
          </div>
        </div>

      <!-- Account Security Card -->
      <div class="security-card glass-effect">
        <h2 class="card-title">
          <i class="fas fa-shield-alt"></i>
          Account Security
        </h2>
        <div class="security-items">
          <div class="security-item">
            <div class="security-icon">
              <i class="fas fa-lock">password</i>
            </div>
            <div class="security-info">
              <h3>Login Password</h3>
              <p>Regular password changes are recommended for account security</p>
            </div>
            <button class="security-action glass-button-small" @click="showPasswordModal">
              <span>Change</span>
              <i class="fas fa-chevron-right"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Change Password Modal -->
    <div class="modal-overlay" v-if="showPasswordDialog" @click.self="closePasswordDialog">
      <div class="modal-card glass-effect scrollable-modal">
        <div class="modal-header sticky-header">
          <h2>
            <i class="fas fa-key"></i>
            Change Password
          </h2>
          <button class="btn-close" @click="closePasswordDialog">
            <i class="fas fa-times"></i>
          </button>
        </div>

        <form class="password-form scrollable-content" @submit.prevent="handlePasswordChange">
          <div class="form-group">
            <div class="input-wrapper">
              <i class="fas fa-lock"></i>
              <input 
                :type="showPassword.old ? 'text' : 'password'" 
                v-model="passwordForm.oldPassword"
                placeholder="Current Password"
              >
              <button type="button" class="toggle-password" @click="togglePassword('old')">
                <i class="fas" :class="showPassword.old ? 'fa-eye-slash' : 'fa-eye'"></i>
              </button>
            </div>
          </div>

          <div class="form-group">
            <div class="input-wrapper">
              <i class="fas fa-key"></i>
              <input 
                :type="showPassword.new ? 'text' : 'password'" 
                v-model="passwordForm.newPassword"
                placeholder="New Password"
              >
              <button type="button" class="toggle-password" @click="togglePassword('new')">
                <i class="fas" :class="showPassword.new ? 'fa-eye-slash' : 'fa-eye'"></i>
              </button>
            </div>
          </div>

          <div class="form-group">
            <div class="input-wrapper">
              <i class="fas fa-check-circle"></i>
              <input 
                :type="showPassword.confirm ? 'text' : 'password'" 
                v-model="passwordForm.confirmPassword"
                placeholder="Confirm New Password"
              >
              <button type="button" class="toggle-password" @click="togglePassword('confirm')">
                <i class="fas" :class="showPassword.confirm ? 'fa-eye-slash' : 'fa-eye'"></i>
              </button>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn-cancel glass-button-small" @click="closePasswordDialog">
              Cancel
            </button>
            <button type="submit" class="btn-submit glass-button-small" :disabled="isSubmitting">
              {{ isSubmitting ? 'Submitting...' : 'Confirm Change' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Edit Profile Modal -->
    <div class="modal-overlay" v-if="showEditDialog" @click.self="closeEditDialog">
      <div class="modal-card glass-effect scrollable-modal">
        <div class="modal-header sticky-header">
          <h2>
            <i class="fas fa-user-edit"></i>
            Edit Profile
          </h2>
          <button class="btn-close" @click="closeEditDialog">
            <i class="fas fa-times"></i>
          </button>
          </div>

        <div class="edit-form scrollable-content">
          <!-- Avatar Upload Section -->
          <div class="avatar-upload-section">
            <div class="avatar-preview" @click="triggerFileInput">
              <img :src="editForm.avatar || userInfo.avatar || defaultAvatar" alt="Avatar Preview">
              <div class="avatar-overlay">
                <i class="fas fa-camera"></i>
                <span>Change Avatar</span>
              </div>
              <input 
                type="file" 
                ref="fileInput" 
                accept="image/*" 
                style="display: none" 
                @change="handleFileChange"
              >
            </div>
            <p class="avatar-tip">Click avatar to change photo</p>
          </div>

          <!-- Basic Info Form -->
          <div class="form-section">
            <div class="form-group">
              <label class="form-label">
                <i class="fas fa-user"></i>
                Username
              </label>
              <div class="input-wrapper">
                <input 
                  type="text" 
                  v-model="editForm.name"
                  placeholder="Enter username"
                >
              </div>
            </div>

            <!-- Password Change Section -->
            <div class="password-section" v-if="showPasswordFields">
              <div class="section-header">
                <h3>Change Password</h3>
                <button type="button" class="btn-toggle" @click="togglePasswordFields">
                  <i class="fas fa-times"></i>
                </button>
          </div>

              <div class="form-group">
                <label class="form-label">
                  <i class="fas fa-lock"></i>
                  Current Password
                </label>
                <div class="input-wrapper">
                  <input 
                    :type="showPassword.old ? 'text' : 'password'" 
                    v-model="editForm.oldPassword"
                    placeholder="Enter current password"
                  >
                  <button type="button" class="toggle-password" @click="togglePassword('old')">
                    <i class="fas" :class="showPassword.old ? 'fa-eye-slash' : 'fa-eye'"></i>
                  </button>
        </div>
    </div>

              <div class="form-group">
                <label class="form-label">
                  <i class="fas fa-key"></i>
                  New Password
                </label>
                <div class="input-wrapper">
                  <input 
                    :type="showPassword.new ? 'text' : 'password'" 
                    v-model="editForm.newPassword"
                    placeholder="Enter new password"
                  >
                  <button type="button" class="toggle-password" @click="togglePassword('new')">
                    <i class="fas" :class="showPassword.new ? 'fa-eye-slash' : 'fa-eye'"></i>
                  </button>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <i class="fas fa-check-circle"></i>
                  Confirm Password
                </label>
                <div class="input-wrapper">
                  <input 
                    :type="showPassword.confirm ? 'text' : 'password'" 
                    v-model="editForm.confirmPassword"
                    placeholder="Re-enter new password"
                  >
                  <button type="button" class="toggle-password" @click="togglePassword('confirm')">
                    <i class="fas" :class="showPassword.confirm ? 'fa-eye-slash' : 'fa-eye'"></i>
                  </button>
  </div>
              </div>
            </div>

            <button 
              v-if="!showPasswordFields" 
              type="button" 
              class="btn-add-password"
              @click="togglePasswordFields"
            >
              <div class="btn-content">
                <i class="fas fa-plus"></i>
                <span>Change Password</span>
              </div>
              <div class="btn-background"></div>
            </button>

            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeEditDialog">
                Cancel
              </button>
              <button 
                type="button" 
                class="btn-submit" 
                :disabled="isSubmitting"
                @click="handleProfileUpdate"
              >
                <span>{{ isSubmitting ? 'Saving...' : 'Save Changes' }}</span>
                <i class="fas fa-arrow-right"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
// Variable Definitions
$primary-gradient: linear-gradient(135deg, #6366f1 0%, #a855f7 100%);
$secondary-gradient: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
$success-gradient: linear-gradient(135deg, #22c55e 0%, #10b981 100%);
$warning-gradient: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
$text-primary: #f8fafc;
$text-secondary: #94a3b8;
$glass-bg: rgba(255, 255, 255, 0.1);
$glass-border: rgba(255, 255, 255, 0.2);
$glass-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

// Global Styles
.profile-container {
  min-height: 100vh;
  background: #0f172a;
  position: relative;
  overflow: hidden;
  padding: 2rem;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  color: $text-primary;
}

// Back Button
.back-button {
  position: fixed;
  top: 2rem;
  left: 2rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.25rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border-radius: 50px;
  color: $text-primary;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  
  i {
    font-size: 16px;
    transition: transform 0.3s ease;
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
    
    i {
      transform: translateX(-3px);
    }
  }
}

// Background Animation
.animated-background {
  position: fixed;
  inset: 0;
  z-index: 0;
  overflow: hidden;

  .gradient-sphere {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.5;
    animation: float 20s infinite ease-in-out;

    &:nth-child(1) {
      width: 400px;
      height: 400px;
      background: $primary-gradient;
      top: -100px;
      left: -100px;
      animation-delay: 0s;
    }

    &:nth-child(2) {
      width: 300px;
      height: 300px;
      background: $secondary-gradient;
      bottom: -50px;
      right: -50px;
      animation-delay: -5s;
    }

    &:nth-child(3) {
      width: 250px;
      height: 250px;
      background: $warning-gradient;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      animation-delay: -10s;
    }
  }
}

// Glass Effect
.glass-effect {
  background: $glass-bg;
  backdrop-filter: blur(12px);
  border: 1px solid $glass-border;
  border-radius: 24px;
  box-shadow: $glass-shadow;
}

.glass-effect-light {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
}

// Profile Main Container
.profile-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1200px;
  display: grid;
  gap: 2rem;
  margin-top: 2rem;
}

// Profile Card
.profile-card {
  padding: 2rem;

  .profile-header {
    display: flex;
    gap: 2rem;
    margin-bottom: 2rem;

    .avatar-container {
      position: relative;

      .avatar-wrapper {
      width: 120px;
      height: 120px;
        border-radius: 24px;
      overflow: hidden;
        position: relative;
        border: 2px solid rgba(255, 255, 255, 0.2);
        transition: transform 0.3s ease;

        &:hover {
          transform: translateY(-5px);

          .avatar-overlay {
            opacity: 1;
          }
        }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .avatar-overlay {
        position: absolute;
        inset: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        opacity: 0;
          transition: opacity 0.3s ease;
        cursor: pointer;

          i {
          font-size: 24px;
            margin-bottom: 8px;
        }

        span {
          font-size: 12px;
          }
        }
      }

      .user-status {
        position: absolute;
        bottom: -10px;
        left: 50%;
        transform: translateX(-50%);
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        gap: 6px;
        background: rgba(0, 0, 0, 0.3);
        backdrop-filter: blur(4px);

        &.active {
          .status-dot {
            background: #22c55e;
            box-shadow: 0 0 10px rgba(34, 197, 94, 0.5);
          }
        }

        &.inactive {
          .status-dot {
            background: #ef4444;
            box-shadow: 0 0 10px rgba(239, 68, 68, 0.5);
          }
        }

        .status-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
        }
      }
    }

    .user-info {
      flex: 1;

      h1 {
        font-size: 32px;
        margin: 0 0 12px;
        background: $primary-gradient;
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
      }

      .role-badge {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 6px 16px;
        border-radius: 20px;
        background: rgba(99, 102, 241, 0.1);
        color: #818cf8;
    font-size: 14px;

        i {
          font-size: 12px;
        }
      }
    }
  }
}

// Info Grid
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;

  .info-card {
    padding: 1.5rem;
    position: relative;
    display: flex;
    align-items: center;
    gap: 1rem;
    overflow: hidden;
    transition: transform 0.3s ease;

    &:hover {
      transform: translateY(-5px);

      .card-decoration {
        transform: rotate(45deg) scale(1.2);
      }
    }

    i {
      font-size: 24px;
      color: #818cf8;
      z-index: 1;
    }

    .info-content {
      z-index: 1;

      label {
        display: block;
        font-size: 12px;
        color: $text-secondary;
        margin-bottom: 4px;
      }

      span {
        font-size: 14px;
        color: $text-primary;
      }
    }

    .card-decoration {
      position: absolute;
      right: -20px;
      top: -20px;
      width: 100px;
      height: 100px;
      background: $primary-gradient;
      opacity: 0.1;
      border-radius: 20px;
      transform: rotate(45deg);
      transition: transform 0.3s ease;
    }
  }
}

// Action Buttons
.action-buttons {
  display: flex;
  gap: 1rem;

  .glass-button {
    flex: 1;
    padding: 12px 24px;
    border: none;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.1);
    color: $text-primary;
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    position: relative;
  overflow: hidden;
    transition: all 0.3s ease;

    i {
      font-size: 16px;
    }

    .button-effect {
      position: absolute;
      inset: 0;
      background: $primary-gradient;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    &:hover {
      transform: translateY(-2px);

      .button-effect {
        opacity: 0.1;
      }
    }

    &:active {
      transform: translateY(0);
    }
  }
}

// Account Security Card
.security-card {
  padding: 2rem;

  .card-title {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 0 0 1.5rem;
    font-size: 20px;

    i {
      color: #818cf8;
    }
  }

  .security-items {
    display: grid;
    gap: 1rem;

    .security-item {
      display: flex;
      align-items: center;
      gap: 1rem;
      padding: 1.5rem;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 16px;
      transition: transform 0.3s ease;

      &:hover {
        transform: translateY(-2px);
      }

      .security-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        background: rgba(99, 102, 241, 0.1);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #818cf8;
        font-size: 20px;
      }

      .security-info {
        flex: 1;
        margin-left: 30px;

        h3 {
          margin: 0 0 4px;
          font-size: 16px;
        }

        p {
      margin: 0;
          font-size: 14px;
          color: $text-secondary;
        }
      }

      .security-action {
        padding: 8px 16px;
        border-radius: 20px;
        background: rgba(255, 255, 255, 0.05);
        color: $text-primary;
        font-size: 14px;
        border: none;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 8px;
        transition: all 0.3s ease;

        &:hover {
          background: rgba(255, 255, 255, 0.1);
          transform: translateX(5px);
        }

        i {
          font-size: 12px;
        }
      }
    }
  }
}

// Change Password Modal
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(8px);
    display: flex;
    align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
  padding: 20px;
  overflow: hidden;

  .modal-card {
    width: 90%;
    max-width: 400px;
    animation: slideUp 0.3s ease;
    max-height: 85vh;
    display: flex;
    flex-direction: column;
    
    &.scrollable-modal {
      display: flex !important;
      flex-direction: column;
      max-height: 85vh;
      overflow: hidden;
    }

    .scrollable-content {
      overflow-y: auto;
      overflow-x: hidden;
      padding-right: calc(2rem - 6px); // Reserve space for scrollbar
      
      /* Custom scrollbar styles */
      &::-webkit-scrollbar {
        width: 6px;
      }
      
      &::-webkit-scrollbar-track {
        background: rgba(255, 255, 255, 0.05);
        border-radius: 3px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: rgba(255, 255, 255, 0.2);
        border-radius: 3px;
        
        &:hover {
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }

    .modal-header {
      padding: 1.5rem;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0;
        font-size: 18px;
      display: flex;
      align-items: center;
      gap: 8px;

        i {
          color: #818cf8;
        }
      }

      .btn-close {
        background: none;
        border: none;
        color: $text-secondary;
        cursor: pointer;
        font-size: 18px;
        padding: 4px;
        transition: all 0.3s ease;

        &:hover {
          color: $text-primary;
          transform: rotate(90deg);
        }
      }
    }
  }
}

// Password Form
.password-form {
  padding: 1.5rem;
  padding-right: calc(1.5rem - 6px); // Reserve space for scrollbar

  .form-group {
    margin-bottom: 1.5rem;

    .input-wrapper {
      position: relative;
      display: flex;
      align-items: center;
      gap: 12px;
      background: rgba(255, 255, 255, 0.05);
      border-radius: 12px;
      padding: 12px 16px;
      transition: all 0.3s ease;

      &:focus-within {
        background: rgba(255, 255, 255, 0.1);
        box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.3);
      }

      i {
        color: #818cf8;
        font-size: 16px;
      }

      input {
      flex: 1;
        background: none;
        border: none;
        color: $text-primary;
        font-size: 14px;
        outline: none;

        &::placeholder {
          color: $text-secondary;
        }
      }

      .toggle-password {
        background: none;
        border: none;
        color: $text-secondary;
        cursor: pointer;
        padding: 0;
        font-size: 14px;
        transition: color 0.3s ease;

        &:hover {
          color: $text-primary;
        }
      }
    }
  }

  .form-actions {
    display: flex;
    gap: 1rem;
    margin-top: 2rem;

    button {
      flex: 1;
      padding: 12px;
      border: none;
      border-radius: 12px;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s ease;

      &.btn-cancel {
        background: rgba(255, 255, 255, 0.1);
        color: $text-primary;

        &:hover {
          background: rgba(255, 255, 255, 0.15);
        }
      }

      &.btn-submit {
        background: $primary-gradient;
        color: $text-primary;

        &:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }
    }
  }
}

// Edit Form Styles
.edit-form {
  padding: 2rem;
  padding-right: calc(2rem - 6px); // Reserve space for scrollbar

  .avatar-upload-section {
    text-align: center;
    margin-bottom: 2rem;

    .avatar-preview {
      width: 120px;
      height: 120px;
      border-radius: 60px;
      margin: 0 auto;
      position: relative;
      cursor: pointer;
      transition: all 0.3s ease;
      border: 3px solid rgba(255, 255, 255, 0.1);
      overflow: hidden;

      &:hover {
        transform: scale(1.05);
        border-color: rgba(255, 255, 255, 0.3);

        .avatar-overlay {
          opacity: 1;
        }
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .avatar-overlay {
        position: absolute;
        inset: 0;
        background: rgba(0, 0, 0, 0.6);
    display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        opacity: 0;
        transition: opacity 0.3s ease;

        i {
          font-size: 24px;
          margin-bottom: 8px;
          color: white;
        }

        span {
          font-size: 12px;
          color: white;
        }
      }
    }

    .avatar-tip {
      margin: 1rem 0 0;
      font-size: 14px;
      color: $text-secondary;
    }
  }

  .form-section {
    .form-group {
      margin-bottom: 1.5rem;

      .form-label {
    display: flex;
    align-items: center;
        gap: 8px;
        margin-bottom: 8px;
        color: $text-secondary;
        font-size: 14px;

        i {
          color: #818cf8;
        }
      }

      .input-wrapper {
        background: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
        padding: 12px 16px;
        display: flex;
        align-items: center;
        gap: 12px;
        transition: all 0.3s ease;

        &:focus-within {
          background: rgba(255, 255, 255, 0.1);
          box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.3);
        }

        input {
          flex: 1;
          background: none;
          border: none;
          color: $text-primary;
          font-size: 14px;
          outline: none;

          &::placeholder {
            color: rgba(255, 255, 255, 0.3);
          }
        }

        .toggle-password {
          background: none;
          border: none;
          color: $text-secondary;
          cursor: pointer;
          padding: 4px;
          font-size: 14px;
    transition: all 0.3s ease;

    &:hover {
            color: $text-primary;
            transform: scale(1.1);
          }
        }
      }
    }
  }

  .password-section {
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid rgba(255, 255, 255, 0.1);

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;

      h3 {
        margin: 0;
        font-size: 16px;
        color: $text-primary;
        display: flex;
        align-items: center;
        gap: 8px;

        &::before {
          content: '';
          display: block;
          width: 4px;
          height: 16px;
          background: $primary-gradient;
          border-radius: 2px;
        }
      }

      .btn-toggle {
        background: none;
        border: none;
        color: $text-secondary;
        cursor: pointer;
        padding: 4px;
        font-size: 14px;
        transition: all 0.3s ease;

        &:hover {
          color: $text-primary;
          transform: rotate(90deg);
        }
      }
    }
  }

  .btn-add-password {
    width: 100%;
    padding: 12px;
    background: none;
    border: 2px dashed rgba(255, 255, 255, 0.2);
    border-radius: 12px;
    color: $text-secondary;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;

    .btn-content {
      position: relative;
      z-index: 1;
        display: flex;
        align-items: center;
        justify-content: center;
      gap: 8px;
    }

    .btn-background {
      position: absolute;
      inset: 0;
      background: $primary-gradient;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    &:hover {
      border-style: solid;
      border-color: transparent;
      color: $text-primary;

      .btn-background {
        opacity: 0.1;
      }
    }
  }

  .form-actions {
    display: flex;
    gap: 1rem;
    margin-top: 2rem;

    button {
      flex: 1;
      padding: 12px;
      border: none;
      border-radius: 12px;
          font-size: 14px;
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;

      &.btn-cancel {
        background: rgba(255, 255, 255, 0.1);
        color: $text-primary;

        &:hover {
          background: rgba(255, 255, 255, 0.15);
        }
      }

      &.btn-submit {
        background: $primary-gradient;
        color: white;
        position: relative;
        overflow: hidden;

        &::after {
          content: '';
          position: absolute;
          inset: 0;
          background: linear-gradient(to right, transparent, rgba(255, 255, 255, 0.2), transparent);
          transform: translateX(-100%);
          transition: transform 0.3s ease;
        }

        &:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);

          &::after {
            transform: translateX(100%);
          }

          i {
            transform: translateX(3px);
          }
        }

        &:disabled {
          opacity: 0.7;
          cursor: not-allowed;
        }

        i {
          transition: transform 0.3s ease;
        }
      }
    }
  }
}

// Add scroll-related styles
.scrollable-modal {
  display: flex;
  flex-direction: column;
  max-height: 85vh;
}

.sticky-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: inherit;
  backdrop-filter: blur(12px);
}

.scrollable-content {
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.2) rgba(255, 255, 255, 0.05);
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

// Media Query Adjustments
@media (max-height: 700px) {
  .scrollable-modal {
    max-height: 90vh;
  }
}

@media (max-height: 600px) {
  .scrollable-modal {
    max-height: 95vh;
  }
}

@media (max-width: 768px) {
  .profile-container {
    padding: 1rem;
  }

  .profile-card .profile-header {
      flex-direction: column;
    align-items: center;
    text-align: center;

    .avatar-container {
      margin-bottom: 1rem;
    }

    .user-info h1 {
      font-size: 24px;
    }
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .security-item {
    flex-direction: column;
    text-align: center;

    .security-icon {
      margin: 0 auto;
    }

    .security-action {
      width: 100%;
      justify-content: center;
    }
  }

  .edit-form {
    padding: 1.5rem;
  }
  
  .scrollable-modal {
    width: 95%;
    max-height: 80vh;
  }
  
  .edit-form {
    padding: 1rem;
    
    .avatar-upload-section .avatar-preview {
      width: 100px;
      height: 100px;
    }
    
    .form-actions {
      flex-direction: column;
    }
  }
}

@media (max-width: 480px) {
  .modal-overlay {
    padding: 10px;
  }
  
  .scrollable-modal {
    width: 100%;
    max-height: 90vh;
  }
  
  .scrollable-content {
    padding: 1rem;
  }
}

// Animations
@keyframes float {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(30px, 30px);
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style> 

<script setup>
import { ref, reactive, onMounted } from 'vue';
import request from '@/utils/request';
import { useRouter } from 'vue-router';

const router = useRouter();

// Back to Home
const goBack = () => {
  router.push('/');
};

// Default Avatar
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// User Information
const userInfo = ref({
  userId: '',
  name: '',
  email: '',
  role: '',
  avatar: '',
  createdAt: '',
  status: ''
});

// Password Form
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// Edit Form State
const showEditDialog = ref(false);
const showPasswordFields = ref(false);
const editForm = reactive({
  name: '',
  avatar: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// State Control
const showPasswordDialog = ref(false);
const isSubmitting = ref(false);
const showPassword = reactive({
  old: false,
  new: false,
  confirm: false
});

// File Upload Related
const fileInput = ref(null);
const triggerFileInput = () => {
  fileInput.value.click();
};

// Get User Information
const fetchUserInfo = async () => {
  try {
    const response = await request.get('/users/info');
    if (response.code === 200) {
      userInfo.value = response.data;
    }
  } catch (error) {
    console.error('Failed to get user information:', error);
  }
};

// Format Date
const formatDate = (date) => {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

// Show Password Change Dialog
const showPasswordModal = () => {
  showPasswordDialog.value = true;
};

// Close Password Change Dialog
const closePasswordDialog = () => {
  showPasswordDialog.value = false;
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
};

// Toggle Password Display State
const togglePassword = (field) => {
  showPassword[field] = !showPassword[field];
};

// Handle Password Change
const handlePasswordChange = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    alert('Please fill in all information');
    return;
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    alert('The two passwords do not match');
    return;
  }

  isSubmitting.value = true;
  try {
    const response = await request.put('/users/update', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    });

    if (response.code === 200) {
      alert('Password change successful');
      closePasswordDialog();
    }
  } catch (error) {
    console.error('Failed to change password:', error);
    alert('Failed to change password');
  } finally {
    isSubmitting.value = false;
  }
};

// Handle Avatar Upload
const handleAvatarUpload = () => {
  showEditDialog.value = true;
  setTimeout(() => {
    triggerFileInput();
  }, 300);
};

// Handle File Selection
const handleFileChange = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await request.post('/files/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    if (response.code === 200) {
      editForm.avatar = response.data.url || response.data;
    }
  } catch (error) {
    console.error('Failed to upload avatar:', error);
    alert('Failed to upload avatar, please try again later');
  }
};

// Start Editing Profile
const startEdit = () => {
  editForm.name = userInfo.value.name;
  editForm.avatar = userInfo.value.avatar;
  showEditDialog.value = true;
};

// Close Edit Dialog
const closeEditDialog = () => {
  showEditDialog.value = false;
  showPasswordFields.value = false;
  editForm.name = '';
  editForm.avatar = '';
  editForm.oldPassword = '';
  editForm.newPassword = '';
  editForm.confirmPassword = '';
};

// Toggle Password Field Display
const togglePasswordFields = () => {
  showPasswordFields.value = !showPasswordFields.value;
  if (!showPasswordFields.value) {
    editForm.oldPassword = '';
    editForm.newPassword = '';
    editForm.confirmPassword = '';
  }
};

// Handle Profile Update
const handleProfileUpdate = async () => {
  if (!editForm.name.trim()) {
    alert('Please enter a username');
    return;
  }

  if (showPasswordFields.value) {
    if (!editForm.oldPassword || !editForm.newPassword || !editForm.confirmPassword) {
      alert('Please fill in all password information');
      return;
    }
    if (editForm.newPassword !== editForm.confirmPassword) {
      alert('The two passwords do not match');
      return;
    }
  }

  isSubmitting.value = true;
  try {
    const updateData = {
      name: editForm.name,
      avatar: editForm.avatar
    };

    if (showPasswordFields.value) {
      Object.assign(updateData, {
        oldPassword: editForm.oldPassword,
        newPassword: editForm.newPassword,
        confirmPassword: editForm.confirmPassword
      });
    }

    const response = await request.put('/users/update', updateData);

    if (response.code === 200) {
      userInfo.value = response.data;
      alert('Update successful');
      closeEditDialog();
    }
  } catch (error) {
    console.error('Failed to update:', error);
    alert('Failed to update, please try again later');
  } finally {
    isSubmitting.value = false;
  }
};

// Component Mounted Get User Information
onMounted(() => {
  fetchUserInfo();
});
</script> 