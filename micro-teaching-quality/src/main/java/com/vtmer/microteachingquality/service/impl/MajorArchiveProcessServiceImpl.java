package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus;
import com.vtmer.microteachingquality.common.component.FileObjectReferenceService;
import com.vtmer.microteachingquality.common.exception.CommonRuntimeException;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.bo.CreateNewMajorBatchVO;
import com.vtmer.microteachingquality.model.bo.MajorArchiveReviewBO;
import com.vtmer.microteachingquality.model.dto.file.FileServiceFileObjectDTO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveBatch;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveFile;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveOpinion;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveTemplateFile;
import com.vtmer.microteachingquality.model.vo.*;
import com.vtmer.microteachingquality.service.MajorArchiveProcessService;
import com.vtmer.microteachingquality.util.UserUtil;
import lombok.SneakyThrows;
import cn.hutool.core.util.StrUtil;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant.*;

/**
 * @author HJW
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MajorArchiveProcessServiceImpl extends ServiceImpl<MajorArchiveBatchMapper, MajorArchiveBatch> implements MajorArchiveProcessService, EvaluationProcessStatus {
    @Resource
    private MajorArchiveBatchMapper majorArchiveBatchMapper;
    @Resource
    private MajorArchiveMapper majorArchiveMapper;
    @Resource
    private MajorArchiveGroupMapper majorArchiveGroupMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private MajorArchiveTemplateFileMapper majorArchiveTemplateFileMapper;
    @Resource
    private MajorMapper majorMapper;
    @Resource
    private MajorArchiveFileMapper majorArchiveFileMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private MajorArchiveOpinionMapper majorArchiveOpinionMapper;
    @Resource
    private FileObjectReferenceService fileObjectReferenceService;

    @SneakyThrows
    @Override
    public Boolean createMajorArchiveBatch(CreateNewMajorBatchVO createNewMajorBatchVO) {

        QueryWrapper<MajorArchiveBatch> majorArchiveBatchQueryWrapper = new QueryWrapper<>();
        majorArchiveBatchQueryWrapper.eq("batch_name", createNewMajorBatchVO.getBatchName());
        majorArchiveBatchQueryWrapper.eq("major_id", createNewMajorBatchVO.getMajorId());
        if (majorArchiveBatchMapper.selectOne(majorArchiveBatchQueryWrapper) != null) {
            throw new CustomException("批次名称已存在");
        }

        User user = UserUtil.getCurrentUser();

        MajorArchiveBatch majorArchiveBatch = new MajorArchiveBatch(createNewMajorBatchVO.getBatchName(), createNewMajorBatchVO.getMajorId(), user.getId());

        return majorArchiveBatch.insert();
    }

    /**
     * 专业归档 查看自己上传的所有文件信息
     *
     * @return 专业归档用户上传的所有文件信息批次
     * @author 墨小小
     */
    @Override
    public MajorArchiveGetUploadedFilesInfoResult getUploadedFilesInfo(Long batchId) {

        User loginUser = UserUtil.getCurrentUser();

        //获取评审批次信息
        MajorArchiveBatch majorArchiveBatch = majorArchiveBatchMapper.selectById(batchId);
        Optional.ofNullable(majorArchiveBatch).orElseThrow(() -> new CommonRuntimeException("评审批次不存在"));

        //获取上传的文件信息
        List<MajorArchiveFile> majorArchiveFileList = majorArchiveFileMapper.selectByBatchId(batchId);


        //再添加FileInfoList
        List<MajorArchiveGetUploadedFilesInfoFileListResult> infoResults = majorArchiveFileList.stream().map(majorArchiveFile -> {
            MajorArchiveGetUploadedFilesInfoFileListResult result = new MajorArchiveGetUploadedFilesInfoFileListResult();
            BeanUtils.copyProperties(majorArchiveFile, result);
            return result;
        }).collect(Collectors.toList());

        return new MajorArchiveGetUploadedFilesInfoResult(majorArchiveBatch.getBatchName(), "", infoResults);
    }

    /**
     * 专业归档 获取模板文件信息
     *
     * @return 模板文件信息
     * @author 墨小小
     */
    @Override
    public List<MajorArchiveGetTemplateFilesInfoResult> getTemplateFilesInfo() {
        List<MajorArchiveGetTemplateFilesInfoResult> resultList = new ArrayList<>();
        List<MajorArchiveTemplateFile> majorArchiveTemplateFileList = majorArchiveTemplateFileMapper.selectAll();
        for (MajorArchiveTemplateFile temp : majorArchiveTemplateFileList) {
            MajorArchiveGetTemplateFilesInfoResult result = new MajorArchiveGetTemplateFilesInfoResult();
            result.setId(temp.getId());
            result.setFilePath(temp.getPath());
            result.setFileName(temp.getFileName());
            result.setUpdateTime(temp.getUpdateTime());
            resultList.add(result);
        }
        return resultList;
    }

    /**
     * 归档专家 提交文件接口
     *
     * @param user                   提交文件的用户
     * @param majorEvaluationBatchId 批次id
     * @param fileName               文件名
     * @param path                   文件加密路径
     * @return insert的数据条数
     * @author 墨小小
     */
    @Override
    public Boolean uploadFileRecord(User user, Long majorEvaluationBatchId, String fileName, String path) {

        MajorArchiveFile majorArchiveFile = new MajorArchiveFile();
        majorArchiveFile.setUserId(user.getId());
        majorArchiveFile.setBatchId(majorEvaluationBatchId);
        majorArchiveFile.setFileName(fileName);
        majorArchiveFile.setPath(path);

        MajorArchiveBatch majorArchiveBatch = majorArchiveBatchMapper.selectById(majorEvaluationBatchId);
        majorArchiveBatch.setPrincipalMaterialStatus(2);
        majorArchiveBatch.setExpertReviewStatus(1);

        return majorArchiveBatch.updateById() && majorArchiveFile.insert();
    }

    @Override
    public Boolean bindUploadedFileRecord(Long fileObjectId, Long majorEvaluationBatchId) {
        User user = UserUtil.getCurrentUser();
        FileServiceFileObjectDTO fileObject = fileObjectReferenceService.getFileObject(fileObjectId);
        if (fileObject == null || StrUtil.isBlank(fileObject.getOriginalName())) {
            throw new CustomException("文件对象缺少原始文件名");
        }

        MajorArchiveBatch majorArchiveBatch = majorArchiveBatchMapper.selectById(majorEvaluationBatchId);
        if (majorArchiveBatch == null) {
            throw new CustomException("专业归档批次不存在");
        }

        return uploadFileRecord(user, majorEvaluationBatchId, fileObject.getOriginalName(),
                fileObjectReferenceService.buildReference(fileObjectId));
    }

    /**
     * 专业归档 删除已经提交的文件
     *
     * @param user 提交文件的用户
     * @param path 文件加密路径
     * @return 删除的数据库条目数
     * @anthor 墨小小
     */
    @Override
    public int deleteUploadedFileRecord(User user, String path) {
        return majorArchiveFileMapper.deleteByUserIdAndPath(user.getId(), path);
    }

    /**
     * 专业归档负责人 上传模板文件记录
     *
     * @param user     上传文件的用户
     * @param fileName 文件的名字
     * @param path     文件的加密路径
     * @return insert的数据条数
     * @author 墨小小
     */
    @Override
    public int uploadTemplateFileRecord(User user, String fileName, String path) {
        MajorArchiveTemplateFile record = new MajorArchiveTemplateFile();
        record.setUpdateTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUserId(user.getId());
        record.setFileName(fileName);
        record.setPath(path);
        return majorArchiveTemplateFileMapper.insert(record);
    }

    @Override
    public int bindTemplateFileRecord(Long fileObjectId) {
        User user = UserUtil.getCurrentUser();
        FileServiceFileObjectDTO fileObject = fileObjectReferenceService.getFileObject(fileObjectId);
        if (fileObject == null || StrUtil.isBlank(fileObject.getOriginalName())) {
            throw new CustomException("文件对象缺少原始文件名");
        }
        return uploadTemplateFileRecord(user, fileObject.getOriginalName(),
                fileObjectReferenceService.buildReference(fileObjectId));
    }

    /**
     * 专业归档负责人 删除上传的模板文件记录
     *
     * @param fileName 文件名字
     * @param path     文件路径
     * @return
     * @author 墨小小
     */
    @Override
    public int deleteTemplateFileRecord(String fileName, String path) {
        return majorArchiveTemplateFileMapper.deleteByFileNameAndPath(fileName, path);
    }

    @Override
    public MajorArchiveTemplateFile getTemplateFileByPath(String path) {
        return majorArchiveTemplateFileMapper.selectByPath(path);
    }

    /**
     * 专业归档负责人 获取评审的文件信息
     *
     * @param batchName 批次名
     * @param majorId   专业id
     * @return 对应批次的文件信息
     * @author 墨小小
     */
    @Override
    public List<MajorArchiveGetBatchFilesInfoResult> getBatchFilesInfo(String batchName, Integer majorId) {
        if (batchName == null || "".equals(batchName) || majorId == null) {
            throw new CommonRuntimeException("参数不能为空");
        }
        List<MajorArchiveFile> majorArchiveFileList = majorArchiveFileMapper.selectByBatchNameAndMajorId(batchName, majorId);
        if (majorArchiveFileList == null || majorArchiveFileList.isEmpty()) {
            return null;
        }
        List<MajorArchiveGetBatchFilesInfoResult> resultList = new ArrayList<>();
        for (MajorArchiveFile fileTemp : majorArchiveFileList) {
            MajorArchiveGetBatchFilesInfoResult result = new MajorArchiveGetBatchFilesInfoResult();
            result.setFileName(fileTemp.getFileName());
            result.setPath(fileTemp.getPath());
            result.setId(fileTemp.getId());
            result.setUpdateTime(fileTemp.getUpdateTime());
            result.setUserId(fileTemp.getUserId());
            User user = userMapper.selectByPrimaryKey(fileTemp.getUserId());
            result.setUserName(user.getRealName());
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public List<MajorArchiveBatch> getMajorArchiveProcessList(Integer majorId, String batchName) {
        QueryWrapper<MajorArchiveBatch> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_id", majorId);
        queryWrapper.eq("batch_name", batchName);
        return majorArchiveBatchMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnEvaluation(Integer majorId, String batchName) {
        QueryWrapper<MajorArchiveBatch> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_id", majorId);
        queryWrapper.eq("batch_name", batchName);
        MajorArchiveBatch process = majorArchiveBatchMapper.selectOne(queryWrapper);


        if (!process.insertOrUpdate()) {
            throw new CommonRuntimeException("退回数据失败");
        }
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitEvaluation(MajorArchiveReviewBO majorArchiveReviewBO) {
        User currentUser = UserUtil.getCurrentUser();

        MajorArchiveOpinion majorArchiveOpinion = new MajorArchiveOpinion(Long.valueOf(majorArchiveReviewBO.getMajorArchiveBatchId()), currentUser.getId(), majorArchiveReviewBO.getOpinion());

        if (!majorArchiveOpinion.insert()) {
            throw new CommonRuntimeException("提交数据失败");
        }

        MajorArchiveBatch majorArchiveBatch = majorArchiveBatchMapper.selectById(Long.valueOf(majorArchiveReviewBO.getMajorArchiveBatchId()));
        majorArchiveBatch.setExpertReviewStatus(2);
        majorArchiveBatch.setProcessEndStatus(1);
        if (majorArchiveBatch.updateById()) {
            return true;
        }

        throw new CommonRuntimeException("更新流程失败");

    }

    @SneakyThrows
    @Override
    public MajorArchiveProcessInfo getMajorArchiveProcessInfo(Long majorArchiveBatchId) {
        MajorArchiveBatch majorArchiveBatch = majorArchiveBatchMapper.selectById(majorArchiveBatchId);

        Optional.ofNullable(majorArchiveBatch).orElseThrow((Supplier<Throwable>) () -> new CustomException("专业归档批次不存在"));

        MajorArchiveProcessInfo majorArchiveProcessInfo = new MajorArchiveProcessInfo();
        BeanUtils.copyProperties(majorArchiveBatch, majorArchiveProcessInfo);

        majorArchiveProcessInfo.setCreatorName(userMapper.selectById(majorArchiveBatch.getCreatorId()).getRealName());

        //根据当前用户查看是否有权限
        User currentUser = UserUtil.getCurrentUser();

        //判断是否专业归档负责人(创建者)
        if (majorArchiveBatch.getCreatorId().equals(currentUser.getId()) || UserUtil.isRoleAvailable(MAJOR_ARCHIVE_PRINCIPAL)) {
            majorArchiveProcessInfo.setFileReviseAuthority(1);
            return majorArchiveProcessInfo;
        }

        // 判断用户是否可以查看文件


        //判断此用户是否已经参与评审
        QueryWrapper<MajorArchiveOpinion> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("batch_id", majorArchiveBatchId);
        queryWrapper.eq("user_id", currentUser.getId());
        MajorArchiveOpinion majorArchiveOpinion = majorArchiveOpinionMapper.selectOne(queryWrapper);
        if (majorArchiveOpinion != null) {
            majorArchiveProcessInfo.setIsReview(true);
        }

        //判断是否专业归档专家
        if (UserUtil.isRoleAvailable(MAJOR_ARCHIVE_EXPERT) || UserUtil.isRoleAvailable(TEST_MAJOR_ARCHIVE_EXPERT)) {
            majorArchiveProcessInfo.setReviewAuthority(1);
            majorArchiveProcessInfo.setReviewInformationAuthority(1);
        }

        if (UserUtil.isRoleAvailable(MASTER)) {
            majorArchiveProcessInfo.setFileReviseAuthority(1);
        }

        return majorArchiveProcessInfo;
    }

    @SneakyThrows
    @Override
    public List<MajorArchiveBatchInfoVO> getMajorArchiveInfoList(Integer majorId) {


        //赋初值
        QueryWrapper<MajorArchiveBatch> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_id", majorId);
        List<MajorArchiveBatch> majorArchiveBatchList = majorArchiveBatchMapper.selectList(queryWrapper);

        return majorArchiveBatchList.stream().map(majorArchiveBatch -> {

            MajorArchiveBatchInfoVO majorArchiveBatchInfoVO = new MajorArchiveBatchInfoVO();
            BeanUtils.copyProperties(majorArchiveBatch, majorArchiveBatchInfoVO);
            majorArchiveBatchInfoVO.setBatchId(String.valueOf(majorArchiveBatch.getId()));

            //赋创建用户名
            majorArchiveBatchInfoVO.setCreatorName(userMapper.selectUserNameById(majorArchiveBatch.getCreatorId()));

            //赋状态
            if (majorArchiveBatch.getProcessEndStatus() == 2) {
                majorArchiveBatchInfoVO.setStatus("流程结束");
                return majorArchiveBatchInfoVO;
            }

            if (majorArchiveBatch.getExpertReviewStatus() == 2) {
                majorArchiveBatchInfoVO.setStatus("专家评审完成");
                return majorArchiveBatchInfoVO;
            }

            if (majorArchiveBatch.getPrincipalMaterialStatus() == 2) {
                majorArchiveBatchInfoVO.setStatus("负责人完成上传材料");
                return majorArchiveBatchInfoVO;
            }

            if (majorArchiveBatch.getPrincipalMaterialStatus() == 1) {
                majorArchiveBatchInfoVO.setStatus("等待负责人上传材料");
                return majorArchiveBatchInfoVO;
            }

            return majorArchiveBatchInfoVO;
        }).collect(Collectors.toList());


    }
}
