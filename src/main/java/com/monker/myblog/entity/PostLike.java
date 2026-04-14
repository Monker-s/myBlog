package com.monker.myblog.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 鏂囦欢鐢ㄩ€旓細鏂囩珷鐐硅禐璁板綍瀹炰綋銆? * 鏁版嵁搴撹〃锛歱ost_likes銆? * 瀵瑰簲鎺ュ彛锛歅ostController 鐨勭偣璧炴帴鍙ｃ€? * 瀵瑰簲鏈嶅姟涓庤闂眰锛歅ostService銆丳ostServiceImpl銆丳ostLikeMapper銆? */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {
    private Long id;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}



