<script setup>
const props = defineProps({
  count: {
    type: Number,
    default: 0
  }
});
</script>

<template>
  <div class="notification-bell" aria-label="通知">
    <span class="icon" aria-hidden="true">⛨</span>
    <span v-if="props.count > 0" class="badge">{{ props.count > 99 ? "99+" : props.count }}</span>
  </div>
</template>

<style scoped>
.notification-bell {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
}

.icon {
  font-size: 18px;
  color: #f4d58f;
  text-shadow: 0 0 10px rgba(212, 166, 59, 0.55);
}

.badge {
  position: absolute;
  top: -8px;
  right: -12px;
  min-width: 18px;
  height: 18px;
  border-radius: 999px;
  padding: 0 5px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #fff;
  background: linear-gradient(145deg, #c33a3a, #901f1f);
  border: 1px solid rgba(255, 255, 255, 0.5);
}
</style>
