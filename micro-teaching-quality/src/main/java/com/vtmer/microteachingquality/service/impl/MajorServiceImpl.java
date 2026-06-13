package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.mapper.MajorEvaluationUserMapper;
import com.vtmer.microteachingquality.mapper.MajorMapper;
import com.vtmer.microteachingquality.model.bo.MajorBO;
import com.vtmer.microteachingquality.model.bo.SelectMajorListBO;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.Major;
import com.vtmer.microteachingquality.model.vo.MajorVO;
import com.vtmer.microteachingquality.service.MajorService;
import com.vtmer.microteachingquality.util.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Hung
 * @date 2022/8/10 1:26
 */
@Service
@Slf4j
public class MajorServiceImpl implements MajorService {

    @Autowired
    private MajorMapper majorMapper;
    @Autowired
    private MajorEvaluationUserMapper majorEvaluationUserMapper;

    @Override
    public List<MajorVO> getMajorList(SelectMajorListBO selectMajorListBO) {
        QueryWrapper<Major> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(selectMajorListBO.getFilter()).ifPresent(filter -> {
            queryWrapper.like("name", filter.getKeywords());
        });
        queryWrapper.orderByDesc("create_time");

        if (UserUtil.isTestRole()) {
            queryWrapper.eq("status", 1);
        } else {
            queryWrapper.eq("status", 0);
        }

        return majorMapper.selectPage(new Page<>(selectMajorListBO.getPageNum(), selectMajorListBO.getPageSize()), queryWrapper)
                .getRecords().stream().map(major -> {
                    MajorVO majorVO = new MajorVO();
                    BeanUtils.copyProperties(major, majorVO);
                    return majorVO;
                }).collect(Collectors.toList());
    }

    @Override
    public List<MajorVO> getMajorsByRole(SelectMajorListBO selectMajorListBO) {
        QueryWrapper<Major> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(selectMajorListBO.getFilter()).ifPresent(filter -> {
            queryWrapper.like("name", filter.getKeywords());
        });
        queryWrapper.orderByDesc("create_time");
        List<Integer> majorIds = majorEvaluationUserMapper.selectMajorId(UserUtil.getCurrentUser().getId());
        if (CollectionUtils.isEmpty(majorIds)) {
            return null;
        }
        queryWrapper.in("id", majorIds);
        return majorMapper.selectPage(new Page<>(selectMajorListBO.getPageNum(), selectMajorListBO.getPageSize()), queryWrapper)
                .getRecords().stream().map(major -> {
                    MajorVO majorVO = new MajorVO();
                    BeanUtils.copyProperties(major, majorVO);
                    return majorVO;
                }).collect(Collectors.toList());
    }

    @Override
    public Boolean createNewMajor(MajorBO majorBO) {
        QueryWrapper<Major> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", majorBO.getName());
        queryWrapper.eq("college", majorBO.getCollege());
        if (!majorMapper.selectList(queryWrapper).isEmpty()) {
            throw new CustomException("此专业已存在");
        }


        Major major = new Major();
        BeanUtils.copyProperties(majorBO, major);

        if (UserUtil.isTestRole()) {
            major.setStatus(1);
        } else {
            major.setStatus(0);
        }

        if (major.insert()) {
            log.info("id为 {} 用户 {}  创建专业 {}", UserUtil.getCurrentUser().getId(), UserUtil.getCurrentUser().getRealName(), majorBO.getName());
            return true;
        }

        throw new RuntimeException("创建专业失败");
    }
}
