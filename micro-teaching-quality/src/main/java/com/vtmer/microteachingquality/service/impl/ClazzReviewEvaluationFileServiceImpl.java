package com.vtmer.microteachingquality.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.domain.ClazzReviewEvaluationFile;
import com.vtmer.microteachingquality.mapper.ClazzEvaluationProcessMapper;
import com.vtmer.microteachingquality.mapper.ClazzFileMapper;
import com.vtmer.microteachingquality.mapper.ClazzReviewEvaluationFileMapper;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzFile;
import com.vtmer.microteachingquality.model.vo.GetUploadedFilesResult;
import com.vtmer.microteachingquality.service.ClazzReviewEvaluationFileService;
import com.vtmer.microteachingquality.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cedirc_Adie
 * @description 针对表【clazz_review_evaluation_file】的数据库操作Service实现
 * @createDate 2023-12-11 21:44:32
 */
@Service
@Slf4j
public class ClazzReviewEvaluationFileServiceImpl extends ServiceImpl<ClazzReviewEvaluationFileMapper, ClazzReviewEvaluationFile>
        implements ClazzReviewEvaluationFileService {


    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);
    @Value("${clazz.path}")
    private String clazzPath;
    @Autowired
    private ClazzFileMapper clazzFileMapper;
    @Autowired
    private ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;

    /**
     * 判断文件是否存在
     *
     * @param reviewEvaluationId 复评id
     * @param filename           文件名
     * @return ClazzReviewEvaluationFile 对象
     */
    private ClazzReviewEvaluationFile existFile(long reviewEvaluationId, String filename) {
        return baseMapper.selectOne(
                new QueryWrapper<ClazzReviewEvaluationFile>()
                        .eq("review_evaluation_id", reviewEvaluationId)
                        .eq("file_name", filename)

        );
    }

    @Override
    public Integer deleteUploadedFile(int id) {
        return baseMapper.deleteById(id);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean principalUploadMaterial(MultipartFile file, Long reviewEvaluationId) throws IOException {
        // 获取当前登陆用户(课程负责人)对象
        User user = UserUtil.getCurrentUser();

        if (StrUtil.isBlank(file.getOriginalFilename())) {
            throw new CustomException("请选择需要上传的文件");
        }

        // 目录添加uuid防重
        String uuid = IdUtil.simpleUUID();
        File path = new File(clazzPath + File.separator + uuid);
        if (!path.isDirectory()) {
            path.mkdirs();
        }

        //生成文件路径
        String filePath = clazzPath + File.separator + uuid + File.separator + file.getOriginalFilename();

        // 加密文件名
        String encryptFileName = aes.encryptHex(uuid + File.separator + file.getOriginalFilename());

        //该评审流程的负责人登录，查询是否已经有此报告了

        ClazzReviewEvaluationFile reviewFile = existFile(reviewEvaluationId, file.getOriginalFilename());

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
        ClazzReviewEvaluationFile clazzReviewEvaluationFile = new ClazzReviewEvaluationFile();


        clazzReviewEvaluationFile.setReviewEvaluationId(reviewEvaluationId);
        clazzReviewEvaluationFile.setFileName(file.getOriginalFilename());
        clazzReviewEvaluationFile.setPath(encryptFileName);


        //TODO 上传文件不会插入id
        if (baseMapper.insert(clazzReviewEvaluationFile) > 0) {
            //rocketMQTemplate.getProducer().send(new Message(ClazzEvaluationTopic.CLAZZ_EVALUATION, ClazzEvaluationTopic.PRINCIPAL_UPLOAD, UserMessageDTO.newInstance(new UserMessageDTO<>(user.getId(), evaluationId))));
            FileUtil.writeBytes(file.getBytes(), filePath);
        }

        log.info("用户id {} {} 调用，流程id：{}", user.getId(), user.getRealName(), reviewEvaluationId);
        log.info("用户 {} {} 上传复评报告成功，流程id：{},文件名为 {} ", user.getId(), user.getRealName(), reviewEvaluationId, file.getOriginalFilename());
        return true;
    }

    @Override
    public List<ClazzReviewEvaluationFile> getUploadedFilesInfo(int userId, Long evaluationId) {
        List<ClazzReviewEvaluationFile> reviewEvaluationFiles = baseMapper.selectList(new QueryWrapper<ClazzReviewEvaluationFile>().eq("review_evaluation_id", evaluationId));
        return reviewEvaluationFiles;
    }

    @Override
    public List<GetUploadedFilesResult> getUploadedAdviceFilesInfo(Long evaluationId) {
        //判断评审流程是否存在
        ClassEvaluationProcess process = clazzEvaluationProcessMapper.selectById(evaluationId);
        if (process == null) {
            throw new CustomException("评审流程不存在");
        }

        QueryWrapper<ClazzFile> clazzFileQueryWrapper = new QueryWrapper<>();
        clazzFileQueryWrapper.eq("clazz_id", process.getClazzId());
        clazzFileQueryWrapper.eq("clazz_evaluation_process_id", evaluationId);
        List<ClazzFile> fileList = clazzFileMapper.selectList(clazzFileQueryWrapper);
        return fileList.stream()
                .map(clazzFile -> new GetUploadedFilesResult(clazzFile.getId(), clazzFile.getFileName(), clazzFile.getPath(), clazzFile.getUpdateTime()))
                .collect(Collectors.toList());
    }


}




