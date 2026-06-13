package com.vtmer.microteachingquality.review;

import com.vtmer.microteachingquality.service.ClazzReviewEvaluationFileService;
import com.vtmer.microteachingquality.service.ClazzReviewEvaluationRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Colin_Knight
 * @create 2023/12/12 15:55
 */
@SpringBootTest
public class ReviewTest {

    @Autowired
    private ClazzReviewEvaluationRecordService reviewEvaluationRecordService;

    @Autowired
    private ClazzReviewEvaluationFileService clazzReviewEvaluationFileService;

    @Test
    public void test() {

        System.out.println(reviewEvaluationRecordService.getClazzReviewInfo(1));
        System.out.println(clazzReviewEvaluationFileService.getUploadedFilesInfo(1, 1705045975292182528L));
    }


    @Test
    public void getReviewInfo() {


    }


}
