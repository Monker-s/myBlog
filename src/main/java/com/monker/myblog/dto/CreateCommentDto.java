package com.monker.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文件用途：文章评论创建参数。
 * 关联接口：PostCommentController.createComment。
 * 作用说明：接收前端提交的一级评论内容，并在进入业务层前完成基础校验。
 *
 * @param content 评论正文内容
 */
public record CreateCommentDto(
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 2000, message = "评论内容不能超过2000个字符")
        String content
) {
}
