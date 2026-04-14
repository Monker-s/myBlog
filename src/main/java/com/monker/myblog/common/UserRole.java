package com.monker.myblog.common;

/**
 * 文件用途：定义系统用户角色枚举。
 * 作用说明：供认证、权限校验、用户实体和前端角色显示统一使用。
 */
public enum UserRole {

    /** 游客角色。 */
    GUEST(0),
    /** 普通注册用户角色。 */
    USER(1),
    /** 管理员角色。 */
    ADMIN(99);

    private final int code;

    /**
     * 函数用途：根据业务码创建角色枚举。
     *
     * @param code 角色码
     */
    UserRole(int code) {
        this.code = code;
    }

    /**
     * 函数用途：返回角色对应的业务码。
     *
     * @return 角色码
     */
    public int getCode() {
        return code;
    }
}
