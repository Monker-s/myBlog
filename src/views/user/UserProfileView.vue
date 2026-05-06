<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElLoading } from "element-plus";
import { useAuthStore } from "@/stores/auth";
import { updateUserInfo } from "@/api/auth";
import { uploadFile } from "@/api/admin";
import { setPageMeta } from "@/utils/seo";

const router = useRouter();
const authStore = useAuthStore();

setPageMeta({
  title: "个人信息 | 个人知识库",
  description: "修改个人用户信息和头像。",
});

const form = ref({
  username: "",
  email: "",
  password: "",
});

const tempAvatarUrl = ref("");
const avatarFile = ref(null);
const isLoading = ref(false);

const user = computed(() => authStore.user);
const avatarUrl = computed(() => tempAvatarUrl.value || user.value?.icon || "");

onMounted(() => {
  if (user.value) {
    form.value.username = user.value.username || "";
    form.value.email = user.value.email || "";
  }
});

const handleUpdate = async () => {
  if (!form.value.email) {
    ElMessage.error("邮箱不能为空");
    return;
  }

  isLoading.value = true;
  try {
    const loading = ElLoading.service({
      lock: true,
      text: "正在更新信息...",
      background: "rgba(0, 0, 0, 0.7)",
    });

    let avatarUrl = user.value?.icon || "";

    // 上传头像（如果有选择新头像）
    if (avatarFile.value) {
      const formData = new FormData();
      formData.append("file", avatarFile.value);
      const uploadResponse = await uploadFile(formData);
      avatarUrl = uploadResponse.data.url;
    }

    const data = {
      username: form.value.username,
      email: form.value.email,
      icon: avatarUrl,
    };

    if (form.value.password) {
      data.password = form.value.password;
    }

    await updateUserInfo(data);
    await authStore.restoreSession();

    // 清空临时头像和文件
    tempAvatarUrl.value = "";
    avatarFile.value = null;

    ElMessage.success("信息更新成功，将自动退出重新登录");
    loading.close();

    // 延迟一秒后退出登录
    setTimeout(async () => {
      await authStore.logoutCurrentUser();
      await router.replace({ name: "auth" });
    }, 1000);
  } catch (error) {
    ElMessage.error("更新失败，请稍后重试");
  } finally {
    isLoading.value = false;
  }
};

const handleAvatarChange = (file) => {
  // 本地预览头像
  const reader = new FileReader();
  reader.onload = (e) => {
    tempAvatarUrl.value = e.target.result;
    avatarFile.value = file.raw;
  };
  reader.readAsDataURL(file.raw);
};
</script>

<template>
  <section class="user-profile page-panel">
    <div class="header-actions">
      <button class="back-button" @click="router.back()">
        <span class="back-icon">←</span>
        <span class="back-text">返回</span>
      </button>
    </div>
    <h1>个人信息管理</h1>

    <div class="profile-content">
      <div class="avatar-section">
        <h2>战团徽章</h2>
        <div class="avatar-container">
          <el-upload
            class="avatar-uploader"
            action=""
            :show-file-list="false"
            :on-change="handleAvatarChange"
            :before-upload="() => false"
          >
            <img
              v-if="user?.icon"
              :src="user.icon"
              class="avatar"
              alt="Avatar"
            />
            <div v-else class="avatar-placeholder">
              <span class="avatar-initial">{{
                user?.username?.charAt(0).toUpperCase() || "?"
              }}</span>
            </div>
            <div class="avatar-upload-text">更换徽章</div>
          </el-upload>

          <!-- 上传预览 -->
          <div v-if="tempAvatarUrl" class="avatar-preview">
            <h3>预览效果</h3>
            <div class="avatar-frame preview-frame">
              <div class="avatar-inner">
                <img :src="tempAvatarUrl" class="avatar-image" alt="Preview" />
              </div>
              <span class="corner-tl" aria-hidden="true"></span>
              <span class="corner-tr" aria-hidden="true"></span>
              <span class="corner-bl" aria-hidden="true"></span>
              <span class="corner-br" aria-hidden="true"></span>
            </div>
            <p class="preview-note">点击保存后将更新为新徽章</p>
          </div>
        </div>
      </div>

      <div class="form-section">
        <h2>个人信息</h2>
        <el-form :model="form" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="邮箱" required>
            <el-input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱"
            />
          </el-form-item>
          <el-form-item label="密码">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="留空则不修改密码"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="handleUpdate"
              :loading="isLoading"
            >
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </section>
</template>

<style scoped>
.user-profile {
  padding: 24px;
}

.user-profile h1 {
  margin: 0 0 24px;
  font-family: "Cinzel", serif;
  font-size: 24px;
  color: #e8dcc8;
  letter-spacing: 0.08em;
  text-align: center;
  text-shadow:
    0 2px 4px rgba(0, 0, 0, 0.8),
    0 0 16px rgba(196, 162, 101, 0.2);
  position: relative;
}

.user-profile h1::after {
  content: "◆";
  position: absolute;
  bottom: -12px;
  left: 50%;
  transform: translateX(-50%);
  color: #b8945f;
  font-size: 12px;
  text-shadow: 0 0 8px rgba(184, 148, 95, 0.5);
}

.header-actions {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 2px solid #7a6650;
  border-radius: 3px;
  background: linear-gradient(
    180deg,
    rgba(60, 45, 30, 0.6),
    rgba(35, 25, 18, 0.8)
  );
  box-shadow:
    0 2px 6px rgba(0, 0, 0, 0.5),
    inset 0 1px 0 rgba(184, 148, 95, 0.15);
  color: #d4c4a8;
  font-family: "Rajdhani", sans-serif;
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
}

.back-button::before {
  content: "";
  position: absolute;
  inset: 2px;
  border: 1px solid rgba(139, 115, 85, 0.2);
  border-radius: 2px;
  pointer-events: none;
}

.back-button:hover {
  transform: translateY(-2px);
  border-color: #b8945f;
  color: #f0e6d2;
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.6),
    0 0 20px rgba(184, 148, 95, 0.2),
    inset 0 1px 0 rgba(184, 148, 95, 0.3);
}

.back-button:active {
  transform: translateY(0);
}

.back-icon {
  font-size: 16px;
  line-height: 1;
}

.back-text {
  font-size: 12px;
}

.profile-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 32px;
  margin-top: 32px;
}

.avatar-section h2,
.form-section h2 {
  margin: 0 0 16px;
  font-family: "Cinzel", serif;
  font-size: 16px;
  color: #c4a265;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  border-bottom: 2px solid rgba(184, 148, 95, 0.4);
  padding-bottom: 8px;
  display: inline-block;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
}

.avatar {
  width: 160px;
  height: 160px;
  border-radius: 8px;
  object-fit: cover;
  border: 3px solid #b8945f;
  box-shadow:
    0 0 0 1px #2a1f14,
    0 0 0 5px rgba(42, 31, 20, 0.8),
    0 0 0 6px #1a120b,
    0 4px 20px rgba(0, 0, 0, 0.6),
    inset 0 0 20px rgba(184, 148, 95, 0.1);
  transition: all 0.3s ease;
}

.avatar:hover {
  transform: scale(1.05);
  box-shadow:
    0 0 0 1px #2a1f14,
    0 0 0 5px rgba(42, 31, 20, 0.8),
    0 0 0 6px #1a120b,
    0 8px 32px rgba(0, 0, 0, 0.8),
    0 0 20px rgba(184, 148, 95, 0.3),
    inset 0 0 20px rgba(184, 148, 95, 0.2);
}

.avatar-placeholder {
  width: 160px;
  height: 160px;
  border: 3px solid #b8945f;
  background:
    linear-gradient(160deg, rgba(15, 10, 8, 0.95), rgba(30, 22, 15, 0.9)),
    repeating-linear-gradient(
      -45deg,
      rgba(60, 45, 30, 0.06) 0px,
      rgba(60, 45, 30, 0.06) 2px,
      transparent 2px,
      transparent 4px
    );
  box-shadow:
    0 0 0 1px #2a1f14,
    0 0 0 5px rgba(42, 31, 20, 0.8),
    0 0 0 6px #1a120b,
    0 4px 20px rgba(0, 0, 0, 0.6),
    inset 0 0 20px rgba(184, 148, 95, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.avatar-placeholder:hover {
  transform: scale(1.05);
  box-shadow:
    0 0 0 1px #2a1f14,
    0 0 0 5px rgba(42, 31, 20, 0.8),
    0 0 0 6px #1a120b,
    0 8px 32px rgba(0, 0, 0, 0.8),
    0 0 20px rgba(184, 148, 95, 0.3),
    inset 0 0 20px rgba(184, 148, 95, 0.2);
}

.avatar-initial {
  font-family: "Cinzel", serif;
  font-size: 64px;
  font-weight: 900;
  color: #d4a63b;
  text-shadow:
    0 0 12px rgba(212, 166, 59, 0.4),
    0 2px 4px rgba(0, 0, 0, 0.8);
  letter-spacing: 0.05em;
  line-height: 1;
}

.avatar-upload-text {
  margin-top: 8px;
  font-size: 13px;
  color: #c4a265;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  text-align: center;
  transition: color 0.3s ease;
}

.avatar-uploader:hover .avatar-upload-text {
  color: #d4a63b;
  text-shadow: 0 0 8px rgba(212, 166, 59, 0.5);
}

/* 预览效果样式 */
.avatar-preview {
  margin-top: 24px;
  text-align: center;
}

.avatar-preview h3 {
  margin: 0 0 12px;
  font-family: "Cinzel", serif;
  font-size: 14px;
  color: #c4a265;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.preview-frame {
  margin: 0 auto;
  width: 100px;
  height: 100px;
  border: 3px solid #b8945f;
  background: linear-gradient(
    145deg,
    rgba(139, 115, 85, 0.15),
    rgba(60, 45, 30, 0.3)
  );
  box-shadow:
    0 0 0 1px #2a1f14,
    0 0 0 5px rgba(42, 31, 20, 0.8),
    0 0 0 6px #1a120b,
    0 4px 20px rgba(0, 0, 0, 0.6),
    inset 0 0 20px rgba(184, 148, 95, 0.1);
  position: relative;
  margin-bottom: 8px;
}

.preview-frame::before {
  content: "";
  position: absolute;
  inset: 4px;
  border: 1px solid rgba(184, 148, 95, 0.3);
  pointer-events: none;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 2px;
}

.preview-note {
  margin: 8px 0 0;
  font-size: 11px;
  color: #8b7d6b;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

/* 预览边框装饰 */
.preview-frame .corner-tl,
.preview-frame .corner-tr,
.preview-frame .corner-bl,
.preview-frame .corner-br {
  position: absolute;
  width: 10px;
  height: 10px;
  pointer-events: none;
}

.preview-frame .corner-tl::before,
.preview-frame .corner-tr::before,
.preview-frame .corner-bl::before,
.preview-frame .corner-br::before,
.preview-frame .corner-tl::after,
.preview-frame .corner-tr::after,
.preview-frame .corner-bl::after,
.preview-frame .corner-br::after {
  content: "";
  position: absolute;
  background: radial-gradient(circle, #d4a63b, #8b7355);
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(212, 166, 59, 0.5);
}

.preview-frame .corner-tl::before,
.preview-frame .corner-tr::before,
.preview-frame .corner-bl::before,
.preview-frame .corner-br::before {
  width: 6px;
  height: 6px;
}

.preview-frame .corner-tl::after,
.preview-frame .corner-tr::after,
.preview-frame .corner-bl::after,
.preview-frame .corner-br::after {
  width: 3px;
  height: 3px;
}

.preview-frame .corner-tl {
  top: -4px;
  left: -4px;
}
.preview-frame .corner-tr {
  top: -4px;
  right: -4px;
}
.preview-frame .corner-bl {
  bottom: -4px;
  left: -4px;
}
.preview-frame .corner-br {
  bottom: -4px;
  right: -4px;
}

.preview-frame .corner-tl::before {
  top: 0;
  left: 0;
}
.preview-frame .corner-tl::after {
  top: -3px;
  left: -3px;
}
.preview-frame .corner-tr::before {
  top: 0;
  right: 0;
}
.preview-frame .corner-tr::after {
  top: -3px;
  right: -3px;
}
.preview-frame .corner-bl::before {
  bottom: 0;
  left: 0;
}
.preview-frame .corner-bl::after {
  bottom: -3px;
  left: -3px;
}
.preview-frame .corner-br::before {
  bottom: 0;
  right: 0;
}
.preview-frame .corner-br::after {
  bottom: -3px;
  right: -3px;
}

.form-section {
  background: rgba(10, 19, 34, 0.6);
  border: 1px solid rgba(121, 158, 214, 0.34);
  border-radius: 12px;
  padding: 20px;
  box-shadow:
    0 10px 25px rgba(2, 8, 19, 0.55),
    inset 0 0 0 1px rgba(255, 255, 255, 0.05),
    inset 0 0 24px rgba(31, 210, 255, 0.07);
}

.el-form {
  max-width: 500px;
}

.el-form-item__label {
  color: #d8e5ff !important;
  font-weight: 600;
  letter-spacing: 0.05em;
}

.el-input__wrapper {
  background: rgba(7, 17, 31, 0.64) !important;
  border-color: rgba(136, 170, 225, 0.35) !important;
}

.el-input__wrapper:hover {
  border-color: rgba(184, 148, 95, 0.5) !important;
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px rgba(184, 148, 95, 0.5) inset !important;
}

.el-input__inner {
  color: #e9effa !important;
  font-family: "Rajdhani", sans-serif;
}

.el-button--primary {
  background: linear-gradient(
    145deg,
    rgba(139, 115, 85, 0.35),
    rgba(75, 55, 35, 0.6)
  ) !important;
  border: 1px solid #b8945f !important;
  color: #f5e6c8 !important;
  font-family: "Rajdhani", sans-serif;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.6),
    0 0 16px rgba(184, 148, 95, 0.15),
    inset 0 1px 0 rgba(212, 180, 120, 0.25);
  transition: all 0.3s ease;
}

.el-button--primary:hover {
  border-color: #d4a63b !important;
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.7),
    0 0 28px rgba(212, 166, 59, 0.3),
    inset 0 1px 0 rgba(212, 166, 59, 0.35);
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .avatar-container {
    margin-bottom: 20px;
  }

  .form-section {
    order: -1;
  }
}
</style>
