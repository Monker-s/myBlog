<script setup>
import { onMounted, onBeforeUnmount, reactive, ref, shallowRef } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";
import { createAdminPost, getAdminPosts, updateAdminPost } from "@/api/admin";
import { uploadFile } from "@/api/admin";
import { unwrapData } from "@/utils/service";
import service from "@/utils/service";
import "@wangeditor/editor/dist/css/style.css";

const route = useRoute();
const router = useRouter();

const postId = ref(null);
const loading = ref(false);
const saving = ref(false);
const categoryOptions = ref([]);

const editorRef = shallowRef();

const form = reactive({
  title: "",
  summary: "",
  cover_url: "",
  category_id: null,
  status: 0,
  is_pinned: 0,
  content_html: "",
  tags: [],
  published_at: "",
});

const tagInput = ref("");

const toolbarConfig = {};
const editorConfig = {
  placeholder: "请输入文章内容...",
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        try {
          const formData = new FormData();
          formData.append("file", file);
          const res = await uploadFile(formData);
          const url = res.data?.url || res.data?.data?.url;
          if (url) {
            insertFn(url, file.name, url);
          } else {
            ElMessage.error("图片上传失败");
          }
        } catch {
          ElMessage.error("图片上传失败");
        }
      },
    },
    insertLink: {
      checkLink(text, url) {
        return true;
      },
    },
  },
};

const handleCreated = (editor) => {
  editorRef.value = editor;
};

const fetchCategories = async () => {
  try {
    const res = await service.get("/api/admin/categories");
    const data = unwrapData(res);
    categoryOptions.value = Array.isArray(data) ? data : data?.records || data?.list || [];
  } catch (err) {
    ElMessage.error("获取分类列表失败");
    categoryOptions.value = [];
  }
};

const fetchPost = async () => {
  if (!postId.value) return;

  loading.value = true;
  try {
    const res = await getAdminPosts({ keyword: "", page: 1, pageSize: 1 });
    const data = unwrapData(res);
    const list = Array.isArray(data) ? data : data?.list || [];
    const post = list.find((p) => p.id === Number(postId.value));

    if (post) {
      form.title = post.title || "";
      form.slug = post.slug || "";
      form.summary = post.summary || "";
      form.cover_url = post.cover_url || "";
      form.category_id = post.category?.id || post.category_id || null;
      form.status = Number(post.status ?? 0);
      form.is_pinned = Number(post.is_pinned ?? 0);
      form.content_html = post.content_html || "";
      form.tags = (post.tags || []).map((tag) => tag.name || tag);
      form.published_at = post.published_at
        ? new Date(post.published_at).toISOString().slice(0, 16)
        : "";
    }
  } catch {
    ElMessage.error("获取文章失败");
  } finally {
    loading.value = false;
  }
};

const addTag = () => {
  const tag = tagInput.value.trim();
  if (tag && !form.tags.includes(tag)) {
    form.tags.push(tag);
    tagInput.value = "";
  }
};

const removeTag = (index) => {
  form.tags.splice(index, 1);
};

const handleCoverChange = async (uploadFile) => {
  const file = uploadFile.raw || uploadFile;
  if (!file) return;

  try {
    const formData = new FormData();
    formData.append("file", file);
    const res = await uploadFile(formData);
    const url = res.data?.url || res.data?.data?.url;
    if (url) {
      form.cover_url = url;
      ElMessage.success("封面上传成功");
    } else {
      ElMessage.error("封面上传失败");
    }
  } catch {
    ElMessage.error("封面上传失败");
  }
};

// 从 HTML 内容中提取目录
const extractToc = (html) => {
  if (!html) return [];
  const toc = [];
  const parser = new DOMParser();
  const doc = parser.parseFromString(html, "text/html");
  const headings = doc.querySelectorAll("h2, h3, h4");

  headings.forEach((heading, index) => {
    const id = `heading-${index}`;
    heading.id = id;
    toc.push({
      id,
      level: parseInt(heading.tagName.charAt(1)),
      text: heading.textContent.trim(),
    });
  });

  return toc;
};

const handleSave = async () => {
  if (!form.title.trim()) {
    ElMessage.warning("标题不能为空");
    return;
  }

  saving.value = true;
  try {
    const editorHtml = editorRef.value ? editorRef.value.getHtml() : form.content_html;

    // 生成目录
    const toc = extractToc(editorHtml);

    const payload = {
      title: form.title,
      summary: form.summary || undefined,
      cover_url: form.cover_url || undefined,
      category_id: form.category_id || undefined,
      status: form.status,
      is_pinned: form.is_pinned,
      content_html: editorHtml,
      toc_json: toc.length > 0 ? JSON.stringify(toc) : undefined,
      tags: form.tags.length > 0 ? form.tags : undefined,
      published_at: form.published_at || undefined,
    };

    if (postId.value) {
      await updateAdminPost(postId.value, payload);
      ElMessage.success("文章已更新");
    } else {
      await createAdminPost(payload);
      ElMessage.success("文章已创建");
    }

    router.push({ name: "admin-posts" });
  } catch {
    ElMessage.error("保存失败，请稍后重试");
  } finally {
    saving.value = false;
  }
};

const goBack = () => {
  router.push({ name: "admin-posts" });
};

onMounted(async () => {
  postId.value = route.params.id || null;
  await fetchCategories();
  if (postId.value) {
    await fetchPost();
  }
});

onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy();
  }
});
</script>

<template>
  <div class="post-edit-background">
    <section class="post-edit-page" v-loading="loading">
      <header class="page-head">
        <div class="head-left">
          <el-button @click="goBack" class="back-btn">
            <el-icon><arrow-left /></el-icon>
            返回列表
          </el-button>
          <h2>{{ postId ? '编辑文章' : '新建文章' }}</h2>
        </div>
        <div class="head-right">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </div>
      </header>

      <div class="edit-content">
        <div class="main-editor">
          <el-form label-position="top" class="edit-form">
            <div class="title-row">
              <el-form-item label="标题" required class="title-field">
                <el-input
                  v-model="form.title"
                  maxlength="200"
                  show-word-limit
                  placeholder="请输入文章标题"
                  size="large"
                />
              </el-form-item>
              <el-form-item label="封面图片" class="cover-field">
                <el-upload
                  class="cover-uploader"
                  action=""
                  :show-file-list="false"
                  :on-change="handleCoverChange"
                  :before-upload="() => false"
                  accept="image/*"
                >
                  <img v-if="form.cover_url" :src="form.cover_url" class="cover-preview" alt="封面" />
                  <div v-else class="cover-placeholder">
                    <el-icon size="24"><Plus /></el-icon>
                    <span>上传封面</span>
                  </div>
                </el-upload>
                <el-button
                  v-if="form.cover_url"
                  link
                  type="danger"
                  size="small"
                  @click="form.cover_url = ''"
                  class="cover-remove"
                >
                  移除
                </el-button>
              </el-form-item>
            </div>

            <el-form-item label="摘要">
              <el-input
                v-model="form.summary"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="文章简短描述"
              />
            </el-form-item>

            <el-form-item label="正文内容" required>
              <div class="editor-wrapper">
                <Toolbar
                  :editor="editorRef"
                  :defaultConfig="toolbarConfig"
                  mode="default"
                  class="editor-toolbar"
                />
                <Editor
                  v-model="form.content_html"
                  :defaultConfig="editorConfig"
                  mode="default"
                  class="editor-body"
                  @onCreated="handleCreated"
                />
              </div>
            </el-form-item>
          </el-form>
        </div>

        <aside class="side-panel">
          <div class="panel-card">
            <h3>发布设置</h3>
            <el-form label-position="top" class="side-form">
              <el-form-item label="状态">
                <el-select v-model="form.status" style="width: 100%">
                  <el-option label="草稿" :value="0" />
                  <el-option label="已发布" :value="1" />
                  <el-option label="隐藏" :value="2" />
                </el-select>
              </el-form-item>

              <el-form-item label="置顶">
                <el-switch
                  v-model="form.is_pinned"
                  :active-value="1"
                  :inactive-value="0"
                />
              </el-form-item>

              <el-form-item label="分类">
                <el-select
                  v-model="form.category_id"
                  clearable
                  placeholder="选择分类"
                  style="width: 100%"
                >
                  <el-option
                    v-for="cat in categoryOptions"
                    :key="cat.id"
                    :label="cat.name"
                    :value="cat.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="发布时间">
                <el-input
                  v-model="form.published_at"
                  type="datetime-local"
                  placeholder="可选"
                />
              </el-form-item>
            </el-form>
          </div>

          <div class="panel-card">
            <h3>标签</h3>
            <div class="tag-input-wrapper">
              <el-tag
                v-for="(tag, index) in form.tags"
                :key="index"
                closable
                @close="removeTag(index)"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-model="tagInput"
                size="small"
                style="width: 120px"
                placeholder="输入后回车"
                @keyup.enter="addTag"
              />
            </div>
          </div>

        </aside>
      </div>
    </section>
  </div>
</template>

<style scoped>
.post-edit-background {
  background: #f8fafc;
  min-height: 100vh;
  padding: 20px 0;
}

.post-edit-page {
  max-width: 1400px;
  margin: 0 auto;
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
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f3f4f6;
}

.head-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.head-left h2 {
  margin: 0;
  font-family: "Cinzel", serif;
  font-size: 20px;
  color: #1f2937;
}

.back-btn {
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-weight: 600;
  color: #4b5563;
  transition: all 0.2s ease;
}

.back-btn:hover {
  border-color: #6366f1;
  color: #6366f1;
}

.head-right {
  display: flex;
  gap: 8px;
}

.edit-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  align-items: start;
}

.main-editor {
  min-width: 0;
}

.edit-form :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 600;
  font-size: 14px;
}

.edit-form :deep(.el-input__wrapper) {
  background: #ffffff;
  border-color: #d1d5db;
}

.edit-form :deep(.el-input__inner) {
  color: #1f2937;
}

.title-row {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.title-field {
  flex: 1;
  min-width: 0;
}

.cover-field {
  flex-shrink: 0;
  width: 200px;
}

.cover-field :deep(.el-form-item__content) {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cover-uploader :deep(.el-upload) {
  width: 200px;
  height: 120px;
  border: 2px dashed #d1d5db;
  border-radius: 10px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-uploader :deep(.el-upload:hover) {
  border-color: #6366f1;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #9ca3af;
  font-size: 13px;
}

.cover-remove {
  margin-top: 4px;
}

.editor-wrapper {
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: #ffffff;
}

.editor-toolbar {
  border-bottom: 2px solid #e5e7eb;
  background: #f9fafb;
}

.editor-body {
  min-height: 500px;
  max-height: 700px;
  overflow-y: auto;
}

.side-panel {
  display: grid;
  gap: 16px;
}

.panel-card {
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
}

.panel-card h3 {
  margin: 0 0 12px;
  font-family: "Cinzel", serif;
  font-size: 14px;
  color: #1f2937;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.side-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.side-form :deep(.el-form-item__label) {
  color: #4b5563;
  font-weight: 500;
  font-size: 13px;
}

.side-form :deep(.el-input__wrapper) {
  background: #ffffff;
  border-color: #d1d5db;
}

.side-form :deep(.el-input__inner) {
  color: #1f2937;
}

.side-form :deep(.el-textarea__inner) {
  background: #ffffff;
  border-color: #d1d5db;
  color: #1f2937;
}

.tag-input-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  padding: 8px;
  background: #ffffff;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  min-height: 40px;
}

.tag-item {
  margin: 2px;
}

@media (max-width: 1080px) {
  .edit-content {
    grid-template-columns: 1fr;
  }

  .side-panel {
    order: -1;
  }
}
</style>