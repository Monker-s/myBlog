// 轻量图片懒加载指令：用于文章列表封面图
export const lazyload = {
  mounted(el, binding) {
    const src = binding.value;
    if (!src) return;

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          el.src = src;
          observer.disconnect();
        }
      });
    });

    observer.observe(el);
  }
};