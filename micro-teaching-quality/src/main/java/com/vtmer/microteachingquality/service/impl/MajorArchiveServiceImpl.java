package com.vtmer.microteachingquality.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.pojo.majorarchive.*;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.Major;
import com.vtmer.microteachingquality.model.vo.*;
import com.vtmer.microteachingquality.service.MajorArchiveService;
import com.vtmer.microteachingquality.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author HJW
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MajorArchiveServiceImpl extends ServiceImpl<MajorArchiveMapper, MajorArchive> implements MajorArchiveService {

    @Autowired
    private MajorArchiveMapper majorArchiveMapper;

    @Autowired
    private MajorMapper majorMapper;

    @Autowired
    private MajorArchiveFileMapper majorArchiveFileMapper;

    @Autowired
    private MajorArchiveGroupUserMapper majorArchiveGroupUserMapper;

    @Autowired
    private MajorArchiveGroupMapper majorArchiveGroupMapper;

    @Autowired
    private MajorArchiveOpinionMapper majorArchiveOpinionMapper;

    @Autowired
    private MajorArchiveBatchMapper majorArchiveBatchMapper;

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<MajorInfoResult> getAllMajorInfo(Integer majorId, Integer pageNum, Integer pageSize) {
        List<MajorArchive> majorArchives = majorArchiveMapper.selectList(null);

        List<MajorInfoResult> resultList = new ArrayList<>();

        for (MajorArchive majorArchive : majorArchives) {
            MajorInfoResult result = new MajorInfoResult();
            result.setMajorId(majorArchive.getMajorId());
            result.setBatchName(majorArchive.getBatchName());
            result.setMajorName(majorMapper.selectById(majorArchive.getMajorId()).getName());

            List<FileInfoResult> fileInfoResults = new ArrayList<>();

            QueryWrapper<MajorArchiveFile> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("major_archive_id", majorArchive.getId());
            List<MajorArchiveFile> majorArchiveFiles = majorArchiveFileMapper.selectList(queryWrapper);
            BeanUtil.copyProperties(majorArchiveFiles, fileInfoResults);
            result.setFileInfoList(fileInfoResults);
            resultList.add(result);
        }
        return resultList;
    }


//    @Override
//    public XSSFWorkbook exportRecord(Integer id) {
//
//        XSSFWorkbook result = new XSSFWorkbook();
//
//
//        QueryWrapper<MajorArchiveOpinion> wrapper = new QueryWrapper<>();
//        wrapper.eq("user_id", id);
//        List<MajorArchiveOpinion> majorArchiveOpinions = majorArchiveOpinionMapper.selectList(wrapper);
//        if (majorArchiveOpinions.size() == 0) {
//            return result;
//        }
//
//        List<com.vtmer.microteachingquality.model.dto.select.MajorArchiveExpertRecordDTO> recordDTOS = new ArrayList<>();
//
//        for (MajorArchiveOpinion opinion : majorArchiveOpinions) {
//            MajorArchiveBatch majorArchiveBatch = majorArchiveBatchMapper.selectById(opinion.getBatchId());
//            String majorName = majorMapper.selectById(majorArchiveBatch.getMajorId()).getName();
//            String batchName = majorArchiveBatch.getBatchName();
//            String opinionContent = opinion.getOpinion();
//            LocalDateTime updateTime = opinion.getUpdateTime();
//
//            QueryWrapper<MajorArchive> wrapper1 = new QueryWrapper<>();
//            wrapper1.eq("major_id", opinion.getMajorId());
//            wrapper1.eq("batch_name", opinion.getBatchName());
//            MajorArchive majorArchive = majorArchiveMapper.selectOne(wrapper1);
//            Integer majorArchiveId = majorArchive.getId();
//            Integer groupId = majorArchive.getGroupId();
//            String groupName = majorArchiveGroupMapper.selectById(groupId).getGroupName();
//
//            com.vtmer.microteachingquality.model.dto.select.MajorArchiveExpertRecordDTO recordDTO = new com.vtmer.microteachingquality.model.dto.select.MajorArchiveExpertRecordDTO(majorArchiveId, majorName, batchName, opinionContent, updateTime, groupName);
//
//            recordDTOS.add(recordDTO);
//        }
//
//        // 一张sheet就够了
//        Sheet sheet = result.createSheet();
//        // 第一列为表头
//        Row titleRow = sheet.createRow(0);
//
//        titleRow.createCell(0).setCellValue("专业归档表id");
//        titleRow.createCell(1).setCellValue("专业名称：");
//        titleRow.createCell(2).setCellValue("批次名称：");
//        titleRow.createCell(3).setCellValue("评价内容：");
//        titleRow.createCell(4).setCellValue("评价时间：");
//        titleRow.createCell(5).setCellValue("组名称：");
//
//        int rowIndex = 1;
//
//        for (com.vtmer.microteachingquality.model.dto.select.MajorArchiveExpertRecordDTO dto : recordDTOS) {
//            Row row = sheet.createRow(rowIndex);
//            row.createCell(0).setCellValue(dto.getMajorArchiveId());
//            row.createCell(1).setCellValue(dto.getMajorName());
//            row.createCell(2).setCellValue(dto.getBatchName());
//            row.createCell(3).setCellValue(dto.getOpinion());
//            row.createCell(4).setCellValue(dto.getUpdateTime().toString());
//            row.createCell(5).setCellValue(dto.getGroupName());
//            rowIndex++;
//
//        }
//        return result;
//    }

    @Override
    public List<MajorArchiveOpinionResult> getMajorArchiveOpinion(Long batchId) {

        MajorArchiveBatch majorArchiveBatch = majorArchiveBatchMapper.selectById(batchId);

        QueryWrapper<MajorArchiveOpinion> wrapper = new QueryWrapper<>();
        wrapper.eq("batch_id", majorArchiveBatch.getId());

        List<MajorArchiveOpinion> majorArchiveOpinions = majorArchiveOpinionMapper.selectList(wrapper);

        return majorArchiveOpinions.stream().map(majorArchiveOpinion -> MajorArchiveOpinionResult.builder()
                .opinion(majorArchiveOpinion.getOpinion())
                .userId(String.valueOf(majorArchiveOpinion.getUserId()))
                .userName(userMapper.selectById(majorArchiveOpinion.getUserId()).getRealName())
                .updateTime(majorArchiveOpinion.getUpdateTime())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<GroupMajorArchiveOpinionResult> getMajorArchiveOpinionByGroup(Integer groupId, Integer userId) {


        List<GroupMajorArchiveOpinionResult> resultList = new ArrayList<>();

        MajorArchiveGroup majorArchiveGroup = majorArchiveGroupMapper.selectById(groupId);
        if (majorArchiveGroup.getLeaderId().equals(userId)) {

            QueryWrapper<MajorArchive> wrapper = new QueryWrapper<>();
            wrapper.eq("group_id", groupId);
            List<MajorArchive> majorArchives = majorArchiveMapper.selectList(wrapper);

            for (MajorArchive majorArchive : majorArchives) {


                Integer majorId = majorArchive.getMajorId();
                String batchName = majorArchive.getBatchName();
                String majorName = majorMapper.selectById(majorId).getName();


                QueryWrapper<MajorArchiveOpinion> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("major_id", majorArchive.getMajorId());
                queryWrapper.eq("batch_name", majorArchive.getBatchName());

                List<MajorArchiveOpinion> majorArchiveOpinions = majorArchiveOpinionMapper.selectList(queryWrapper);

                for (MajorArchiveOpinion majorArchiveOpinion : majorArchiveOpinions) {
                    GroupMajorArchiveOpinionResult result = new GroupMajorArchiveOpinionResult();

                    if (majorArchiveOpinion != null) {
                        result.setOpinion(majorArchiveOpinion.getOpinion());
                        result.setUpdateTime(majorArchiveOpinion.getUpdateTime());
                        result.setUserId(majorArchiveOpinion.getUserId());

                    }
                    result.setBatchName(batchName);
                    result.setMajorName(majorName);
                    result.setGroupId(groupId);
                    resultList.add(result);
                }


            }
        } else {
            QueryWrapper<MajorArchive> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("group_id", groupId);
            List<MajorArchive> majorArchives = majorArchiveMapper.selectList(wrapper1);
            for (MajorArchive archive : majorArchives) {
                Integer majorId = archive.getMajorId();
                String batchName = archive.getBatchName();
                String majorName = majorMapper.selectById(majorId).getName();

                QueryWrapper<MajorArchiveOpinion> wrapper2 = new QueryWrapper<>();
                wrapper2.eq("major_id", majorId);
                wrapper2.eq("batch_name", batchName);
                wrapper2.eq("user_id", userId);
                MajorArchiveOpinion majorArchiveOpinion = majorArchiveOpinionMapper.selectOne(wrapper2);

                GroupMajorArchiveOpinionResult result = new GroupMajorArchiveOpinionResult();
                if (majorArchiveOpinion != null) {
                    result.setOpinion(majorArchiveOpinion.getOpinion());
                    result.setUpdateTime(majorArchiveOpinion.getUpdateTime());
                }
                result.setBatchName(batchName);
                result.setMajorName(majorName);

                result.setUserId(userId);
                result.setGroupId(groupId);

                resultList.add(result);
            }

        }

        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setEvaluationEnd(Long batchId) {
        return majorArchiveBatchMapper.endBatchProcess(batchId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean takeGroupLeader(Integer userId, String groupName, String majorName, String batchName) {
        MajorArchiveGroup majorArchiveGroup = new MajorArchiveGroup();
        majorArchiveGroup.setGroupName(groupName);
        majorArchiveGroup.setLeaderId(userId);
        if (!majorArchiveGroup.insertOrUpdate()) {
            return false;
        }

        QueryWrapper<Major> wrapper = new QueryWrapper<>();
        wrapper.eq("major_name", majorName);
        Integer majorId = majorMapper.selectOne(wrapper).getId();

        QueryWrapper<MajorArchive> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("major_id", majorId);
        wrapper1.eq("batch_name", batchName);
        List<MajorArchive> majorArchives = majorArchiveMapper.selectList(wrapper1);

        for (MajorArchive majorArchive : majorArchives) {
            majorArchive.setGroupId(majorArchiveGroup.getId());
            if (majorArchive.insertOrUpdate()) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean takeGroupUser(Integer userId, Integer groupId) {
        MajorArchiveGroupUser majorArchiveGroupUser = new MajorArchiveGroupUser();
        majorArchiveGroupUser.setUserId(userId);
        majorArchiveGroupUser.setGroupId(groupId);

        if (new MajorArchiveGroup().selectById(groupId) == null) {
            return false;
        }
        return majorArchiveGroupUser.insertOrUpdate();
    }

    @Override
    public List<UserInfoResult> getGroupUser(Integer groupId) {
        List<UserInfoResult> resultList = new ArrayList<>();
        QueryWrapper<MajorArchiveGroupUser> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        List<MajorArchiveGroupUser> majorArchiveGroupUsers = majorArchiveGroupUserMapper.selectList(wrapper);
        for (MajorArchiveGroupUser majorArchiveGroupUser : majorArchiveGroupUsers) {
            resultList.add(userService.findUserById(majorArchiveGroupUser.getUserId()));
        }
        return null;
    }

    @Override
    public Integer getGroupLeader(Integer groupId) {
        MajorArchiveGroup majorArchiveGroup = majorArchiveGroupMapper.selectById(groupId);
        return majorArchiveGroup.getLeaderId();
    }

    @Override
    public List<GroupInfoResult> getAllGroupInfo() {
        List<GroupInfoResult> resultList = new ArrayList<>();
        List<MajorArchiveGroup> majorArchiveGroups = majorArchiveGroupMapper.selectList(null);
        for (MajorArchiveGroup majorArchiveGroup : majorArchiveGroups) {
            GroupInfoResult result = new GroupInfoResult();
            result.setGroupId(majorArchiveGroup.getId());
            result.setGroupName(majorArchiveGroup.getGroupName());
            result.setGroupLeader(userService.findUserById(majorArchiveGroup.getLeaderId()));
            result.setUserInfoList(getGroupUser(majorArchiveGroup.getId()));
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGroupUser(Integer userId, Integer groupId) {
        QueryWrapper<MajorArchiveGroupUser> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("group_id", groupId);
        return majorArchiveGroupUserMapper.delete(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGroup(Integer groupId) {
        QueryWrapper<MajorArchive> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        List<MajorArchive> majorArchives = majorArchiveMapper.selectList(wrapper);
        for (MajorArchive majorArchive : majorArchives) {
            majorArchive.setGroupId(null);
        }
        QueryWrapper<MajorArchiveGroupUser> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("group_id", groupId);
        majorArchiveGroupUserMapper.delete(wrapper1);

        return majorArchiveGroupMapper.deleteById(groupId) > 0;
    }

    @Override
    public List<GroupInfoResult> getGroupInfo(Integer userId) {
        List<GroupInfoResult> resultList = new ArrayList<>();
        QueryWrapper<MajorArchiveGroupUser> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<MajorArchiveGroupUser> majorArchiveGroupUsers = majorArchiveGroupUserMapper.selectList(wrapper);
        for (MajorArchiveGroupUser majorArchiveGroupUser : majorArchiveGroupUsers) {
            GroupInfoResult result = new GroupInfoResult();
            result.setGroupId(majorArchiveGroupUser.getGroupId());
            result.setGroupName(majorArchiveGroupMapper.selectById(majorArchiveGroupUser.getGroupId()).getGroupName());
            result.setGroupLeader(userService.findUserById(majorArchiveGroupMapper.selectById(majorArchiveGroupUser.getGroupId()).getLeaderId()));
            result.setUserInfoList(getGroupUser(majorArchiveGroupUser.getGroupId()));
            resultList.add(result);
        }
        return resultList;
    }
}
