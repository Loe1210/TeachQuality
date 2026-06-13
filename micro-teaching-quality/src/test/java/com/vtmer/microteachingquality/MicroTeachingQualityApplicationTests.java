package com.vtmer.microteachingquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzEvaluationUser;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.OptionRecord;
import com.vtmer.microteachingquality.service.MajorEvaluationProcessService;
import com.vtmer.microteachingquality.service.MajorEvaluationRecodeService;
import com.vtmer.microteachingquality.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class MicroTeachingQualityApplicationTests {

    @Resource
    private MajorEvaluationProcessService majorEvaluationProcessService;


    @Resource
    private MajorEvaluationRecodeService majorEvaluationRecodeService;

    @Resource
    private ReportService reportService;

    @Resource
    private OptionRecordMapper optionRecordMapper;

    @Resource
    private MasterEvaluationMapper masterEvaluationMapper;
    @Resource
    private ClazzEvaluationUserMapper clazzEvaluationUserMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ClazzOpinionRecordMapper clazzOpinionRecordMapper;

    @Test
    void contextLoads() {


        System.out.println(majorEvaluationRecodeService.getSingleMajorFinishReview(1656142701805961216L, 83));

    }

    @Test
    void test() {
        System.out.println(reportService.listOptionByType("环境生态工程"));
    }

    @Test
    void test1() {
        System.out.println(majorEvaluationRecodeService.getSingleMajorFinishReview(1648139404829224962L, 580));
    }

    @Test
    void test2() {


        long majorEvaluationProcessId = 1648139404829224962L;


        //QW获取option_result的数据
        QueryWrapper<OptionRecord> optionResultQueryWrapper = new QueryWrapper<>();

        optionResultQueryWrapper.eq("major_evaluation_process_id", majorEvaluationProcessId);

        List<OptionRecord> optionRecords = optionRecordMapper.selectList(optionResultQueryWrapper);

        System.out.println(optionRecords);


    }

    @Test
    void test3() {
        System.out.println(majorEvaluationProcessService.getAllFinishedReviews(1648139404829224962L));
    }

    @Test
    void test4() {
        System.out.println(majorEvaluationRecodeService.getSingleMajorFinishReview(1641442913309888513L, 557));
    }

    @Test
    void test5() {
        System.out.println(masterEvaluationMapper.deleteById(100));
    }

    @Test
    void test6() {

        //查询User
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.between("id", 520, 561);

        List<User> users = userMapper.selectList(userQueryWrapper);

        //查询权限
        List<List<ClazzEvaluationUser>> collect = users.stream().map(user -> {
            QueryWrapper<ClazzEvaluationUser> clazzEvaluationUserQueryWrapper = new QueryWrapper<>();
            clazzEvaluationUserQueryWrapper.eq("user_id", user.getId());
            List<ClazzEvaluationUser> clazzEvaluationUsers = clazzEvaluationUserMapper.selectList(clazzEvaluationUserQueryWrapper);
            return clazzEvaluationUsers;
        }).collect(Collectors.toList());

        System.out.println(collect);

    }

}
