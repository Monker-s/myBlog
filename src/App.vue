<template>
  <!-- 使用过渡包裹路由页面，符合需求文档中的 fade-slide 切换 -->
  <router-view v-slot="{ Component, route }">
    <transition name="fade-slide" mode="out-in">
      <component :is="Component" :key="route.fullPath" />
    </transition>
  </router-view>
</template>
