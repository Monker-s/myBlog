<script setup>
import { reactive, computed } from "vue";

const props = defineProps({
  // 回复模式：是否为回复某条评论
  replyTo: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(["submit", "cancel"]);

const isReplyMode = computed(() => !!props.replyTo);

const form = reactive({
  content: "",
});

const submit = () => {
  const text = form.content.trim();
  if (!text) return;
  emit("submit", { content: text });
  form.content = "";
};

const cancel = () => {
  form.content = "";
  emit("cancel");
};

const placeholder = computed(() => {
  if (isReplyMode.value) {
    const userName =
      props.replyTo.userName || props.replyTo.username || "匿名用户";
    return `回复 @${userName}：`;
  }
  return "请输入评论内容";
});
</script>

<template>
  <section
    class="comment-form page-panel"
    :class="{ 'reply-mode': isReplyMode }"
    :aria-label="isReplyMode ? '回复评论' : '评论输入区'"
  >
    <div v-if="isReplyMode" class="reply-target">
      <span
        >回复 @{{ replyTo.userName || replyTo.username || "匿名用户" }}</span
      >
      <el-button class="cancel-reply" size="small" text @click="cancel">
        取消
      </el-button>
    </div>

    <el-input
      v-model="form.content"
      type="textarea"
      :rows="isReplyMode ? 3 : 4"
      maxlength="500"
      show-word-limit
      :placeholder="placeholder"
    />

    <div class="actions">
      <el-button v-if="isReplyMode" @click="cancel">取消</el-button>
      <el-button type="primary" @click="submit">
        {{ isReplyMode ? "回复" : "提交评论" }}
      </el-button>
    </div>
  </section>
</template>

<style scoped>
.comment-form {
  transition: all 0.25s ease;
}

.comment-form.reply-mode {
  border-color: rgba(31, 210, 255, 0.5);
  box-shadow: 0 0 12px rgba(31, 210, 255, 0.15);
}

.reply-target {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  margin-bottom: 8px;
  background: rgba(31, 210, 255, 0.08);
  border-radius: 6px;
  color: #88aae1;
  font-size: 13px;
}

.cancel-reply {
  color: #88aae1;
  padding: 0;
}

.cancel-reply:hover {
  color: #f2d489;
}

.actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
