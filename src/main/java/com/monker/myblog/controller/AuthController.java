package com.monker.myblog.controller;

import com.monker.myblog.common.Result;
import com.monker.myblog.dto.ForgetPasswordDto;
import com.monker.myblog.dto.LoginDto;
import com.monker.myblog.dto.RegisterDto;
import com.monker.myblog.service.AuthService;
import com.monker.myblog.vo.AuthSessionResponse;
import com.monker.myblog.vo.CurrentUserResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件用途：提供登录、注册、登出和会话续期等认证接口入口。
 * 作用说明：统一承接前端鉴权请求，并把请求分发到 {@link AuthService} 处理。
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 函数用途：注入认证服务。
     *
     * @param authService 认证服务
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 函数用途：处理用户注册请求。
     *
     * @param request 注册参数
     * @return 注册后的会话信息
     */
    @PostMapping("/register")
    public Result<AuthSessionResponse> register(@Valid @RequestBody RegisterDto request) {
        return Result.success(authService.register(request));
    }

    /**
     * 函数用途：处理用户登录请求。
     *
     * @param request 登录参数
     * @return 登录后的会话信息
     */
    @PostMapping("/login")
    public Result<AuthSessionResponse> login(@Valid @RequestBody LoginDto request) {
        log.info("用户登录：{}", request);
        return Result.success(authService.login(request));
    }

    /**
     * 函数用途：处理用户登出请求。
     *
     * @return 统一成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户登出");
        authService.logout();
        return Result.success("退出成功", null);
    }

    /**
     * 函数用途：处理登录态静默续期请求。
     *
     * @return 刷新后的会话信息
     */
    @PostMapping("/refresh")
    public Result<AuthSessionResponse> refresh() {
        return Result.success(authService.refresh());
    }

    /**
     * 函数用途：返回当前登录用户信息。
     *
     * @return 当前登录用户视图对象
     */
    @GetMapping("/me")
    public Result<CurrentUserResponse> currentUser() {
        log.info("获取当前登录用户信息");
        return Result.success(authService.currentUser());
    }

    /**
     * 函数用途：处理忘记密码重置请求。
     *
     * @param request 忘记密码参数
     * @return 统一成功响应
     */
    @PostMapping("/forgot")
    public Result<Void> forgetPassword(@Valid @RequestBody ForgetPasswordDto request) {
        log.info("用户忘记密码：{}", request);
        authService.forgetPassword(request);
        return Result.success("密码重置成功", null);
    }

    /**
     * 函数用途：发送邮箱验证码。
     *
     * @param email 目标邮箱地址
     * @param scene 使用场景（register: 注册, forgot: 忘记密码）
     * @return 统一成功响应
     */
    @PostMapping("/sendCode")
    public Result<Void> sendVerificationCode(String email, String scene) {
        log.info("发送验证码：email={}, scene={}", email, scene);
        authService.sendVerificationCode(email, scene);
        return Result.success("验证码已发送", null);
    }
}
