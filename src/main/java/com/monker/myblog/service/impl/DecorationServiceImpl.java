package com.monker.myblog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monker.myblog.dto.UpdateDecorationConfigDto;
import com.monker.myblog.entity.SiteDecoration;
import com.monker.myblog.exception.BusinessException;
import com.monker.myblog.common.ResultCode;
import com.monker.myblog.mapper.SiteDecorationMapper;
import com.monker.myblog.service.DecorationService;
import com.monker.myblog.vo.DecorationConfigResponse;
import com.monker.myblog.vo.DecorationConfigFlatResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文件用途：站点装饰配置业务实现类。
 * 作用说明：提供站点装饰配置的查询和更新能力，数据持久化到 site_decorations 表。
 */
@Service
@Slf4j
public class DecorationServiceImpl implements DecorationService {

    @Autowired
    private SiteDecorationMapper siteDecorationMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 函数用途：返回当前生效的装饰配置。
     *
     * @return 站点装饰配置
     */
    @Override
    public DecorationConfigResponse getActiveDecoration() {
        // 从数据库查询激活的配置
        SiteDecoration decoration = siteDecorationMapper.findFirstByActiveTrueOrderByIdDesc();
        
        if (decoration == null) {
            // 如果没有配置，返回默认配置
            log.warn("未找到激活的站点装饰配置，返回默认配置");
            return getDefaultDecoration();
        }
        
        try {
            // 解析 config_json
            Map<String, Object> config = objectMapper.readValue(
                    decoration.getConfigJson(), 
                    Map.class
            );
            
            // 将 banner_image 字段添加到 config 中（优先使用独立字段）
            if (decoration.getBannerImage() != null && !decoration.getBannerImage().isEmpty()) {
                config.put("bannerImage", decoration.getBannerImage());
            }
            
            return new DecorationConfigResponse(
                    decoration.getVersionTag(),
                    config
            );
        } catch (JsonProcessingException e) {
            log.error("解析站点装饰配置失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "配置数据格式错误");
        }
    }

    /**
     * 函数用途：返回当前生效的装饰配置（平铺结构，用于公开接口）。
     *
     * @return 站点装饰配置（平铺结构）
     */
    public DecorationConfigFlatResponse getActiveDecorationFlat() {
        // 从数据库查询激活的配置
        SiteDecoration decoration = siteDecorationMapper.findFirstByActiveTrueOrderByIdDesc();
        
        if (decoration == null) {
            // 如果没有配置，返回默认配置
            log.warn("未找到激活的站点装饰配置，返回默认配置");
            return getDefaultDecorationFlat();
        }
        
        try {
            // 解析 config_json
            Map<String, Object> config = objectMapper.readValue(
                    decoration.getConfigJson(), 
                    Map.class
            );
            
            // 提取各个字段，提供默认值
            String siteName = getStringValue(config, "siteName", "个人知识库");
            String slogan = getStringValue(config, "slogan", "记录构建路径，而不只是结论");
            String logoText = getStringValue(config, "logoText", "KB");
            String bannerText = getStringValue(config, "bannerText", "Macragge Command Deck");
            String primary = getStringValue(config, "primaryColor", "#1e3a8a");
            String accent = getStringValue(config, "accentColor", "#d4af37");
            
            // bannerImage 优先使用独立字段
            String bannerImage = decoration.getBannerImage();
            if (bannerImage == null || bannerImage.isEmpty()) {
                bannerImage = getStringValue(config, "bannerImage", "");
            }
            
            // bannerSub 优先使用独立字段
            String bannerSub = decoration.getBannerSub();
            if (bannerSub == null || bannerSub.isEmpty()) {
                bannerSub = getStringValue(config, "bannerSub", "");
            }
            
            // bannerTag 优先使用独立字段
            String bannerTag = decoration.getBannerTag();
            if (bannerTag == null || bannerTag.isEmpty()) {
                bannerTag = getStringValue(config, "bannerTag", "");
            }
            
            // heroImage 优先使用独立字段
            String heroImage = decoration.getHeroImage();
            if (heroImage == null || heroImage.isEmpty()) {
                heroImage = getStringValue(config, "heroImage", "");
            }
            
            // heroEyebrow 优先使用独立字段
            String heroEyebrow = decoration.getHeroEyebrow();
            if (heroEyebrow == null || heroEyebrow.isEmpty()) {
                heroEyebrow = getStringValue(config, "heroEyebrow", "");
            }
            
            return new DecorationConfigFlatResponse(
                    siteName,
                    slogan,
                    logoText,
                    bannerText,
                    primary,
                    accent,
                    bannerImage,
                    bannerSub,
                    bannerTag,
                    heroImage,
                    heroEyebrow
            );
        } catch (JsonProcessingException e) {
            log.error("解析站点装饰配置失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "配置数据格式错误");
        }
    }

    /**
     * 函数用途：从 Map 中安全地获取字符串值。
     *
     * @param config 配置 Map
     * @param key 键名
     * @param defaultValue 默认值
     * @return 字符串值
     */
    private String getStringValue(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    /**
     * 函数用途：更新站点装饰配置。
     *
     * @param request 更新配置请求参数
     * @return 更新后的站点装饰配置
     */
    @Override
    @Transactional
    public DecorationConfigResponse updateDecorationConfig(UpdateDecorationConfigDto request) {
        log.info("更新站点装饰配置: siteName={}, bannerImage={}", request.siteName(), request.bannerImage());
        
        // 查询是否已有激活的配置
        SiteDecoration existing = siteDecorationMapper.findFirstByActiveTrueOrderByIdDesc();
        
        // 构建配置 Map：先加载现有配置，再覆盖新值
        Map<String, Object> config = new HashMap<>();
        if (existing != null && existing.getConfigJson() != null) {
            try {
                Map<String, Object> existingConfig = objectMapper.readValue(
                        existing.getConfigJson(), 
                        Map.class
                );
                config.putAll(existingConfig);
                log.info("加载现有配置，共 {} 个字段", existingConfig.size());
            } catch (JsonProcessingException e) {
                log.warn("解析现有配置失败，使用空配置", e);
            }
        }
        
        // 覆盖或新增字段（只更新非 null 的字段）
        if (request.siteName() != null) {
            config.put("siteName", request.siteName());
        }
        if (request.slogan() != null) {
            config.put("slogan", request.slogan());
        }
        if (request.bannerText() != null) {
            config.put("bannerText", request.bannerText());
        }
        
        // theme 为必填，如果为空则使用默认值
        String theme = request.theme();
        if (theme != null && !theme.trim().isEmpty()) {
            config.put("theme", theme);
        } else if (!config.containsKey("theme")) {
            // 如果现有配置中也没有 theme，使用默认值
            theme = "ultramarines-light";
            config.put("theme", theme);
            log.warn("未提供主题名称，使用默认主题: {}", theme);
        }
        
        if (request.primaryColor() != null) {
            config.put("primaryColor", request.primaryColor());
        }
        if (request.accentColor() != null) {
            config.put("accentColor", request.accentColor());
        }
        
        // 序列化为 JSON
        String configJson;
        try {
            configJson = objectMapper.writeValueAsString(config);
            log.info("准备保存的配置JSON: {}", configJson);
        } catch (JsonProcessingException e) {
            log.error("序列化配置失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "配置序列化失败");
        }
        
        LocalDateTime now = LocalDateTime.now();
        String versionTag = "v" + System.currentTimeMillis();
        
        SiteDecoration decoration;
        if (existing != null) {
            // 更新已有配置 - 使用 UpdateWrapper 只更新非 null 字段
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<SiteDecoration> updateWrapper = 
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
            updateWrapper.eq("id", existing.getId());
            updateWrapper.set("config_json", configJson);
            updateWrapper.set("version_tag", versionTag);
            updateWrapper.set("updated_at", now);
            
            // bannerImage 单独处理，如果传了就更新，否则不更新
            if (request.bannerImage() != null) {
                updateWrapper.set("banner_image", request.bannerImage());
                log.info("更新 bannerImage: {}", request.bannerImage());
            }
            
            // bannerSub 单独处理，如果传了就更新，否则不更新
            if (request.bannerSub() != null) {
                updateWrapper.set("banner_sub", request.bannerSub());
                log.info("更新 bannerSub: {}", request.bannerSub());
            }
            
            // bannerTag 单独处理，如果传了就更新，否则不更新
            if (request.bannerTag() != null) {
                updateWrapper.set("banner_tag", request.bannerTag());
                log.info("更新 bannerTag: {}", request.bannerTag());
            }
            
            // heroImage 单独处理，如果传了就更新，否则不更新
            if (request.heroImage() != null) {
                updateWrapper.set("hero_image", request.heroImage());
                log.info("更新 heroImage: {}", request.heroImage());
            }
            
            // heroEyebrow 单独处理，如果传了就更新，否则不更新
            if (request.heroEyebrow() != null) {
                updateWrapper.set("hero_eyebrow", request.heroEyebrow());
                log.info("更新 heroEyebrow: {}", request.heroEyebrow());
            }
            
            int rows = siteDecorationMapper.update(null, updateWrapper);
            
            // 重新查询获取最新数据
            decoration = siteDecorationMapper.findFirstByActiveTrueOrderByIdDesc();
            log.info("更新现有配置，id={}, 影响行数={}, bannerImage={}", existing.getId(), rows, decoration.getBannerImage());
        } else {
            // 创建新配置
            decoration = SiteDecoration.builder()
                    .configJson(configJson)
                    .active(true)
                    .versionTag(versionTag)
                    .bannerImage(request.bannerImage())
                    .bannerSub(request.bannerSub())
                    .bannerTag(request.bannerTag())
                    .heroImage(request.heroImage())
                    .heroEyebrow(request.heroEyebrow())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            siteDecorationMapper.insert(decoration);
            log.info("创建新配置，id={}", decoration.getId());
        }
        
        // 构建返回的 config，包含所有独立字段
        Map<String, Object> responseConfig = new HashMap<>(config);
        
        // bannerImage
        String finalBannerImage = request.bannerImage() != null ? request.bannerImage() : 
                                  (decoration.getBannerImage() != null ? decoration.getBannerImage() : null);
        if (finalBannerImage != null) {
            responseConfig.put("bannerImage", finalBannerImage);
        }
        
        // bannerSub
        String finalBannerSub = request.bannerSub() != null ? request.bannerSub() : 
                                (decoration.getBannerSub() != null ? decoration.getBannerSub() : null);
        if (finalBannerSub != null) {
            responseConfig.put("bannerSub", finalBannerSub);
        }
        
        // bannerTag
        String finalBannerTag = request.bannerTag() != null ? request.bannerTag() : 
                                (decoration.getBannerTag() != null ? decoration.getBannerTag() : null);
        if (finalBannerTag != null) {
            responseConfig.put("bannerTag", finalBannerTag);
        }
        
        // heroImage
        String finalHeroImage = request.heroImage() != null ? request.heroImage() : 
                                (decoration.getHeroImage() != null ? decoration.getHeroImage() : null);
        if (finalHeroImage != null) {
            responseConfig.put("heroImage", finalHeroImage);
        }
        
        // heroEyebrow
        String finalHeroEyebrow = request.heroEyebrow() != null ? request.heroEyebrow() : 
                                  (decoration.getHeroEyebrow() != null ? decoration.getHeroEyebrow() : null);
        if (finalHeroEyebrow != null) {
            responseConfig.put("heroEyebrow", finalHeroEyebrow);
        }
        
        log.info("返回给前端的配置: versionTag={}, config大小={}", 
                decoration.getVersionTag(), responseConfig.size());
        
        return new DecorationConfigResponse(
                decoration.getVersionTag(),
                responseConfig
        );
    }

    /**
     * 函数用途：返回默认装饰配置。
     *
     * @return 默认站点装饰配置
     */
    private DecorationConfigResponse getDefaultDecoration() {
        return new DecorationConfigResponse(
                "default-v1",
                Map.of(
                        "siteName", "My Blog",
                        "slogan", "记录生活，分享知识",
                        "bannerText", "欢迎来到我的博客",
                        "bannerImage", "",
                        "theme", "ultramarines-light",
                        "primaryColor", "#1E3A8A",
                        "accentColor", "#D4AF37"
                )
        );
    }

    /**
     * 函数用途：返回默认装饰配置（平铺结构）。
     *
     * @return 默认站点装饰配置（平铺结构）
     */
    private DecorationConfigFlatResponse getDefaultDecorationFlat() {
        return new DecorationConfigFlatResponse(
                "个人知识库",
                "记录构建路径，而不只是结论",
                "KB",
                "Macragge Command Deck",
                "#1e3a8a",
                "#d4af37",
                "",
                "",
                "",
                "",
                ""
        );
    }
}
