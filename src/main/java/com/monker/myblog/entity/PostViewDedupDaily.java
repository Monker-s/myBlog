package com.monker.myblog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 文件用途：文章浏览去重明细实体。
 * 数据库表：post_view_dedup_daily。
 * 对应接口：PostController 的浏览上报接口。
 * 对应服务与访问层：PostService、PostServiceImpl、PostViewDedupDailyMapper。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("post_view_dedup_daily")
public class PostViewDedupDaily {
    private Long id;
    private Long postId;
    private LocalDate dedupDate;
    private String dedupKeyHash;
    private LocalDateTime createdAt;
    private LocalDateTime lastViewedAt;
}



