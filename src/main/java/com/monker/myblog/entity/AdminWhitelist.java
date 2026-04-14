package com.monker.myblog.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 鏂囦欢鐢ㄩ€旓細绠＄悊鍛樼櫧鍚嶅崟瀹炰綋銆? * 鏁版嵁搴撹〃锛歛dmin_whitelist銆? * 瀵瑰簲鎺ュ彛锛氭棤鐩存帴瀵瑰鎺ュ彛锛屼富瑕佹湇鍔′簬 AuthController 瀵瑰簲鐨勮璇侀€昏緫銆? * 瀵瑰簲鏈嶅姟涓庤闂眰锛欰uthService銆丄uthServiceImpl銆丄dminWhitelistMapper銆? */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWhitelist {
    private Long id;
    private Long userId;
    private LocalDateTime grantedAt;
}



