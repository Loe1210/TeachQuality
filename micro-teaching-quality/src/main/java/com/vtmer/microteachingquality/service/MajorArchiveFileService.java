package com.vtmer.microteachingquality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveFile;
import com.vtmer.microteachingquality.model.vo.MajorArchiveFileResult;

import java.util.List;

/**
 * @author HJW
 */
public interface MajorArchiveFileService extends IService<MajorArchiveFile> {

    /**
     * 获取当前专业+批次的文件信息
     *
     * @param majorId   专业id
     * @param batchName 批次名称
     * @return 文件信息
     */
    List<MajorArchiveFile> getMajorArchiveFileList(Integer majorId, String batchName);

    /**
     * 获取当前文件名的文件
     *
     * @param majorId   专业id
     * @param batchName 批次名称
     * @param fileName  文件名
     * @param userId    上传人id
     * @return 文件
     */
    MajorArchiveFile getMajorArchiveFileByName(Integer majorId, String batchName, String fileName, Integer userId);

    /**
     * 获取负责人上传的文件信息
     *
     * @param id 负责人id
     * @return 文件信息
     */
    List<MajorArchiveFileResult> getUploadedFiles(Integer id);

    /**
     * 获取上传的文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    MajorArchiveFile getMajorArchiveFileByPath(String filePath);

    /**
     * 删除上传的文件
     *
     * @param path 文件路径
     * @return 文件
     */
    Integer deleteUploadedFile(String path);
}
