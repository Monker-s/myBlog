package com.monker.myblog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 文件用途：文章标签实体。
 * 数据库表：tags。
 * 对应接口：PostController、AdminPostController、TagController。
 * 对应服务与访问层：PostService、PostServiceImpl、TagService、TagMapper。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tags")
public class Tag {
    private Long id;
    private String name;
    private String slug;
    private Integer postCount;
    private LocalDateTime createdAt;
}



