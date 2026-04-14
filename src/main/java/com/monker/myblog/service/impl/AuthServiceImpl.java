package com.monker.myblog.service.impl;

import com.monker.myblog.common.ResultCode;
import com.monker.myblog.common.UserRole;
import com.monker.myblog.dto.ForgetPasswordDto;
import com.monker.myblog.dto.LoginDto;
import com.monker.myblog.dto.RegisterDto;
import com.monker.myblog.dto.UserDto;
import com.monker.myblog.entity.User;
import com.monker.myblog.exception.BusinessException;
import com.monker.myblog.mapper.UserMapper;
import com.monker.myblog.service.AuthService;
import com.monker.myblog.service.EmailVerificationService;
import com.monker.myblog.util.JwtUtil;
import com.monker.myblog.util.UserHolder;
import com.monker.myblog.vo.AuthSessionResponse;
import com.monker.myblog.vo.CurrentUserResponse;
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 文件用途：认证业务实现类。
 * 作用说明：实现基于 JWT 的注册、登录、登出和忘记密码流程，并通过 MyBatis 持久化用户信息。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, 
                          EmailVerificationService emailVerificationService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailVerificationService = emailVerificationService;
    }

    /**
     * 函数用途：执行用户注册流程。
     *
     * @param request 注册参数
     * @return 注册后的用户信息和 JWT Token
     */
    @Override
    public AuthSessionResponse register(RegisterDto request) {
        // 验证邮箱验证码
        emailVerificationService.verifyCode(request.email(), request.code(), "register");
        
        if (userMapper.findByUsername(request.username()) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }

        if (userMapper.findByEmail(request.email()) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "该邮箱已被注册");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.USER.getCode())
                .status(1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userMapper.insert(user);
        return buildAuthSessionResponse(user);
    }

    /**
     * 函数用途：执行用户登录流程。
     *
     * @param request 登录参数
     * @return 登录后的用户信息和 JWT Token
     */
    @Override
    public AuthSessionResponse login(LoginDto request) {
        User user = userMapper.findByUsernameOrEmail(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        UserHolder.setUserId(userDto);

        return buildAuthSessionResponse(user);
    }

    /**
     * 函数用途：刷新当前用户 Token。
     *
     * @return 刷新后的会话信息
     */
    @Override
    public AuthSessionResponse refresh() {
        // 从 UserHolder 获取当前用户 ID
        UserDto currentUser = UserHolder.getUserId();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        
        // 查询最新的用户信息
        User user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        
        // 生成新的 Token
        return buildAuthSessionResponse(user);
    }

    /**
     * 函数用途：获取当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    @Override
    public CurrentUserResponse currentUser() {
        // 从 UserHolder 获取当前用户 ID
        UserDto currentUser = UserHolder.getUserId();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        
        // 查询最新的用户信息
        User user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    /**
     * 函数用途：执行登出流程。
     */
    @Override
    public void logout() {
        // JWT 为无状态认证，当前实现无需服务端额外处理。
    }

    /**
     * 函数用途：执行忘记密码重置流程。
     *
     * @param request 忘记密码参数
     */
    @Override
    public void forgetPassword(ForgetPasswordDto request) {
        if (!request.password().equals(request.agPassword())) {
            throw new BusinessException(ResultCode.INVALID_ARGUMENT, "两次输入的密码不一致");
        }

        // 验证邮箱验证码
        emailVerificationService.verifyCode(request.email(), request.code(), "forgot");
        
        User user = userMapper.findByEmail(request.email());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该邮箱未注册");
        }

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 函数用途：发送邮箱验证码。
     *
     * @param email 目标邮箱地址
     * @param scene 使用场景（register: 注册, forgot: 忘记密码）
     */
    @Override
    public void sendVerificationCode(String email, String scene) {
        // 如果是注册场景，检查邮箱是否已被注册
        if ("register".equals(scene) && userMapper.findByEmail(email) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "该邮箱已被注册");
        }
        
        // 如果是忘记密码场景，检查邮箱是否已注册
        if ("forgot".equals(scene) && userMapper.findByEmail(email) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该邮箱未注册");
        }
        
        emailVerificationService.sendVerificationCode(email, scene);
    }

    /**
     * 函数用途：组装认证成功后的统一返回结果。
     *
     * @param user 用户实体
     * @return 会话响应对象
     */
    private AuthSessionResponse buildAuthSessionResponse(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        CurrentUserResponse userResponse = new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
        return new AuthSessionResponse(userResponse, token);
    }
}
