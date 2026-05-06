<script setup>
import { computed, onMounted, ref } from "vue";
import { getAdminUvStats } from "@/api/admin";
import { unwrapData } from "@/utils/service";

const loading = ref(false);
const days = ref(30);
const rows = ref([]);

const fetchStats = async () => {
  loading.value = true;
  try {
    const res = await getAdminUvStats({ days: days.value });
    const data = unwrapData(res);
    rows.value = Array.isArray(data) ? data : data?.list || [];
  } catch (err) {
    ElMessage.error("获取统计数据失败");
    rows.value = [];
  } finally {
    loading.value = false;
  }
};

const uvTotal = computed(() =>
  rows.value.reduce((sum, item) => sum + Number(item.uv_count || 0), 0),
);
const pvTotal = computed(() =>
  rows.value.reduce((sum, item) => sum + Number(item.pv_count || 0), 0),
);
const uvAvg = computed(() =>
  rows.value.length ? Math.round(uvTotal.value / rows.value.length) : 0,
);
const maxUv = computed(() =>
  Math.max(1, ...rows.value.map((item) => Number(item.uv_count || 0))),
);

const quickSetDays = async (n) => {
  days.value = n;
  await fetchStats();
};

onMounted(fetchStats);
</script>

<template>
  <section class="admin-page" v-loading="loading">
    <header class="page-head">
      <h2>UV / PV 看板</h2>
      <div class="actions">
        <el-button
          :type="days === 7 ? 'primary' : 'default'"
          @click="quickSetDays(7)"
          >近7天</el-button
        >
        <el-button
          :type="days === 30 ? 'primary' : 'default'"
          @click="quickSetDays(30)"
          >近30天</el-button
        >
        <el-button
          :type="days === 90 ? 'primary' : 'default'"
          @click="quickSetDays(90)"
          >近90天</el-button
        >
      </div>
    </header>

    <div class="summary-grid">
      <article>
        <small>UV 总量</small>
        <strong>{{ uvTotal }}</strong>
      </article>
      <article>
        <small>PV 总量</small>
        <strong>{{ pvTotal }}</strong>
      </article>
      <article>
        <small>日均 UV</small>
        <strong>{{ uvAvg }}</strong>
      </article>
      <article>
        <small>数据天数</small>
        <strong>{{ rows.length }}</strong>
      </article>
    </div>

    <section class="trend-list">
      <article v-for="item in rows" :key="item.stat_date" class="trend-item">
        <time>{{ item.stat_date }}</time>
        <div class="bar-track">
          <span
            class="bar"
            :style="{ width: `${(Number(item.uv_count || 0) / maxUv) * 100}%` }"
          ></span>
        </div>
        <strong>UV {{ item.uv_count }}</strong>
        <span>PV {{ item.pv_count }}</span>
      </article>
    </section>
  </section>
</template>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.page-head h2 {
  margin: 0;
  font-family: "Cinzel", serif;
  font-size: 20px;
  color: #1f2937;
}

.actions {
  display: flex;
  gap: 8px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-grid article {
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #f9fafb, #f3f4f6);
  text-align: center;
}

.summary-grid small {
  display: block;
  color: #6b7280;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 4px;
}

.summary-grid strong {
  display: block;
  font-size: 28px;
  font-weight: 900;
  color: #3b82f6;
  font-family: "Cinzel", serif;
}

.trend-list {
  display: grid;
  gap: 10px;
}

.trend-item {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr) 100px 100px;
  align-items: center;
  gap: 12px;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px 16px;
  background: #f9fafb;
  transition: all 0.2s ease;
}

.trend-item:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.trend-item time,
.trend-item span {
  color: #6b7280;
  font-weight: 500;
}

.trend-item strong {
  color: #1f2937;
  font-weight: 700;
}

.bar-track {
  height: 12px;
  border-radius: 999px;
  background: #e5e7eb;
  border: 1px solid #d1d5db;
  overflow: hidden;
}

.bar {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
}

@media (max-width: 980px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .trend-item {
    grid-template-columns: 1fr;
  }
}
</style>
