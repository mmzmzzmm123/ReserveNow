import axios from "axios";
import { ElMessage } from "element-plus";

const service = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 5000,
});

// Request interceptor
service.interceptors.request.use(
  (config) => {
    // Get token from cookie
    const token = document.cookie.replace(/(?:(?:^|.*;\s*)token\s*=\s*([^;]*).*$)|^.*$/, "$1");
    
    console.log(token);
    // Add token to request header if exists
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      ElMessage.error(res.message || "Error");
      return Promise.reject(new Error(res.message || "Error"));
    }
    return res;
  },
  (error) => {
    ElMessage.error(error.message || "Request failed");
    return Promise.reject(error);
  }
);

export default service;
