package com.monker.myblog.service;

import com.monker.myblog.dto.ForgetPasswordDto;
import com.monker.myblog.dto.LoginDto;
import com.monker.myblog.dto.RegisterDto;
import com.monker.myblog.dto.UpdateUserInfoDto;
import com.monker.myblog.vo.AuthSessionResponse;
import com.monker.myblog.vo.CurrentUserResponse;
import jakarta.validation.Valid;

/**
 * 文件用途：定义认证业务能力。
 * 作用说明：为 Controller 提供注册、登录、续期、登出和获取当前用户的统一业务接口。
 */
public interface AuthService {

    /**
     * 函数用途：执行用户注册。
     *
     * @param request 注册参数
     * @return 注册后的会话信息
     */
    AuthSessionResponse register(RegisterDto request);

    /**
     * 函数用途：执行用户登录。
     *
     * @param request 登录参数
     * @return 登录后的会话信息
     */
    AuthSessionResponse login(LoginDto request);

    /**
     * 函数用途：刷新当前会话。
     *
     * @return 刷新后的会话信息
     */
    AuthSessionResponse refresh();

    /**
     * 函数用途：获取当前登录用户。
     *
     * @return 当前用户信息
     */
    CurrentUserResponse currentUser();

    /**
     * 函数用途：执行用户登出。
     */
    void logout();

    /**
     * 函数用途：执行忘记密码重置流程。
     *
     * @param request 忘记密码参数
     */
    void forgetPassword(ForgetPasswordDto request);

    /**
     * 函数用途：发送邮箱验证码。
     *
     * @param email 目标邮箱地址
     * @param scene 使用场景（register: 注册, forgot: 忘记密码）
     */
    void sendVerificationCode(String email, String scene);

    /**
     * 函数用途：更新用户信息。
     *
     * @param request 更新参数
     */
    void updateUserInfo(@Valid UpdateUserInfoDto request);
}
