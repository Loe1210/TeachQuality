package com.vtmer.microteachingquality.file;

import com.vtmer.microteachingquality.mapper.ReportTemplateMapper;
import com.vtmer.microteachingquality.model.pojo.ReportTemplate;
import com.vtmer.microteachingquality.service.CourseReportService;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;

/**
 * @author Colin_Knight
 * @create 2023/9/15 4:25
 */
@SpringBootTest
public class SchoolTemplateFile {

    @Autowired
    ReportTemplateMapper reportTemplateMapper;
    @Resource
    private CourseReportService courseReportService;

    /**
     * 附件上传
     */
    @Test
    public void test() {
        String path = "附件3.广东报工业大学本科课程改进计划告.docx";

        File file = new File(path);

        Integer integer = courseReportService.saveCourseReportTemplate(getMultipartFile(file));
        System.out.println(integer);
    }

    private MultipartFile getMultipartFile(File file) {
        FileInputStream fileInputStream = null;
        MultipartFile multipartFile = null;
        try {
            fileInputStream = new FileInputStream(file);
            multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                    ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return multipartFile;
    }

    private void insertRrportTemplate(ReportTemplate reportTemplate) {

        reportTemplateMapper.insertReportTemplate(reportTemplate);


    }
}
