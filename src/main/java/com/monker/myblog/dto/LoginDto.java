package com.monker.myblog.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 文件用途：登录请求参数。
 * 关联接口：AuthController.login。
 * 作用说明：封装账号登录时提交的标识信息与密码，支持用户名或邮箱登录。
 *             前端根据登录方式传递不同参数：用户名登录传 username，邮箱登录传 email。
 *
 * @param username 登录用户名（用户名登录时必填）
 * @param email 登录邮箱（邮箱登录时必填）
 * @param password 登录密码明文
 */
public record LoginDto(
        String username,
        String email,
        @NotBlank(message = "密码不能为空")
        String password
) {
}
