<template>
  <AppNavbar />
  <div class="explore-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">Explore the Infinite Possibilities of Food</h1>
        <p class="hero-subtitle">Make every meal a memorable experience</p>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-number">1000+</span>
            <span class="stat-label">Curated Restaurants</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">500K+</span>
            <span class="stat-label">Trusted Users</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">98%</span>
            <span class="stat-label">Satisfaction Rate</span>
          </div>
        </div>
      </div>
      <div class="hero-decoration"></div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <h2 class="section-title">Why Choose Us</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Calendar /></el-icon>
          </div>
          <h3>Easy Booking</h3>
          <p>Book your favorite restaurant anytime, anywhere with just one click, no more phone calls</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Star /></el-icon>
          </div>
          <h3>Curated Restaurants</h3>
          <p>Strictly selected partner restaurants to ensure the ultimate dining experience</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Discount /></el-icon>
          </div>
          <h3>Exclusive Offers</h3>
          <p>Member-only discounts and holiday specials for an amazing dining experience</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon><Service /></el-icon>
          </div>
          <h3>Attentive Service</h3>
          <p>Professional customer service team to solve any booking issues</p>
        </div>
      </div>
    </section>

    <!-- Process Section -->
    <section class="process-section">
      <h2 class="section-title">Easy Booking, Great Dining</h2>
      <div class="process-steps">
        <div class="process-step">
          <div class="step-number">1</div>
          <h3>Choose Restaurant</h3>
          <p>Browse curated restaurants, check menus, ambiance, and user reviews</p>
        </div>
        <div class="process-step">
          <div class="step-number">2</div>
          <h3>Book Online</h3>
          <p>Select date and time, fill in booking details, complete reservation in one click</p>
        </div>
        <div class="process-step">
          <div class="step-number">3</div>
          <h3>Enjoy Dining</h3>
          <p>Arrive on time, show your booking code, and enjoy your meal</p>
        </div>
      </div>
    </section>

    <!-- Testimonials Section -->
    <section class="testimonials-section">
      <h2 class="section-title">User Reviews</h2>
      <div class="testimonials-carousel">
        <el-carousel :interval="4000" type="card">
          <el-carousel-item v-for="testimonial in testimonials" :key="testimonial.id">
            <div class="testimonial-card">
              <div class="testimonial-content">
                <div class="quote-icon">
                  <el-icon><ChatRound /></el-icon>
                </div>
                <p class="testimonial-text">{{ testimonial.content }}</p>
                <div class="testimonial-author">
                  <img :src="testimonial.avatar" :alt="testimonial.name" class="author-avatar">
                  <div class="author-info">
                    <h4>{{ testimonial.name }}</h4>
                    <p>{{ testimonial.title }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="cta-content">
        <h2>Start Your Culinary Journey</h2>
        <p>Register now and enjoy convenient dining experiences</p>
        <el-button type="primary" size="large" @click="goToRegister">
          Free Registration
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </section>
  </div>
  <AppFooter />
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import AppNavbar from '@/components/AppNavbar.vue';
import AppFooter from '@/components/AppFooter.vue';
import { 
  Calendar, 
  Star, 
  Discount, 
  Service, 
  ChatRound,
  ArrowRight
} from '@element-plus/icons-vue';

const router = useRouter();

const testimonials = ref([
  {
    id: 1,
    content: "This platform makes restaurant booking so convenient. The interface is clean and user-friendly, and most importantly, I can see real user reviews which helped me find many great restaurants!",
    name: "Ms. Zhang",
    title: "Food Enthusiast",
    avatar: "https://randomuser.me/api/portraits/women/1.jpg"
  },
  {
    id: 2,
    content: "As a business professional who frequently needs to book restaurants, this platform saves me a lot of time. Customer service responds quickly and resolves any issues promptly.",
    name: "Mr. Li",
    title: "Corporate Executive",
    avatar: "https://randomuser.me/api/portraits/men/2.jpg"
  },
  {
    id: 3,
    content: "The restaurants on the platform are all high-quality and prices are transparent. The recent holiday promotions are great, I've already recommended it to my friends!",
    name: "Ms. Wang",
    title: "Food Blogger",
    avatar: "https://randomuser.me/api/portraits/women/3.jpg"
  }
]);

const goToRegister = () => {
  router.push('/login');
};
</script>

<style lang="scss" scoped>
$primary-color: #ff4757;
$secondary-color: #f8f1ff;
$accent-color: #6c5ce7;
$dark-color: #2d3436;
$light-color: #f9f9f9;
$gray-color: #636e72;

.explore-page {
  padding-top: 70px;
  background-color: #fcfcfc;
}

.hero-section {
  position: relative;
  min-height: 600px;
  background: linear-gradient(135deg, $secondary-color, #fff);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 60px 20px;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      radial-gradient(circle at 20% 30%, rgba($primary-color, 0.05) 0%, transparent 50%),
      radial-gradient(circle at 80% 70%, rgba($accent-color, 0.05) 0%, transparent 50%);
  }

  .hero-content {
    position: relative;
    z-index: 2;
    text-align: center;
    max-width: 800px;
  }

  .hero-title {
    font-size: 48px;
    font-weight: 800;
    margin: 0 0 20px;
    background: linear-gradient(135deg, $dark-color, $primary-color);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: fadeInUp 0.8s ease;
  }

  .hero-subtitle {
    font-size: 20px;
    color: $gray-color;
    margin: 0 0 40px;
    animation: fadeInUp 0.8s ease 0.2s both;
  }

  .hero-stats {
    display: flex;
    justify-content: center;
    gap: 40px;
    animation: fadeInUp 0.8s ease 0.4s both;

    .stat-item {
      text-align: center;

      .stat-number {
        display: block;
        font-size: 36px;
        font-weight: 800;
        color: $primary-color;
        margin-bottom: 5px;
      }

      .stat-label {
        font-size: 16px;
        color: $gray-color;
      }
    }
  }
}

// 特色区域
.features-section {
  padding: 80px 20px;
  background-color: white;

  .section-title {
    text-align: center;
    font-size: 36px;
    font-weight: 700;
    margin-bottom: 60px;
    color: $dark-color;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -15px;
      left: 50%;
      transform: translateX(-50%);
      width: 60px;
      height: 4px;
      background: linear-gradient(to right, $primary-color, $accent-color);
      border-radius: 2px;
    }
  }

  .features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 30px;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .feature-card {
    background: white;
    padding: 30px;
    border-radius: 20px;
    text-align: center;
    transition: all 0.3s ease;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);

    &:hover {
      transform: translateY(-10px);
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);

      .feature-icon {
        background: $primary-color;
        color: white;
        transform: scale(1.1);
      }
    }

    .feature-icon {
      width: 70px;
      height: 70px;
      margin: 0 auto 20px;
      background: $secondary-color;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s ease;
      color: $primary-color;
      font-size: 24px;
    }

    h3 {
      font-size: 20px;
      margin: 0 0 15px;
      color: $dark-color;
    }

    p {
      font-size: 14px;
      color: $gray-color;
      line-height: 1.6;
      margin: 0;
    }
  }
}

.process-section {
  padding: 80px 20px;
  background: $secondary-color;

  .process-steps {
    display: flex;
    justify-content: center;
    gap: 40px;
    max-width: 1200px;
    margin: 0 auto;
    flex-wrap: wrap;
  }

  .process-step {
    flex: 1;
    min-width: 250px;
    max-width: 300px;
    text-align: center;
    position: relative;

    &:not(:last-child)::after {
      content: '';
      position: absolute;
      top: 40px;
      right: -20px;
      width: 40px;
      height: 2px;
      background: linear-gradient(to right, $primary-color, transparent);
    }

    .step-number {
      width: 50px;
      height: 50px;
      background: $primary-color;
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      font-weight: 700;
      margin: 0 auto 20px;
      position: relative;
      z-index: 1;

      &::before {
        content: '';
        position: absolute;
        top: -5px;
        left: -5px;
        right: -5px;
        bottom: -5px;
        background: rgba($primary-color, 0.2);
        border-radius: 50%;
        z-index: -1;
        animation: pulse 2s infinite;
      }
    }

    h3 {
      font-size: 20px;
      margin: 0 0 10px;
      color: $dark-color;
    }

    p {
      font-size: 14px;
      color: $gray-color;
      margin: 0;
      line-height: 1.6;
    }
  }
}

// 用户评价
.testimonials-section {
  padding: 80px 20px;
  background: white;

  .testimonials-carousel {
    max-width: 1200px;
    margin: 0 auto;

    :deep(.el-carousel__item) {
      padding: 20px;
    }
  }

  .testimonial-card {
    background: white;
    border-radius: 20px;
    padding: 30px;
    height: 100%;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);

    .quote-icon {
      color: $primary-color;
      font-size: 24px;
      margin-bottom: 20px;
    }

    .testimonial-text {
      font-size: 16px;
      color: $gray-color;
      line-height: 1.8;
      margin: 0 0 20px;
    }

    .testimonial-author {
      display: flex;
      align-items: center;
      gap: 15px;

      .author-avatar {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        object-fit: cover;
      }

      .author-info {
        h4 {
          margin: 0 0 5px;
          font-size: 16px;
          color: $dark-color;
        }

        p {
          margin: 0;
          font-size: 14px;
          color: $gray-color;
        }
      }
    }
  }
}

// CTA区域
.cta-section {
  padding: 80px 20px;
  background: linear-gradient(135deg, $primary-color, $accent-color);
  text-align: center;
  color: white;

  .cta-content {
    max-width: 600px;
    margin: 0 auto;

    h2 {
      font-size: 36px;
      font-weight: 700;
      margin: 0 0 20px;
    }

    p {
      font-size: 18px;
      margin: 0 0 30px;
      opacity: 0.9;
    }

    .el-button {
      padding: 15px 40px;
      font-size: 18px;
      border-radius: 30px;
      background: white;
      color: $primary-color;
      border: none;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
      }
    }
  }
}

// 动画
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

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.5;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

// 响应式样式
@media (max-width: 768px) {
  .hero-section {
    min-height: 500px;
    padding: 40px 20px;

    .hero-title {
      font-size: 36px;
    }

    .hero-subtitle {
      font-size: 18px;
    }

    .hero-stats {
      flex-direction: column;
      gap: 20px;
    }
  }

  .process-section .process-step::after {
    display: none;
  }

  .features-section .features-grid {
    grid-template-columns: 1fr;
  }
}
</style> 