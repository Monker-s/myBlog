package com.monker.myblog.controller;

import com.monker.myblog.common.PageResponse;
import com.monker.myblog.common.Result;
import com.monker.myblog.dto.UpdateUserInfoDto;
import com.monker.myblog.dto.UpdateUserStatusDto;
import com.monker.myblog.dto.UserDto;
import com.monker.myblog.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文件用途：提供后台管理用户相关接口。
 * 作用说明：处理用户列表查询等后台管理功能。
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminUserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 函数用途：分页查询用户列表。
     *
     * @param page 页码，从1开始，默认为1
     * @param size 每页大小，默认为10
     * @return 用户分页列表
     */
    @GetMapping("/users")
    public Result<PageResponse<UserDto>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("管理员查询用户列表, page={}, size={}", page, size);
        return Result.success(userService.listUsers(page, size));
    }
    
    /**
     * 函数用途：更新用户状态。
     *
     * @param id 用户ID
     * @param request 更新请求参数
     * @return 更新后的用户信息
     */
    @PutMapping("/users/{id}/status")
    public Result<UserDto> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateUserStatusDto request) {
        log.info("管理员更新用户状态, id={}, status={}", id, request.getStatus());
        UserDto updatedUser = userService.updateUserStatus(id, request.getStatus());
        return Result.success(updatedUser);
    }
    
    /**
     * 函数用途：更新用户信息。
     *
     * @param id 用户ID
     * @param request 更新请求参数
     * @return 更新后的用户信息
     */
    @PutMapping("/users/{id}")
    public Result<UserDto> updateUserInfo(
            @PathVariable Long id,
            @RequestBody UpdateUserInfoDto request) {
        log.info("管理员更新用户信息, id={}, username={}, email={}", 
                id, request.getUsername(), request.getEmail());
        UserDto updatedUser = userService.updateUserInfo(id, request);
        return Result.success(updatedUser);
    }
}
