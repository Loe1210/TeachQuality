package com.vtmer.microteachingquality.userTest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.vtmer.microteachingquality.mapper.ClazzMapper;
import com.vtmer.microteachingquality.mapper.UserMapper;
import com.vtmer.microteachingquality.mapper.UserRoleMapper;
import com.vtmer.microteachingquality.model.dto.InsertUserDTO;
import com.vtmer.microteachingquality.model.dto.UserDTO;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.UserRole;
import com.vtmer.microteachingquality.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;

/**
 * @author Colin_Knight
 * @create 2023/9/15 2:12
 */
@SpringBootTest
@Slf4j
public class UserRegisterTest {

    private final String COURSE_LEADER = "课程负责人";
    private final String MAJOR_LEADER = "专业负责人";
    @Resource
    private ClazzMapper clazzMapper;
    @Resource
    private UserService userService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Resource
    private UserRoleMapper userRoleMapper;

    @org.junit.jupiter.api.Test
    public void userRegisterTest() throws Exception {
        String path = "2023课程评估账号密码.xlsx";

        File file = new File(path);

        FileInputStream fileInputStream = new FileInputStream(file);

        Workbook workbook = new XSSFWorkbook(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        int rowSizes = sheet.getPhysicalNumberOfRows();
        int rowCounts;
        for (rowCounts = 1; rowCounts < rowSizes; rowCounts++) {
            Row row = sheet.getRow(rowCounts);
            if (row != null) {

                String username = row.getCell(0).getStringCellValue();
                String userId = row.getCell(1).getStringCellValue();


                String type = COURSE_LEADER;
                String clazzName = row.getCell(2).getStringCellValue();
                String college = row.getCell(1).getStringCellValue();
                String majorName = row.getCell(2).getStringCellValue();
                String account = row.getCell(4).getStringCellValue();
                String password = row.getCell(5).getStringCellValue();


                InsertUserDTO insertUserDTO = new InsertUserDTO(account, clazzName, type, clazzName, 1);


                User user = new User();
                user.setUserPwd(passwordEncoder.encode(password));

                UserDTO userDTO = new UserDTO();
                BeanUtils.copyProperties(insertUserDTO, userDTO);

                userDTO.setUserPwd(password);


                if (userMapper.exists(new QueryWrapper<User>().eq("user_name", userDTO.getUserName()))) {
                    userMapper.update(null,
                            new UpdateWrapper<User>().eq("user_name", userDTO.getUserName())
                                    .set("user_pwd", user.getUserPwd()));
                } else {
                    if (userService.createUser(userDTO)) {
                        log.info("注册成功:{}", clazzName);
                    } else {
                        log.info("注册失败:{}", clazzName);
                    }
                }


            }
        }

        System.out.println("插入的总数" + rowCounts);

    }

    @Test
    public void classMasterRegisterTest() throws Exception {
        String path = "G:\\Java\\program\\work\\school\\MegreRemote-tracking\\micro-teaching-quality\\评审2023\\未注册专家.xlsx";

        String type = "课程评估专家";

        File file = new File(path);

        FileInputStream fileInputStream = new FileInputStream(file);

        Workbook workbook = new XSSFWorkbook(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        int rowSizes = sheet.getPhysicalNumberOfRows();
        int rowCounts;
        for (rowCounts = 1; rowCounts < rowSizes; rowCounts++) {
            Row row = sheet.getRow(rowCounts);
            if (row != null) {

                String username = row.getCell(0).getStringCellValue();
                String userId = row.getCell(1).getStringCellValue();


                InsertUserDTO insertUserDTO = new InsertUserDTO(userId, username, type, type, 1);


                User user = new User();
                user.setUserPwd(passwordEncoder.encode(userId));

                UserDTO userDTO = new UserDTO();
                BeanUtils.copyProperties(insertUserDTO, userDTO);

                userDTO.setUserPwd(userId);

                if (userMapper.exists(new QueryWrapper<User>().eq("user_name", userDTO.getUserName()))) {
                    userMapper.update(null,
                            new UpdateWrapper<User>().eq("user_name", userDTO.getUserName())
                                    .set("user_pwd", user.getUserPwd()));
                } else {
                    if (userService.createUser(userDTO)) {
                        log.info("注册成功:{}", username);
                    } else {
                        log.info("注册失败:{}", username);
                    }
                }

                changeUserRole(userId);

            }
        }

        System.out.println("插入的总数" + rowCounts);

    }

    @Test
    public void majorMasterRegisterTest() throws Exception {
        String path = "G:\\Java\\program\\work\\school\\MegreRemote-tracking\\micro-teaching-quality\\评审2023\\专业评审未参加的用户.xlsx";

        String type = "专业评估专家";

        File file = new File(path);

        FileInputStream fileInputStream = new FileInputStream(file);

        Workbook workbook = new XSSFWorkbook(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        int rowSizes = sheet.getPhysicalNumberOfRows();
        int rowCounts;
        for (rowCounts = 1; rowCounts < rowSizes; rowCounts++) {
            Row row = sheet.getRow(rowCounts);
            if (row != null) {

                String username = row.getCell(0).getStringCellValue();
                String userId = row.getCell(1).getStringCellValue();


                InsertUserDTO insertUserDTO = new InsertUserDTO(userId, username, type, type, 1);


                User user = new User();
                user.setUserPwd(passwordEncoder.encode(userId));

                UserDTO userDTO = new UserDTO();
                BeanUtils.copyProperties(insertUserDTO, userDTO);

                userDTO.setUserPwd(userId);

                if (userMapper.exists(new QueryWrapper<User>().eq("user_name", userDTO.getUserName()))) {
                    userMapper.update(null,
                            new UpdateWrapper<User>().eq("user_name", userDTO.getUserName())
                                    .set("user_pwd", user.getUserPwd()));
                } else {
                    if (userService.createUser(userDTO)) {
                        log.info("注册成功:{}", username);
                    } else {
                        log.info("注册失败:{}", username);
                    }
                }

                majorChangeUserRole(userId);

            }
        }

        System.out.println("插入的总数" + rowCounts);

    }

    private void changeUserRole(String username) {

        User u = userMapper.selectOne(new QueryWrapper<User>().eq("user_name", username));

        userRoleMapper.update(null, new UpdateWrapper<UserRole>()
                .eq("user_id", u.getId())
                .eq("role_id", 4)
                .set("role_id", 2)
        );

    }

    private void majorChangeUserRole(String username) {

        User u = userMapper.selectOne(new QueryWrapper<User>().eq("user_name", username));

        userRoleMapper.update(null, new UpdateWrapper<UserRole>()
                .eq("user_id", u.getId())
                .eq("role_id", 5)
                .set("role_id", 6)
        );

    }

    @Test
    public void testChangeUserRole() {
        changeUserRole("00006326");
    }


    @org.junit.jupiter.api.Test
    public void majorRegisterTest() throws Exception {
        String path = "2023专业评估账号密码.xlsx";

        File file = new File(path);

        FileInputStream fileInputStream = new FileInputStream(file);

        Workbook workbook = new XSSFWorkbook(fileInputStream);
        Sheet sheet = workbook.getSheetAt(0);
        int rowSizes = sheet.getPhysicalNumberOfRows();
        int rowCounts;
        for (rowCounts = 1; rowCounts < rowSizes; rowCounts++) {
            Row row = sheet.getRow(rowCounts);
            if (row != null) {
                String type = MAJOR_LEADER;
                String userName = row.getCell(2).getStringCellValue();
                String college = row.getCell(1).getStringCellValue();
                String majorName = row.getCell(1).getStringCellValue();
                String account = row.getCell(2).getStringCellValue();
                String password = row.getCell(3).getStringCellValue();


                InsertUserDTO insertUserDTO = new InsertUserDTO(account, majorName, type, majorName, 1);
                System.out.println(insertUserDTO);


                User user = new User();
                user.setUserPwd(passwordEncoder.encode(password));


                UserDTO userDTO = new UserDTO();
                BeanUtils.copyProperties(insertUserDTO, userDTO);

                userDTO.setUserPwd(password);


                if (userMapper.exists(new QueryWrapper<User>().eq("user_name", userDTO.getUserName()))) {
                    userMapper.update(null,
                            new UpdateWrapper<User>().eq("user_name", userDTO.getUserName())
                                    .set("user_pwd", user.getUserPwd()));
                } else {
                    if (userService.createUser(userDTO)) {
                        log.info("注册成功:{}", majorName);
                    } else {
                        log.info("注册失败:{}", majorName);
                    }
                }


            }
        }

        System.out.println("插入的总数" + rowCounts);

    }


    @Test
    public void SimpleUserRegisterTest() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName("080904K");
        userDTO.setRealName("信息安全");
        userDTO.setUserBelong("信息安全");
        userDTO.setUserType(MAJOR_LEADER);
        userDTO.setUserPwd("pgzxM@2016");


        User user = new User();
        user.setUserPwd(passwordEncoder.encode("pgzxM@2016"));


        if (userMapper.exists(new QueryWrapper<User>().eq("user_name", userDTO.getUserName()))) {
            userMapper.update(null,
                    new UpdateWrapper<User>().eq("user_name", userDTO.getUserName())
                            .set("user_pwd", user.getUserPwd()));
        } else {
            if (userService.createUser(userDTO)) {
                log.info("注册成功:{}", userDTO.getRealName());
            } else {
                log.info("注册失败:{}", userDTO.getRealName());
            }
        }
    }


}
