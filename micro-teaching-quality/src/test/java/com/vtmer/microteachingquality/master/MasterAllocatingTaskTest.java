package com.vtmer.microteachingquality.master;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.Clazz;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzEvaluationUser;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.Major;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Colin_Knight
 * @create 2024/6/23 16:42
 */
@SpringBootTest
public class MasterAllocatingTaskTest {

    @Resource
    private ClazzMapper clazzMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MajorMapper majorMapper;

    @Resource
    private MajorEvaluationUserMapper majorEvaluationUserMapper;

    @Resource
    private ClazzExpertManageInfoMapper clazzExpertManageInfoMapper;

    @Resource
    private ClazzEvaluationUserMapper clazzEvaluationUserMapper;

    private Map<String, Integer> clazzIdList = new HashMap<>(16);

    // clazz  college
    private Map<String, String> clazzNoCreatedRecode = new HashMap<>(16);

    private List<String> userNoCreatedRecode = new ArrayList<>();


    /**
     * 分配课程专家任务
     */
    @Test
    public void majorTest() {
        //使用poi 从《2023校内课程评审专家安排.xlsx》读取数据
        try {
            String file = "G:\\Java\\program\\work\\school\\MegreRemote-tracking\\micro-teaching-quality\\评审2023\\2023校内专业评估专家组.xlsx";
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(1);
            Map<String, ClazzAllocationTask> clazzMap = new HashMap<>(64);

            Map<String, MasterAllocationTask> masterMap = new HashMap<>(64);

            String tempLeader = "";
            for (Row cells : sheet) {

                String college = cells.getCell(0).getStringCellValue();
                String major = cells.getCell(1).getStringCellValue();
                String group = cells.getCell(2).getStringCellValue();
                String leader = cells.getCell(3).getStringCellValue();
                String master1 = cells.getCell(4).getStringCellValue();
                String master2 = cells.getCell(5).getStringCellValue();
                String master3 = cells.getCell(6).getStringCellValue();
                if (!leader.isEmpty()) {
                    tempLeader = leader;
                }
                MasterAllocationTask temp = masterMap.getOrDefault(tempLeader, new MasterAllocationTask(tempLeader));
                temp.getTaskList().add(new ClazzInfoForTask(major, college));
                masterMap.put(tempLeader, temp);

                MasterAllocationTask task1 = masterMap.getOrDefault(master1, new MasterAllocationTask(master1));
                task1.getTaskList().add(new ClazzInfoForTask(major, college));
                masterMap.put(master1, task1);

                MasterAllocationTask task2 = masterMap.getOrDefault(master2, new MasterAllocationTask(master2));
                task2.getTaskList().add(new ClazzInfoForTask(major, college));
                masterMap.put(master2, task2);


                MasterAllocationTask task3 = masterMap.getOrDefault(master3, new MasterAllocationTask(master3));
                task3.getTaskList().add(new ClazzInfoForTask(major, college));
                masterMap.put(master3, task3);


                ClazzAllocationTask clazzAllocationTask = new ClazzAllocationTask(major, college, tempLeader, master1, master2, master3);
                clazzMap.put(major, clazzAllocationTask);

            }
            for (Map.Entry<String, MasterAllocationTask> stringMasterAllocationTaskEntry : masterMap.entrySet()) {
                System.out.println(stringMasterAllocationTaskEntry);
            }

            //执行任务
            System.out.println("开始执行任务");
            majorAllocationTask(masterMap);

            //处理未创建
            recodeUncreated();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @Test
    public void classTest() {
        //使用poi 从《2023校内课程评审专家安排.xlsx》读取数据
        try {
            String file = "G:\\Java\\program\\work\\school\\MegreRemote-tracking\\micro-teaching-quality\\评审2023\\2023校内课程评审专家安排.xlsx";
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(1);
            Map<String, ClazzAllocationTask> clazzMap = new HashMap<>(64);

            Map<String, MasterAllocationTask> masterMap = new HashMap<>(64);

            String tempLeader = "";
            for (Row cells : sheet) {

                String clazz = cells.getCell(0).getStringCellValue();
                String collage = cells.getCell(1).getStringCellValue();
                String type = cells.getCell(2).getStringCellValue();
                String leader = cells.getCell(3).getStringCellValue();
                String master1 = cells.getCell(4).getStringCellValue();
                String master2 = cells.getCell(5).getStringCellValue();
                String master3 = cells.getCell(6).getStringCellValue();
                if (!leader.isEmpty()) {
                    tempLeader = leader;
                }
                MasterAllocationTask temp = masterMap.getOrDefault(tempLeader, new MasterAllocationTask(tempLeader));
                temp.getTaskList().add(new ClazzInfoForTask(clazz, collage));
                masterMap.put(tempLeader, temp);

                MasterAllocationTask task1 = masterMap.getOrDefault(master1, new MasterAllocationTask(master1));
                task1.getTaskList().add(new ClazzInfoForTask(clazz, collage));
                masterMap.put(master1, task1);

                MasterAllocationTask task2 = masterMap.getOrDefault(master2, new MasterAllocationTask(master2));
                task2.getTaskList().add(new ClazzInfoForTask(clazz, collage));
                masterMap.put(master2, task2);


                MasterAllocationTask task3 = masterMap.getOrDefault(master3, new MasterAllocationTask(master3));
                task3.getTaskList().add(new ClazzInfoForTask(clazz, collage));
                masterMap.put(master3, task3);


                ClazzAllocationTask clazzAllocationTask = new ClazzAllocationTask(clazz, collage, tempLeader, master1, master2, master3);
                clazzMap.put(clazz, clazzAllocationTask);

            }
            for (Map.Entry<String, MasterAllocationTask> stringMasterAllocationTaskEntry : masterMap.entrySet()) {
                System.out.println(stringMasterAllocationTaskEntry);
            }

            //执行任务
            System.out.println("开始执行任务");
            allocationTask(masterMap);

            //处理未创建
            recodeUncreated();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 记录未创建的内容
     */
    private void recodeUncreated() {
        String uncreatedClassFilePath = "G:\\Java\\program\\work\\school\\MegreRemote-tracking\\micro-teaching-quality\\评审2023\\未创建专业.xlsx";
        String uncreatedUserFilePath = "G:\\Java\\program\\work\\school\\MegreRemote-tracking\\micro-teaching-quality\\评审2023\\专业评审未参加的用户.xlsx";

        try {

            Workbook uncreatedClassWorkbook = new XSSFWorkbook();
            Workbook uncreatedUserWorkbook = new XSSFWorkbook();


            Sheet classSheet = uncreatedClassSheet(uncreatedClassWorkbook);
            Sheet userSheet = uncreatedUserSheet(uncreatedUserWorkbook);


            FileOutputStream classFileOS = new FileOutputStream(uncreatedClassFilePath);
            uncreatedClassWorkbook.write(classFileOS);
            System.out.println("未创建课程记录完成");

            FileOutputStream userFileOS = new FileOutputStream(uncreatedUserFilePath);
            uncreatedUserWorkbook.write(userFileOS);
            System.out.println("未创建用户记录完成");


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 未创建用户表
     *
     * @param uncreatedUserWorkbook
     * @return
     */
    private Sheet uncreatedUserSheet(Workbook uncreatedUserWorkbook) {
        Sheet userSheet = uncreatedUserWorkbook.createSheet();
        Row title1 = userSheet.createRow(0);
        Cell user = title1.createCell(0);
        user.setCellValue("未创建用户");

        int userCount = 1;

        for (String string : userNoCreatedRecode) {
            Row row = userSheet.createRow(userCount++);
            Cell cell = row.createCell(0);
            cell.setCellValue(string);
        }

        return userSheet;
    }

    /**
     * 为创建课程表
     *
     * @param uncreatedClassWorkbook
     * @return
     * @throws IOException
     */
    private Sheet uncreatedClassSheet(Workbook uncreatedClassWorkbook) throws IOException {


        Sheet classSheet = uncreatedClassWorkbook.createSheet();

        Row title = classSheet.createRow(0);
        Cell clazz = title.createCell(0);
        clazz.setCellValue("未创建课程");

        Cell college = title.createCell(1);
        college.setCellValue("未创建课程对应专业");

        int classCount = 1;

        for (Map.Entry<String, String> entry : clazzNoCreatedRecode.entrySet()) {
            String clazzName = entry.getKey();
            String collegeName = entry.getValue();

            Row row = classSheet.createRow(classCount++);
            Cell cell1 = row.createCell(0);
            cell1.setCellValue(clazzName);

            Cell cell2 = row.createCell(1);
            cell2.setCellValue(collegeName);

        }
        return classSheet;
    }


    public void allocationTask(Map<String, MasterAllocationTask> masterMap) {

        System.out.println(masterMap);
        for (Map.Entry<String, MasterAllocationTask> entry : masterMap.entrySet()) {
            MasterAllocationTask masterAllocationTask = entry.getValue();

            String masterName = masterAllocationTask.getMasterName();
            User user = getUserIdByName(masterName);
            //记录未创建的用户
            if (user == null || user.getId() == null) {
                userNoCreatedRecode.add(masterName);
                continue;
            }
            for (ClazzInfoForTask clazzInfo : masterAllocationTask.getTaskList()) {
                List<Clazz> clazzes = getClazzId(clazzInfo);
                //记录未创建的课程
                if (clazzes == null || clazzes.isEmpty()) {
                    recordClazzNoCreated(clazzInfo);
                    continue;
                }
                //分配任务
                ClazzEvaluationUser clazzEvaluationUser = new ClazzEvaluationUser();
                clazzEvaluationUser.setClazzId(clazzes.get(0).getId());
                clazzEvaluationUser.setUserId(user.getId());
                //插入数据
                clazzEvaluationUserMapper.insert(clazzEvaluationUser);

            }

        }


    }

    public List<Clazz> getClazzId(ClazzInfoForTask clazzInfo) {
        QueryWrapper<Clazz> wrapper = new QueryWrapper<>();
        wrapper
                .eq("name", clazzInfo.getClazzName())
                .eq("college", clazzInfo.getCollege());

        return clazzMapper.selectList(wrapper);

    }

    public List<Major> getMajorId(ClazzInfoForTask majorInfo) {
        QueryWrapper<Major> wrapper = new QueryWrapper<>();
        wrapper.eq("college", majorInfo.getCollege())
                .eq("name", majorInfo.getClazzName());

        return majorMapper.selectList(wrapper);
    }

    /**
     * 通过用户名查找用户信息
     *
     * @param username
     * @return
     */
    private User getUserIdByName(String username) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>()
                        .eq("real_name", username)
                        .between("id", 520, 580)
        );
        if (user == null || user.getId() == null) {
            user = userMapper.selectOne(
                    new QueryWrapper<User>()
                            .eq("real_name", username)
                            .gt("id", 719)
            );
        }
        return user;
    }


    /**
     * 记录未创建的课程
     *
     * @param clazzInfo
     */
    public void recordClazzNoCreated(ClazzInfoForTask clazzInfo) {
        clazzNoCreatedRecode.put(clazzInfo.getClazzName(), clazzInfo.getCollege());
    }


    /**
     * 分配专业评审权限
     *
     * @param masterMap
     */
    public void majorAllocationTask(Map<String, MasterAllocationTask> masterMap) {

        System.out.println(masterMap);
        for (Map.Entry<String, MasterAllocationTask> entry : masterMap.entrySet()) {
            MasterAllocationTask masterAllocationTask = entry.getValue();

            String masterName = masterAllocationTask.getMasterName();
            User user = getUserIdByName(masterName);
            //记录未创建的用户
            if (user == null || user.getId() == null) {
                userNoCreatedRecode.add(masterName);
                continue;
            }
            for (ClazzInfoForTask clazzInfo : masterAllocationTask.getTaskList()) {
                List<Major> majorIdList = getMajorId(clazzInfo);
                //记录未创建的课程
                if (majorIdList == null || majorIdList.isEmpty()) {
                    recordClazzNoCreated(clazzInfo);
                    continue;
                }
                //分配任务
                MajorEvaluationUser majorEvaluationUser = new MajorEvaluationUser();
                majorEvaluationUser.setMajorId(majorIdList.get(0).getId());
                majorEvaluationUser.setUserId(user.getId());

                //插入数据
                majorEvaluationUserMapper.insert(majorEvaluationUser);

            }

        }


    }


}


@Data
@ToString
class ClazzAllocationTask {

    private String className;
    private String collageName;
    private String leader;
    private List<String> masters = new ArrayList<>();

    public ClazzAllocationTask() {
        this.masters = new ArrayList<>();
    }

    public ClazzAllocationTask(String className, String collageName, String leader, String... args) {

        this.className = className;
        this.collageName = collageName;
        this.leader = leader;
        masters.addAll(List.of(args));
    }


}


@Data
class MasterAllocationTask {
    private String masterName;
    private List<ClazzInfoForTask> taskList = new ArrayList<>();

    public MasterAllocationTask() {
    }

    public MasterAllocationTask(String masterName) {
        this.masterName = masterName;
    }

}

@Data
@AllArgsConstructor
class ClazzInfoForTask {
    private String clazzName;
    private String college;
}

