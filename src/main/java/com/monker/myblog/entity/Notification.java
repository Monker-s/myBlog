package com.monker.myblog.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 鏂囦欢鐢ㄩ€旓細閫氱煡涓績瀹炰綋銆?
 * 鏁版嵁搴撹〃锛歯otifications銆?
 * 瀵瑰簲鎺ュ彛锛歂otificationController銆?
 * 瀵瑰簲鏈嶅姟涓庤闂眰锛歂otificationService銆丯otificationServiceImpl銆丯otificationMapper銆?
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long id;
    private Long userId;
    private String type;
    private Long postId;
    private String contentHash;
    private String title;
    private String body;
    private Boolean read;
    private LocalDateTime readAt;
    private String payloadJson;
    private LocalDateTime createdAt;
}



