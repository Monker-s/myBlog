<script setup>
import { computed, reactive, ref, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { resetPassword, sendCode } from "@/api/auth";
import { useAuthStore } from "@/stores/auth";
import { ROUTE_NAMES } from "@/constants/routes";
import { setPageMeta } from "@/utils/seo";

setPageMeta({
  title: "登录 / 注册 | 个人知识库",
  description: "通过账号登录后可查看全文、点赞、评论和通知。",
});

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activeTab = ref("login");

const loginForm = reactive({
  username: "",
  password: "",
});

const registerForm = reactive({
  username: "",
  email: "",
  password: "",
});

const loginSubmitting = ref(false);
const registerSubmitting = ref(false);
const forgotPasswordDialogVisible = ref(false);
const forgotPasswordSubmitting = ref(false);

// 验证码倒计时
const registerCountdown = ref(0);
const forgotCountdown = ref(0);
let registerTimer = null;
let forgotTimer = null;

const forgotPasswordForm = reactive({
  email: "",
  code: "",
  password: "",
  agPassword: "",
});

/**
 * 登录窗交互状态：
 * - mx/my: 鼠标在卡片上的相对位置，用于流光跟随
 * - tiltX/tiltY: 3D 倾斜角度
 */
const motion = reactive({
  mx: 50,
  my: 50,
  tiltX: 0,
  tiltY: 0,
  active: false,
});

/**
 * 开始倒计时
 */
const startCountdown = (targetRef, timerRef) => {
  targetRef.value = 60;
  timerRef = setInterval(() => {
    targetRef.value--;
    if (targetRef.value <= 0) {
      clearInterval(timerRef);
      timerRef = null;
    }
  }, 1000);
  return timerRef;
};

/**
 * 发送注册验证码
 */
const sendRegisterCode = async () => {
  const email = registerForm.email.trim();
  if (!email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.warning("邮箱格式不正确");
    return;
  }
  if (registerCountdown.value > 0) return;

  try {
    await sendCode({ scene: "register" });
    ElMessage.success("验证码已发送");
    registerTimer = startCountdown(registerCountdown, registerTimer);
  } catch {
    ElMessage.error("发送失败，请稍后重试");
  }
};

/**
 * 发送忘记密码验证码
 */
const sendForgotCode = async () => {
  const email = forgotPasswordForm.email.trim();
  if (!email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.warning("邮箱格式不正确");
    return;
  }
  if (forgotCountdown.value > 0) return;

  try {
    await sendCode({ scene: "forgot" });
    ElMessage.success("验证码已发送");
    forgotTimer = startCountdown(forgotCountdown, forgotTimer);
  } catch {
    ElMessage.error("发送失败，请稍后重试");
  }
};

// 组件卸载时清除定时器
onUnmounted(() => {
  if (registerTimer) clearInterval(registerTimer);
  if (forgotTimer) clearInterval(forgotTimer);
});

const windowStyle = computed(() => ({
  "--mx": `${motion.mx}%`,
  "--my": `${motion.my}%`,
  transform: motion.active
    ? `perspective(1200px) rotateX(${motion.tiltX}deg) rotateY(${motion.tiltY}deg) scale(1.012)`
    : "perspective(1200px) rotateX(0deg) rotateY(0deg) scale(1)",
}));

const handleWindowMove = (event) => {
  const rect = event.currentTarget.getBoundingClientRect();
  const x = ((event.clientX - rect.left) / rect.width) * 100;
  const y = ((event.clientY - rect.top) / rect.height) * 100;

  motion.mx = Math.max(0, Math.min(100, x));
  motion.my = Math.max(0, Math.min(100, y));
  motion.tiltY = (motion.mx - 50) / 12;
  motion.tiltX = -(motion.my - 50) / 14;
  motion.active = true;
};

const resetWindowMotion = () => {
  motion.active = false;
  motion.mx = 50;
  motion.my = 50;
  motion.tiltX = 0;
  motion.tiltY = 0;
};

const doLogin = async () => {
  loginSubmitting.value = true;
  try {
    await authStore.loginWithPassword(loginForm);
    ElMessage.success("登录成功");

    const redirect =
      typeof route.query.redirect === "string" ? route.query.redirect : null;
    if (redirect) {
      await router.replace(redirect);
    } else {
      await router.replace({ name: ROUTE_NAMES.HOME });
    }
  } catch {
    ElMessage.error("登录失败，请检查账号密码");
  } finally {
    loginSubmitting.value = false;
  }
};

const doRegister = async () => {
  registerSubmitting.value = true;
  try {
    await authStore.registerUser(registerForm);
    ElMessage.success("注册成功，请登录");
    activeTab.value = "login";
  } catch {
    ElMessage.error("注册失败，请稍后重试");
  } finally {
    registerSubmitting.value = false;
  }
};

const openForgotPassword = () => {
  forgotPasswordForm.email = "";
  forgotPasswordForm.code = "";
  forgotPasswordForm.password = "";
  forgotPasswordForm.agPassword = "";
  forgotPasswordDialogVisible.value = true;
};

const doForgotPassword = async () => {
  const email = forgotPasswordForm.email.trim();
  const code = forgotPasswordForm.code.trim();
  const password = forgotPasswordForm.password;
  const agPassword = forgotPasswordForm.agPassword;

  if (!email || !code || !password || !agPassword) {
    ElMessage.warning("请完整填写邮箱、验证码和新密码");
    return;
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.warning("邮箱格式不正确");
    return;
  }

  if (password.length < 6) {
    ElMessage.warning("新密码至少 6 位");
    return;
  }

  if (password !== agPassword) {
    ElMessage.warning("两次输入的新密码不一致");
    return;
  }

  forgotPasswordSubmitting.value = true;
  try {
    await resetPassword({
      email,
      code,
      password,
      agPassword,
    });
    ElMessage.success("密码重置成功，请使用新密码登录");
    forgotPasswordDialogVisible.value = false;
    activeTab.value = "login";
    loginForm.password = "";
  } catch {
    ElMessage.error("重置失败，请检查邮箱、验证码或接口地址");
  } finally {
    forgotPasswordSubmitting.value = false;
  }
};
</script>

<template>
  <main class="auth-stage">
    <!-- 使用基利曼上半身图作为背景主视觉 -->
    <div class="auth-backdrop" aria-hidden="true"></div>

    <!-- 中央登录窗：高对比玻璃金属风 -->
    <section
      class="auth-window"
      role="main"
      aria-label="登录注册窗口"
      :style="windowStyle"
      @mousemove="handleWindowMove"
      @mouseleave="resetWindowMotion"
    >
      <header class="auth-head">
        <p class="signal">ULTRAMAR COMMAND ACCESS</p>
        <h1>指挥终端认证</h1>
        <p class="desc">验证身份后解锁全文、通知、评论与后台操作权限。</p>
      </header>

      <el-tabs v-model="activeTab" class="auth-tabs" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form label-position="top" @submit.prevent="doLogin">
            <el-form-item label="用户名">
              <el-input
                v-model="loginForm.username"
                autocomplete="username"
                placeholder="请输入用户名"
              />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                autocomplete="current-password"
                placeholder="请输入密码"
              />
            </el-form-item>
            <el-button
              type="warning"
              class="submit-btn"
              :loading="loginSubmitting"
              @click="doLogin"
            >
              进入指挥终端
            </el-button>
            <div class="forgot-link-wrapper">
              <el-button link class="forgot-link" @click="openForgotPassword">
                忘记密码？
              </el-button>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form label-position="top" @submit.prevent="doRegister">
            <el-form-item label="用户名">
              <el-input
                v-model="registerForm.username"
                autocomplete="username"
                placeholder="设置用户名"
              />
            </el-form-item>
            <el-form-item label="邮箱">
              <div class="input-with-btn">
                <el-input
                  v-model="registerForm.email"
                  autocomplete="email"
                  placeholder="输入邮箱"
                />
                <el-button
                  class="send-code-btn"
                  :disabled="registerCountdown > 0"
                  @click="sendRegisterCode"
                >
                  {{
                    registerCountdown > 0
                      ? `${registerCountdown}s`
                      : "发送验证码"
                  }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="registerForm.password"
                type="password"
                show-password
                autocomplete="new-password"
                placeholder="设置密码"
              />
            </el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="registerSubmitting"
              @click="doRegister"
            >
              创建账号
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <!-- 忘记密码对话框 -->
      <el-dialog
        v-model="forgotPasswordDialogVisible"
        title="重置密码"
        width="420px"
        class="forgot-password-dialog"
        modal-class="forgot-password-overlay"
        append-to-body
        :close-on-click-modal="false"
        destroy-on-close
      >
        <div class="forgot-password-content">
          <p class="forgot-desc">
            请输入注册邮箱、验证码与新密码，完成账号密码重置。
          </p>
          <el-form label-position="top" @submit.prevent="doForgotPassword">
            <el-form-item label="邮箱">
              <div class="input-with-btn">
                <el-input
                  v-model="forgotPasswordForm.email"
                  autocomplete="email"
                  placeholder="请输入注册邮箱"
                  maxlength="100"
                />
                <el-button
                  class="send-code-btn"
                  :disabled="forgotCountdown > 0"
                  @click="sendForgotCode"
                >
                  {{
                    forgotCountdown > 0 ? `${forgotCountdown}s` : "发送验证码"
                  }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="验证码">
              <el-input
                v-model="forgotPasswordForm.code"
                autocomplete="one-time-code"
                placeholder="请输入邮箱验证码"
                maxlength="20"
              />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input
                v-model="forgotPasswordForm.password"
                type="password"
                show-password
                autocomplete="new-password"
                placeholder="请输入新密码"
              />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input
                v-model="forgotPasswordForm.agPassword"
                type="password"
                show-password
                autocomplete="new-password"
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-button
              type="warning"
              class="submit-btn"
              :loading="forgotPasswordSubmitting"
              @click="doForgotPassword"
            >
              确认重置密码
            </el-button>
          </el-form>
        </div>
      </el-dialog>
    </section>
  </main>
</template>

<style scoped>
.auth-stage {
  position: relative;
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  overflow: hidden;
}

.auth-backdrop {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(
      120deg,
      rgba(3, 8, 16, 0.9) 0%,
      rgba(6, 12, 22, 0.78) 46%,
      rgba(6, 12, 22, 0.94) 100%
    ),
    radial-gradient(
      circle at 80% 20%,
      rgba(42, 104, 220, 0.35),
      transparent 45%
    ),
    radial-gradient(
      circle at 15% 85%,
      rgba(212, 166, 59, 0.18),
      transparent 42%
    ),
    url("/images/auth-bg-guilliman-throne.jpg") center 22% / cover no-repeat;
  filter: saturate(1.22) contrast(1.08);
}

.auth-backdrop::before {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: repeating-linear-gradient(
    0deg,
    rgba(255, 255, 255, 0.03) 0,
    rgba(255, 255, 255, 0.03) 2px,
    transparent 2px,
    transparent 4px
  );
  opacity: 0.35;
}

.auth-window {
  position: relative;
  z-index: 1;
  width: min(92vw, 560px);
  border-radius: 18px;
  border: 1px solid rgba(212, 166, 59, 0.42);
  background:
    radial-gradient(
      450px 280px at var(--mx, 50%) var(--my, 50%),
      rgba(31, 210, 255, 0.2),
      rgba(31, 210, 255, 0) 60%
    ),
    radial-gradient(
      340px 220px at calc(var(--mx, 50%) + 12%) calc(var(--my, 50%) - 18%),
      rgba(212, 166, 59, 0.18),
      rgba(212, 166, 59, 0) 65%
    ),
    linear-gradient(145deg, rgba(7, 14, 26, 0.52), rgba(11, 20, 36, 0.36)),
    linear-gradient(35deg, rgba(31, 210, 255, 0.06), transparent 40%);
  box-shadow:
    0 28px 52px rgba(0, 0, 0, 0.52),
    inset 0 0 0 1px rgba(255, 255, 255, 0.1),
    0 0 36px rgba(31, 210, 255, 0.2);
  padding: 22px 20px 18px;
  backdrop-filter: blur(12px) saturate(1.25);
  transition:
    transform 0.18s ease-out,
    border-color 0.28s ease,
    box-shadow 0.28s ease;
  will-change: transform;
}

.auth-window::before {
  content: "";
  position: absolute;
  inset: 10px;
  border: 1px solid rgba(143, 177, 230, 0.28);
  border-radius: 12px;
  pointer-events: none;
}

.auth-window::after {
  content: "";
  position: absolute;
  inset: 0;
  pointer-events: none;
  border-radius: 18px;
  background: linear-gradient(
    120deg,
    transparent 0%,
    rgba(255, 255, 255, 0.04) 20%,
    transparent 38%
  );
  animation: windowSweep 5.8s linear infinite;
  mix-blend-mode: screen;
}

.auth-head {
  margin-bottom: 10px;
}

.signal {
  margin: 0;
  color: #f2d489;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.auth-head h1 {
  margin: 8px 0 6px;
  color: #f3f7ff;
  font-size: clamp(1.55rem, 2.4vw, 2rem);
  letter-spacing: 0.04em;
  text-shadow:
    0 0 10px rgba(31, 210, 255, 0.3),
    0 0 26px rgba(31, 210, 255, 0.12);
}

.desc {
  margin: 0;
  color: #9fb3d5;
  line-height: 1.65;
}

.auth-tabs {
  margin-top: 12px;
}

.auth-tabs :deep(.el-tabs__header) {
  margin-bottom: 14px;
}

.auth-tabs :deep(.el-tabs__item) {
  color: #c8d8f1;
  letter-spacing: 0.06em;
}

.auth-tabs :deep(.el-tabs__item.is-active) {
  color: #f6ddb1;
}

.auth-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #d4a63b, #f1da9a);
}

.auth-tabs :deep(.el-form-item__label) {
  color: #c7d8f3;
}

.auth-tabs :deep(.el-input__wrapper) {
  background: rgba(6, 14, 26, 0.56);
  box-shadow:
    inset 0 0 0 1px rgba(131, 166, 220, 0.35),
    inset 0 0 14px rgba(31, 210, 255, 0.08);
  transition:
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.auth-tabs :deep(.el-input__wrapper.is-focus) {
  background: rgba(6, 14, 26, 0.7);
  box-shadow:
    inset 0 0 0 1px rgba(212, 166, 59, 0.72),
    0 0 0 2px rgba(212, 166, 59, 0.2),
    inset 0 0 18px rgba(212, 166, 59, 0.15);
}

.auth-tabs :deep(.el-input__inner) {
  color: #eaf2ff;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  position: relative;
  overflow: hidden;
}

.submit-btn::after {
  content: "";
  position: absolute;
  top: 0;
  left: -120%;
  width: 60%;
  height: 100%;
  background: linear-gradient(
    100deg,
    transparent,
    rgba(255, 255, 255, 0.35),
    transparent
  );
  transition: left 0.55s ease;
}

.submit-btn:hover::after {
  left: 150%;
}

.forgot-link-wrapper {
  text-align: center;
  margin-top: 12px;
}

.forgot-link {
  color: #9fb3d5;
  font-size: 13px;
  transition: color 0.2s ease;
}

.forgot-link:hover {
  color: #f2d489;
}

.forgot-desc {
  margin: 0 0 16px;
  color: #9fb3d5;
  font-size: 14px;
  line-height: 1.6;
  text-align: center;
}

@media (max-width: 640px) {
  .auth-stage {
    padding: 14px;
  }

  .auth-window {
    width: 100%;
    padding: 18px 14px 14px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-window {
    transform: none !important;
  }

  .auth-window::after,
  .submit-btn::after {
    animation: none;
    transition: none;
  }
}

@keyframes windowSweep {
  0% {
    transform: translateX(-35%) skewX(-14deg);
  }
  100% {
    transform: translateX(120%) skewX(-14deg);
  }
}
</style>

<style>
/* 使用非 scoped 样式命中 el-dialog 传送节点，避免默认白色主题覆盖 */
.forgot-password-overlay {
  background: rgba(2, 8, 18, 0.72) !important;
  backdrop-filter: blur(3px) saturate(1.12);
}

.forgot-password-dialog {
  border: 1px solid rgba(212, 166, 59, 0.42);
  border-radius: 14px;
  overflow: hidden;
  background:
    radial-gradient(
      380px 220px at 18% 0%,
      rgba(31, 210, 255, 0.14),
      rgba(31, 210, 255, 0) 62%
    ),
    linear-gradient(155deg, rgba(7, 14, 26, 0.94), rgba(9, 18, 32, 0.92));
  box-shadow:
    0 26px 46px rgba(0, 0, 0, 0.52),
    inset 0 0 0 1px rgba(255, 255, 255, 0.08),
    0 0 26px rgba(31, 210, 255, 0.14);
}

.forgot-password-dialog .el-dialog__header {
  background: linear-gradient(
    125deg,
    rgba(23, 52, 116, 0.52),
    rgba(10, 20, 34, 0.7)
  );
  border-bottom: 1px solid rgba(130, 167, 226, 0.34);
  padding: 16px 20px;
}

.forgot-password-dialog .el-dialog__title {
  color: #e8f0ff;
  font-weight: 600;
  letter-spacing: 0.03em;
}

.forgot-password-dialog .el-dialog__headerbtn .el-dialog__close {
  color: #b6c8e6;
}

.forgot-password-dialog .el-dialog__headerbtn:hover .el-dialog__close {
  color: #f3deb0;
}

.forgot-password-dialog .el-dialog__body {
  padding: 22px 20px 20px;
  background: transparent;
  max-height: min(70vh, 560px);
  overflow-y: auto;
}

.forgot-password-dialog .el-form-item__label {
  color: #c7d8f3;
}

.forgot-password-dialog .el-input__wrapper {
  background: rgba(6, 14, 26, 0.62);
  box-shadow:
    inset 0 0 0 1px rgba(131, 166, 220, 0.35),
    inset 0 0 14px rgba(31, 210, 255, 0.08);
  transition:
    box-shadow 0.2s ease,
    background-color 0.2s ease;
}

.forgot-password-dialog .el-input__wrapper.is-focus {
  background: rgba(6, 14, 26, 0.74);
  box-shadow:
    inset 0 0 0 1px rgba(212, 166, 59, 0.72),
    0 0 0 2px rgba(212, 166, 59, 0.2),
    inset 0 0 18px rgba(212, 166, 59, 0.15);
}

.forgot-password-dialog .el-input__inner {
  color: #eaf2ff;
}

/* 输入框+按钮组合样式 */
.input-with-btn {
  display: flex;
  gap: 10px;
  align-items: center;
}

.input-with-btn .el-input {
  flex: 1;
}

.send-code-btn {
  white-space: nowrap;
  min-width: 100px;
}
</style>
