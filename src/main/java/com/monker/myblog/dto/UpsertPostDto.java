package com.monker.myblog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 文件用途：文章新增或编辑参数。
 * 关联接口：AdminPostController.create、AdminPostController.update。
 * 作用说明：统一承接后台文章保存时需要的主体内容、分类、标签和状态信息。
 *
 * @param title 文章标题
 * @param slug 文章访问别名
 * @param summary 文章摘要
 * @param coverUrl 封面图片地址
 * @param contentHtml 正文 HTML 内容
 * @param tocJson 目录结构 JSON
 * @param categoryId 分类主键
 * @param status 文章状态编码
 * @param pinned 是否置顶
 * @param tagIds 标签主键列表
 */
public record UpsertPostDto(
        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题长度不能超过200个字符")
        String title,
        @NotBlank(message = "slug 不能为空")
        @Size(max = 200, message = "slug 长度不能超过200个字符")
        String slug,
        @Size(max = 500, message = "摘要长度不能超过500个字符")
        String summary,
        @Size(max = 500, message = "封面地址长度不能超过500个字符")
        String coverUrl,
        @NotBlank(message = "正文内容不能为空")
        String contentHtml,
        String tocJson,
        Long categoryId,
        @NotNull(message = "文章状态不能为空")
        Integer status,
        Boolean pinned,
        List<Long> tagIds
) {
}
