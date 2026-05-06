<script setup>
const props = defineProps({
  liked: {
    type: Boolean,
    default: false,
  },
  count: {
    type: Number,
    default: 0,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["toggle"]);
</script>

<template>
  <el-button
    :class="['like-button', { 'is-liked': props.liked }]"
    :loading="props.loading"
    @click="emit('toggle')"
  >
    <span class="like-icon">{{ props.liked ? "❤️" : "🤍" }}</span>
    <span class="like-text">{{ props.liked ? "已点赞" : "点赞" }}</span>
    <span class="like-count"></span>
  </el-button>
</template>

<style scoped>
.like-button {
  display: inline-flex !important;
  align-items: center;
  gap: 4px;
  border: 1px solid rgba(136, 170, 225, 0.38) !important;
  background: rgba(7, 15, 26, 0.85) !important;
  color: #c8d8f1 !important;
  transition: all 0.28s ease;
}

.like-button:hover {
  border-color: rgba(212, 166, 59, 0.6) !important;
  background: rgba(212, 166, 59, 0.12) !important;
  color: #f2d489 !important;
}

.like-button.is-liked {
  border-color: rgba(214, 73, 73, 0.65) !important;
  background: linear-gradient(
    135deg,
    rgba(214, 73, 73, 0.18),
    rgba(214, 73, 73, 0.08)
  ) !important;
  color: #ff6b6b !important;
  box-shadow: 0 0 12px rgba(214, 73, 73, 0.2) !important;
}

.like-button.is-liked:hover {
  border-color: rgba(214, 73, 73, 0.8) !important;
  background: linear-gradient(
    135deg,
    rgba(214, 73, 73, 0.28),
    rgba(214, 73, 73, 0.15)
  ) !important;
  box-shadow: 0 0 16px rgba(214, 73, 73, 0.3) !important;
}

.like-icon {
  font-size: 16px;
  transition: transform 0.25s ease;
}

.like-button.is-liked .like-icon {
  animation: likeHeartbeat 0.4s ease-in-out;
}

@keyframes likeHeartbeat {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.25);
  }
}

.like-text {
  font-weight: 500;
}

.like-count {
  opacity: 0.85;
}
</style>
