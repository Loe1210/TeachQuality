package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.domain.MajorReviewEvaluationFile;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author Cedirc_Adie
 * @description 针对表【major_review_evaluation_file】的数据库操作Service
 * @createDate 2023-12-13 20:23:48
 */
public interface MajorReviewEvaluationFileService extends IService<MajorReviewEvaluationFile> {
    Integer deleteUploadedFile(int id);

    /**
     * 上传复评报告
     *
     * @param file               复评报告
     * @param reviewEvaluationId 复评id
     * @return true 成功上传 | false 上传失败
     * @throws IOException 上传过程中出现IO流异常时，抛出IO异常
     */
    Boolean principalUploadMaterial(MultipartFile file, Long reviewEvaluationId) throws IOException;

    List<MajorReviewEvaluationFile> getUploadedFilesInfo(int userId, Long evaluationId);

    List<MajorEvaluationFile> getEvaluatedFiles(long majorId);
}
