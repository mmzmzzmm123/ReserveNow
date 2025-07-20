<template>
  <footer class="app-footer">
    <div class="footer-top">
      <div class="container">
        <div class="footer-row">
          <div class="footer-col footer-brand">
            <div class="logo">
              <span class="icon-utensils"><el-icon><KnifeFork /></el-icon></span>
              <h2>ReserveNow</h2>
            </div>
            <p class="brand-description">
              The best platform to discover and book tables at the finest restaurants in your city. We make dining reservations simple and enjoyable.
            </p>
            <div class="social-media">
              <a href="#" class="social-link facebook">
                <el-icon><Position /></el-icon>
              </a>
              <a href="#" class="social-link twitter">
                <el-icon><Message /></el-icon>
              </a>
              <a href="#" class="social-link instagram">
                <el-icon><Picture /></el-icon>
              </a>
              <a href="#" class="social-link linkedin">
                <el-icon><Share /></el-icon>
              </a>
            </div>
          </div>
          
          <div class="footer-col footer-links">
            <h3>Quick Links</h3>
            <ul>
              <li><router-link to="/">Home</router-link></li>
              <li><router-link to="/explore">Explore</router-link></li>
              <li><router-link to="/reservations">My Reservations</router-link></li>
              <li><router-link to="/favorites">My Favorites</router-link></li>
              <li><router-link to="/profile">My Profile</router-link></li>
            </ul>
          </div>
          
          <div class="footer-col footer-newsletter">
            <h3>Newsletter</h3>
            <p>Subscribe to our newsletter for the latest restaurant updates and exclusive offers.</p>
            <div class="newsletter-form">
              <el-input
                v-model="email"
                placeholder="Your email address"
              />
              <el-button type="primary" @click="subscribeNewsletter">Subscribe</el-button>
            </div>
            <p class="newsletter-info">By subscribing, you agree to our Privacy Policy</p>
          </div>
        </div>
      </div>
    </div>
    
    <div class="footer-bottom">
      <div class="container">
        <div class="footer-bottom-content">
          <div class="copyright">
            &copy; {{ currentYear }} Restaurant Reservation. All rights reserved.
          </div>
          <div class="footer-cards">
            <span class="payment-card visa">
              <el-icon><CreditCard /></el-icon>
            </span>
            <span class="payment-card mastercard">
              <el-icon><CreditCard /></el-icon>
            </span>
            <span class="payment-card amex">
              <el-icon><CreditCard /></el-icon>
            </span>
            <span class="payment-card paypal">
              <el-icon><Money /></el-icon>
            </span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import {
  KnifeFork, Position, Message, Picture, Share,
  CreditCard, Money, Iphone
} from '@element-plus/icons-vue';

const email = ref('');
const currentYear = computed(() => new Date().getFullYear());

const subscribeNewsletter = () => {
  if (!email.value) {
    ElMessage.warning('Please enter your email address');
    return;
  }
  
  if (!validateEmail(email.value)) {
    ElMessage.warning('Please enter a valid email address');
    return;
  }
  
  // TODO: Implement actual newsletter subscription
  ElMessage.success('Thank you for subscribing to our newsletter!');
  email.value = '';
};

const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};
</script>

<style lang="scss" scoped>
// Variables
$primary-color: #e02020; // Electric red
$secondary-color: #fff0f0;
$footer-bg: #1a1a1a;
$footer-bg-secondary: #111111;
$border-radius: 8px;
$gradient-primary: linear-gradient(135deg, $primary-color, #ff6b6b);

.app-footer {
  position: relative;
  color: white;
  font-size: 14px;
  line-height: 1.6;
  
  .container {
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 20px;
  }
  
  a {
    color: #fff;
    text-decoration: none;
    transition: color 0.3s;
    
    &:hover {
      color: rgba($primary-color, 0.8);
    }
  }
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 20px;
    position: relative;
    padding-bottom: 10px;
    
    &::after {
      content: '';
      position: absolute;
      left: 0;
      bottom: 0;
      width: 40px;
      height: 3px;
      background: $gradient-primary;
      border-radius: 2px;
    }
  }
}

.footer-top {
  background-color: $footer-bg;
  padding: 70px 0 50px;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(circle at 10% 30%, rgba($primary-color, 0.05) 0%, transparent 50%);
    z-index: 1;
  }
  
  .footer-row {
    display: flex;
    flex-wrap: wrap;
    gap: 30px;
    position: relative;
    z-index: 2;
  }
  
  .footer-col {
    flex: 1;
    min-width: 250px;
  }
}

.footer-brand {
  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    
    .icon-utensils {
      font-size: 22px;
      color: $primary-color;
      background: rgba(255, 255, 255, 0.1);
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    
    h2 {
      font-size: 18px;
      font-weight: 700;
      margin: 0;
      background: $gradient-primary;
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
  
  .brand-description {
    color: rgba(255, 255, 255, 0.7);
    margin-bottom: 25px;
    max-width: 300px;
  }
  
  .social-media {
    display: flex;
    gap: 12px;
    
    .social-link {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      transition: all 0.3s;
      
      &:hover {
        background: $gradient-primary;
        transform: translateY(-3px);
      }
    }
  }
}

.footer-links {
  ul {
    list-style: none;
    padding: 0;
    margin: 0;
    
    li {
      margin-bottom: 12px;
      
      a {
        display: inline-block;
        position: relative;
        padding-left: 18px;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 6px;
          height: 6px;
          background-color: $primary-color;
          border-radius: 50%;
          transition: all 0.3s;
        }
        
        &:hover {
          padding-left: 22px;
          
          &::before {
            width: 10px;
            height: 10px;
            background: $gradient-primary;
          }
        }
      }
    }
  }
}

.footer-newsletter {
  p {
    color: rgba(255, 255, 255, 0.7);
    margin-bottom: 20px;
  }
  
  .newsletter-form {
    display: flex;
    margin-bottom: 10px;
    
    .el-input {
      :deep(.el-input__wrapper) {
        border-radius: 4px 0 0 4px;
        background-color: rgba(255, 255, 255, 0.1);
        box-shadow: none;
        border: none;
        
        .el-input__inner {
          color: white;
          
          &::placeholder {
            color: rgba(255, 255, 255, 0.5);
          }
        }
      }
    }
    
    .el-button {
      border-radius: 0 4px 4px 0;
      background: $gradient-primary;
      border: none;
      box-shadow: none;
      
      &:hover {
        background: linear-gradient(135deg, darken($primary-color, 5%), #ff5252);
      }
    }
  }
  
  .newsletter-info {
    font-size: 12px;
    color: rgba(255, 255, 255, 0.5);
    margin-top: 10px;
  }
}

.footer-bottom {
  background-color: $footer-bg-secondary;
  padding: 25px 0;
  
  .footer-bottom-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .copyright {
      color: rgba(255, 255, 255, 0.7);
    }
    
    .footer-cards {
      display: flex;
      gap: 10px;
      
      .payment-card {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 25px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: 4px;
        color: rgba(255, 255, 255, 0.7);
        
        &.visa {
          color: #1434CB;
          background: white;
        }
        
        &.mastercard {
          color: #FF5F00;
          background: white;
        }
        
        &.amex {
          color: #2E77BB;
          background: white;
        }
        
        &.paypal {
          color: #253B80;
          background: white;
        }
      }
    }
  }
}

.footer-app-badge {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 99;
  
  .badge-content {
    display: flex;
    align-items: center;
    gap: 10px;
    background: $gradient-primary;
    color: white;
    padding: 12px 20px;
    border-radius: 50px;
    box-shadow: 0 5px 15px rgba($primary-color, 0.3);
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 20px rgba($primary-color, 0.4);
    }
    
    .el-icon {
      font-size: 22px;
    }
    
    .badge-text {
      display: flex;
      flex-direction: column;
      line-height: 1.2;
      
      span {
        font-size: 10px;
        opacity: 0.9;
      }
      
      strong {
        font-size: 14px;
        font-weight: 600;
      }
    }
  }
}

@media (max-width: 768px) {
  .footer-top {
    padding: 50px 0 30px;
    
    .footer-row {
      flex-direction: column;
      gap: 40px;
    }
  }
  
  .footer-bottom {
    .footer-bottom-content {
      flex-direction: column;
      gap: 15px;
      text-align: center;
    }
  }
  
  .footer-brand {
    .logo {
      justify-content: center;
    }
    
    .brand-description {
      max-width: none;
      text-align: center;
    }
    
    .social-media {
      justify-content: center;
    }
  }
  
  .footer-links {
    h3 {
      text-align: center;
      
      &::after {
        left: 50%;
        transform: translateX(-50%);
      }
    }
    
    ul {
      text-align: center;
    }
  }
  
  .footer-newsletter {
    h3 {
      text-align: center;
      
      &::after {
        left: 50%;
        transform: translateX(-50%);
      }
    }
    
    p {
      text-align: center;
    }
  }
}
</style> 