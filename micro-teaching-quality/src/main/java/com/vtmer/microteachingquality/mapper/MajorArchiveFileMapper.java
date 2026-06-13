package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MajorArchiveFileMapper extends BaseMapper<MajorArchiveFile> {
    int deleteByPrimaryKey(Integer id);

    int deleteByUserIdAndPath(Integer userId, String path);

    int insert(MajorArchiveFile record);

    int insertSelective(MajorArchiveFile record);

    MajorArchiveFile selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MajorArchiveFile record);

    int updateByPrimaryKey(MajorArchiveFile record);

    @Select("select id,file_name,path,create_time,batch_id,user_id,update_time from major_archive_file where user_id = #{userId} and batch_id=#{batchId} and is_delete=0;")
    List<MajorArchiveFile> selectByUserId(Integer userId, Long batchId);

    @Select("select id,file_name,path,create_time,batch_id,user_id,update_time from major_archive_file where batch_id=#{batchId} and is_delete=0;")
    List<MajorArchiveFile> selectByBatchId(Long batchId);

    List<MajorArchiveFile> selectByBatchNameAndMajorId(String batchName, Integer majorId);
}