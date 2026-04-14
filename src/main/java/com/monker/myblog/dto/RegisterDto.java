package com.monker.myblog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文件用途：用户注册参数。
 * 关联接口：AuthController.register。
 * 作用说明：封装新用户注册时提交的用户名、邮箱、验证码与密码。
 *
 * @param username 注册用户名
 * @param email 注册邮箱
 * @param code 邮箱验证码
 * @param password 注册密码明文
 */
public record RegisterDto(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50个字符")
        String username,
        @Email(message = "邮箱格式不正确")
        String email,
        @NotBlank(message = "验证码不能为空")
        @Size(max = 50, message = "验证码长度不能超过50个字符")
        String code,
        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 32, message = "密码长度需在8到32位之间")
        String password
) {
}
