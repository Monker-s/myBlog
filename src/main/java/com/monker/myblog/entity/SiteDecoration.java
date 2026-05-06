package com.monker.myblog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 文件用途：站点装饰配置实体。
 * 数据库表：site_decorations。
 * 对应接口：DecorationController。
 * 对应服务与访问层：DecorationService、DecorationServiceImpl、SiteDecorationMapper。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("site_decorations")
public class SiteDecoration {
    private Long id;
    
    @TableField("config_json")
    private String configJson;
    
    @TableField("is_active")
    private Boolean active;
    
    @TableField("version_tag")
    private String versionTag;
    
    @TableField("banner_image")
    private String bannerImage;
    
    @TableField("banner_sub")
    private String bannerSub;
    
    @TableField("banner_tag")
    private String bannerTag;
    
    @TableField("hero_image")
    private String heroImage;
    
    @TableField("hero_eyebrow")
    private String heroEyebrow;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}



