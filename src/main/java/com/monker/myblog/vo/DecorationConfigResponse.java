package com.monker.myblog.vo;

import java.util.Map;

/**
 * 文件用途：站点装扮配置响应对象。
 * 关联接口：DecorationController.getActiveConfig。
 * 作用说明：返回当前生效的装扮版本标识与具体配置内容。
 *
 * @param versionTag 当前启用的装扮版本号
 * @param config 装扮配置明细
 */
public record DecorationConfigResponse(
        String versionTag,
        Map<String, Object> config
) {
}
