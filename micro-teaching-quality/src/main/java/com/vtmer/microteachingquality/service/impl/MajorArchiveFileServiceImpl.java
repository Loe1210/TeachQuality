package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vtmer.microteachingquality.mapper.MajorArchiveFileMapper;
import com.vtmer.microteachingquality.mapper.MajorArchiveMapper;
import com.vtmer.microteachingquality.mapper.MajorMapper;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchive;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveFile;
import com.vtmer.microteachingquality.model.vo.MajorArchiveFileResult;
import com.vtmer.microteachingquality.service.MajorArchiveFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HJW
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MajorArchiveFileServiceImpl extends ServiceImpl<MajorArchiveFileMapper, MajorArchiveFile> implements MajorArchiveFileService {

    @Autowired
    private MajorArchiveFileMapper majorArchiveFileMapper;

    @Autowired
    private MajorArchiveMapper majorArchiveMapper;

    @Autowired
    private MajorMapper majorMapper;


    @Override
    public List<MajorArchiveFile> getMajorArchiveFileList(Integer majorId, String batchName) {
        return null;
    }

    @Override
    public MajorArchiveFile getMajorArchiveFileByName(Integer majorId, String batchName, String fileName, Integer userId) {
        QueryWrapper<MajorArchiveFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_id", majorId);
        queryWrapper.eq("batch_name", batchName);
        queryWrapper.eq("file_name", fileName);
        queryWrapper.eq("user_id", userId);

        return majorArchiveFileMapper.selectOne(queryWrapper);
    }

    @Override
    public List<MajorArchiveFileResult> getUploadedFiles(Integer id) {
        List<MajorArchiveFileResult> majorArchiveFileResults = new ArrayList<>();


        QueryWrapper<MajorArchiveFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", id);
        List<MajorArchiveFile> majorArchiveFiles = majorArchiveFileMapper.selectList(queryWrapper);

        for (MajorArchiveFile majorArchiveFile : majorArchiveFiles) {
            MajorArchive majorArchive = majorArchiveMapper.selectById(majorArchiveFile.getId());
            MajorArchiveFileResult result = new MajorArchiveFileResult(majorArchiveFile.getId(),
                    majorArchiveFile.getFileName(),
                    majorArchiveFile.getPath(),
                    majorArchiveFile.getUpdateTime(),
                    majorMapper.selectById(majorArchive.getMajorId()).getName(),
                    majorArchive.getBatchName());
            majorArchiveFileResults.add(result);
        }


        return majorArchiveFileResults;
    }

    @Override
    public MajorArchiveFile getMajorArchiveFileByPath(String filePath) {
        QueryWrapper<MajorArchiveFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("path", filePath);

        return majorArchiveFileMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteUploadedFile(String path) {
        QueryWrapper<MajorArchiveFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("path", path);
        return majorArchiveFileMapper.delete(queryWrapper);
    }


}
