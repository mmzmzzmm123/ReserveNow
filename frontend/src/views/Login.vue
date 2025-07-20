<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>{{ isLogin ? 'Restaurant ReserveNow' : 'User Registration' }}</h1>
        <div class="header-decoration"></div>
      </div>
      
      <!-- Login Panel -->
      <div class="login-form" v-if="isLogin">
        <el-form :model="loginForm" ref="loginFormRef">
          <el-form-item>
            <el-input 
              v-model="loginForm.email" 
              placeholder="Please input email"
              :prefix-icon="Message"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="Please input password"
              :prefix-icon="Lock"
              class="custom-input"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <div class="captcha-container">
              <el-input 
                v-model="loginForm.captcha" 
                placeholder="Please input captcha"
                :prefix-icon="Key"
                class="custom-input captcha-input"
              />
              <div class="captcha-box" @click="getCaptcha">
                <img v-if="captchaImg" :src="captchaImg" alt="Captcha" class="captcha-image" />
                <div v-else class="mock-captcha">Loading...</div>
              </div>
            </div>
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="loginForm.remember" class="remember-me">Remember me</el-checkbox>
            <el-button type="text" class="forgot-password">Forgot password?</el-button>
          </div>
          
          <el-form-item class="login-btn-container">
            <el-button 
              type="primary" 
              class="login-btn" 
              @click="handleLogin"
            >
              Login
            </el-button>
          </el-form-item>
          
          <div class="other-login-methods">
            <div class="divider">
              <span class="divider-text">Other login methods</span>
            </div>
            <div class="social-login">
              <el-button circle class="social-icon" type="info">
                <el-icon><Message /></el-icon>
              </el-button>
              <el-button circle class="social-icon" type="success">
                <el-icon><ChatDotRound /></el-icon>
              </el-button>
              <el-button circle class="social-icon" type="danger">
                <el-icon><Phone /></el-icon>
              </el-button>
            </div>
          </div>
        </el-form>
        
        <div class="login-footer">
          <p>Don't have an account? <el-button type="text" class="register-link" @click="switchPanel(false)">Register now</el-button></p>
        </div>
      </div>
      
      <!-- Registration Panel -->
      <div class="register-form" v-else>
        <el-form :model="registerForm" ref="registerFormRef">
          <el-form-item>
            <el-input 
              v-model="registerForm.username" 
              placeholder="Please input username"
              :prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="Please input password"
              :prefix-icon="Lock"
              class="custom-input"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="Please confirm password"
              :prefix-icon="Lock"
              class="custom-input"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-input 
              v-model="registerForm.email" 
              placeholder="Please input email"
              :prefix-icon="Message"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item>
            <div class="captcha-container">
              <el-input 
                v-model="registerForm.captcha" 
                placeholder="Please input captcha"
                :prefix-icon="Key"
                class="custom-input captcha-input"
              />
              <div class="captcha-box" @click="getCaptcha">
                <img v-if="captchaImg" :src="captchaImg" alt="Captcha" class="captcha-image" />
                <div v-else class="mock-captcha">Loading...</div>
              </div>
            </div>
          </el-form-item>
          
          <el-form-item class="register-btn-container">
            <el-button 
              type="primary" 
              class="register-btn" 
              @click="handleRegister"
            >
              Register
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="register-footer">
          <p>Already have an account? <el-button type="text" class="login-link" @click="switchPanel(true)">Back to login</el-button></p>
        </div>
      </div>
    </div>
    
    <div class="decoration-box decoration-box-1"></div>
    <div class="decoration-box decoration-box-2"></div>
    <div class="decoration-box decoration-box-3"></div>
    <div class="decoration-box decoration-box-4"></div>
    <div class="decoration-box decoration-box-5"></div>
    
    <div class="floating-shapes">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { User, Lock, Message, ChatDotRound, Phone, Key } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
import { useRouter } from 'vue-router';

const router = useRouter();

// Control the current panel display (login/register)
const isLogin = ref(true);

const loginFormRef = ref(null);
const registerFormRef = ref(null);

// Captcha image
const captchaImg = ref('');
// Captcha ID
const captchaCode = ref('');

// Login form data
const loginForm = reactive({
  email: '',
  password: '',
  captcha: '',
  remember: false
});

// Registration form data
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  captcha: ''
});

// Get captcha when page loads
onMounted(() => {
  getCaptcha();
  loadSavedCredentials();
});

// Load saved credentials from local storage
const loadSavedCredentials = () => {
  const savedCredentials = localStorage.getItem('savedCredentials');
  if (savedCredentials) {
    const { email, password } = JSON.parse(savedCredentials);
    loginForm.email = email;
    loginForm.password = password;
    loginForm.remember = true;
  }
};

// Get captcha
const getCaptcha = async () => {
  try {
    const res = await request({
      url: '/users/captcha',
      method: 'GET'
    });
    captchaImg.value = res.data.image;
    captchaCode.value = res.data.captchaId;
  } catch (error) {
    console.error('Failed to get captcha', error);
  }
};

// Set cookie
const setCookie = (name, value, days) => {
  const expires = new Date();
  expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
  document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/`;
};

// Switch panel
const switchPanel = (login) => {
  isLogin.value = login;
  // Get new captcha when switching panels
  getCaptcha();
  
  // Clear form data when switching to registration panel
  if (!login) {
    loginForm.email = '';
    loginForm.password = '';
    loginForm.captcha = '';
    loginForm.remember = false;
  }
};

// Clear saved credentials
const clearSavedCredentials = () => {
  localStorage.removeItem('savedCredentials');
};

// Login handler
const handleLogin = async () => {
  if (!loginForm.email) {
    ElMessage.warning('Please input email');
    return;
  }
  
  if (!loginForm.password) {
    ElMessage.warning('Please input password');
    return;
  }
  
  if (!loginForm.captcha) {
    ElMessage.warning('Please input captcha');
    return;
  }
  
  try {
    const res = await request({
      url: '/users/login',
      method: 'POST',
      data: {
        email: loginForm.email,
        password: loginForm.password,
        captcha: loginForm.captcha,
        captchaId: captchaCode.value
      }
    });
    
    // Login success
    ElMessage.success('Login successful');
    
    // Save credentials if remember me is checked
    console.log(loginForm.remember);
    if (loginForm.remember) {
      localStorage.setItem('savedCredentials', JSON.stringify({
        email: loginForm.email,
        password: loginForm.password
      }));
    } else {
      localStorage.removeItem('savedCredentials');
    }
    
    // Store user info locally
    const userData = res.data;
    localStorage.setItem('userInfo', JSON.stringify({
      userId: userData.userId,
      role: userData.role,
      name: userData.name,
      email: userData.email,
      avatar: userData.avatar,
      token: userData.token
    }));
    // Store token in cookie
    setCookie('token', userData.token, 7);
    
    if (userData.role === 2) {
      router.push('/');
    } else {
      router.push('/admin/dashboard');
    }
  } catch (error) {
    // Login failed, refresh captcha
    getCaptcha();
  }
};

// Registration handler
const handleRegister = async () => {
  if (!registerForm.username) {
    ElMessage.warning('Please input username');
    return;
  }
  
  if (!registerForm.password) {
    ElMessage.warning('Please input password');
    return;
  }
  
  if (!registerForm.confirmPassword) {
    ElMessage.warning('Please confirm password');
    return;
  }
  
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning('Passwords do not match');
    return;
  }
  
  if (!registerForm.email) {
    ElMessage.warning('Please input email');
    return;
  }
  
  if (!registerForm.captcha) {
    ElMessage.warning('Please input captcha');
    return;
  }
  
  try {
    const res = await request({
      url: '/users/register',
      method: 'POST',
      data: {
        name: registerForm.username,
        email: registerForm.email,
        password: registerForm.password,
        confirmPassword: registerForm.confirmPassword,
        captcha: registerForm.captcha,
        captchaId: captchaCode.value
      }
    });
    
    // Registration success
    ElMessage.success('Registration successful, please login');
    
    // Switch to login panel
    switchPanel(true);
  } catch (error) {
    // Registration failed, refresh captcha
    getCaptcha();
  }
};
</script>

<style lang="scss" scoped>
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e6eb 100%);
  overflow: hidden;
}

.login-box {
  position: relative;
  z-index: 10;
  width: 420px;
  background-color: rgba(255, 255, 255, 0.92);
  border-radius: 12px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  padding: 40px;
  backdrop-filter: blur(5px);
  animation: fadeIn 0.6s ease-out;
  
  &::before {
    content: '';
    position: absolute;
    top: -5px;
    left: -5px;
    right: -5px;
    bottom: -5px;
    background: linear-gradient(45deg, transparent 30%, #409eff 45%, #409eff 55%, transparent 70%);
    z-index: -1;
    border-radius: 15px;
    filter: blur(10px);
    opacity: 0.7;
    animation: borderLight 6s linear infinite;
  }
}

.login-header {
  text-align: center;
  margin-bottom: 35px;
  
  h1 {
    color: #303133;
    font-size: 28px;
    font-weight: 600;
    margin-bottom: 10px;
    letter-spacing: 1px;
    animation: slideDown 0.6s ease;
  }
  
  .header-decoration {
    height: 4px;
    width: 60px;
    background: linear-gradient(90deg, #67c23a, #409eff);
    margin: 0 auto;
    clip-path: polygon(0 0, 100% 0, 95% 100%, 5% 100%);
    animation: expandWidth 0.8s ease-out;
  }
}

.login-form, .register-form {
  .custom-input {
    --el-input-height: 50px;
    --el-input-border-radius: 10px;
    margin-bottom: 15px;
    
    &:deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px rgba(220, 223, 230, 0.5) inset;
      background-color: rgba(255, 255, 255, 0.8);
      backdrop-filter: blur(5px);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      padding: 0 15px;
      
      &:hover {
        box-shadow: 0 3px 12px rgba(0, 0, 0, 0.07), 0 0 0 1px rgba(192, 196, 204, 0.7) inset;
        transform: translateY(-2px);
      }
      
      &.is-focus {
        box-shadow: 0 6px 18px rgba(64, 158, 255, 0.1), 0 0 0 2px rgba(64, 158, 255, 0.4) inset;
        transform: translateY(-2px);
        background-color: rgba(255, 255, 255, 0.95);
      }
      
      .el-input__prefix-inner {
        margin-right: 10px;
        
        .el-icon {
          font-size: 18px;
          color: #909399;
          transition: all 0.3s ease;
        }
      }
      
      .el-input__inner {
        font-size: 15px;
        color: #303133;
        letter-spacing: 0.5px;
        
        &::placeholder {
          color: #c0c4cc;
          transition: all 0.3s ease;
        }
      }
      
      &:hover .el-input__prefix-inner .el-icon,
      &.is-focus .el-input__prefix-inner .el-icon {
        color: #409eff;
      }
      
      &.is-focus .el-input__inner::placeholder {
        opacity: 0.6;
        transform: translateX(5px);
      }
    }
  }
}

.captcha-container {
  display: flex;
  gap: 15px;
  
  .captcha-input {
    flex: 1;
  }
  
  .captcha-box {
    width: 120px;
    height: 50px;
    border-radius: 10px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f7f8fa, #e9ebee);
    cursor: pointer;
    box-shadow: 0 0 0 1px rgba(220, 223, 230, 0.5) inset;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    .mock-captcha {
      color: #909399;
      font-size: 14px;
      font-weight: 500;
      letter-spacing: 2px;
      text-align: center;
      user-select: none;
    }
    
    &:hover {
      box-shadow: 0 3px 12px rgba(0, 0, 0, 0.07), 0 0 0 1px rgba(192, 196, 204, 0.7) inset;
      transform: translateY(-2px);
    }
    
    .captcha-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 5px 0 20px;
  
  .remember-me {
    color: #606266;
    font-size: 14px;
  }
  
  .forgot-password {
    font-size: 14px;
    color: #409eff;
    
    &:hover {
      color: #337ecc;
    }
  }
}

.login-btn-container, .register-btn-container {
  margin-top: 10px;
  margin-bottom: 25px;
}

.login-btn, .register-btn {
  width: 100%;
  height: 50px;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #409eff, #53a8ff);
  border: none;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  transform: translateY(0);
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: all 0.5s ease;
  }
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 7px 14px rgba(50, 50, 93, 0.1), 0 3px 6px rgba(0, 0, 0, 0.08);
    
    &::before {
      left: 100%;
      animation: shine 1.5s infinite;
    }
  }
  
  &:active {
    transform: translateY(1px);
  }
}

.other-login-methods {
  margin-top: 20px;
}

.divider {
  display: flex;
  align-items: center;
  margin: 15px 0;
  
  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background-color: #dcdfe6;
  }
  
  .divider-text {
    padding: 0 15px;
    color: #909399;
    font-size: 14px;
  }
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
  
  .social-icon {
    width: 42px;
    height: 42px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    }
  }
}

.login-footer, .register-footer {
  text-align: center;
  margin-top: 20px;
  color: #606266;
  font-size: 14px;
  
  .register-link, .login-link {
    font-weight: 500;
    color: #409eff;
    
    &:hover {
      color: #337ecc;
    }
  }
}

.decoration-box {
  position: absolute;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 4px;
  transform: rotate(45deg);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.05);
  
  &-1 {
    width: 200px;
    height: 200px;
    bottom: -100px;
    right: 10%;
    background: linear-gradient(135deg, #ffffff, #f2f2f2);
    animation: float 10s ease-in-out infinite;
  }
  
  &-2 {
    width: 300px;
    height: 300px;
    top: -150px;
    left: 10%;
    background: linear-gradient(135deg, #ffffff, #f5f5f5);
    animation: float 12s ease-in-out infinite;
    animation-delay: 1s;
  }
  
  &-3 {
    width: 150px;
    height: 150px;
    top: 20%;
    right: -75px;
    background: linear-gradient(135deg, #f8f8f8, #ffffff);
    animation: float 8s ease-in-out infinite;
    animation-delay: 2s;
  }
  
  &-4 {
    width: 120px;
    height: 120px;
    bottom: 15%;
    left: 5%;
    background: linear-gradient(135deg, #f5f5f5, #ffffff);
    animation: float 9s ease-in-out infinite;
    animation-delay: 1.5s;
  }
  
  &-5 {
    width: 80px;
    height: 80px;
    top: 15%;
    left: 25%;
    background: linear-gradient(135deg, #ffffff, #f8f8f8);
    animation: float 7s ease-in-out infinite;
    animation-delay: 0.5s;
  }
}

.floating-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-shape {
  position: absolute;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(103, 194, 58, 0.1));
  border-radius: 50%;
  
  &.shape-1 {
    width: 80px;
    height: 80px;
    top: 20%;
    left: 15%;
    animation: floatShape 15s ease-in-out infinite;
  }
  
  &.shape-2 {
    width: 60px;
    height: 60px;
    bottom: 30%;
    right: 15%;
    animation: floatShape 12s ease-in-out infinite;
    animation-delay: 1s;
  }
  
  &.shape-3 {
    width: 40px;
    height: 40px;
    top: 70%;
    left: 30%;
    animation: floatShape 10s ease-in-out infinite;
    animation-delay: 2s;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes expandWidth {
  from {
    width: 0;
  }
  to {
    width: 60px;
  }
}

@keyframes borderLight {
  0% {
    background-position: 0 0;
  }
  100% {
    background-position: 400% 0;
  }
}

@keyframes float {
  0% {
    transform: translateY(0) rotate(45deg);
  }
  50% {
    transform: translateY(-20px) rotate(45deg);
  }
  100% {
    transform: translateY(0) rotate(45deg);
  }
}

@keyframes floatShape {
  0% {
    transform: translate(0, 0);
  }
  25% {
    transform: translate(10px, 10px);
  }
  50% {
    transform: translate(0, 20px);
  }
  75% {
    transform: translate(-10px, 10px);
  }
  100% {
    transform: translate(0, 0);
  }
}

@keyframes shine {
  0% {
    left: -100%;
  }
  20% {
    left: 100%;
  }
  100% {
    left: 100%;
  }
}

@media (max-width: 480px) {
  .login-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .form-options {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .captcha-container {
    flex-direction: column;
    
    .captcha-box {
      width: 100%;
    }
  }
}
</style>