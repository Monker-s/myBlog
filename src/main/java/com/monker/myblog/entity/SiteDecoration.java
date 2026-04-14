package com.monker.myblog.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 鏂囦欢鐢ㄩ€旓細绔欑偣瑁呴グ閰嶇疆瀹炰綋銆?
 * 鏁版嵁搴撹〃锛歴ite_decorations銆?
 * 瀵瑰簲鎺ュ彛锛欴ecorationController銆?
 * 瀵瑰簲鏈嶅姟涓庤闂眰锛欴ecorationService銆丏ecorationServiceImpl銆丼iteDecorationMapper銆?
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteDecoration {
    private Long id;
    private String configJson;
    private Boolean active;
    private String versionTag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



