package com.monker.myblog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件用途：文章列表查询参数。
 * 关联接口：PostController.list、AdminPostController.list。
 * 作用说明：承接文章列表页的筛选、排序与分页条件。
 */
@Getter
@Setter
public class PostListDto {

    /** 关键字搜索条件，可匹配标题或摘要。 */
    private String keyword;

    /** 分类筛选条件，对应 category_id。 */
    private Long categoryId;
    
    /**
     * 兼容前端的 category 参数名
     * @param category 分类ID
     */
    public void setCategory(Long category) {
        this.categoryId = category;
    }
    
    /**
     * 获取 category（与 getCategoryId 相同）
     * @return 分类ID
     */
    public Long getCategory() {
        return this.categoryId;
    }

    /** 标签筛选条件，用于多标签组合过滤。 */
    private List<Long> tagIds;

    /** 排序方式，默认按最新发布时间排序。 */
    private String sort = "latest";

    /** 是否只查看置顶文章。 */
    private Boolean pinned;

    /** 页码，最小值为 1。 */
    @Min(value = 1, message = "页码最小为 1")
    private Integer page = 1;

    /** 每页条数，限制在 1 到 100 之间。 */
    @Min(value = 1, message = "每页数量最少为 1")
    @Max(value = 100, message = "每页数量最多为 100")
    private Integer size = 10;
}
