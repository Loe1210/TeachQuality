package com.vtmer.microteachingquality.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.dto.EvaluationUserDTO;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzEvaluationUser;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzReviewEvaluationUser;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationUser;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorReviewEvaluationUser;
import com.vtmer.microteachingquality.model.vo.EvaluationUserVO;
import com.vtmer.microteachingquality.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {

    @Resource
    MajorEvaluationUserMapper majorEvaluationUserMapper;
    @Resource
    MajorReviewEvaluationUserMapper majorReviewEvaluationUserMapper;
    @Resource
    ClazzEvaluationUserMapper clazzEvaluationUserMapper;
    @Resource
    ClazzReviewEvaluationUserMapper clazzReviewEvaluationUserMapper;
    @Resource
    ClazzMapper clazzMapper;
    @Resource
    MajorMapper majorMapper;
    @Resource
    UserMapper userMapper;

    @Override
    public List<EvaluationUserVO> getMajorEvaluationUser(Map<String, Object> map) {
        List<EvaluationUserVO> result = new ArrayList<>();
        majorEvaluationUserMapper.selectByMap(map).forEach(user -> {
            result.add(buildMajorEvaluationUserVO(
                    user.getId(),
                    user.getUserId(),
                    user.getMajorId(),
                    majorMapper.getMajorName(user.getMajorId()),
                    "专业评审"));
        });
        majorReviewEvaluationUserMapper.selectByMap(map).forEach(user -> {
            result.add(buildMajorEvaluationUserVO(
                    user.getId(),
                    user.getUserId(),
                    user.getMajorId(),
                    majorMapper.getMajorName(user.getMajorId()),
                    "专业复评"));
        });
        clazzEvaluationUserMapper.selectByMap(map).forEach(user -> {
            result.add(buildMajorEvaluationUserVO(
                    user.getId(),
                    user.getUserId(),
                    user.getClazzId(),
                    clazzMapper.getClazzName(user.getClazzId()),
                    "课程评审"));
        });
        clazzReviewEvaluationUserMapper.selectByMap(map).forEach(user -> {
            result.add(buildMajorEvaluationUserVO(
                    user.getId(),
                    user.getUserId(),
                    user.getClazzId(),
                    clazzMapper.getClazzName(user.getClazzId()),
                    "课程复评"));
        });
        return result;
    }

    @Override
    public Boolean createMajorEvaluationUser(String majorName, Integer userId) {
        Integer id = majorMapper.getMajorIdByName(majorName);
        if (id == null) return null;
        if (existsMajorEvaluationUser(id, userId)) return false;
        return majorEvaluationUserMapper.insert(new MajorEvaluationUser().setUserId(userId).setMajorId(id)) == 1;
    }

    @Override
    public Boolean createMajorReviewEvaluationUser(String majorName, Integer userId) {
        Integer id = majorMapper.getMajorIdByName(majorName);
        if (id == null) return null;
        if (existsMajorReviewEvaluationUser(id, userId)) return false;
        return majorReviewEvaluationUserMapper.insert(new MajorReviewEvaluationUser().setUserId(userId).setMajorId(id)) == 1;
    }

    @Override
    public Boolean createClazzEvaluationUser(String clazzName, Integer userId) {
        Integer id = clazzMapper.getClazzIdByName(clazzName);
        if (id == null) return null;
        if (existsClazzEvaluationUser(id, userId)) return false;
        return clazzEvaluationUserMapper.insert(new ClazzEvaluationUser().setClazzId(id).setUserId(userId)) == 1;
    }

    @Override
    public Boolean createClazzReviewEvaluationUser(String clazzName, Integer userId) {
        Integer id = clazzMapper.getClazzIdByName(clazzName);
        if (id == null) return null;
        if (existsClazzReviewEvaluationUser(id, userId)) return false;
        return clazzReviewEvaluationUserMapper.insert(new ClazzReviewEvaluationUser().setClazzId(id).setUserId(userId)) == 1;
    }

    @Override
    public Boolean updateMajorEvaluationUser(EvaluationUserDTO evaluationUserDTO) {
        MajorEvaluationUser user = majorEvaluationUserMapper.selectById(evaluationUserDTO.getId());
        if (user == null) return null;
        Integer id = majorMapper.getMajorIdByName(evaluationUserDTO.getName());
        if (id == null) return null;
        return majorEvaluationUserMapper.updateById(user.setMajorId(id)) == 1;
    }

    @Override
    public Boolean updateMajorReviewEvaluationUser(EvaluationUserDTO evaluationUserDTO) {
        MajorReviewEvaluationUser user = majorReviewEvaluationUserMapper.selectById(evaluationUserDTO.getId());
        if (user == null) return null;
        Integer id = majorMapper.getMajorIdByName(evaluationUserDTO.getName());
        if (id == null) return null;
        return majorReviewEvaluationUserMapper.updateById(user.setMajorId(id)) == 1;
    }

    @Override
    public Boolean updateClazzEvaluationUser(EvaluationUserDTO evaluationUserDTO) {
        ClazzEvaluationUser user = clazzEvaluationUserMapper.selectById(evaluationUserDTO.getId());
        if (user == null) return null;
        Integer id = clazzMapper.getClazzIdByName(evaluationUserDTO.getName());
        if (id == null) return null;
        return clazzEvaluationUserMapper.updateById(user.setClazzId(id)) == 1;
    }

    @Override
    public Boolean updateClazzReviewEvaluationUser(EvaluationUserDTO evaluationUserDTO) {
        ClazzReviewEvaluationUser user = clazzReviewEvaluationUserMapper.selectById(evaluationUserDTO.getId());
        if (user == null) return null;
        Integer id = clazzMapper.getClazzIdByName(evaluationUserDTO.getName());
        if (id == null) return null;
        return clazzReviewEvaluationUserMapper.updateById(user.setClazzId(id)) == 1;
    }

    @Override
    public Boolean deleteEvaluationUser(Integer evaluationUserId, Integer kind) {
        if (kind == 0) {
            MajorEvaluationUser user = majorEvaluationUserMapper.selectById(evaluationUserId);
            if (user == null) return null;
            return majorEvaluationUserMapper.deleteById(evaluationUserId) == 1;
        } else if (kind == 1) {
            MajorReviewEvaluationUser user = majorReviewEvaluationUserMapper.selectById(evaluationUserId);
            if (user == null) return null;
            return majorReviewEvaluationUserMapper.deleteById(evaluationUserId) == 1;
        } else if (kind == 2) {
            ClazzEvaluationUser user = clazzEvaluationUserMapper.selectById(evaluationUserId);
            if (user == null) return null;
            return clazzEvaluationUserMapper.deleteById(evaluationUserId) == 1;
        } else if (kind == 3) {
            ClazzReviewEvaluationUser user = clazzReviewEvaluationUserMapper.selectById(evaluationUserId);
            if (user == null) return null;
            return clazzReviewEvaluationUserMapper.deleteById(evaluationUserId) == 1;
        }

        return null;
    }

    private EvaluationUserVO buildMajorEvaluationUserVO(
            Integer evaluationUserId,
            Integer userId,
            Integer clazzOrMajorId,
            String clazzOrMajorName,
            String kind
    ) {
        return new EvaluationUserVO()
                .setEvaluationUserId(evaluationUserId)
                .setUserId(userId)
                .setClazzOrMajorId(clazzOrMajorId)
                .setUsername(userMapper.selectUserNameById(userId))
                .setClazzOrMajorName(clazzOrMajorName)
                .setKind(kind);
    }

    private boolean existsMajorEvaluationUser(Integer majorId, Integer userId) {
        QueryWrapper<MajorEvaluationUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_id", majorId).eq("user_id", userId);
        return !majorEvaluationUserMapper.selectList(queryWrapper).isEmpty();
    }

    private boolean existsMajorReviewEvaluationUser(Integer majorId, Integer userId) {
        QueryWrapper<MajorReviewEvaluationUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_id", majorId).eq("user_id", userId);
        return !majorReviewEvaluationUserMapper.selectList(queryWrapper).isEmpty();
    }

    private boolean existsClazzEvaluationUser(Integer clazzId, Integer userId) {
        QueryWrapper<ClazzEvaluationUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("clazz_id", clazzId).eq("user_id", userId);
        return !clazzEvaluationUserMapper.selectList(queryWrapper).isEmpty();
    }

    private boolean existsClazzReviewEvaluationUser(Integer clazzId, Integer userId) {
        QueryWrapper<ClazzReviewEvaluationUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("clazz_id", clazzId).eq("user_id", userId);
        return !clazzReviewEvaluationUserMapper.selectList(queryWrapper).isEmpty();
    }
}
