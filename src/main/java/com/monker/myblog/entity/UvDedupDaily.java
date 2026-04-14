package com.monker.myblog.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 鏂囦欢鐢ㄩ€旓細UV 鍘婚噸鏄庣粏瀹炰綋銆? * 鏁版嵁搴撹〃锛歶v_dedup_daily銆? * 瀵瑰簲鎺ュ彛锛氭棤鐩存帴瀵瑰鎺ュ彛锛岀敱鍚庡彴缁熻鎺ュ彛闂存帴浣跨敤銆? * 瀵瑰簲鏈嶅姟涓庤闂眰锛歋tatsService銆丼tatsServiceImpl銆乁vDedupDailyMapper銆? */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UvDedupDaily {
    private Long id;
    private LocalDate statDate;
    private String ipPrefixHash;
    private String uaHash;
    private String visitorHash;
    private LocalDateTime createdAt;
}



