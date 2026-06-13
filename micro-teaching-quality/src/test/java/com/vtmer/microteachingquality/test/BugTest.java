package com.vtmer.microteachingquality.test;

import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.vo.MasterEvaluationResult;
import com.vtmer.microteachingquality.service.EvaluationProcessFileService;
import com.vtmer.microteachingquality.service.MajorEvaluationFileService;
import com.vtmer.microteachingquality.service.MajorEvaluationProcessService;
import com.vtmer.microteachingquality.service.ReportService;
import com.vtmer.microteachingquality.util.WriteExcelUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class BugTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EvaluationProcessFileService evaluationProcessFileService;

    @Autowired
    private MajorEvaluationProcessService majorEvaluationProcessService;
    @Autowired
    private MajorEvaluationFileService majorEvaluationFileService;

    @Resource
    private NowMapper nowMapper;

    @Resource
    private MajorEvaluationProcessMapper majorEvaluationProcessMapper;

    @Resource
    private LeaderEvaluationMapper leaderEvaluationMapper;
    @Resource
    private ClazzOpinionRecordMapper clazzOpinionRecordMapper;
    @Resource
    private MasterEvaluationMapper masterEvaluationMapper;
    @Autowired
    private ReportService reportService;

    @Test
    public void tests() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        System.out.println(bCryptPasswordEncoder.encode("00006340"));
//        System.out.println(bCryptPasswordEncoder.matches("wbrz080202", "$2a$10$JfCWjsaPtFNrjffBLPPlN.JRgjqydFNg.T6UUFYUecPCHT2/Or/TO"));
//        String[][] majors = {
//                {"管理学院 信息管理与信息系统", "120102"},
//                {"管理学院 工商管理", "120201K"},
//                {"管理学院 市场营销", "120202"},
//                {"管理学院 人力资源管理", "120206"},
//                {"管理学院 土地资源管理", "120404"},
//                {"外国语学院 商务英语", "050262"},
//                {"外国语学院 日语", "050207"},
//                {"数学与统计学院 信息与计算科学", "070102"},
//                {"艺术与设计学院 美术学", "130401"},
//                {"艺术与设计学院 视觉传达设计", "130502"},
//                {"艺术与设计学院 产品设计", "130504"},
//                {"艺术与设计学院 服装与服饰设计", "130505"},
//                {"艺术与设计学院 表演", "130301"}
//        };
//
//        for (String[] major : majors) {
//            System.out.println(major[0] + " " + major[1] + " " + bCryptPasswordEncoder.encode(major[1]));
//        }
    }

    @Test
    public void getAllClazz() {
        MasterEvaluationResult result = reportService.getMasterEvaluationByUserIdAndMajorNameWithCache(83, "网络工程", false);
//        QueryWrapper<MasterEvaluation> queryWrapper = new QueryWrapper<>();
//        List<MasterEvaluation> masterEvaluations = masterEvaluationMapper.selectList(queryWrapper);
//        for (MasterEvaluation masterEvaluation : masterEvaluations) {
//            MajorEvaluationProcess majorEvaluationProcess = majorEvaluationProcessMapper.selectById(masterEvaluation.getMajorEvaluationProcessId());
//            if (majorEvaluationProcess != null) {
//                masterEvaluation.setMajorId(majorEvaluationProcess.getMajorId());
//                masterEvaluationMapper.updateById(masterEvaluation);
//            }
//        }
    }

    @Test
    public void getAllMajor() {
        List<Map<String, Object>> all = masterEvaluationMapper.selectAllInYear();
        Map<String, List<Map<String, Object>>> res = new HashMap<>();
        for (Map<String, Object> map : all) {
            String college = map.get("college").toString();
            if (!res.containsKey(college)) {
                res.put(college, new ArrayList<>());
            }

            res.get(college).add(map);
        }

        WriteExcelUtils.writeAllMajorDetails(res);
    }

    @Test
    public void test() {
        String[] names = {"陈位志", "成思源", "何嘉年", "谢武明", "张成科", "周祥"};
        Map<String, Map<String, String>> res = new HashMap<>();
        for (String name : names) {
            List<Map<String, Object>> list = nowMapper.getDetailsByName(name);
            for (Map<String, Object> map : list) {
                String college = map.get("college").toString();
                String clazz = map.get("name").toString();
                String remark = map.get("remark").toString();
                if (!res.containsKey(college)) {
                    res.put(college, new HashMap<>());
                }

                res.get(college).put(clazz, remark);
            }
        }

        WriteExcelUtils.write(res);
    }

    @Test
    public void testProtectedEndpoint() throws Exception {

        List<Map<String, Object>> list = nowMapper.getDetailsByName("张成科");
        Map<String, Long> res = new HashMap<>();
        for (Map<String, Object> map : list) {
            String name = map.get("name").toString();
            String id = map.get("evaluation_id").toString();
            res.put(name, Long.parseLong(id));
        }
        String names =
                "习近平新时代中国特色社会主义思想概论\n" +
                        "形势与政策\n" +
                        "编程基础\n" +
                        "设计色彩\n" +
                        "书法与篆刻\n" +
                        "广告创意设计\n" +
                        "室内设计原理\n";
        String[] split = names.split("\n");
        for (String s : split) {
            evaluationProcessFileService.generateClazzEvaluationReport(res.get(s));
        }


//        Long[] nums = new Long[]{
//                1811049637595840512L,
//                1763597822478778368L,
//                1762754166242934784L,
//                1804830659483533312L,
//                1805798303019827200L,
//                1805250388064993280L,
//                1805218068985544704L
//        };
//        for (Long num : nums) {
//            evaluationProcessFileService.generateClazzEvaluationReport(num);
//        }
    }
}
