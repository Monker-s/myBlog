package com.monker.myblog.util;

import com.monker.myblog.dto.UserDto;
import org.springframework.stereotype.Component;
//当用户登录时，会将用户的信息记录
public class UserHolder {
    private static final ThreadLocal<UserDto> thread1 = new ThreadLocal<>();
    //获取用户id
    public static UserDto getUserId() {return thread1.get();}
    //记录
    public static void setUserId(UserDto user) {thread1.set(user);}
    //删除
    public static void removeUserId() {thread1.remove();}
}
