package com.vtmer.microteachingquality.model.bo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MajorEvaluationToWordBO {
    String collegeAndMajor;
    String result;
    String opinion;
    String leader;
    String members;
    String date;

}
