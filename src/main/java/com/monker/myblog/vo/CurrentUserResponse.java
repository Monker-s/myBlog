package com.monker.myblog.vo;

/**
 * 文件用途：当前登录用户信息响应对象。
 * 关联接口：AuthController.currentUser 以及认证相关返回体。
 * 作用说明：向前端暴露登录态下可直接使用的用户基础信息。
 *
 * @param id 用户主键
 * @param username 用户名
 * @param email 邮箱
 * @param role 角色编码
 */
public record CurrentUserResponse(
        Long id,
        String username,
        String email,
        Integer role,
        String icon
) {
}
