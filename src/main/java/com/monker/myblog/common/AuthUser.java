package com.monker.myblog.common;

/**
 * 文件用途：描述当前登录用户的安全上下文快照。
 * 作用说明：后续接入鉴权过滤器后，可将认证后的用户信息封装为该对象在服务层传递。
 */
public record AuthUser(
        Long id,
        String username,
        UserRole role
) {
}
