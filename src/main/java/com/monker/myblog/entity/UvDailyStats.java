package com.monker.myblog.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 鏂囦欢鐢ㄩ€旓細姣忔棩 UV/PV 鑱氬悎缁熻瀹炰綋銆? * 鏁版嵁搴撹〃锛歶v_daily_stats銆? * 瀵瑰簲鎺ュ彛锛欰dminStatsController銆? * 瀵瑰簲鏈嶅姟涓庤闂眰锛歋tatsService銆丼tatsServiceImpl銆乁vDailyStatsMapper銆? */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UvDailyStats {
    private LocalDate statDate;
    private Integer uvCount;
    private Integer pvCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



