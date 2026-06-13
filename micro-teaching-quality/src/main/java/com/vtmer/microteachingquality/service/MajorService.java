package com.vtmer.microteachingquality.service;

import com.vtmer.microteachingquality.model.bo.MajorBO;
import com.vtmer.microteachingquality.model.bo.SelectMajorListBO;
import com.vtmer.microteachingquality.model.vo.MajorVO;

import java.util.List;

/**
 * @author Hung
 * @date 2022/8/10 1:26
 */
public interface MajorService {

    /**
     * 获取专业信息
     *
     * @return 专业信息
     */
    List<MajorVO> getMajorList(SelectMajorListBO selectMajorListBO);

    /**
     * 分权限获取专业信息
     *
     * @param selectMajorListBO
     * @return
     */
    List<MajorVO> getMajorsByRole(SelectMajorListBO selectMajorListBO);


    Boolean createNewMajor(MajorBO majorBO);

}
