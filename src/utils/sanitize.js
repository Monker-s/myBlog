// 前端兜底清洗：后端仍应执行严格白名单过滤
export function sanitizeHtml(html) {
  if (!html) return "";

  const template = document.createElement("template");
  template.innerHTML = html;

  template.content.querySelectorAll("script,style,iframe,object,embed").forEach((el) => el.remove());

  template.content.querySelectorAll("*").forEach((el) => {
    [...el.attributes].forEach((attr) => {
      const name = attr.name.toLowerCase();
      const value = attr.value || "";

      if (name.startsWith("on")) {
        el.removeAttribute(attr.name);
      }

      if ((name === "href" || name === "src") && /^javascript:/i.test(value)) {
        el.removeAttribute(attr.name);
      }
    });
  });

  return template.innerHTML;
}