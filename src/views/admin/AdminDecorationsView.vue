<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { getDecorationConfig, updateDecorationConfig } from "@/api/config";
import { unwrapData } from "@/utils/service";

const loading = ref(false);
const saving = ref(false);

const form = reactive({
  siteName: "个人知识库",
  slogan: "记录构建路径，而不只是结论",
  logoText: "KB",
  bannerText: "Macragge Command Deck",
  primary: "#1e3a8a",
  accent: "#d4af37"
});

const previewStyle = computed(() => ({
  "--preview-primary": form.primary,
  "--preview-accent": form.accent
}));

const loadConfig = async () => {
  loading.value = true;
  try {
    const res = await getDecorationConfig();
    const data = unwrapData(res);
    if (data && typeof data === "object") {
      Object.assign(form, data);
    }
  } catch {
    // 失败时沿用默认配置
  } finally {
    loading.value = false;
  }
};

const saveConfig = async () => {
  saving.value = true;
  try {
    await updateDecorationConfig({ ...form });
    ElMessage.success("装饰配置已保存");
  } catch {
    ElMessage.error("保存失败");
  } finally {
    saving.value = false;
  }
};

onMounted(loadConfig);
</script>

<template>
  <section class="admin-page" v-loading="loading">
    <header class="page-head">
      <h2>站点装饰配置</h2>
      <el-button type="primary" :loading="saving" @click="saveConfig">保存配置</el-button>
    </header>

    <el-form label-position="top" class="form-grid">
      <el-form-item label="站点名称">
        <el-input v-model="form.siteName" />
      </el-form-item>
      <el-form-item label="站点标语">
        <el-input v-model="form.slogan" />
      </el-form-item>
      <el-form-item label="Logo 文本">
        <el-input v-model="form.logoText" />
      </el-form-item>
      <el-form-item label="Banner 文案">
        <el-input v-model="form.bannerText" />
      </el-form-item>
      <el-form-item label="主色">
        <el-color-picker v-model="form.primary" />
      </el-form-item>
      <el-form-item label="强调色">
        <el-color-picker v-model="form.accent" />
      </el-form-item>
    </el-form>

    <article class="preview" :style="previewStyle">
      <h3>{{ form.siteName }}</h3>
      <p>{{ form.slogan }}</p>
      <span>{{ form.bannerText }}</span>
    </article>
  </section>
</template>

<style scoped>
.admin-page {
  display: grid;
  gap: 16px;
  padding: 24px;
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f3f4f6;
}

.page-head h2 {
  margin: 0;
  font-family: "Cinzel", serif;
  font-size: 20px;
  color: #1f2937;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
}

.form-grid :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 500;
}

.preview {
  border: 2px solid #e5e7eb;
  border-radius: 14px;
  padding: 20px;
  background:
    linear-gradient(135deg, var(--preview-primary, #1e3a8a), rgba(8, 18, 33, 0.85)),
    radial-gradient(circle at 80% 20%, var(--preview-accent, #d4af37), transparent 45%);
}

.preview h3 {
  margin: 0;
  color: #fff;
}

.preview p {
  margin: 8px 0;
  color: #d8e7ff;
}

.preview span {
  color: #f4dca1;
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
