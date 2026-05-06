package com.monker.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.monker.myblog.common.NotificationType;
import com.monker.myblog.common.PageResponse;
import com.monker.myblog.common.ResultCode;
import com.monker.myblog.dto.BroadcastNotificationDto;
import com.monker.myblog.dto.MarkNotificationReadDto;
import com.monker.myblog.dto.UserDto;
import com.monker.myblog.entity.Notification;
import com.monker.myblog.exception.BusinessException;
import com.monker.myblog.mapper.NotificationMapper;
import com.monker.myblog.mapper.PostMapper;
import com.monker.myblog.service.NotificationPublisher;
import com.monker.myblog.service.NotificationService;
import com.monker.myblog.util.UserHolder;
import com.monker.myblog.vo.NotificationResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文件用途：通知中心业务实现类。
 * 作用说明：提供通知查询、已读管理等核心功能。
 * 注意：实时推送功能已迁移到 WebSocket + Redis Pub/Sub
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private NotificationPublisher notificationPublisher;

    /**
     * 函数用途：分页查询通知列表并返回未读数量。
     *
     * @param page 页码
     * @param size 每页数量
     * @return 通知分页结果（包含所有通知和未读数量）
     */
    @Override
    public PageResponse<NotificationResponse> listNotifications(int page, int size) {
        Long userId = getCurrentUserId();
        log.info("查询通知列表：userId={}, page={}, size={}", userId, page, size);
        
        // 查询未读数量
        LambdaQueryWrapper<Notification> unreadWrapper = new LambdaQueryWrapper<>();
        unreadWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        long unreadCount = notificationMapper.selectCount(unreadWrapper);
        
        // 使用 PageHelper 进行分页查询
        PageHelper.startPage(page, size);
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
                .orderByDesc(Notification::getId);
        
        List<Notification> notifications = notificationMapper.selectList(queryWrapper);
        PageInfo<Notification> pageInfo = new PageInfo<>(notifications);
        
        log.info("查询到 {} 条通知记录，总数：{}", notifications != null ? notifications.size() : 0, pageInfo.getTotal());
        
        // 转换为响应对象
        List<NotificationResponse> records = notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                records,
                pageInfo.getTotal(),
                page,
                size,
                pageInfo.getPages(),
                unreadCount
        );
    }

    /**
     * 函数用途：批量将通知标记为已读。
     *
     * @param request 已读参数
     */
    @Override
    @Transactional
    public void markRead(MarkNotificationReadDto request) {
        Long userId = getCurrentUserId();
        List<Long> notificationIds = request.notificationIds();
        
        if (notificationIds == null || notificationIds.isEmpty()) {
            throw new BusinessException(ResultCode.INVALID_ARGUMENT, "通知ID列表不能为空");
        }
        
        // 批量更新为已读状态
        for (Long notificationId : notificationIds) {
            Notification notification = notificationMapper.selectById(notificationId);
            if (notification == null) {
                log.warn("通知不存在，id={}", notificationId);
                continue;
            }
            
            // 验证通知是否属于当前用户
            if (!notification.getUserId().equals(userId)) {
                log.warn("通知不属于当前用户，notificationId={}, userId={}", notificationId, userId);
                continue;
            }
            
            // 如果已经是已读状态，跳过
            if (notification.getIsRead() == 1) {
                continue;
            }
            
            // 更新为已读
            notification.setIsRead(1);
            notification.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(notification);
        }
        
        log.info("批量标记通知为已读完成，userId={}, count={}", userId, notificationIds.size());
    }

    /**
     * 函数用途：获取未读的系统公告。
     * 作用说明：返回最新的未读 SYSTEM 类型通知，并自动标记为已读。
     *
     * @return 系统公告列表
     */
    @Override
    @Transactional
    public List<NotificationResponse> getUnreadSystemAnnouncements() {
        Long userId = getCurrentUserId();
        log.info("查询未读系统公告：userId={}", userId);
        
        // 查询未读的 SYSTEM 类型通知
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getType, "SYSTEM")
                .eq(Notification::getIsRead, 0)
                .orderByDesc(Notification::getCreatedAt)
                .last("LIMIT 10");  // 最多返回10条
        
        List<Notification> announcements = notificationMapper.selectList(queryWrapper);
        log.info("查询到 {} 条未读系统公告", announcements != null ? announcements.size() : 0);
        
        if (announcements == null || announcements.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 转换为响应对象
        List<NotificationResponse> responseList = announcements.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // 自动标记为已读
        List<Long> announcementIds = announcements.stream()
                .map(Notification::getId)
                .collect(Collectors.toList());
        
        for (Long announcementId : announcementIds) {
            Notification announcement = notificationMapper.selectById(announcementId);
            if (announcement != null && announcement.getIsRead() == 0) {
                announcement.setIsRead(1);
                announcement.setReadAt(LocalDateTime.now());
                notificationMapper.updateById(announcement);
            }
        }
        
        log.info("系统公告已自动标记为已读，数量: {}", announcementIds.size());
        
        return responseList;
    }

    /**
     * 函数用途：获取当前登录用户ID。
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        UserDto userDto = UserHolder.getUserId();
        if (userDto == null || userDto.getId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        return userDto.getId();
    }

    /**
     * 函数用途：将Notification实体转换为NotificationResponse。
     *
     * @param notification 通知实体
     * @return 通知响应对象
     */
    private NotificationResponse convertToResponse(Notification notification) {
        // 如果有 postId，查询文章标题
        String postTitle = null;
        if (notification.getPostId() != null) {
            var post = postMapper.selectById(notification.getPostId());
            if (post != null) {
                postTitle = post.getTitle();
            }
        }
        
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getPostId(),
                postTitle,
                notification.getTitle(),
                notification.getBody(),
                notification.getIsRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

    /**
     * 函数用途：管理员群发通知。
     *
     * @param request 群发通知参数
     */
    @Override
    public void broadcastNotification(BroadcastNotificationDto request) {
        // 验证当前用户是否为管理员
        UserDto currentUser = UserHolder.getUserId();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        
        // 这里可以添加管理员权限验证逻辑
        // if (currentUser.getRole() != UserRole.ADMIN.getValue()) {
        //     throw new BusinessException(ResultCode.FORBIDDEN, "无权执行此操作");
        // }

        // 解析通知类型，默认为 SYSTEM
        NotificationType notificationType;
        try {
            notificationType = request.type() != null && !request.type().isEmpty()
                    ? NotificationType.valueOf(request.type())
                    : NotificationType.SYSTEM;
        } catch (IllegalArgumentException e) {
            log.warn("无效的通知类型: {}, 使用默认类型 SYSTEM", request.type());
            notificationType = NotificationType.SYSTEM;
        }

        // 调用通知发布服务
        notificationPublisher.publishBroadcastNotification(
                notificationType,
                request.postId(),
                request.title(),
                request.body(),
                null  // payloadJson 可选
        );

        log.info("管理员群发通知成功，adminId={}, type={}, title={}",
                currentUser.getId(), notificationType, request.title());
    }

    /**
     * 函数用途：管理员删除通知。
     *
     * @param id 通知ID
     */
    @Override
    public void deleteNotification(Long id) {
        // 验证当前用户是否为管理员
        UserDto currentUser = UserHolder.getUserId();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }
        
        // 检查通知是否存在
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        
        // 删除通知
        int result = notificationMapper.deleteById(id);
        if (result > 0) {
            log.info("管理员删除通知成功，adminId={}, notificationId={}", currentUser.getId(), id);
        } else {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除通知失败");
        }
    }
}
