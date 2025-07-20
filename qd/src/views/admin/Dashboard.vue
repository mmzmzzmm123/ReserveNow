<template>
  <div class="dashboard-container">
    <div class="stats-section neomorphic">
      <div class="section-header">
        <h2>System Overview</h2>
        <el-button
          type="primary"
          size="small"
          class="neomorphic-button"
          @click="refreshStats"
        >
          <el-icon><Refresh /></el-icon> Refresh Data
        </el-button>
      </div>

      <el-row :gutter="20" class="stats-row">
        <el-col :xs="24" :sm="12" :md="6" class="stat-col">
          <div class="stat-card neomorphic-inset">
            <div class="stat-icon restaurant-icon neomorphic-float">
              <el-icon :size="24"><KnifeFork /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">Total Restaurants</div>
              <div class="stat-value">
                {{ statisticsData.totalRestaurants }}
              </div>
              <div class="stat-desc">Active Restaurants</div>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6" class="stat-col">
          <div class="stat-card neomorphic-inset">
            <div class="stat-icon reservation-icon neomorphic-float">
              <el-icon :size="24"><Calendar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">Pending Reservations</div>
              <div class="stat-value">
                {{ statisticsData.pendingReservations }}
              </div>
              <div class="stat-desc">Reservations to Confirm</div>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6" class="stat-col">
          <div class="stat-card neomorphic-inset">
            <div class="stat-icon weekly-icon neomorphic-float">
              <el-icon :size="24"><Calendar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">Weekly Reservations</div>
              <div class="stat-value">
                {{ statisticsData.weeklyReservations }}
              </div>
              <div class="stat-desc">Total Reservations in Past 7 Days</div>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6" class="stat-col">
          <div class="stat-card neomorphic-inset">
            <div class="stat-icon total-icon neomorphic-float">
              <el-icon :size="24"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">Total Reservations</div>
              <div class="stat-value">
                {{ statisticsData.totalReservations }}
              </div>
              <div class="stat-desc">Cumulative Reservations</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="charts-section neomorphic">
      <div class="section-header">
        <h2>Data Statistics</h2>
        <el-button
          type="primary"
          size="small"
          class="neomorphic-button"
          @click="refreshStats"
        >
          <el-icon><Refresh /></el-icon> Refresh Charts
        </el-button>
      </div>

      <el-row :gutter="20">
        <el-col :xs="24" :md="12">
          <div class="chart-box neomorphic-inset">
            <div class="chart-title">Reservation Statistics (Last 7 Days)</div>
            <div class="chart-container" ref="reservationChartDom"></div>
          </div>
        </el-col>
        <el-col :xs="24" :md="12">
          <div class="chart-box neomorphic-inset">
            <div class="chart-title">Restaurant Statistics (Last 7 Days)</div>
            <div class="chart-container" ref="restaurantChartDom"></div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from "vue";
import { ElMessage } from "element-plus";
import {
  KnifeFork,
  Calendar,
  User,
  Document,
  Setting,
  DataLine,
  Refresh,
} from "@element-plus/icons-vue";
import request from "@/utils/request";
import * as echarts from "echarts";

// Get user information
const userInfo = ref(JSON.parse(localStorage.getItem("userInfo") || "{}"));
const username = computed(() => userInfo.value.name || "Admin");

// Statistics data
const statisticsData = ref({
  totalRestaurants: 0,
  pendingReservations: 0,
  weeklyReservations: 0,
  totalReservations: 0,
});

// Reservation chart data
const reservationData = ref({
  dailyReservations: [],
  statusCount: {},
});

// Restaurant chart data
const restaurantData = ref({
  dailyRestaurants: [],
  statusCount: {},
});

// Current time and day of week
const currentTime = computed(() => {
  const now = new Date();
  return now.toLocaleDateString("en-US", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
});

const dayOfWeek = computed(() => {
  const now = new Date();
  const days = [
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday",
  ];
  return days[now.getDay()];
});

// Chart instances and DOM references
const reservationChartDom = ref(null);
const restaurantChartDom = ref(null);
let reservationChart = null;
let restaurantChart = null;

// Get statistics data
const fetchStatistics = async () => {
  try {
    const res = await request({
      url: "/admin/statistics/overview",
      method: "get",
    });

    if (res.code === 200) {
      statisticsData.value = res.data;
      ElMessage.success("Data updated");
    }
  } catch (error) {
    console.error("Failed to fetch statistics:", error);
    ElMessage.error("Failed to fetch statistics, please try again later");

    // Use mock data (development environment)
    statisticsData.value = {
      totalRestaurants: 125,
      pendingReservations: 15,
      weeklyReservations: 78,
      totalReservations: 1200,
    };
  }
};

// Get reservation statistics
const fetchReservationData = async () => {
  try {
    const res = await request({
      url: "/admin/statistics/reservations",
      method: "get",
    });

    if (res.code === 200) {
      reservationData.value = res.data;
      renderReservationChart();
    }
  } catch (error) {
    console.error("Failed to fetch reservation statistics:", error);
    renderReservationChart();
  }
};

// Get restaurant statistics
const fetchRestaurantData = async () => {
  try {
    const res = await request({
      url: "/admin/statistics/restaurants",
      method: "get",
    });

    if (res.code === 200) {
      restaurantData.value = res.data;
      renderRestaurantChart();
    }
  } catch (error) {
    console.error("Failed to fetch restaurant statistics:", error);
    renderRestaurantChart();
  }
};

// Render reservation statistics chart
const renderReservationChart = () => {
  if (!reservationChartDom.value) return;

  const data = reservationData.value;
  if (!data || !data.dailyReservations || data.dailyReservations.length === 0)
    return;

  // Process dates and data
  const dates = data.dailyReservations.map((item) => item.date);
  const totalCounts = data.dailyReservations.map((item) => item.count);

  // Create status data series
  const statusSeries = Object.keys(data.statusCount).map((status) => {
    const statusData = data.dailyReservations.map(
      (item) => item.statusCount[status] || 0
    );
    const colors = {
      "Pending": "#f7c34a",
      "Confirmed": "#4af7a1",
      "Completed": "#67c23a",
      "Cancelled": "#f56c6c",
      "Rejected": "#909399",
    };

    return {
      name: status,
      type: "line",
      data: statusData,
      smooth: true,
      symbol: "circle",
      symbolSize: 6,
      lineStyle: {
        width: 2,
        color: colors[status] || "#909399",
      },
      itemStyle: {
        color: colors[status] || "#909399",
      },
    };
  });

  // Initialize chart
  if (reservationChart) {
    reservationChart.dispose();
  }

  if (reservationChartDom.value) {
    reservationChart = echarts.init(
      reservationChartDom.value,
      isDarkMode() ? "dark" : null
    );

    // Configure options
    const option = {
      tooltip: {
        trigger: "axis",
        axisPointer: {
          type: "cross",
          label: {
            backgroundColor: "#6a7985",
          },
        },
      },
      legend: {
        data: ["Total Reservations", ...Object.keys(data.statusCount)],
        bottom: "0%",
      },
      grid: {
        left: "3%",
        right: "4%",
        bottom: "15%",
        top: "3%",
        containLabel: true,
      },
      xAxis: {
        type: "category",
        boundaryGap: false,
        data: dates,
        axisLabel: {
          formatter: (value) => {
            const date = new Date(value);
            return `${date.getMonth() + 1}/${date.getDate()}`;
          },
        },
      },
      yAxis: {
        type: "value",
      },
      series: [
        {
          name: "Total Reservations",
          type: "line",
          smooth: true,
          lineStyle: {
            width: 3,
            color: "#4a6cf7",
          },
          itemStyle: {
            color: "#4a6cf7",
          },
          symbolSize: 8,
          data: totalCounts,
        },
        ...statusSeries,
      ],
    };

    // Set dark mode
    if (isDarkMode()) {
      setDarkModeChart(option);
    }

    reservationChart.setOption(option);
  }
};

// Render restaurant statistics chart
const renderRestaurantChart = () => {
  if (!restaurantChartDom.value) return;

  const data = restaurantData.value;
  if (!data || !data.dailyRestaurants || data.dailyRestaurants.length === 0)
    return;

  // Process dates and data
  const dates = data.dailyRestaurants.map((item) => item.date);
  const totalCounts = data.dailyRestaurants.map((item) => item.count);

  // Create status data series
  const statusSeries = Object.keys(data.statusCount).map((status) => {
    const statusData = data.dailyRestaurants.map(
      (item) => item.statusCount[status] || 0
    );
    const colors = {
      "Pending Review": "#f7c34a",
      "Approved": "#4af7a1",
      "Operating": "#67c23a",
      "Closed": "#f56c6c",
    };

    return {
      name: status,
      type: "line",
      data: statusData,
      smooth: true,
      symbol: "circle",
      symbolSize: 6,
      lineStyle: {
        width: 2,
        color: colors[status] || "#909399",
      },
      itemStyle: {
        color: colors[status] || "#909399",
      },
    };
  });

  // Initialize chart
  if (restaurantChart) {
    restaurantChart.dispose();
  }

  if (restaurantChartDom.value) {
    restaurantChart = echarts.init(
      restaurantChartDom.value,
      isDarkMode() ? "dark" : null
    );

    // Configure options
    const option = {
      tooltip: {
        trigger: "axis",
        axisPointer: {
          type: "cross",
          label: {
            backgroundColor: "#6a7985",
          },
        },
      },
      legend: {
        data: ["Total Restaurants", ...Object.keys(data.statusCount)],
        bottom: "0%",
      },
      grid: {
        left: "3%",
        right: "4%",
        bottom: "15%",
        top: "3%",
        containLabel: true,
      },
      xAxis: {
        type: "category",
        boundaryGap: false,
        data: dates,
        axisLabel: {
          formatter: (value) => {
            const date = new Date(value);
            return `${date.getMonth() + 1}/${date.getDate()}`;
          },
        },
      },
      yAxis: {
        type: "value",
      },
      series: [
        {
          name: "Total Restaurants",
          type: "line",
          smooth: true,
          lineStyle: {
            width: 3,
            color: "#4a6cf7",
          },
          itemStyle: {
            color: "#4a6cf7",
          },
          symbolSize: 8,
          data: totalCounts,
        },
        ...statusSeries,
      ],
    };

    // Set dark mode
    if (isDarkMode()) {
      setDarkModeChart(option);
    }

    restaurantChart.setOption(option);
  }
};

// Check if dark mode is enabled
const isDarkMode = () => {
  return (
    window.matchMedia &&
    window.matchMedia("(prefers-color-scheme: dark)").matches
  );
};

// Set dark mode chart
const setDarkModeChart = (option) => {
  option.backgroundColor = "#2d3748";
  option.textStyle = { color: "#e2e8f0" };

  if (option.legend) {
    option.legend.textStyle = { color: "#e2e8f0" };
  }

  if (option.xAxis) {
    if (Array.isArray(option.xAxis)) {
      option.xAxis.forEach((axis) => {
        axis.axisLine = { lineStyle: { color: "#4a5568" } };
        axis.axisLabel = { ...axis.axisLabel, color: "#a0aec0" };
      });
    } else {
      option.xAxis.axisLine = { lineStyle: { color: "#4a5568" } };
      option.xAxis.axisLabel = { ...option.xAxis.axisLabel, color: "#a0aec0" };
    }
  }

  if (option.yAxis) {
    if (Array.isArray(option.yAxis)) {
      option.yAxis.forEach((axis) => {
        axis.axisLine = { lineStyle: { color: "#4a5568" } };
        axis.axisLabel = { ...axis.axisLabel, color: "#a0aec0" };
        axis.splitLine = { lineStyle: { color: "#4a5568" } };
      });
    } else {
      option.yAxis.axisLine = { lineStyle: { color: "#4a5568" } };
      option.yAxis.axisLabel = { ...option.yAxis.axisLabel, color: "#a0aec0" };
      option.yAxis.splitLine = { lineStyle: { color: "#4a5568" } };
    }
  }
};

// Refresh statistics data
const refreshStats = () => {
  fetchStatistics();
  fetchReservationData();
  fetchRestaurantData();
};

// Handle window resize
const handleResize = () => {
  if (reservationChart && reservationChartDom.value) {
    reservationChart.resize();
  }
  if (restaurantChart && restaurantChartDom.value) {
    restaurantChart.resize();
  }
};

// Setup dark mode listener
const setupDarkModeListener = () => {
  const darkModeMediaQuery = window.matchMedia("(prefers-color-scheme: dark)");

  const handleDarkModeChange = () => {
    if (reservationChart && reservationChartDom.value) {
      const option = reservationChart.getOption();
      reservationChart.dispose();
      reservationChart = echarts.init(
        reservationChartDom.value,
        isDarkMode() ? "dark" : null
      );

      if (isDarkMode()) {
        setDarkModeChart(option);
      }

      reservationChart.setOption(option);
    }

    if (restaurantChart && restaurantChartDom.value) {
      const option = restaurantChart.getOption();
      restaurantChart.dispose();
      restaurantChart = echarts.init(
        restaurantChartDom.value,
        isDarkMode() ? "dark" : null
      );

      if (isDarkMode()) {
        setDarkModeChart(option);
      }

      restaurantChart.setOption(option);
    }
  };

  darkModeMediaQuery.addEventListener("change", handleDarkModeChange);

  return () => {
    darkModeMediaQuery.removeEventListener("change", handleDarkModeChange);
  };
};

// Fetch data on component mount
onMounted(() => {
  fetchStatistics();
  fetchReservationData();
  fetchRestaurantData();

  // Add window resize event listener
  window.addEventListener("resize", handleResize);

  // Add MutationObserver to listen for DOM changes
  const observer = new MutationObserver(() => {
    // When DOM structure changes, such as sidebar collapse, resize charts
    setTimeout(() => {
      handleResize();
    }, 300);
  });

  // Observe document.body's child elements
  observer.observe(document.body, {
    childList: true,
    subtree: true,
    attributes: true,
    attributeFilter: ["class", "style"],
  });

  // Setup dark mode listener
  const removeDarkModeListener = setupDarkModeListener();

  onBeforeUnmount(() => {
    // Dispose chart instances
    if (reservationChart) {
      reservationChart.dispose();
      reservationChart = null;
    }

    if (restaurantChart) {
      restaurantChart.dispose();
      restaurantChart = null;
    }

    // Remove event listeners and observers
    window.removeEventListener("resize", handleResize);
    observer.disconnect();
    removeDarkModeListener();
  });
});
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background-color: #e6e7ee;
  min-height: 100vh;
}

/* Neomorphic style base classes */
.neomorphic {
  border-radius: 18px;
  background: #e6e7ee;
  box-shadow: 8px 8px 16px #c3c4ca, -8px -8px 16px #ffffff;
  padding: 25px;
  margin-bottom: 30px;
  transition: all 0.3s ease;
}

.neomorphic-inset {
  border-radius: 15px;
  background: #e6e7ee;
  box-shadow: inset 5px 5px 10px #c3c4ca, inset -5px -5px 10px #ffffff;
}

.neomorphic-float {
  border-radius: 12px;
  background: linear-gradient(145deg, #f3f4fc, #d1d2d7);
  box-shadow: 4px 4px 8px #c3c4ca, -4px -4px 8px #ffffff;
  transition: all 0.3s ease;
}

.neomorphic-float:hover {
  transform: translateY(-2px);
  box-shadow: 6px 6px 12px #c3c4ca, -6px -6px 12px #ffffff;
}

.neomorphic-button {
  background: #e6e7ee;
  border: none;
  border-radius: 12px;
  color: #44476a;
  padding: 8px 16px;
  box-shadow: 3px 3px 6px #c3c4ca, -3px -3px 6px #ffffff;
  transition: all 0.2s ease;
}

.neomorphic-button:hover {
  box-shadow: 4px 4px 8px #c3c4ca, -4px -4px 8px #ffffff;
  transform: translateY(-2px);
}

.neomorphic-button:active {
  box-shadow: inset 2px 2px 5px #c3c4ca, inset -2px -2px 5px #ffffff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.section-header h2 {
  font-size: 1.5rem;
  margin: 0;
  font-weight: 600;
  color: #44476a;
  text-shadow: 1px 1px 1px rgba(255, 255, 255, 0.5);
}

.stats-section,
.charts-section {
  margin-bottom: 30px;
}

.stats-row {
  margin-bottom: 12px;
}

.stat-col {
  margin-bottom: 25px;
}

.stat-card {
  padding: 20px;
  display: flex;
  align-items: center;
  height: 100%;
  transition: all 0.3s ease;
}

.stat-icon {
  width: 52px;
  height: 52px;
  margin-right: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.restaurant-icon {
  background: linear-gradient(145deg, #4a6cf7, #3f5bcf);
}

.reservation-icon {
  background: linear-gradient(145deg, #f7c34a, #d1a43e);
}

.weekly-icon {
  background: linear-gradient(145deg, #4af7a1, #3fd188);
}

.total-icon {
  background: linear-gradient(145deg, #f74a6c, #d13f5c);
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 0.95rem;
  color: #44476a;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: #31344b;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
  text-shadow: 1px 1px 1px rgba(255, 255, 255, 0.5);
}

.stat-desc {
  font-size: 0.8rem;
  color: #7f83a2;
}

.chart-box {
  padding: 20px;
  margin-bottom: 20px;
  height: 100%;
}

.chart-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #44476a;
  margin-bottom: 20px;
  text-align: center;
  text-shadow: 1px 1px 1px rgba(255, 255, 255, 0.5);
}

.chart-container {
  height: 320px;
  width: 100%;
  padding: 10px;
}

/* Adapt to dark mode */
@media (prefers-color-scheme: dark) {
  .dashboard-container {
    background-color: #2d3748;
  }

  .neomorphic {
    background: #2d3748;
    box-shadow: 8px 8px 16px #202632, -8px -8px 16px #3a485e;
  }

  .neomorphic-inset {
    background: #2d3748;
    box-shadow: inset 5px 5px 10px #202632, inset -5px -5px 10px #3a485e;
  }

  .neomorphic-float {
    background: linear-gradient(145deg, #313d50, #293340);
    box-shadow: 4px 4px 8px #202632, -4px -4px 8px #3a485e;
  }

  .neomorphic-button {
    background: #2d3748;
    color: #cbd5e0;
    box-shadow: 3px 3px 6px #202632, -3px -3px 6px #3a485e;
  }

  .neomorphic-button:active {
    box-shadow: inset 2px 2px 5px #202632, inset -2px -2px 5px #3a485e;
  }

  .section-header h2,
  .chart-title {
    color: #e2e8f0;
    text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.3);
  }

  .stat-title {
    color: #cbd5e0;
  }

  .stat-value {
    color: #f7fafc;
    text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.3);
  }

  .stat-desc {
    color: #a0aec0;
  }
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }

  .neomorphic {
    padding: 20px;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .chart-container {
    height: 280px;
  }
}
</style>
