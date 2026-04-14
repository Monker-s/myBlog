package com.monker.myblog.service;

/**
 * 文件用途：定义邮箱验证码业务能力。
 * 作用说明：提供发送验证码和验证验证码的统一业务接口。
 */
public interface EmailVerificationService {

    /**
     * 函数用途：生成并发送邮箱验证码。
     *
     * @param email 目标邮箱地址
     * @param scene 使用场景（register: 注册, forgot: 忘记密码）
     */
    void sendVerificationCode(String email, String scene);

    /**
     * 函数用途：验证邮箱验证码是否正确。
     *
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @param scene 使用场景（register: 注册, forgot: 忘记密码）
     */
    void verifyCode(String email, String code, String scene);
}
