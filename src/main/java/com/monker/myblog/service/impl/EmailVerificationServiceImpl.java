package com.monker.myblog.service.impl;

import com.monker.myblog.exception.BusinessException;
import com.monker.myblog.common.ResultCode;
import com.monker.myblog.service.EmailVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 文件用途：实现邮箱验证码业务逻辑。
 * 作用说明：通过 Redis 存储验证码，使用 Spring Mail 发送邮件。
 */
@Service
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JavaMailSender mailSender;

    private static final int CODE_LENGTH = 6;
    private static final long EXPIRE_MINUTES = 5;
    private static final String REDIS_KEY_PREFIX = "email:code:";

    /**
     * 函数用途：生成并发送邮箱验证码。
     *
     * @param email 目标邮箱地址
     * @param scene 使用场景（register: 注册, forgot: 忘记密码）
     */
    @Override
    public void sendVerificationCode(String email, String scene) {
        // 生成6位随机验证码
        String code = generateCode();
        
        // 构建 Redis key
        String redisKey = REDIS_KEY_PREFIX + scene + ":" + email;
        
        // 存储到 Redis，设置5分钟过期
        redisTemplate.opsForValue().set(redisKey, code, EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        // 发送邮件
        sendEmail(email, code, scene);
        
        log.info("验证码已发送至 {}: {}", email, scene);
    }

    /**
     * 函数用途：验证邮箱验证码是否正确。
     *
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @param scene 使用场景（register: 注册, forgot: 忘记密码）
     */
    @Override
    public void verifyCode(String email, String code, String scene) {
        String redisKey = REDIS_KEY_PREFIX + scene + ":" + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        
        if (storedCode == null) {
            throw new BusinessException(ResultCode.INVALID_ARGUMENT, "验证码已过期，请重新获取");
        }
        
        if (!storedCode.equals(code)) {
            throw new BusinessException(ResultCode.INVALID_ARGUMENT, "验证码错误");
        }
        
        // 验证成功后删除验证码，防止重复使用
        redisTemplate.delete(redisKey);
    }

    /**
     * 函数用途：生成指定位数的随机数字验证码。
     *
     * @return 验证码字符串
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 函数用途：发送验证码邮件。
     *
     * @param email 收件人邮箱
     * @param code 验证码
     * @param scene 使用场景
     */
    private void sendEmail(String email, String code, String scene) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        
        if ("register".equals(scene)) {
            message.setSubject("【MyBlog】注册验证码");
            message.setText("您的注册验证码为：" + code + "，有效期5分钟。请勿将验证码泄露给他人。");
        } else if ("forgot".equals(scene)) {
            message.setSubject("【MyBlog】密码重置验证码");
            message.setText("您的密码重置验证码为：" + code + "，有效期5分钟。请勿将验证码泄露给他人。");
        } else {
            message.setSubject("【MyBlog】验证码");
            message.setText("您的验证码为：" + code + "，有效期5分钟。");
        }
        
        mailSender.send(message);
    }
}
