package com.vtmer.microteachingquality.model.bo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ClazzEvaluationToWordBO {
    String name;
    String date;
    String result;
    String advantage;
    String problem;
    String advice;
    String leader;
    String members;
}
