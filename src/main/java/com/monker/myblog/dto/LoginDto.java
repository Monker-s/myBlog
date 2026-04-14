package com.monker.myblog.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 文件用途：登录请求参数。
 * 关联接口：AuthController.login。
 * 作用说明：封装账号登录时提交的标识信息与密码。
 *
 * @param username 登录标识，可由用户名
 * @param password 登录密码明文
 */
public record LoginDto(
        @NotBlank(message = "登录标识不能为空")
        String username,
        @NotBlank(message = "密码不能为空")
        String password
) {
}
