package com.monker.myblog.dto;

import lombok.Data;

/**
 * 文件用途：定义更新用户信息的请求参数。
 * 作用说明：用于接收前端传递的用户信息更新请求。
 */
@Data
public class UpdateUserInfoDto {
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 密码（可选，不传则不修改）
     */
    private String password;
    
    /**
     * 头像 URL
     */
    private String icon;
    
    /**
     * 角色
     */
    private Integer role;
}
