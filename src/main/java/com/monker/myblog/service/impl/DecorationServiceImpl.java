package com.monker.myblog.service.impl;

import com.monker.myblog.service.DecorationService;
import com.monker.myblog.vo.DecorationConfigResponse;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 文件用途：站点装饰配置业务实现类。
 * 作用说明：当前返回骨架版站点装饰配置，后续会对接 `site_decorations` 表中的动态配置数据。
 */
@Service
public class DecorationServiceImpl implements DecorationService {

    /**
     * 函数用途：返回当前生效的装饰配置。
     *
     * @return 站点装饰配置
     */
    @Override
    public DecorationConfigResponse getActiveDecoration() {
        return new DecorationConfigResponse(
                "skeleton-v1",
                Map.of(
                        "theme", "ultramarines-light",
                        "primaryColor", "#1E3A8A",
                        "accentColor", "#D4AF37"
                )
        );
    }
}
