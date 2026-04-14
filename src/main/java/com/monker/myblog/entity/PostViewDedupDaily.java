package com.monker.myblog.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 鏂囦欢鐢ㄩ€旓細鏂囩珷娴忚鍘婚噸鏄庣粏瀹炰綋銆? * 鏁版嵁搴撹〃锛歱ost_view_dedup_daily銆? * 瀵瑰簲鎺ュ彛锛歅ostController 鐨勬祻瑙堜笂鎶ユ帴鍙ｃ€? * 瀵瑰簲鏈嶅姟涓庤闂眰锛歅ostService銆丳ostServiceImpl銆丳ostViewDedupDailyMapper銆? */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostViewDedupDaily {
    private Long id;
    private Long postId;
    private LocalDate dedupDate;
    private String dedupKeyHash;
    private LocalDateTime createdAt;
    private LocalDateTime lastViewedAt;
}



