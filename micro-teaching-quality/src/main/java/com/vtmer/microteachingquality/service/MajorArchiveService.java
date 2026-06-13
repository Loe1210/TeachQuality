package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchive;
import com.vtmer.microteachingquality.model.vo.*;

import java.util.List;

/**
 * @author HJW
 */
public interface MajorArchiveService extends IService<MajorArchive> {


    /**
     * 获取专业归档课程名字、id和文件列表
     *
     * @return 专业归档课程名字、id和文件列表
     */
    List<MajorInfoResult> getAllMajorInfo(Integer majorId, Integer pageNum, Integer pageSize);


//    /**
//     * 导出记录
//     * @param id 专家id
//     * @return 导出记录
//     */
//    XSSFWorkbook exportRecord(Integer id);

    /**
     * 获取该专业归档表的所有评审记录
     *
     * @param batchId 归档流程Id
     * @return 评审记录
     */
    List<MajorArchiveOpinionResult> getMajorArchiveOpinion(Long batchId);

    /**
     * 通过小组id获取小组所有评审记录
     *
     * @param groupId 小组id
     * @param userId  用户id
     * @return 评审记录
     */
    List<GroupMajorArchiveOpinionResult> getMajorArchiveOpinionByGroup(Integer groupId, Integer userId);

    /**
     * 结束评审流程
     *
     * @param majorArchiveBtachId 专业归档表id
     * @return 是否结束
     */
    boolean setEvaluationEnd(Long batchId);


    /**
     * 设置评审小组组长
     *
     * @param userId    用户id
     * @param groupName 小组名称
     * @param majorName 专业名称
     * @param batchName 批次名称
     * @return 是否设置成功
     */
    boolean takeGroupLeader(Integer userId, String groupName, String majorName, String batchName);

    /**
     * 添加评审小组成员
     *
     * @param userId  用户id
     * @param groupId 小组id
     * @return 是否添加成功
     */
    boolean takeGroupUser(Integer userId, Integer groupId);

    /**
     * 获取评审小组成员
     *
     * @param groupId 小组id
     * @return 成员列表
     */
    List<UserInfoResult> getGroupUser(Integer groupId);

    /**
     * 获取评审小组组长
     *
     * @param groupId 小组id
     * @return 组长id
     */
    Integer getGroupLeader(Integer groupId);


    /**
     * 获取所有的小组信息
     *
     * @return 小组信息
     */
    List<GroupInfoResult> getAllGroupInfo();

    /**
     * 删除小组成员
     *
     * @param userId  成员id
     * @param groupId 小组id
     * @return 是否删除成功
     */
    boolean deleteGroupUser(Integer userId, Integer groupId);

    /**
     * 删除小组
     *
     * @param groupId 小组id
     * @return 是否删除成功
     */
    boolean deleteGroup(Integer groupId);

    /**
     * 获取成员的评审小组的信息
     *
     * @param userId 用户id
     * @return 小组信息
     */
    List<GroupInfoResult> getGroupInfo(Integer userId);
}
