package com.vtmer.microteachingquality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author HJW
 */
@Mapper
public interface MajorArchiveBatchMapper extends BaseMapper<MajorArchiveBatch> {


    @Update("update major_archive_batch set expert_review_status='2' and  process_end_status = '1' where id=#{batchId};")
    int submitExpertOpinion(Long batchId);

    @Update("update major_archive_batch set process_end_status='2' where id=#{batchId};")
    int endBatchProcess(Long batchId);

    @Update("update major_archive_batch set principal_material_status= '2' and  expert_review_status= '1' where id=#{batchId};")
    int uploadFiles(Long batchId);
}
