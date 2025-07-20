<template>
  <div class="popup-overlay" v-if="visible" @click.self="closePanel">
    <div class="popup-panel neumorphic">
      <div class="panel-header">
        <h2>{{ title }}</h2>
        <button class="close-btn neumorphic-btn" @click="closePanel">&times;</button>
      </div>
      
      <div class="panel-body">
        <form @submit.prevent="submitForm">
          <div class="form-grid">
            <div 
              v-for="(field, index) in fields" 
              :key="field.name"
              class="form-group"
            >
              <label :for="field.name">{{ field.label }}</label>
              <input 
                :type="field.type" 
                :id="field.name" 
                :name="field.name"
                :placeholder="field.placeholder"
                v-model="formData[field.name]"
                class="neumorphic-input"
              >
            </div>
          </div>
          
          <div class="panel-footer">
            <button type="button" class="neumorphic-btn cancel-btn" @click="closePanel">取消</button>
            <button type="submit" class="neumorphic-btn submit-btn">提交</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PopupPanel',
  props: {
    fields: {
      type: Array,
      required: true
    },
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: '表单'
    }
  },
  data() {
    return {
      formData: {}
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        // 初始化表单数据
        this.initFormData();
        // 添加滚动锁定
        document.body.style.overflow = 'hidden';
      } else {
        // 移除滚动锁定
        document.body.style.overflow = '';
      }
    },
    fields: {
      handler() {
        this.initFormData();
      },
      immediate: true
    }
  },
  methods: {
    initFormData() {
      const data = {};
      this.fields.forEach(field => {
        data[field.name] = '';
      });
      this.formData = data;
    },
    submitForm() {
      this.$emit('submit', this.formData);
      this.closePanel();
    },
    closePanel() {
      this.$emit('close');
    }
  }
}
</script>

<style scoped>
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease;
}

.popup-panel {
  width: 90%;
  max-width: 600px;
  background-color: #ecf0f3;
  border-radius: 20px;
  overflow: hidden;
  animation: scaleIn 0.3s ease;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.panel-header h2 {
  margin: 0;
  color: #555;
  font-size: 1.5rem;
  font-weight: 600;
}

.close-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-btn:hover {
  transform: rotate(90deg);
  color: #ff5252;
}

.panel-body {
  padding: 20px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
  font-size: 0.9rem;
}

.neumorphic-input {
  padding: 12px 15px;
  border: none;
  background-color: #ecf0f3;
  border-radius: 10px;
  font-size: 1rem;
  color: #333;
  box-shadow: 
    inset 3px 3px 7px #d1d9e6,
    inset -3px -3px 7px #ffffff;
  transition: all 0.3s ease;
  border-bottom: 2px solid transparent;
}

.neumorphic-input:focus {
  outline: none;
  box-shadow: 
    inset 5px 5px 10px #d1d9e6,
    inset -5px -5px 10px #ffffff;
  border-bottom: 2px solid #42b983;
  transform: translateY(-2px);
}

.panel-footer {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.neumorphic-btn {
  padding: 10px 20px;
  border: none;
  background-color: #ecf0f3;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 
    3px 3px 7px #d1d9e6,
    -3px -3px 7px #ffffff;
  position: relative;
  overflow: hidden;
}

.neumorphic-btn:hover {
  transform: translateY(-3px);
}

.neumorphic-btn:active {
  box-shadow: 
    inset 5px 5px 10px #d1d9e6,
    inset -5px -5px 10px #ffffff;
  transform: translateY(0);
  animation: shake 0.3s ease-in-out;
}

.submit-btn {
  background-color: #42b983;
  color: white;
  box-shadow: 
    3px 3px 7px rgba(66, 185, 131, 0.3),
    -3px -3px 7px #ffffff;
}

.submit-btn:hover {
  background-color: #3aa876;
  box-shadow: 
    4px 4px 8px rgba(66, 185, 131, 0.4),
    -4px -4px 8px #ffffff;
}

.submit-btn:active {
  box-shadow: 
    inset 5px 5px 10px rgba(66, 185, 131, 0.5),
    inset -5px -5px 10px rgba(255, 255, 255, 0.5);
}

.cancel-btn {
  color: #555;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleIn {
  from { transform: scale(0.9); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}
</style>