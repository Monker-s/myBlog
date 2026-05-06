import service from "@/utils/service";

export function getDecorationConfig() {
  return service.get("/api/config/decoration");
}

export function updateDecorationConfig(payload) {
  // 公开配置接口在文档中为 GET；
  // CMS 更新通常走管理端路由，这里按后台语义封装 PUT。
  return service.put("/api/admin/config/decoration", payload);
}
