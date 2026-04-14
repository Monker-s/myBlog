package com.monker.myblog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 文件用途：文章评论实体。
 * 数据库表：comments。
 * 对应接口：PostCommentController、CommentReplyController。
 * 对应服务与访问层：CommentService、CommentServiceImpl、CommentMapper。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("comments")
public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private Integer depth;
    private String content;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



