package com.monker.myblog.vo;

/**
 * 文件用途：认证会话响应对象。
 * 关联接口：AuthController.register、AuthController.login、AuthController.refresh。
 * 作用说明：向前端返回当前登录用户信息与 JWT Token。
 *
 * @param user 当前登录用户信息
 * @param token JWT 访问令牌
 */
public record AuthSessionResponse(
        CurrentUserResponse user,
        String token
) {
}
