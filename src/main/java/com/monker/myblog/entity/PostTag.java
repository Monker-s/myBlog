package com.monker.myblog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 文件用途：文章与标签关联实体。
 * 数据库表：post_tags。
 * 对应接口：无直接对外接口，由文章管理能力间接维护。
 * 对应服务与访问层：PostService、PostServiceImpl、PostTagMapper。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("post_tags")
public class PostTag {

    private Long postId;

    private Long tagId;
}
