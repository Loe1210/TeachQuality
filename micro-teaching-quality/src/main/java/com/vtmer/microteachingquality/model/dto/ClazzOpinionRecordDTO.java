package com.vtmer.microteachingquality.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Hung
 * @date 2022/4/23 22:57
 */
@Data
public class ClazzOpinionRecordDTO {
    Integer userId;
    LocalDateTime updateTime;
}
