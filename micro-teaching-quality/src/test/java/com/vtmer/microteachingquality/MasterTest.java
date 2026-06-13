package com.vtmer.microteachingquality;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.bo.SelectMajorEvaluationListBO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.Clazz;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzOpinionLeaderRecord;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzOpinionRecord;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.LeaderEvaluation;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.Major;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MasterEvaluation;
import com.vtmer.microteachingquality.model.vo.ClazzOpinionLeaderRecordVO;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationProcessSimpleInfoVo;
import com.vtmer.microteachingquality.model.vo.MajorLeaderEvaluationInfoVO;
import com.vtmer.microteachingquality.model.vo.MajorMasterEvaluationInfoVo;
import com.vtmer.microteachingquality.service.MajorEvaluationProcessService;
import com.vtmer.microteachingquality.service.MajorEvaluationRecodeService;
import com.vtmer.microteachingquality.service.ReportService;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Colin_Knight
 * @create 2023/6/26 16:00
 */
@SpringBootTest
public class MasterTest {


    @Resource
    private LeaderEvaluationMapper leaderEvaluationMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MajorEvaluationProcessMapper majorEvaluationProcessMapper;

    @Resource
    private MajorMapper majorMapper;
    @Resource
    private ClazzOpinionLeaderRecordMapper clazzOpinionLeaderRecordMapper;
    @Resource
    private ClazzMapper clazzMapper;
    @Resource
    private ClazzOpinionRecordMapper clazzOpinionRecordMapper;
    @Resource
    private MasterEvaluationMapper masterEvaluationMapper;
    @Resource
    private ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;

    @Resource
    private MajorEvaluationProcessService majorEvaluationProcessService;

    @Resource
    private ReportService reportService;
    @Resource
    private MajorEvaluationRecodeService majorEvaluationRecodeService;
//    @Resource
//    private MasterEvaluationMapper masterEvaluationMapper;

    @Test
    void test() {
        List<MajorEvaluationProcessSimpleInfoVo> evaluationProcesses = majorEvaluationProcessService.getEvaluationProcesses(new SelectMajorEvaluationListBO(208, 1, 1));
        System.out.println(evaluationProcesses);
//        System.out.println(masterEvaluationMapper.deleteById(174));
//
//        MasterEvaluateBO masterEvaluateBO = new MasterEvaluateBO();
//        masterEvaluateBO.setMajorEvaluationProcessId("1907352773226135552");
//        masterEvaluateBO.setOpinion("da");
//        HashMap<Integer, String> map = new HashMap<>();
//        map.put(231, "通过");
//        map.put(232, "通过");
//        map.put(233, "通过");
//        map.put(234, "通过");
//        map.put(235, "通过");
//        map.put(236, "通过");
//        map.put(237, "通过");
//        masterEvaluateBO.setOptionMap(map);
//        masterEvaluateBO.setRemark("da");
//        majorEvaluationProcessService.saveMasterEvaluation(masterEvaluateBO);
//        System.out.println(reportService.listOptionByType("国际贸易"));
//        MajorEvaluationResult singleMajorFinishReview = majorEvaluationRecodeService.getSingleMajorFinishReview(1907352773226135552L, 83);
//        System.out.println(singleMajorFinishReview);
    }

    /**
     * 获取专业评审组长意见
     */
    @Test
    public void MajorLeaderEvaluationCollection() {
        QueryWrapper<LeaderEvaluation> leaderEvaluationQueryWrapper = new QueryWrapper<>();
        leaderEvaluationQueryWrapper.ge("id", 33);
        List<LeaderEvaluation> leaderEvaluations = leaderEvaluationMapper.selectList(leaderEvaluationQueryWrapper);
        List<MajorLeaderEvaluationInfoVO> majorLeaderEvaluationInfoVOS = leaderEvaluations.stream().map(leaderEvaluation -> {


            Integer userId = leaderEvaluation.getUserId();
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("id", userId);
            User user = userMapper.selectOne(userQueryWrapper);

            QueryWrapper<MajorEvaluationProcess> majorEvaluationProcessQueryWrapper = new QueryWrapper<>();

            QueryWrapper<Major> majorQueryWrapper = new QueryWrapper<>();


            majorEvaluationProcessQueryWrapper.eq("id", leaderEvaluation.getMajorEvaluationProcessId());

            MajorEvaluationProcess majorEvaluationProcess = majorEvaluationProcessMapper.selectOne(majorEvaluationProcessQueryWrapper);

            majorQueryWrapper.eq("id", majorEvaluationProcess.getMajorId());

            Major major = majorMapper.selectOne(majorQueryWrapper);

            MajorLeaderEvaluationInfoVO majorLeaderEvaluationInfoVO = new MajorLeaderEvaluationInfoVO(user, major, leaderEvaluation.getResult(), leaderEvaluation.getOpinion());


            return majorLeaderEvaluationInfoVO;


        }).collect(Collectors.toList());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream("专业评审组长评审.xlsx")
        ) {

            XSSFSheet sheet = workbook.createSheet();
            int num = 0;
            XSSFRow row = sheet.createRow(num++);

            XSSFCell cell;

            row.createCell(0).setCellValue("姓名");
            row.createCell(1).setCellValue("专业名");
            row.createCell(2).setCellValue("评价");
            row.createCell(3).setCellValue("组长建议");

            for (MajorLeaderEvaluationInfoVO majorLeaderEvaluationInfoVO : majorLeaderEvaluationInfoVOS) {
                row = sheet.createRow(num++);

                cell = row.createCell(0);
                cell.setCellValue(majorLeaderEvaluationInfoVO.getUsername());
                cell = row.createCell(1);
                cell.setCellValue(majorLeaderEvaluationInfoVO.getMajorName());
                cell = row.createCell(2);
                cell.setCellValue(majorLeaderEvaluationInfoVO.getResult());
                cell = row.createCell(3);
                cell.setCellValue(majorLeaderEvaluationInfoVO.getOpinion());
            }

            workbook.write(fileOut);

            workbook.close();
            fileOut.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 提取课程组长评审结果
     */
    @Test
    public void ClazzLeaderEvaluationCollection() {
        QueryWrapper<ClazzOpinionLeaderRecord> clazzOpinionLeaderRecordQueryWrapper = new QueryWrapper<>();
        clazzOpinionLeaderRecordQueryWrapper.ge("id", 13);

        List<ClazzOpinionLeaderRecord> clazzOpinionLeaderRecords = clazzOpinionLeaderRecordMapper.selectList(clazzOpinionLeaderRecordQueryWrapper);


        List<ClazzOpinionLeaderRecordVO> clazzOpinionLeaderRecordVOS = clazzOpinionLeaderRecords.stream().map(clazzOpinionLeaderRecord -> {


            QueryWrapper<Clazz> clazzQueryWrapper = new QueryWrapper<>();
            clazzQueryWrapper.eq("id", clazzOpinionLeaderRecord.getClazzId());
            Clazz clazz = clazzMapper.selectOne(clazzQueryWrapper);

            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("id", clazzOpinionLeaderRecord.getUserId());
            User user = userMapper.selectOne(userQueryWrapper);

            QueryWrapper<ClazzOpinionRecord> clazzOpinionRecordQueryWrapper = new QueryWrapper<>();
            clazzOpinionRecordQueryWrapper.eq("user_id", clazzOpinionLeaderRecord.getUserId());
            clazzOpinionRecordQueryWrapper.eq("clazz_evaluation_process_id", clazzOpinionLeaderRecord.getClazzEvaluationProcessId());

            ClazzOpinionRecord clazzOpinionRecord = clazzOpinionRecordMapper.selectOne(clazzOpinionRecordQueryWrapper);

            ClazzOpinionLeaderRecordVO clazzOpinionLeaderRecordVO = new ClazzOpinionLeaderRecordVO(clazz, user, clazzOpinionLeaderRecord.getEvaluationOpinion(), clazzOpinionRecord);


            return clazzOpinionLeaderRecordVO;


        }).collect(Collectors.toList());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream("课程评审组长评审.xlsx")
        ) {

            XSSFSheet sheet = workbook.createSheet();
            int num = 0;
            XSSFRow row = sheet.createRow(num++);

            XSSFCell cell;

            row.createCell(0).setCellValue("姓名");
            row.createCell(1).setCellValue("课程名");
            row.createCell(2).setCellValue("总评");
            row.createCell(3).setCellValue("评价");

            for (ClazzOpinionLeaderRecordVO clazzOpinionLeaderRecordVO : clazzOpinionLeaderRecordVOS) {
                row = sheet.createRow(num++);

                cell = row.createCell(0);
                cell.setCellValue(clazzOpinionLeaderRecordVO.getUsername());
                cell = row.createCell(1);
                cell.setCellValue(clazzOpinionLeaderRecordVO.getClazzName());
                cell = row.createCell(2);
                cell.setCellValue(clazzOpinionLeaderRecordVO.getRemark());
                cell = row.createCell(3);
                cell.setCellValue(clazzOpinionLeaderRecordVO.getEvaluationOpinion());

            }

            workbook.write(fileOut);

            workbook.close();
            fileOut.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 提取课程专家
     */
/*    @Test
    public void ClazzEvaluationCollection() {
        QueryWrapper<ClazzOpinionRecord> clazzOpinionRecordQueryWrapper = new QueryWrapper<>();
        clazzOpinionRecordQueryWrapper.in("user_id", 527, 549);

        List<ClazzOpinionRecord> clazzOpinionRecords = clazzOpinionRecordMapper.selectList(clazzOpinionRecordQueryWrapper);

        List<ClazzOpinionRecordVO> clazzOpinionRecordVOS = clazzOpinionRecords.stream().map(clazzOpinionRecord -> {
            QueryWrapper<ClassEvaluationProcess> classEvaluationProcessQueryWrapper = new QueryWrapper<>();
            classEvaluationProcessQueryWrapper.eq("evaluation_id", clazzOpinionRecord.getClazzEvaluationProcessId());
            ClassEvaluationProcess classEvaluationProcess = clazzEvaluationProcessMapper.selectOne(classEvaluationProcessQueryWrapper);

            QueryWrapper<Clazz> clazzQueryWrapper = new QueryWrapper<>();
            clazzQueryWrapper.eq("id", classEvaluationProcess.getClazzId());
            Clazz clazz = clazzMapper.selectOne(clazzQueryWrapper);

            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("id", clazzOpinionRecord.getUserId());
            User user = userMapper.selectOne(userQueryWrapper);


            return new ClazzOpinionRecordVO(clazz, user, clazzOpinionRecord);
        }).collect(Collectors.toList());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream("课程评审专家评审.xlsx")
        ) {

            XSSFSheet sheet = workbook.createSheet();
            int num = 0;
            XSSFRow row = sheet.createRow(num++);

            XSSFCell cell;

            row.createCell(0).setCellValue("姓名");
            row.createCell(1).setCellValue("课程名");
            row.createCell(2).setCellValue("总评");
            row.createCell(3).setCellValue("课程优势");
            row.createCell(4).setCellValue("课程问题");
            row.createCell(5).setCellValue("课程建议");

            for (ClazzOpinionRecordVO clazzOpinionRecordVO : clazzOpinionRecordVOS) {
                row = sheet.createRow(num++);

                cell = row.createCell(0);
                cell.setCellValue(clazzOpinionRecordVO.getUsername());
                cell = row.createCell(1);
                cell.setCellValue(clazzOpinionRecordVO.getClazzName());
                cell = row.createCell(2);
                cell.setCellValue(clazzOpinionRecordVO.getRemark());
                cell = row.createCell(3);
                cell.setCellValue(clazzOpinionRecordVO.getAdvantage());
                cell = row.createCell(4);
                cell.setCellValue(clazzOpinionRecordVO.getProblem());
                cell = row.createCell(5);
                cell.setCellValue(clazzOpinionRecordVO.getAdvice());
            }

            workbook.write(fileOut);

            workbook.close();
            fileOut.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    @Test
    public void MajorExpertEvaluationCollection() {


        QueryWrapper<MasterEvaluation> masterEvaluationQueryWrapper = new QueryWrapper<>();
        masterEvaluationQueryWrapper.ge("id", 83);
        List<MasterEvaluation> masterEvaluations = masterEvaluationMapper.selectList(masterEvaluationQueryWrapper);
        List<MajorMasterEvaluationInfoVo> majorMasterEvaluationInfoVos = masterEvaluations.stream().map(masterEvaluation -> {


            Integer userId = masterEvaluation.getUserId();
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("id", userId);
            User user = userMapper.selectOne(userQueryWrapper);

            QueryWrapper<MajorEvaluationProcess> majorEvaluationProcessQueryWrapper = new QueryWrapper<>();

            QueryWrapper<Major> majorQueryWrapper = new QueryWrapper<>();


            majorEvaluationProcessQueryWrapper.eq("id", masterEvaluation.getMajorEvaluationProcessId());

            MajorEvaluationProcess majorEvaluationProcess = majorEvaluationProcessMapper.selectOne(majorEvaluationProcessQueryWrapper);

            majorQueryWrapper.eq("id", majorEvaluationProcess.getMajorId());

            Major major = majorMapper.selectOne(majorQueryWrapper);

            MajorMasterEvaluationInfoVo majorMasterEvaluationInfoVo = new MajorMasterEvaluationInfoVo(user, major, masterEvaluation.getRemark(), masterEvaluation.getOpinion());

            return majorMasterEvaluationInfoVo;


        }).collect(Collectors.toList());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream("专业评审专家评审.xlsx")
        ) {

            XSSFSheet sheet = workbook.createSheet();
            int num = 0;
            XSSFRow row = sheet.createRow(num++);

            XSSFCell cell;

            row.createCell(0).setCellValue("姓名");
            row.createCell(1).setCellValue("专业名");
            row.createCell(2).setCellValue("评价");
            row.createCell(3).setCellValue("专家建议");

            for (MajorMasterEvaluationInfoVo majorMasterEvaluationInfoVo : majorMasterEvaluationInfoVos) {
                row = sheet.createRow(num++);

                cell = row.createCell(0);
                cell.setCellValue(majorMasterEvaluationInfoVo.getUsername());
                cell = row.createCell(1);
                cell.setCellValue(majorMasterEvaluationInfoVo.getMajorName());
                cell = row.createCell(2);
                cell.setCellValue(majorMasterEvaluationInfoVo.getResult());
                cell = row.createCell(3);
                cell.setCellValue(majorMasterEvaluationInfoVo.getOpinion());
            }

            workbook.write(fileOut);

            workbook.close();
            fileOut.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //    @Autowired
    private MockMvc mockMvc;

    @Test
    public void teefaf() throws Exception {
        File file = new File("D:\\codes\\java\\TeachQuality-develop\\micro-teaching-quality\\专业评审专家评审.xlsx");
        FileInputStream inputStream = new FileInputStream(file);
        MockMultipartFile mockFile = new MockMultipartFile("file", "file.txt", "text/plain", inputStream);
        majorEvaluationProcessService.principalUploadMaterial(mockFile, 1863591168181272576L);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/principalUpload/12345")
                .file(mockFile))
                .andExpect(status().isOk());
    }


}
