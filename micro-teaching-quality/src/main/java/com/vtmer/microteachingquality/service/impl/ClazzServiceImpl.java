package com.vtmer.microteachingquality.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.vtmer.microteachingquality.common.constant.enums.UserTypeConstant;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.common.exception.clazz.ClazzFileNotExistException;
import com.vtmer.microteachingquality.common.exception.clazz.ExpertNotFoundException;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.bo.ClazzBo;
import com.vtmer.microteachingquality.model.bo.SelectClazzListBO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.*;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.Major;
import com.vtmer.microteachingquality.model.vo.ClazzVO;
import com.vtmer.microteachingquality.model.vo.FileInfoResult;
import com.vtmer.microteachingquality.model.vo.GetAllClazzInfoResult;
import com.vtmer.microteachingquality.model.vo.GetEvaluationClazzByUserIdResult;
import com.vtmer.microteachingquality.service.ClazzService;
import com.vtmer.microteachingquality.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @program: personneltraining2
 * @description:
 * @author: 周华娟
 * @create: 2021-07-26 00:19
 **/
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class ClazzServiceImpl implements ClazzService, UserTypeConstant {

    @Autowired
    private ClazzFileTemplateMapper clazzFileTemplateMapper;
    @Autowired
    private ClazzAnnotationMapper clazzAnnotationMapper;
    @Autowired
    private ClazzFileMapper clazzFileMapper;
    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private ClazzMapper clazzMapper;
    @Autowired
    private ClazzExpertManageInfoMapper clazzExpertManageInfoMapper;
    @Autowired
    private ClazzEvaluateOptionRecordMapper clazzEvaluateOptionRecordMapper;
    @Autowired
    private ClazzOpinionRecordMapper clazzOpinionRecordMapper;
    @Autowired
    private ClazzEvaluateOptionMapper clazzEvaluateOptionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;
    @Autowired
    private ClazzEvaluationUserMapper clazzEvaluationUserMapper;

    @Override
    public List<ClazzAnnotation> getAnnotation() {
        return clazzAnnotationMapper.selectAll();
    }

    @Override
    public ClazzAnnotation getClazzAnnotationById(Integer id) {
        return clazzAnnotationMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<ClazzFileTemplate> getAllClazzTemplate() {
        Integer userId = UserUtil.getCurrentUser().getId();
        return clazzFileTemplateMapper.selectAllByUserId(userId);
    }

    @Override
    public ClazzFile exitFile(Integer userId, Long clazzEvaluationProcessId, String fileName) {
        QueryWrapper<ClazzFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("clazz_evaluation_process_id", clazzEvaluationProcessId);
        queryWrapper.eq("file_name", fileName);
        List<ClazzFile> clazzFiles = clazzFileMapper.selectList(queryWrapper);

        if (clazzFiles.size() == 0) {
            return null;
        }

        return clazzFiles.get(0);
    }

    @Override
    public ClazzFile getClazzFile(Integer userId, String clazzName) {
        return clazzFileMapper.selectByClazzNameAndUserId(userId, clazzName);
    }

    @Override
    public int saveClazzFile(ClazzFile clazzFile) {
        return clazzFileMapper.insert(clazzFile);
    }

    @Override
    public ClazzFileTemplate getClazzFileTemplateByMajor(String major) {
        return clazzFileTemplateMapper.selectByMajor(major);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateClazzFile(User loginUser, ClazzFile clazzFile) {
        ClazzFile clazzFile1 = clazzFileMapper.selectByClazzNameAndUserId(loginUser.getId(), clazzFile.getClazzName());
        if (ObjectUtil.isNull(clazzFile1)) {
            throw new ClazzFileNotExistException();
        }
        return clazzFileMapper.updateByPrimaryKey(clazzFile);
    }

    @Override
    public ClazzFileTemplate getClazzByMajor(String major) {
        return clazzFileTemplateMapper.selectByMajor(major);
    }

    @Override
    public Major getMajorByName(String name) {
        return majorMapper.selectByMajorName(name);
    }

    @Override
    public Clazz getClazzByName(String name) {
        return clazzMapper.selectByName(name);
    }


    /**
     * 通过课程评审专家id获取这个专家所有要评审的课程信息
     *
     * @param userId
     * @return
     */
    @Override
    public List<GetEvaluationClazzByUserIdResult> getEvaluationClazzByUserId(Integer userId) {
        List<ClazzExpertManageInfo> clazzExpertManageInfoList = clazzExpertManageInfoMapper.selectByUserId(userId);
        if (ObjectUtil.isNull(clazzExpertManageInfoList) || clazzExpertManageInfoList.size() == 0) {
            throw new ExpertNotFoundException();
        }

        return clazzExpertManageInfoList.stream().map(clazzExpertManageInfo -> {
            Integer clazzId = clazzExpertManageInfo.getClazzId();
            Clazz clazz = clazzMapper.selectByPrimaryKey(clazzId);
            return new GetEvaluationClazzByUserIdResult(clazz.getId(), clazz.getCollege(), clazz.getMajor(), clazz.getName(), clazz.getType(), clazzExpertManageInfo.getStatus());
        }).collect(Collectors.toList());
    }

    @Override
    public List<GetAllClazzInfoResult> getAllClazzInformation() {
        List<Clazz> clazzList = clazzMapper.selectAll();
        return clazzList.stream().map(clazzTemp -> {
            //课程负责人已提交的文件表
            List<FileInfoResult> fileInfoResultList = clazzFileMapper.selectByClazzName(clazzTemp.getName()).stream()
                    .map(clazzFile -> new FileInfoResult(clazzFile.getId(), clazzFile.getFileName(), clazzFile.getPath(), clazzFile.getUpdateTime()))
                    .collect(Collectors.toList());
            return new GetAllClazzInfoResult(clazzTemp.getName(), clazzTemp.getId(), fileInfoResultList);
        }).collect(Collectors.toList());
    }


    @Override
    public Integer deleteUploadedFile(String path) {
        return clazzFileMapper.deleteByPath(path);
    }

    @Override
    public XSSFWorkbook exportRecord(Integer userId) {
        XSSFWorkbook result = new XSSFWorkbook();

        //查询该用户参与了多少评审 ClassEvaluationProcess
        List<ClazzEvaluateOptionRecord> clazzEvaluateOptionRecords = clazzEvaluateOptionMapper.selectDistinctEvaluationId(userId);

        clazzEvaluateOptionRecords.stream()
                .distinct()
                .forEach(clazzEvaluateOptionRecord -> {
                    //查询出这个评审流程和课程 的详细信息
                    ClassEvaluationProcess classEvaluationProcess = clazzEvaluationProcessMapper.selectById(clazzEvaluateOptionRecord.getClazzEvaluationProcessId());
                    if (classEvaluationProcess == null) {
                        return;
                    }

                    Clazz clazz = clazzMapper.selectById(classEvaluationProcess.getClazzId());


                    //获取这个专家 关于这个评审的文字意见
                    QueryWrapper<ClazzOpinionRecord> opinionRecordQueryWrapper = new QueryWrapper<>();
                    opinionRecordQueryWrapper.eq("clazz_evaluation_process_id", clazzEvaluateOptionRecord.getClazzEvaluationProcessId());
                    opinionRecordQueryWrapper.eq("user_id", userId);
                    ClazzOpinionRecord clazzOpinionRecord = clazzOpinionRecordMapper.selectList(opinionRecordQueryWrapper).get(0);

                    //每个批次都是一张Sheet
                    Sheet sheet = result.createSheet(clazz.getName() + System.currentTimeMillis());
                    Row titleRow = sheet.createRow(0);
                    Cell titleCell = titleRow.createCell(0);
                    //设置基本信息
                    //在第一行写入一些基本数据，例如更新时间，课程名字，
                    titleCell.setCellValue("课程名：" + clazz.getName());
                    titleCell = titleRow.createCell(1);
                    titleCell.setCellValue("更新时间：" + clazzOpinionRecord.getUpdateTime());

                    titleRow = sheet.createRow(1);
                    titleRow.createCell(0).setCellValue("一级标题");
                    titleRow.createCell(1).setCellValue("具体内容");
                    titleRow.createCell(2).setCellValue("选项");
                    int rowIndex = 2;

                    //循环题目
                    //获取这个用户 这个评审  说选的选项
                    QueryWrapper<ClazzEvaluateOptionRecord> wrapper = new QueryWrapper<>();
                    wrapper.eq("clazz_evaluation_process_id", classEvaluationProcess.getId());
                    wrapper.eq("user_id", userId);

                    for (ClazzEvaluateOptionRecord clazzEvaluateOptionRecordTemp : clazzEvaluateOptionRecordMapper.selectList(wrapper)) {
                        ClazzEvaluateOption clazzEvaluateOption = clazzEvaluateOptionMapper.selectByPrimaryKey(clazzEvaluateOptionRecordTemp.getEvaluationOptionId());
                        Row row = sheet.createRow(rowIndex);
                        row.createCell(0).setCellValue(clazzEvaluateOption.getFirstTarget());
                        row.createCell(1).setCellValue(clazzEvaluateOption.getDetail());
                        row.createCell(2).setCellValue(clazzEvaluateOptionRecordTemp.getMark());
                        rowIndex++;
                    }
                    rowIndex++;

                    //处理专家意见
                    Row opinionRow = sheet.createRow(rowIndex);
                    opinionRow.createCell(0).setCellValue("课程的突出优点:");
                    opinionRow.createCell(1).setCellValue(clazzOpinionRecord.getClazzAdvantage());
                    rowIndex++;
                    opinionRow = sheet.createRow(rowIndex);
                    opinionRow.createCell(0).setCellValue("课程存在的主要问题:");
                    opinionRow.createCell(1).setCellValue(clazzOpinionRecord.getClazzProblem());
                    rowIndex++;
                    opinionRow = sheet.createRow(rowIndex);
                    opinionRow.createCell(0).setCellValue("改进意见建议:");
                    opinionRow.createCell(1).setCellValue(clazzOpinionRecord.getClazzAdvice());
                    //设置列宽度
                    sheet.setColumnWidth(0, 25 * 256);
                    sheet.setColumnWidth(1, 175 * 256);
                    sheet.setColumnWidth(2, 20 * 256);
                });
        return result;
    }


    @Override
    public Clazz getClazzById(Integer clazzId) {
        return clazzMapper.selectById(clazzId);
    }

    @Override
    public List<ClazzVO> getClazzByUserType(User user, Integer pageNum, Integer pageSize) {
        List<String> userType = UserUtil.getUserRole();
        QueryWrapper<Clazz> queryWrapper = new QueryWrapper<>();
        if (userType.contains(ENGINEERING_EXPERT_LEADER) || userType.contains(ENGINEERING_EXPERT)) {
            queryWrapper.eq("type", ENGINEERING);
        } else if (userType.contains(HUMANITIES_AND_SOCIAL_SCIENCES_EXPERT) || userType.contains(HUMANITIES_AND_SOCIAL_SCIENCES_EXPERT_LEADER)) {
            queryWrapper.eq("type", HUMANITIES_AND_SOCIAL_SCIENCES);
        } else if (userType.contains(ARTS_AND_SCIENCES_EXPERT) || userType.contains(ARTS_AND_SCIENCES_EXPERT_LEADER)) {
            queryWrapper.eq("type", ARTS_AND_SCIENCES);
        }
        List<Clazz> selectList = clazzMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper).getRecords();
        return selectList.stream().map(clazz -> {
            ClazzVO clazzVO = new ClazzVO();
            BeanUtils.copyProperties(clazz, clazzVO);
            clazzVO.setUserName(userMapper.selectByPrimaryKey(clazz.getUserId()).getRealName());
            return clazzVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ClazzVO> getClasses(SelectClazzListBO selectClazzListBO) {
        QueryWrapper<Clazz> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");

        if (UserUtil.isTestRole()) {
            queryWrapper.eq("status", 1);
        } else {
            queryWrapper.eq("status", 0);
        }

        if (UserUtil.isRoleAvailable(CLAZZ_EVALUATION_PRINCIPAL)) {
            queryWrapper.eq("user_id", UserUtil.getCurrentUser().getId());
        }

        Optional.ofNullable(selectClazzListBO.getFilter())
                .ifPresent(clazzListConditionBO -> queryWrapper.like("name", selectClazzListBO.getFilter().getKeywords()));

        return clazzMapper.selectPage(new PageDTO<>(selectClazzListBO.getPageNum(), selectClazzListBO.getPageSize()), queryWrapper)
                .getRecords().stream().map(clazz -> {
                    ClazzVO clazzVO = new ClazzVO();
                    BeanUtils.copyProperties(clazz, clazzVO);
                    clazzVO.setUserName(userMapper.selectUserNameById(clazz.getUserId()));
                    return clazzVO;
                }).collect(Collectors.toList());
    }

    @Override
    public List<ClazzVO> getClassesByRole(SelectClazzListBO selectClazzListBO) {
        QueryWrapper<Clazz> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<Integer> clazzIds = clazzEvaluationUserMapper.selectClazzId(UserUtil.getCurrentUser().getId());
        if (CollectionUtils.isEmpty(clazzIds)) {
            return null;
        }
        queryWrapper.in("id", clazzIds);
        Optional.ofNullable(selectClazzListBO.getFilter())
                .ifPresent(clazzListConditionBO -> queryWrapper.like("name", selectClazzListBO.getFilter().getKeywords()));

        return clazzMapper.selectPage(new PageDTO<>(selectClazzListBO.getPageNum(), selectClazzListBO.getPageSize()), queryWrapper)
                .getRecords().stream().map(clazz -> {
                    ClazzVO clazzVO = new ClazzVO();
                    BeanUtils.copyProperties(clazz, clazzVO);
                    clazzVO.setUserName(userMapper.selectUserNameById(clazz.getUserId()));
                    return clazzVO;
                }).collect(Collectors.toList());
    }

    /**
     * @param clazzBo
     * @return
     */
    @Override
    public Boolean createClazz(ClazzBo clazzBo) {
        QueryWrapper<Clazz> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("college", clazzBo.getCollege());
        queryWrapper.eq("major", clazzBo.getMajor());
        queryWrapper.eq("name", clazzBo.getName());

        if (!clazzMapper.selectList(queryWrapper).isEmpty()) {
            throw new CustomException("已经有此课程了");
        }

        Clazz clazz = new Clazz();
        BeanUtils.copyProperties(clazzBo, clazz);
        clazz.setUserId(UserUtil.getCurrentUser().getId());

        if (UserUtil.isTestRole()) {
            clazz.setStatus(1);
        } else {
            clazz.setStatus(0);
        }

        log.info("id为 {} 用户 {}  创建课程 {}", UserUtil.getCurrentUser().getId(), UserUtil.getCurrentUser().getRealName(), clazz.getName());
        return clazz.insert();
    }
}
