package com.monker.myblog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文件用途：忘记密码请求参数。
 * 关联接口：AuthController.forgetPassword。
 * 作用说明：封装用户重置密码时提交的邮箱、验证码、新密码及确认密码。
 *
 * @param email 用户邮箱
 * @param code 验证码
 * @param password 新密码
 * @param agPassword 确认密码
 */
public record ForgetPasswordDto(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,
        
        @NotBlank(message = "验证码不能为空")
        @Size(max = 50, message = "验证码长度不能超过50个字符")
        String code,
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需在6到32位之间")
        String password,
        
        @NotBlank(message = "确认密码不能为空")
        String agPassword
) {
}
