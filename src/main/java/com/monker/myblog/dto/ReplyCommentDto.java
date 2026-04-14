package com.monker.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文件用途：评论回复参数。
 * 关联接口：CommentReplyController.reply。
 * 作用说明：接收对某条评论发起回复时提交的正文内容。
 *
 * @param content 回复正文内容
 */
public record ReplyCommentDto(
        @NotBlank(message = "回复内容不能为空")
        @Size(max = 2000, message = "回复内容不能超过2000个字符")
        String content
) {
}
