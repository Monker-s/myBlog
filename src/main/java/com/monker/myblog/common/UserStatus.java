package com.monker.myblog.common;

/**
 * 文件用途：定义用户状态枚举。
 * 作用说明：供用户实体和后台用户管理逻辑识别用户是否可用。
 */
public enum UserStatus {

    /** 已禁用状态。 */
    DISABLED(0),
    /** 正常启用状态。 */
    ENABLED(1);

    private final int code;

    /**
     * 函数用途：根据业务码创建用户状态枚举。
     *
     * @param code 状态码
     */
    UserStatus(int code) {
        this.code = code;
    }

    /**
     * 函数用途：返回用户状态码。
     *
     * @return 用户状态码
     */
    public int getCode() {
        return code;
    }
}
