package com.monker.myblog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 鏂囦欢鐢ㄩ€旓細鏂囩珷涓庢爣绛惧叧鑱斿疄浣撱€? * 鏁版嵁搴撹〃锛歱ost_tags銆? * 瀵瑰簲鎺ュ彛锛氭棤鐩存帴瀵瑰鎺ュ彛锛岀敱鏂囩珷绠＄悊鑳藉姏闂存帴缁存姢銆? * 瀵瑰簲鏈嶅姟涓庤闂眰锛歅ostService銆丳ostServiceImpl銆丳ostTagMapper銆? */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostTag {

    private Long postId;

    private Long tagId;
}
