package com.monker.myblog.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PostCommentDto {
    private Long postId;
    /** 页码，最小值为 1。 */
    @Min(value = 1, message = "页码最小为 1")
    private Integer page = 1;

    /** 每页条数，限制在 1 到 100 之间。 */
    @Min(value = 1, message = "每页数量最少为 1")
    @Max(value = 100, message = "每页数量最多为 100")
    private Integer pageSize = 10;
}
