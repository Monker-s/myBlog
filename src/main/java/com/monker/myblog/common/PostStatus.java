package com.monker.myblog.common;

/**
 * 文件用途：定义文章状态枚举。
 * 作用说明：供文章实体、文章服务和后台发布流程统一识别文章的生命周期。
 */
public enum PostStatus {

    /** 草稿状态。 */
    DRAFT(0),
    /** 已发布状态。 */
    PUBLISHED(1),
    /** 隐藏状态。 */
    HIDDEN(2);

    private final int code;

    /**
     * 函数用途：根据业务码创建文章状态枚举。
     *
     * @param code 状态码
     */
    PostStatus(int code) {
        this.code = code;
    }

    /**
     * 函数用途：返回枚举对应的业务状态码。
     *
     * @return 文章状态码
     */
    public int getCode() {
        return code;
    }
}
