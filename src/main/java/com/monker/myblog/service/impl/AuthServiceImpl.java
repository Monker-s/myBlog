package com.monker.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.monker.myblog.common.ResultCode;
import com.monker.myblog.common.UserRole;
import com.monker.myblog.dto.*;
import com.monker.myblog.entity.Notification;
import com.monker.myblog.entity.User;
import com.monker.myblog.exception.BusinessException;
import com.monker.myblog.mapper.NotificationMapper;
import com.monker.myblog.mapper.UserMapper;
import com.monker.myblog.service.AuthService;
import com.monker.myblog.service.EmailVerificationService;
import com.monker.myblog.util.JwtUtil;
import com.monker.myblog.util.UserHolder;
import com.monker.myblog.vo.AuthSessionResponse;
import com.monker.myblog.vo.CurrentUserResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 文件用途：认证业务实现类。
 * 作用说明：实现基于 JWT 的注册、登录、登出和忘记密码流程，并通过 MyBatis 持久化用户信息。
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;
    private final NotificationMapper notificationMapper;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, 
                          EmailVerificationService emailVerificationService, NotificationMapper notificationMapper) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailVerificationService = emailVerificationService;
        this.notificationMapper = notificationMapper;
    }

    /**
     * 函数用途：执行用户注册流程。
     *
     * @param request 注册参数
     * @return 注册后的用户信息和 JWT Token
     */
    @Override
    public AuthSessionResponse register(RegisterDto request) {
        // 先检查用户名是否已存在（不消耗验证码）
        if (userMapper.findByUsername(request.username()) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }

        // 再检查邮箱是否已被注册（不消耗验证码）
        if (userMapper.findByEmail(request.email()) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "该邮箱已被注册");
        }
        
        // 唯一性检查通过后，再验证邮箱验证码（验证通过后会删除验证码）
        emailVerificationService.verifyCode(request.email(), request.code(), "register");

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
        
        // 为新注册用户复制所有 SYSTEM 类型的通知
        copySystemNotificationsToNewUser(user.getId());
        
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
        User user = null;
        
        // 根据前端传来的参数判断登录方式
        if (request.username() != null && !request.username().isEmpty()) {
            // 用户名登录
            user = userMapper.findByUsername(request.username());
        } else if (request.email() != null && !request.email().isEmpty()) {
            // 邮箱登录
            user = userMapper.findByEmail(request.email());
        } else {
            throw new BusinessException(ResultCode.INVALID_ARGUMENT, "请提供用户名或邮箱");
        }
        
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        
        // 检查用户状态，如果被禁用则不允许登录
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
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
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
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
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getIcon()
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
        log.info("开始发送验证码：email={}, scene={}", email, scene);
        
        // 如果是注册场景，检查邮箱是否已被注册
        if ("register".equals(scene) && userMapper.findByEmail(email) != null) {
            log.warn("注册失败：邮箱已被注册 - {}", email);
            throw new BusinessException(ResultCode.CONFLICT, "该邮箱已被注册");
        }
        
        // 如果是忘记密码场景，检查邮箱是否已注册
        if ("forgot".equals(scene) && userMapper.findByEmail(email) == null) {
            log.warn("忘记密码失败：邮箱未注册 - {}", email);
            throw new BusinessException(ResultCode.NOT_FOUND, "该邮箱未注册");
        }
        
        log.info("验证通过，调用 EmailVerificationService 发送验证码");
        emailVerificationService.sendVerificationCode(email, scene);
        log.info("验证码发送流程完成：{}", email);
    }

    @Override
    public void updateUserInfo(UpdateUserInfoDto request) {
        // 根据id查询用户
        Long userId = UserHolder.getUserId().getId();
        
        log.info("前端传来的数据: {}", request);
        
        // 使用 UpdateWrapper 动态更新非空字段
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId);
        
        boolean hasUpdate = false;
        
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            updateWrapper.set("username", request.getUsername());
            hasUpdate = true;
            log.info("准备更新username: {}", request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            updateWrapper.set("email", request.getEmail());
            hasUpdate = true;
            log.info("准备更新email: {}", request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            updateWrapper.set("password_hash", passwordEncoder.encode(request.getPassword()));
            hasUpdate = true;
            log.info("准备更新password");
        }
        if (request.getIcon() != null && !request.getIcon().isEmpty()) {
            updateWrapper.set("icon", request.getIcon());
            hasUpdate = true;
            log.info("准备更新icon: {}", request.getIcon());
        }
        
        if (hasUpdate) {
            updateWrapper.set("updated_at", LocalDateTime.now());
            int rows = userMapper.update(null, updateWrapper);
            log.info("用户信息更新完成，影响行数: {}", rows);
            
            if (rows == 0) {
                throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
            }
        } else {
            log.warn("没有需要更新的字段");
        }
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
                user.getRole(),
                user.getIcon()
        );
        return new AuthSessionResponse(userResponse, token);
    }

    /**
     * 函数用途：为新注册用户复制所有 SYSTEM 类型的通知。
     * 作用说明：确保新用户能够收到之前发布的系统公告。
     *
     * @param userId 新用户ID
     */
    private void copySystemNotificationsToNewUser(Long userId) {
        try {
            // 查询所有 SYSTEM 类型的通知模板（使用 DISTINCT 去重）
            LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Notification::getType, "SYSTEM")
                    .select(Notification::getType, Notification::getPostId, 
                            Notification::getContentHash, Notification::getTitle, 
                            Notification::getBody, Notification::getPayloadJson);
            
            List<Notification> systemNotifications = notificationMapper.selectList(queryWrapper);
            
            if (systemNotifications == null || systemNotifications.isEmpty()) {
                log.info("没有 SYSTEM 类型的通知，跳过复制");
                return;
            }
            
            // 去重：根据 contentHash 去重
            java.util.Set<String> processedHashes = new java.util.HashSet<>();
            LocalDateTime now = LocalDateTime.now();
            int copiedCount = 0;
            
            for (Notification template : systemNotifications) {
                String contentHash = template.getContentHash();
                
                // 如果已经处理过这个 contentHash，跳过
                if (processedHashes.contains(contentHash)) {
                    continue;
                }
                processedHashes.add(contentHash);
                
                // 检查该用户是否已经有这条通知（防止重复）
                LambdaQueryWrapper<Notification> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(Notification::getUserId, userId)
                        .eq(Notification::getContentHash, contentHash);
                Long existingCount = notificationMapper.selectCount(checkWrapper);
                
                if (existingCount > 0) {
                    log.debug("用户 {} 已有通知: {}", userId, contentHash);
                    continue;
                }
                
                // 创建新通知记录
                Notification newNotification = Notification.builder()
                        .userId(userId)
                        .type(template.getType())
                        .postId(template.getPostId())
                        .contentHash(contentHash)
                        .title(template.getTitle())
                        .body(template.getBody())
                        .isRead(0)  // 默认为未读
                        .payloadJson(template.getPayloadJson())
                        .createdAt(now)
                        .build();
                
                notificationMapper.insert(newNotification);
                copiedCount++;
            }
            
            log.info("为新用户 {} 复制了 {} 条 SYSTEM 通知", userId, copiedCount);
            
        } catch (Exception e) {
            log.error("为新用户 {} 复制 SYSTEM 通知失败", userId, e);
            // 不抛出异常，避免影响注册流程
        }
    }
}
