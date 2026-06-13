package com.vtmer.microteachingquality.file;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.*;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClazzFile;
import com.vtmer.microteachingquality.model.pojo.majorarchive.MajorArchiveFile;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationFile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Colin_Knight
 * @create 2024/5/11 19:37
 */
@SpringBootTest
@Slf4j
public class SelfEvaluationFilePackingTest {


    final static List<String> UNSUBMIT_FILE = new LinkedList<>();
    private final byte[] ENCRYPT_KEY = "9fcccb8912be487e91dc2743d94e566a".getBytes();
    private final AES aes = SecureUtil.aes(ENCRYPT_KEY);
    @Resource
    public MajorArchiveFileMapper majorArchiveFileMapper;
    //自己创建文件夹保存
    String classPath = "C:\\Users\\雪碧\\Desktop\\2023上传评审资料\\课程";
    String classPath21 = "G:\\work\\Vtmer\\自评材料\\21年\\课程";
    String classPath22 = "G:\\work\\Vtmer\\自评材料\\22年\\课程";
    String classPath23 = "G:\\work\\Vtmer\\自评材料\\23年\\课程";
    String majorPath21 = "G:\\work\\Vtmer\\自评材料\\21年\\专业";
    String majorPath22 = "G:\\work\\Vtmer\\自评材料\\22年\\专业";
    String majorPath23 = "G:\\work\\Vtmer\\自评材料\\23年\\专业";
    @Value("${clazz.path}")
    private String clazzPath;
    @Value("${report.path}")
    private String majorPath;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ClazzFileMapper clazzFileMapper;
    @Autowired
    private ClazzEvaluationProcessMapper cepm;
    @Resource
    private ClazzMapper clazzMapper;
    @Resource
    private MajorMapper majorMapper;
    @Resource
    private MajorEvaluationFileMapper majorEvaluationFileMapper;
    @Value("${major.archive.file}")
    private String majorArchivePath;

    /**
     * 获取sftp连接
     *
     * @return channelSftp实例
     * @throws JSchException
     */
    private static ChannelSftp getChannelSftp() throws JSchException {
        ChannelSftp channelSftp;

        //sftp连接
        JSch jSch = new JSch();
        Session session = jSch.getSession("root", "119.91.200.72", 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword("vtmer2020");
        session.connect();
        System.out.println(session.isConnected());

        channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();


        return channelSftp;
    }

    @Test
    public void classFilePacking() {


        ChannelSftp channelSftp = null;

        try {
            channelSftp = getChannelSftp();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        List<ClazzFile> clazzFiles = clazzFileMapper.selectList(
                new QueryWrapper<ClazzFile>()
                        .like("file_name", "自评")
                        .between("create_time", "2023-09-01", "2024-08-30")

        );
        int count = 0;
        if (channelSftp != null && channelSftp.isConnected()) {

            for (ClazzFile clazzFile : clazzFiles) {
                try {
                    downloadFile(channelSftp, getRemoteFilePath(clazzPath, clazzFile.getPath()), classPath23);
                    System.out.println("下载成功,下载索引为【" + count + "】");
                    count++;

                } catch (SftpException e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

    @Test
    public void majorFilePacking() {

        ChannelSftp channelSftp = null;

        try {
            channelSftp = getChannelSftp();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        List<MajorEvaluationFile> majorEvaluationFiles = majorEvaluationFileMapper.selectList(
                new QueryWrapper<MajorEvaluationFile>()
                        .like("file_name", "自评")
                        .between("create_time", "2021-09-01", "2022-08-30")

        );


        int count = 0;
        if (channelSftp != null && channelSftp.isConnected()) {

            for (MajorEvaluationFile majorEvaluationFile : majorEvaluationFiles) {
                try {
                    downloadFile(channelSftp, getRemoteFilePath(majorPath, majorEvaluationFile.getPath()), majorPath23);
                    System.out.println("下载成功,下载索引为【" + count + "】");
//                    break;
                } catch (SftpException e) {
                    System.out.println("下载失败,下载索引为【" + count + "】");
                    System.out.println("【" + count + "】: " + majorEvaluationFile.getFileName());
                }
                count++;
            }

        }

    }

    @Test
    public void reportFilePacking() {

        ChannelSftp channelSftp = null;

        try {
            channelSftp = getChannelSftp();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        List<MajorArchiveFile> majorArchiveFiles = majorArchiveFileMapper.selectList(
                new QueryWrapper<MajorArchiveFile>()
                        .like("file_name", "自评")
                        .between("create_time", "2022-09-01", "2023-08-30")

        );

//        majorArchiveFiles.forEach(System.out::println);


        int count = 0;
        if (channelSftp != null && channelSftp.isConnected()) {

            for (MajorArchiveFile majorArchiveFile : majorArchiveFiles) {
                try {
                    downloadFile(channelSftp, getRemoteFilePath(majorArchivePath, majorArchiveFile.getPath()), majorPath22);
                    System.out.println("下载成功,下载索引为【" + count + "】");
//                    break;
                } catch (SftpException e) {
                    System.out.println("下载失败,下载索引为【" + count + "】");
                    System.out.println("【" + count + "】: " + majorArchiveFile.getFileName());
                }
                count++;
            }

        }

    }

    private String getRemoteFilePath(String basePath, String classPath) {

        return basePath + "/" + aes.decryptStr(classPath, CharsetUtil.CHARSET_UTF_8);

    }

    private void downloadFile(ChannelSftp channelSftp, String remotePath, String localPath) throws SftpException {
        channelSftp.get(remotePath, localPath);
    }


    @Test
    public void test1() {
        System.out.println(clazzMapper.selectClazzByClazzFileName(1742367980257280000L));
    }

}
