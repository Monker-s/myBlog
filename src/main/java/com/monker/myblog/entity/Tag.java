package com.monker.myblog.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 鏂囦欢鐢ㄩ€旓細鏂囩珷鏍囩瀹炰綋銆? * 鏁版嵁搴撹〃锛歵ags銆? * 瀵瑰簲鎺ュ彛锛歅ostController銆丄dminPostController銆? * 瀵瑰簲鏈嶅姟涓庤闂眰锛歅ostService銆丳ostServiceImpl銆乀agMapper銆? */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    private Long id;
    private String name;
    private String slug;
    private Integer postCount;
    private LocalDateTime createdAt;
}



