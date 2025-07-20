<template>
  <div class="system-settings-container">
    <!-- System information card -->
    <div class="info-section">
      <div class="system-card neomorphic-card">
        <div class="card-header">
          <div class="logo-container">
            <div class="logo">
              <span class="logo-text">QD</span>
            </div>
          </div>
          <h1>Restaurant Reservation Management System</h1>
          <p class="version">Version 1.0.0</p>
        </div>
        <div class="card-content">
          <p class="description">
            A modern restaurant reservation management system, providing restaurant management, reservation management, staff management and other functions.
            The front-end separation architecture is adopted, and the latest technology stack is used to ensure the reliability and scalability of the system.
          </p>
        </div>
      </div>
    </div>

    <!-- Technology Stack Display -->
    <div class="tech-stack-section">
      <h2 class="section-title">Technology Stack</h2>
      
      <!-- Backend Technologies -->
      <div class="stack-container">
        <h3 class="stack-category">Backend Technologies</h3>
        <div class="tech-grid">
          <div class="tech-card neomorphic-card" data-tech="Spring Boot">
            <div class="tech-icon">
              <img src="https://spring.io/img/spring.svg" alt="Spring Boot" />
            </div>
            <div class="tech-info">
              <h4>Spring Boot</h4>
              <p>Core Framework</p>
            </div>
          </div>
          
          <div class="tech-card neomorphic-card" data-tech="Java 17">
            <div class="tech-icon">
              <img src="https://www.oracle.com/a/tech/img/cb88-java-logo-001.jpg" alt="Java" />
            </div>
            <div class="tech-info">
              <h4>Java 17</h4>
              <p>Development Language</p>
            </div>
          </div>
          
          <div class="tech-card neomorphic-card" data-tech="MyBatis Plus">
            <div class="tech-icon">
              <img src="https://baomidou.com/assets/asset.cIbiVTt_.svg" alt="MyBatis Plus" />
            </div>
            <div class="tech-info">
              <h4>MyBatis Plus</h4>
              <p>ORM Framework</p>
            </div>
          </div>
          
          <div class="tech-card neomorphic-card" data-tech="JWT">
            <div class="tech-icon">
              <img src="https://jwt.io/img/pic_logo.svg" alt="JWT" />
            </div>
            <div class="tech-info">
              <h4>JWT</h4>
              <p>Authentication</p>
            </div>
          </div>
          
          <div class="tech-card neomorphic-card" data-tech="MySQL">
            <div class="tech-icon">
              <img src="https://www.mysql.com/common/logos/logo-mysql-170x115.png" alt="MySQL" />
            </div>
            <div class="tech-info">
              <h4>MySQL</h4>
              <p>Database</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Frontend Technologies -->
      <div class="stack-container">
        <h3 class="stack-category">Frontend Technologies</h3>
        <div class="tech-grid">
          <div class="tech-card neomorphic-card" data-tech="Vue 3">
            <div class="tech-icon">
              <img src="https://vuejs.org/images/logo.png" alt="Vue 3" />
            </div>
            <div class="tech-info">
              <h4>Vue 3</h4>
              <p>Frontend Framework</p>
            </div>
          </div>
          
          <div class="tech-card neomorphic-card" data-tech="Vuex">
            <div class="tech-icon">
              <img src="https://vuex.vuejs.org/logo.png" alt="Vuex" />
            </div>
            <div class="tech-info">
              <h4>Vuex</h4>
              <p>State Management</p>
            </div>
          </div>
          
          <div class="tech-card neomorphic-card" data-tech="Element Plus">
            <div class="tech-icon">
              <img src="https://element-plus.org/images/element-plus-logo.svg" alt="Element Plus" />
            </div>
            <div class="tech-info">
              <h4>Element Plus</h4>
              <p>UI Component Library</p>
            </div>
          </div>
          
          <div class="tech-card neomorphic-card" data-tech="Axios">
            <div class="tech-icon">
              <img src="https://axios-http.com/assets/logo.svg" alt="Axios" />
            </div>
            <div class="tech-info">
              <h4>Axios</h4>
              <p>HTTP Client</p>
            </div>
          </div>
          
          <div class="tech-card neomorphic-card" data-tech="Vue Router">
            <div class="tech-icon">
              <img src="https://router.vuejs.org/logo.png" alt="Vue Router" />
            </div>
            <div class="tech-info">
              <h4>Vue Router</h4>
              <p>Router Management</p>
            </div>
          </div>
          
          <div class="tech-card neomorphic-card" data-tech="ECharts">
            <div class="tech-icon">
              <img src="https://echarts.apache.org/zh/images/logo.png" alt="ECharts" />
            </div>
            <div class="tech-info">
              <h4>ECharts</h4>
              <p>Chart Library</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- System Features -->
    <div class="features-section">
      <h2 class="section-title">System Features</h2>
      <div class="features-grid">
        <div class="feature-card neomorphic-card">
          <div class="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
          </div>
          <h3>Secure & Reliable</h3>
          <p>JWT authentication ensures system security</p>
        </div>

        <div class="feature-card neomorphic-card">
          <div class="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M16 8l-4 4-4-4"/>
              <path d="M12 6v8"/>
            </svg>
          </div>
          <h3>High Performance</h3>
          <p>Optimized database design and caching strategy</p>
        </div>

        <div class="feature-card neomorphic-card">
          <div class="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
              <path d="M8 21h8"/>
              <path d="M12 17v4"/>
            </svg>
          </div>
          <h3>Responsive Design</h3>
          <p>Perfect adaptation for all device screens</p>
        </div>

        <div class="feature-card neomorphic-card">
          <div class="feature-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              <path d="M9 12l2 2 4-4"/>
            </svg>
          </div>
          <h3>Easy to Use</h3>
          <p>Intuitive user interface and operation flow</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  // 添加技术卡片悬停效果
  const techCards = document.querySelectorAll('.tech-card')
  techCards.forEach(card => {
    card.addEventListener('mousemove', (e) => {
      const rect = card.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top
      
      const centerX = rect.width / 2
      const centerY = rect.height / 2
      
      const angleX = (y - centerY) / 10
      const angleY = (centerX - x) / 10
      
      card.style.transform = `perspective(1000px) rotateX(${angleX}deg) rotateY(${angleY}deg) scale3d(1.05, 1.05, 1.05)`
    })
    
    card.addEventListener('mouseleave', () => {
      card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) scale3d(1, 1, 1)'
    })
  })
})
</script>

<style scoped>
.system-settings-container {
  padding: 30px;
  min-height: 100vh;
  background-color: #e6e7ee;
  background-image: linear-gradient(135deg, #e6e7ee 0%, #f5f7fa 100%);
  color: #5e6687;
}

/* 新拟态卡片基础样式 */
.neomorphic-card {
  background: #e6e7ee;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 8px 8px 16px #c3c4ca,
              -8px -8px 16px #ffffff;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.neomorphic-card:hover {
  transform: translateY(-5px);
  box-shadow: 12px 12px 20px #c3c4ca,
              -12px -12px 20px #ffffff;
}

/* 系统信息卡片样式 */
.info-section {
  margin-bottom: 40px;
}

.system-card {
  text-align: center;
  padding: 40px;
}

.logo-container {
  margin-bottom: 20px;
}

.logo {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  background: linear-gradient(135deg, #4d78ef, #5a6bed);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 6px 6px 12px #c3c4ca,
              -6px -6px 12px #ffffff;
}

.logo-text {
  color: white;
  font-size: 32px;
  font-weight: bold;
}

.card-header h1 {
  font-size: 28px;
  margin: 20px 0 10px;
  color: #5e6687;
}

.version {
  font-size: 16px;
  color: #8a94a6;
  margin-bottom: 20px;
}

.description {
  font-size: 16px;
  line-height: 1.6;
  color: #5e6687;
  max-width: 800px;
  margin: 0 auto;
}

/* 技术栈部分样式 */
.section-title {
  font-size: 24px;
  margin-bottom: 30px;
  text-align: center;
  color: #5e6687;
}

.stack-container {
  margin-bottom: 40px;
}

.stack-category {
  font-size: 20px;
  margin-bottom: 20px;
  color: #5e6687;
  text-align: center;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.tech-card {
  display: flex;
  align-items: center;
  padding: 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  transform-style: preserve-3d;
}

.tech-icon {
  width: 60px;
  height: 60px;
  margin-right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tech-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.tech-info h4 {
  font-size: 18px;
  margin: 0 0 5px;
  color: #5e6687;
}

.tech-info p {
  font-size: 14px;
  margin: 0;
  color: #8a94a6;
}

/* 特性部分样式 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  margin-top: 30px;
}

.feature-card {
  text-align: center;
  padding: 30px;
}

.feature-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 20px;
  color: #4d78ef;
}

.feature-card h3 {
  font-size: 20px;
  margin-bottom: 10px;
  color: #5e6687;
}

.feature-card p {
  font-size: 16px;
  color: #8a94a6;
  margin: 0;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .system-settings-container {
    background-color: #2d3748;
    background-image: linear-gradient(135deg, #2d3748 0%, #1a202c 100%);
    color: #e2e8f0;
  }

  .neomorphic-card {
    background: #2d3748;
    box-shadow: 8px 8px 16px #202632,
                -8px -8px 16px #3a485e;
    border: 1px solid rgba(255, 255, 255, 0.1);
  }

  .neomorphic-card:hover {
    box-shadow: 12px 12px 20px #202632,
                -12px -12px 20px #3a485e;
  }

  .card-header h1,
  .section-title,
  .stack-category,
  .tech-info h4,
  .feature-card h3 {
    color: #e2e8f0;
  }

  .version,
  .description,
  .tech-info p,
  .feature-card p {
    color: #a0aec0;
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .system-settings-container {
    padding: 20px;
  }

  .tech-grid,
  .features-grid {
    grid-template-columns: 1fr;
  }

  .system-card {
    padding: 30px 20px;
  }

  .card-header h1 {
    font-size: 24px;
  }

  .description {
    font-size: 14px;
  }
}

/* 添加动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tech-card,
.feature-card {
  animation: fadeIn 0.6s ease-out forwards;
}

.tech-card:nth-child(n) {
  animation-delay: calc(0.1s * var(--i));
}

.feature-card:nth-child(n) {
  animation-delay: calc(0.1s * var(--i));
}
</style> 