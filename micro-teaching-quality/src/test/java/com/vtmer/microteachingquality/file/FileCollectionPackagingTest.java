package com.vtmer.microteachingquality.file;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.*;
import com.vtmer.microteachingquality.mapper.ClazzEvaluationProcessMapper;
import com.vtmer.microteachingquality.mapper.ClazzFileMapper;
import com.vtmer.microteachingquality.mapper.ClazzMapper;
import com.vtmer.microteachingquality.mapper.UserMapper;
import com.vtmer.microteachingquality.model.pojo.User;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * 打包已经提交的文件夹，需要修改用户参数，根据需要提交的用户来实现
 *
 * @author Colin_Knight
 * @create 2023/11/14 20:33
 */
@SpringBootTest
@Slf4j
public class FileCollectionPackagingTest {

    final static List<String> UNSUBMIT_FILE = new LinkedList<>();
    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);
    //自己创建文件夹保存
    String classPath = "C:\\Users\\雪碧\\Desktop\\2023上传评审资料\\课程";
    String majorPath = "C:\\Users\\雪碧\\Desktop\\2023上传评审资料\\专业";
    @Value("${clazz.path}")
    private String clazzPath;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ClazzFileMapper clazzFileMapper;

    @Autowired
    private ClazzEvaluationProcessMapper cepm;

    @Resource
    private ClazzMapper clazzMapper;

    public static void writeListToExcel(List<String> dataList, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            int rownum = 0;
            for (String data : dataList) {
                Row row = sheet.createRow(rownum++);
                Cell cell = row.createCell(0, CellType.STRING);
                cell.setCellValue(data);
            }

            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void classFilePacking() {


        String filePath = "2023课程评估账号密码.xlsx";
        boolean flag = true;

        try (InputStream fileInputStream = new FileInputStream(filePath)) {

            //sftp连接
            JSch jSch = new JSch();
            Session session = jSch.getSession("root", "119.91.200.72", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword("vtmer2020");
            session.connect();
            System.out.println(session.isConnected());

            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();


            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0); // 假设你要读取第一个工作表


            for (Row row : sheet) {
                if (flag) {
                    flag = false;
                    continue;
                }
                Cell cell1 = row.getCell(0); // 读取第一列数据 序号
                Cell cell2 = row.getCell(1); // 读取第二列数据  学院
                Cell cell3 = row.getCell(2); // 读取第三列数据  课程名称
                Cell cell4 = row.getCell(3); // 读取第四列数据  参考学期
                Cell cell5 = row.getCell(4); // 读取第五列数据  账号
                Cell cell6 = row.getCell(5); // 读取第六列数据  密码

                String college = cell2.getStringCellValue();
                String className = cell3.getStringCellValue();
                String account = cell5.getStringCellValue();
                System.out.println("collage:" + college + " className:" + className + " account:" + account);

                //获取用户信息
                User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_name", account));

                List<ClassEvaluationProcess> classEvaluationProcesses = cepm
                        .selectList(
                                new QueryWrapper<ClassEvaluationProcess>()
                                        .eq("creator_id", user.getId()));


                //获取课程信息
//                Clazz clazz = clazzMapper.selectOne(
//                        new QueryWrapper<Clazz>().eq("major", className));
//                if(clazz == null){
//                    UNSUBMIT_FILE.add(className);
//                    continue;
//                }


                //查看创建的流程
//                List<ClassEvaluationProcess> classEvaluationProcesses = cepm
//                        .selectList(
//                                new QueryWrapper<ClassEvaluationProcess>()
//                                        .eq("clazz_id",clazz.getId()));

                if (classEvaluationProcesses == null || classEvaluationProcesses.size() == 0) {
                    UNSUBMIT_FILE.add(className);
                    continue;
                }
                String localPath = classFileCreate(college, className);

                if (localPath == null) {
                    continue;
                }

                //对流程进行处理
                for (ClassEvaluationProcess process : classEvaluationProcesses) {
                    Long evaluationId = process.getId();
                    QueryWrapper<ClazzFile> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("clazz_evaluation_process_id", evaluationId);
                    List<ClazzFile> clazzFiles = clazzFileMapper.selectList(queryWrapper);
                    //对文件进行处理
                    for (ClazzFile clazzFile : clazzFiles) {
                        String path = clazzFile.getPath();
                        String remotePath = clazzPath + "/" + aes.decryptStr(path, CharsetUtil.CHARSET_UTF_8);
                        // 截取文件名
                        String fileName = StrUtil.subAfter(remotePath, "/", true);
                        File file = new File(localPath + File.separator + fileName);
                        file.createNewFile();
                        channelSftp.get(remotePath, new FileOutputStream(localPath + File.separator + fileName));
                    }
                }


            }
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            throw new RuntimeException(e);
        }

    }

    public String classFileCreate(String college, String directoryName) {
        String collegePath = classPath + File.separator + college;
        File collegeFile = new File(collegePath);
        if (!collegeFile.exists()) {
            collegeFile.mkdir();
        }


        String thisClassPath = collegePath + File.separator + directoryName;

        File dirFile = new File(thisClassPath);
        if (!dirFile.exists()) {
            dirFile.mkdir();
            return thisClassPath;
        }
        return null;
    }

    public void majorFileCreate(String directoryName) {
        File file = new File(majorPath + File.separator + directoryName);
        if (!file.exists()) {
            file.mkdir();
            log.info("创建文件夹: " + directoryName + "成功");
        }
    }

    public void uploadFile(ChannelSftp channelSftp, String remoteFilePath, String localPath) throws SftpException {
        channelSftp.get(remoteFilePath, localPath);
    }

    @Test
    public void test() throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession("root", "119.91.200.72", 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword("vtmer2020");
        session.connect();
        System.out.println(session.isConnected());

        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");


    }


}
