package com.vtmer.microteachingquality.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.domain.MajorReviewEvaluationFile;
import com.vtmer.microteachingquality.mapper.MajorEvaluationFileMapper;
import com.vtmer.microteachingquality.mapper.MajorReviewEvaluationFileMapper;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationFile;
import com.vtmer.microteachingquality.service.MajorReviewEvaluationFileService;
import com.vtmer.microteachingquality.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Cedirc_Adie
 * @description 针对表【major_review_evaluation_file】的数据库操作Service实现
 * @createDate 2023-12-13 20:23:48
 */
@Service
@Slf4j
public class MajorReviewEvaluationFileServiceImpl extends ServiceImpl<MajorReviewEvaluationFileMapper, MajorReviewEvaluationFile>
        implements MajorReviewEvaluationFileService {


    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();


    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);

    @Value("${report.path}")
    private String reportPath;
    @Autowired
    MajorEvaluationFileMapper majorEvaluationFileMapper;


    private MajorReviewEvaluationFile existFile(long reviewEvaluationId, String filename) {
        return baseMapper.selectOne(
                new QueryWrapper<MajorReviewEvaluationFile>()
                        .eq("review_evaluation_id", reviewEvaluationId)
                        .eq("file_name", filename)

        );
    }


    @Override
    public Integer deleteUploadedFile(int id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public Boolean principalUploadMaterial(MultipartFile file, Long reviewEvaluationId) throws IOException {
        // 获取当前登陆用户(课程负责人)对象
        User user = UserUtil.getCurrentUser();

        if (StrUtil.isBlank(file.getOriginalFilename())) {
            throw new CustomException("请选择需要上传的文件");
        }

        // 目录添加uuid防重
        String uuid = IdUtil.simpleUUID();
        File path = new File(reportPath + File.separator + uuid);
        if (!path.isDirectory()) {
            path.mkdirs();
        }

        //生成文件路径
        String filePath = reportPath + File.separator + uuid + File.separator + file.getOriginalFilename();

        // 加密文件名
        String encryptFileName = aes.encryptHex(uuid + File.separator + file.getOriginalFilename());

        //该评审流程的负责人登录，查询是否已经有此报告了

        MajorReviewEvaluationFile reviewFile = existFile(reviewEvaluationId, file.getOriginalFilename());

        if (reviewFile != null) {
            //有数据库记录删除文件
            //todo 删除数据库记录
            Integer isDelete = deleteUploadedFile(reviewFile.getId());
            if (isDelete == 0) {
                throw new CustomException("删除数据库记录失败");
            }
//            throw new CustomException(EvaluationProcessStatus.FILE_EXIST);


        }

        //clazzFile == null说明数据库中没有还没有这个负责人上传该课程评审的相同报告
        MajorReviewEvaluationFile majorReviewEvaluationFile = new MajorReviewEvaluationFile();

        majorReviewEvaluationFile.setReviewEvaluationId(reviewEvaluationId);
        majorReviewEvaluationFile.setFileName(file.getOriginalFilename());
        majorReviewEvaluationFile.setPath(encryptFileName);

        //TODO 上传文件不会插入id
        if (baseMapper.insert(majorReviewEvaluationFile) > 0) {
            //rocketMQTemplate.getProducer().send(new Message(ClazzEvaluationTopic.CLAZZ_EVALUATION, ClazzEvaluationTopic.PRINCIPAL_UPLOAD, UserMessageDTO.newInstance(new UserMessageDTO<>(user.getId(), evaluationId))));
            FileUtil.writeBytes(file.getBytes(), filePath);
        }

        log.info("用户id {} {} 调用，流程id：{}", user.getId(), user.getRealName(), reviewEvaluationId);
        log.info("用户 {} {} 上传复评报告成功，流程id：{},文件名为 {} ", user.getId(), user.getRealName(), reviewEvaluationId, file.getOriginalFilename());
        return true;
    }

    @Override
    public List<MajorReviewEvaluationFile> getUploadedFilesInfo(int userId, Long evaluationId) {
        List<MajorReviewEvaluationFile> reviewEvaluationId = baseMapper.selectList(new QueryWrapper<MajorReviewEvaluationFile>().eq("review_evaluation_id", evaluationId));
        return reviewEvaluationId;
    }

    @Override
    public List<MajorEvaluationFile> getEvaluatedFiles(long majorId) {
        QueryWrapper<MajorEvaluationFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_id", majorId);
        return majorEvaluationFileMapper.selectList(queryWrapper);
    }
}




