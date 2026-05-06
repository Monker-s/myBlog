<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { ROLE_ADMIN, ROLE_USER, ROLE_GUEST } from "@/constants/roles";
import { ROUTE_NAMES } from "@/constants/routes";

const props = defineProps({
  stats: {
    type: Array,
    default: () => [],
  },
});

const router = useRouter();
const authStore = useAuthStore();

const roleLabel = computed(() => {
  const role = Number(authStore.user?.role);
  if (role === ROLE_ADMIN) return "战团长";
  if (role === ROLE_USER) return "战斗兄弟";
  return "凡人辅军";
});

const roleRank = computed(() => {
  const role = Number(authStore.user?.role);
  if (role === ROLE_ADMIN) return "CHAPTER MASTER";
  if (role === ROLE_USER) return "BATTLE BROTHER";
  return "AUXILIARY";
});

const displayName = computed(() => {
  return authStore.user?.username || "未知战士";
});

const userInitial = computed(() => {
  const name = displayName.value;
  return name ? name.charAt(0).toUpperCase() : "?";
});

const userAvatar = computed(() => {
  return authStore.user?.icon || "";
});

const navigateToProfile = () => {
  router.push({ name: ROUTE_NAMES.USER_PROFILE });
};
</script>

<template>
  <section
    class="sidebar panel-surface"
    role="complementary"
    aria-label="侧边功能"
  >
    <div class="profile-block">
      <div class="avatar-frame">
        <div class="avatar-inner">
          <img
            v-if="userAvatar"
            :src="userAvatar"
            class="avatar-image"
            alt="Avatar"
          />
          <span v-else class="avatar-initial">{{ userInitial }}</span>
        </div>
        <span class="corner-tl" aria-hidden="true"></span>
        <span class="corner-tr" aria-hidden="true"></span>
        <span class="corner-bl" aria-hidden="true"></span>
        <span class="corner-br" aria-hidden="true"></span>
      </div>

      <h2 class="profile-name">{{ displayName }}</h2>
      <span class="profile-rank">{{ roleRank }}</span>
      <span class="profile-role">{{ roleLabel }}</span>

      <div class="profile-divider" aria-hidden="true">
        <span class="div-line"></span>
        <span class="div-gem">◆</span>
        <span class="div-line"></span>
      </div>

      <button class="profile-button" @click="navigateToProfile">
        <span class="button-icon">⚙</span>
        <span class="button-text">个人信息管理</span>
      </button>
    </div>

    <div class="block">
      <h3>战况仪表</h3>
      <div class="stat-grid" role="status" aria-live="polite">
        <article v-for="stat in props.stats" :key="stat.label">
          <strong>{{ stat.value }}</strong>
          <small>{{ stat.label }}</small>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.sidebar {
  padding: 14px;
  border-color: var(--border-strong);
}

.profile-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 18px 10px 14px;
}

.avatar-frame {
  position: relative;
  width: 110px;
  height: 110px;
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
  margin-bottom: 14px;
}

.avatar-frame::before {
  content: "";
  position: absolute;
  inset: 4px;
  border: 1px solid rgba(184, 148, 95, 0.3);
  pointer-events: none;
}

.avatar-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(160deg, rgba(15, 10, 8, 0.95), rgba(30, 22, 15, 0.9)),
    repeating-linear-gradient(
      -45deg,
      rgba(60, 45, 30, 0.06) 0px,
      rgba(60, 45, 30, 0.06) 2px,
      transparent 2px,
      transparent 4px
    );
}

.avatar-initial {
  font-family: "Cinzel", serif;
  font-size: 42px;
  font-weight: 900;
  color: #d4a63b;
  text-shadow:
    0 0 12px rgba(212, 166, 59, 0.4),
    0 2px 4px rgba(0, 0, 0, 0.8);
  letter-spacing: 0.05em;
  line-height: 1;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 2px;
}

.corner-tl,
.corner-tr,
.corner-bl,
.corner-br {
  position: absolute;
  width: 10px;
  height: 10px;
  pointer-events: none;
}

.corner-tl::before,
.corner-tr::before,
.corner-bl::before,
.corner-br::before,
.corner-tl::after,
.corner-tr::after,
.corner-bl::after,
.corner-br::after {
  content: "";
  position: absolute;
  background: radial-gradient(circle, #d4a63b, #8b7355);
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(212, 166, 59, 0.5);
}

.corner-tl::before,
.corner-tr::before,
.corner-bl::before,
.corner-br::before {
  width: 6px;
  height: 6px;
}

.corner-tl::after,
.corner-tr::after,
.corner-bl::after,
.corner-br::after {
  width: 3px;
  height: 3px;
}

.corner-tl {
  top: -4px;
  left: -4px;
}
.corner-tr {
  top: -4px;
  right: -4px;
}
.corner-bl {
  bottom: -4px;
  left: -4px;
}
.corner-br {
  bottom: -4px;
  right: -4px;
}

.corner-tl::before {
  top: 0;
  left: 0;
}
.corner-tl::after {
  top: -3px;
  left: -3px;
}
.corner-tr::before {
  top: 0;
  right: 0;
}
.corner-tr::after {
  top: -3px;
  right: -3px;
}
.corner-bl::before {
  bottom: 0;
  left: 0;
}
.corner-bl::after {
  bottom: -3px;
  left: -3px;
}
.corner-br::before {
  bottom: 0;
  right: 0;
}
.corner-br::after {
  bottom: -3px;
  right: -3px;
}

.profile-name {
  margin: 0;
  font-family: "Cinzel", serif;
  font-size: 18px;
  color: #e8dcc8;
  letter-spacing: 0.08em;
  text-align: center;
  text-shadow:
    0 2px 4px rgba(0, 0, 0, 0.8),
    0 0 16px rgba(196, 162, 101, 0.2);
}

.profile-rank {
  display: block;
  margin-top: 4px;
  font-size: 10px;
  color: #8b7d6b;
  letter-spacing: 0.25em;
  text-transform: uppercase;
}

.profile-role {
  display: inline-block;
  margin-top: 8px;
  border: 1px solid rgba(184, 148, 95, 0.5);
  border-radius: 3px;
  padding: 3px 14px;
  color: #c4a265;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  background: linear-gradient(
    145deg,
    rgba(184, 148, 95, 0.12),
    rgba(184, 148, 95, 0.03)
  );
  box-shadow: inset 0 0 10px rgba(184, 148, 95, 0.15);
}

.profile-divider {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin-top: 16px;
}

.profile-divider .div-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(184, 148, 95, 0.4),
    transparent
  );
}

.profile-divider .div-gem {
  color: #b8945f;
  font-size: 8px;
  text-shadow: 0 0 6px rgba(184, 148, 95, 0.5);
}

.profile-button {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin-top: 12px;
  padding: 10px 16px;
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

.profile-button::before {
  content: "";
  position: absolute;
  inset: 3px;
  border: 1px solid rgba(139, 115, 85, 0.2);
  border-radius: 2px;
  pointer-events: none;
}

.profile-button:hover {
  transform: translateY(-2px);
  border-color: #b8945f;
  color: #f0e6d2;
  box-shadow:
    0 4px 12px rgba(0, 0, 0, 0.6),
    0 0 20px rgba(184, 148, 95, 0.2),
    inset 0 1px 0 rgba(184, 148, 95, 0.3);
}

.profile-button:active {
  transform: translateY(0);
}

.button-icon {
  font-size: 16px;
  line-height: 1;
}

.button-text {
  flex: 1;
  text-align: center;
}

.block {
  margin-top: 14px;
  border-top: 1px dashed rgba(151, 181, 228, 0.3);
  padding-top: 12px;
}

.block h3 {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--accent-soft);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.stat-grid article {
  border: 1px solid rgba(136, 170, 225, 0.35);
  border-radius: 10px;
  padding: 8px;
  background: rgba(7, 17, 31, 0.64);
}

.stat-grid strong {
  display: block;
  color: #f9fbff;
}

.stat-grid small {
  color: var(--text-muted);
}
</style>
