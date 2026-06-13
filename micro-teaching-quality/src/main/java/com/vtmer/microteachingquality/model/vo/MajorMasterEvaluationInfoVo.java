package com.vtmer.microteachingquality.model.vo;

import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.Major;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Colin_Knight
 * @create 2023/6/29 19:58
 */
@Data
@NoArgsConstructor
public class MajorMasterEvaluationInfoVo {

    private String username;

    private String majorName;

    private String result;

    private String opinion;

    public MajorMasterEvaluationInfoVo(User user, Major major, String result, String opinion) {

        this.username = user.getRealName();
        this.majorName = major.getName();
        this.result = result;
        this.opinion = opinion;

    }

}
