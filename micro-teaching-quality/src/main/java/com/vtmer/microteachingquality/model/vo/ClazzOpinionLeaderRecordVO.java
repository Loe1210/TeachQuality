package com.vtmer.microteachingquality.model.vo;

import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.Clazz;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzOpinionRecord;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Colin_Knight
 * @create 2023/6/29 13:36
 */
@Data
@NoArgsConstructor
public class ClazzOpinionLeaderRecordVO {

    private String clazzName;
    private String username;

    private String evaluationOpinion;

    private String remark;

    public ClazzOpinionLeaderRecordVO(Clazz clazz, User user, String evaluationOption, ClazzOpinionRecord clazzOpinionRecord) {
        this.clazzName = clazz.getName();
        this.username = user.getRealName();
        this.evaluationOpinion = evaluationOption;
        this.remark = clazzOpinionRecord.getClazzRemark();
    }


}
