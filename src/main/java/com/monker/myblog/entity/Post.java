package com.monker.myblog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 文件用途：文章主实体。
 * 数据库表：posts。
 * 对应接口：PostController、AdminPostController。
 * 对应服务与访问层：PostService、PostServiceImpl、PostMapper。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("posts")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverUrl;
    private String contentHtml;
    private String tocJson;
    private Long authorId;
    private Long categoryId;
    private Integer status;
    private Boolean pinned;
    private LocalDateTime pinnedAt;
    private LocalDateTime publishedAt;
    private Integer likeCount;
    private Integer viewCount;
    private Integer commentCount;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



