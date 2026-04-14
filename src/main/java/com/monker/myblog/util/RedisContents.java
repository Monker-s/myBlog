package com.monker.myblog.util;

public class RedisContents {
    public static final String POST_LIKE_USER = "post:like:set:";   //文章点赞人
    public static final String POST_LIKE_COUNT = "post:like:count:";  //文章点赞数
    public static final String POST_VIEW_COUNT = "post:view:";   //文章浏览key

    public static final String POST_DETAIL = "post:detail:";   //文章详细
    public static final String POST_LIST = "post:list:";  //文章列表未置顶的列表
    public static final String POST_LIST_PINNED = "post:list:pinned:";   //文章列表置顶的列表
    public static final String POST_COMMENT = "post:comment:";   //文章评论
}
