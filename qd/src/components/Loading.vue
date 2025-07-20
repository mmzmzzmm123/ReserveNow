<template>
  <div class="loading-container">
    <div class="loading-content">
      <div class="loading-spinner">
        <div class="circle-group">
          <div class="circle c1"></div>
          <div class="circle c2"></div>
          <div class="circle c3"></div>
        </div>
        <div class="pulse-ring"></div>
      </div>
      <div class="loading-text">Loding...</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Loading'
}
</script>

<style scoped>
.loading-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  background-color: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(5px);
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.loading-spinner {
  position: relative;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.circle-group {
  position: relative;
  width: 60px;
  height: 60px;
}

.circle {
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: linear-gradient(to right, #ff23ed, #2575fc);
  box-shadow: 0 0 10px rgba(255, 166, 31, 0.5);
  animation-duration: 1.5s;
  animation-iteration-count: infinite;
  animation-timing-function: cubic-bezier(0.165, 0.84, 0.44, 1);
}

.c1 {
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  animation-name: bounce-vertical;
  animation-delay: 0s;
}

.c2 {
  bottom: 0;
  left: 0;
  animation-name: bounce-diagonal-1;
  animation-delay: 0.2s;
}

.c3 {
  bottom: 0;
  right: 0;
  animation-name: bounce-diagonal-2;
  animation-delay: 0.4s;
}

.pulse-ring {
  position: absolute;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.5);
  animation: pulse 2s cubic-bezier(0.455, 0.03, 0.515, 0.955) infinite;
}

.loading-text {
  color: white;
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  animation: fade-in-out 1.5s ease-in-out infinite;
}

@keyframes bounce-vertical {
  0%, 100% { transform: translate(-50%, 0); }
  50% { transform: translate(-50%, 30px); }
}

@keyframes bounce-diagonal-1 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(15px, -25px); }
}

@keyframes bounce-diagonal-2 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-15px, -25px); }
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 0.8; }
  50% { transform: scale(1.1); opacity: 0.5; }
  100% { transform: scale(0.8); opacity: 0.8; }
}

@keyframes fade-in-out {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}
</style>