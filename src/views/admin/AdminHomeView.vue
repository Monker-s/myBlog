<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { LineChart, BarChart, PieChart } from "echarts/charts";
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
} from "echarts/components";
import VChart from "vue-echarts";
import { getAdminUvStats } from "@/api/admin";
import service from "@/utils/service";
import { unwrapData } from "@/utils/service";

use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  TitleComponent,
]);

const loading = ref(false);
const days = ref(30);

// UV/PV 数据
const uvRows = ref([]);
// 文章数据
const postStats = ref({ total: 0, newToday: 0, newWeek: 0, newMonth: 0 });
// 用户数据
const userStats = ref({ total: 0, newToday: 0, newWeek: 0, newMonth: 0 });

const fetchStats = async () => {
  loading.value = true;
  try {
    // 获取 UV/PV 数据
    const uvRes = await getAdminUvStats({ days: days.value });
    const uvData = unwrapData(uvRes);
    uvRows.value = Array.isArray(uvData) ? uvData : uvData?.list || [];

    // 获取文章统计
    const postRes = await service.get("/api/admin/stats/posts");
    const postData = unwrapData(postRes);
    postStats.value = {
      total: postData?.total || 0,
      newToday: postData?.newToday || 0,
      newWeek: postData?.newWeek || 0,
      newMonth: postData?.newMonth || 0,
    };

    // 获取用户统计
    const userRes = await service.get("/api/admin/stats/users");
    const userData = unwrapData(userRes);
    userStats.value = {
      total: userData?.total || 0,
      newToday: userData?.newToday || 0,
      newWeek: userData?.newWeek || 0,
      newMonth: userData?.newMonth || 0,
    };
  } catch (err) {
    ElMessage.error("获取统计数据失败");
    uvRows.value = [];
    postStats.value = { total: 0, newToday: 0, newWeek: 0, newMonth: 0 };
    userStats.value = { total: 0, newToday: 0, newWeek: 0, newMonth: 0 };
  } finally {
    loading.value = false;
  }
};

const quickSetDays = async (n) => {
  days.value = n;
  await fetchStats();
};

// UV 趋势图配置
const uvChartOption = computed(() => {
  const dates = uvRows.value.map((item) => item.stat_date);
  const uvData = uvRows.value.map((item) => item.uv_count);
  const pvData = uvRows.value.map((item) => item.pv_count);

  return {
    title: {
      text: "UV/PV 趋势",
      left: "center",
      textStyle: { fontSize: 16, fontWeight: "bold", color: "#1f2937" },
    },
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "cross" },
    },
    legend: {
      data: ["UV", "PV"],
      bottom: 0,
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "15%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      boundaryGap: false,
      data: dates,
      axisLabel: { rotate: 45 },
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        name: "UV",
        type: "line",
        smooth: true,
        data: uvData,
        itemStyle: { color: "#3b82f6" },
        areaStyle: {
          color: {
            type: "linear",
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: "rgba(59, 130, 246, 0.3)" },
              { offset: 1, color: "rgba(59, 130, 246, 0.05)" },
            ],
          },
        },
      },
      {
        name: "PV",
        type: "line",
        smooth: true,
        data: pvData,
        itemStyle: { color: "#10b981" },
        areaStyle: {
          color: {
            type: "linear",
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: "rgba(16, 185, 129, 0.3)" },
              { offset: 1, color: "rgba(16, 185, 129, 0.05)" },
            ],
          },
        },
      },
    ],
  };
});

// 文章统计图配置
const postChartOption = computed(() => {
  return {
    title: {
      text: "文章数据",
      left: "center",
      textStyle: { fontSize: 16, fontWeight: "bold", color: "#1f2937" },
    },
    tooltip: {
      trigger: "item",
      formatter: "{b}: {c} ({d}%)",
    },
    legend: {
      orient: "vertical",
      left: "left",
      top: "middle",
    },
    series: [
      {
        name: "文章",
        type: "pie",
        radius: ["40%", "70%"],
        center: ["60%", "50%"],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: "#fff",
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: "{b}\n{c}",
        },
        data: [
          { value: postStats.value.newToday, name: "今日新增", itemStyle: { color: "#3b82f6" } },
          { value: postStats.value.newWeek, name: "本周新增", itemStyle: { color: "#10b981" } },
          { value: postStats.value.newMonth, name: "本月新增", itemStyle: { color: "#f59e0b" } },
          { value: postStats.value.total - postStats.value.newMonth, name: "历史文章", itemStyle: { color: "#6b7280" } },
        ],
      },
    ],
  };
});

// 用户统计图配置
const userChartOption = computed(() => {
  return {
    title: {
      text: "用户数据",
      left: "center",
      textStyle: { fontSize: 16, fontWeight: "bold", color: "#1f2937" },
    },
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" },
    },
    grid: {
      left: "3%",
      right: "4%",
      bottom: "3%",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      data: ["今日新增", "本周新增", "本月新增", "总用户数"],
      axisTick: { alignWithLabel: true },
    },
    yAxis: {
      type: "value",
    },
    series: [
      {
        name: "用户数",
        type: "bar",
        barWidth: "60%",
        data: [
          { value: userStats.value.newToday, itemStyle: { color: "#3b82f6" } },
          { value: userStats.value.newWeek, itemStyle: { color: "#10b981" } },
          { value: userStats.value.newMonth, itemStyle: { color: "#f59e0b" } },
          { value: userStats.value.total, itemStyle: { color: "#8b5cf6" } },
        ],
        label: {
          show: true,
          position: "top",
        },
      },
    ],
  };
});

onMounted(fetchStats);
</script>

<template>
  <section class="admin-home">
    <div class="welcome-panel">
      <h1>欢迎来到管理后台</h1>
      <p>快速访问各个管理模块，高效管理您的站点</p>
    </div>

    <!-- 统计概览卡片 -->
    <div class="overview-cards">
      <div class="overview-card uv-card">
        <div class="card-header">
          <h3>UV 访问</h3>
          <span class="badge blue">实时</span>
        </div>
        <div class="card-body">
          <div class="stat-item">
            <span class="stat-value">{{ uvRows.reduce((s, i) => s + Number(i.uv_count || 0), 0) }}</span>
            <span class="stat-label">总访问量</span>
          </div>
          <div class="stat-item">
            <span class="stat-value highlight">{{ uvRows[uvRows.length - 1]?.uv_count || 0 }}</span>
            <span class="stat-label">今日 UV</span>
          </div>
        </div>
      </div>

      <div class="overview-card post-card">
        <div class="card-header">
          <h3>文章数据</h3>
          <span class="badge green">内容</span>
        </div>
        <div class="card-body">
          <div class="stat-item">
            <span class="stat-value">{{ postStats.total }}</span>
            <span class="stat-label">文章总数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value highlight">+{{ postStats.newToday }}</span>
            <span class="stat-label">今日新增</span>
          </div>
        </div>
      </div>

      <div class="overview-card user-card">
        <div class="card-header">
          <h3>用户数据</h3>
          <span class="badge purple">用户</span>
        </div>
        <div class="card-body">
          <div class="stat-item">
            <span class="stat-value">{{ userStats.total }}</span>
            <span class="stat-label">用户总数</span>
          </div>
          <div class="stat-item">
            <span class="stat-value highlight">+{{ userStats.newToday }}</span>
            <span class="stat-label">今日新增</span>
          </div>
        </div>
      </div>
    </div>

    <!-- UV/PV 看板 -->
    <div class="stats-section" v-loading="loading">
      <header class="stats-header">
        <h2>UV / PV 看板</h2>
        <div class="actions">
          <el-button :type="days === 7 ? 'primary' : 'default'" @click="quickSetDays(7)">近7天</el-button>
          <el-button :type="days === 30 ? 'primary' : 'default'" @click="quickSetDays(30)">近30天</el-button>
          <el-button :type="days === 90 ? 'primary' : 'default'" @click="quickSetDays(90)">近90天</el-button>
        </div>
      </header>

      <!-- 三张图表 -->
      <div class="charts-grid">
        <div class="chart-card">
          <v-chart class="chart" :option="uvChartOption" autoresize />
        </div>
        <div class="chart-card">
          <v-chart class="chart" :option="postChartOption" autoresize />
        </div>
        <div class="chart-card">
          <v-chart class="chart" :option="userChartOption" autoresize />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-home {
  display: grid;
  gap: 24px;
}

.welcome-panel {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px;
  border-radius: 16px;
  text-align: center;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.welcome-panel h1 {
  margin: 0 0 8px;
  font-family: "Cinzel", serif;
  font-size: 32px;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.welcome-panel p {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
}

/* 概览卡片 */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.overview-card {
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  font-family: "Cinzel", serif;
  font-size: 16px;
  color: #1f2937;
}

.badge {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.badge.blue {
  background: #dbeafe;
  color: #3b82f6;
}

.badge.green {
  background: #d1fae5;
  color: #10b981;
}

.badge.purple {
  background: #ede9fe;
  color: #8b5cf6;
}

.card-body {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 900;
  color: #1f2937;
  font-family: "Cinzel", serif;
}

.stat-value.highlight {
  color: #3b82f6;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
}

/* UV/PV 看板 */
.stats-section {
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.stats-header h2 {
  margin: 0;
  font-family: "Cinzel", serif;
  font-size: 20px;
  color: #1f2937;
}

.actions {
  display: flex;
  gap: 8px;
}

/* 图表网格 - 每个图表一行 */
.charts-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

.chart-card {
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
}

.chart {
  width: 100%;
  height: 400px;
}

@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }
}
</style>
