package com.hung.microoauth2auth.service;

import cn.hutool.core.collection.CollUtil;
import com.hung.microoauth2auth.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 资源与角色匹配关系管理业务类
 *
 * @author Hung
 * @date 2021年11月15日 18:40
 */
@Service
public class ResourceServiceImpl {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void initData() {
        Map<String, List<String>> resourceRolesMap = new TreeMap<>();
        resourceRolesMap.put("/teaching/clazz", CollUtil.toList("all", "clazz_principal", "clazz_expert_leader", "clazz_expert"));
        resourceRolesMap.put("/teaching/coursereport", CollUtil.toList("all", "clazz_principal", "clazz_expert_leader", "clazz_expert"));
        resourceRolesMap.put("/teaching/ClazzExpertLeader", CollUtil.toList("all", "clazz_principal", "clazz_expert_leader", "clazz_expert"));
        resourceRolesMap.put("/teaching/courseEvaluationExpert", CollUtil.toList("all", "clazz_principal", "clazz_expert_leader", "clazz_expert"));
        resourceRolesMap.put("/teaching/report", CollUtil.toList("all", "major_archive_expert", "major_archive_principal"));
        resourceRolesMap.put("/teaching/user", CollUtil.toList("all", "major_evaluation_principal", "major_evaluation_expert", "major_evaluation_expert_leader", "clazz_principal", "clazz_expert_leader", "clazz_expert", "major_archive_expert", "major_archive_principal"));
        resourceRolesMap.put("/teaching/major", CollUtil.toList("all", "major_evaluation_principal", "major_evaluation_expert", "major_evaluation_expert_leader", "major_archive_expert", "major_archive_principal"));
        resourceRolesMap.put("/teaching/clazzEvaluation", CollUtil.toList("all", "clazz_principal", "clazz_expert_leader", "clazz_expert"));
        resourceRolesMap.put("/teaching/notify", CollUtil.toList("all", "major_evaluation_principal", "major_evaluation_expert", "major_evaluation_expert_leader", "clazz_principal", "clazz_expert_leader", "clazz_expert", "major_archive_expert", "major_archive_principal"));
        resourceRolesMap.put("/teaching/majorArchive", CollUtil.toList("all", "major_archive_expert", "major_archive_principal"));
        resourceRolesMap.put("/teaching/majorEvaluation", CollUtil.toList("all", "major_evaluation_principal", "major_evaluation_expert", "major_evaluation_expert_leader"));
        redisTemplate.opsForHash().putAll(RedisConstant.RESOURCE_ROLES_MAP, resourceRolesMap);
    }
}
