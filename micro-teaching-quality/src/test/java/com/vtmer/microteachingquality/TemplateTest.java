package com.vtmer.microteachingquality;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.mapper.ClazzFileTemplateMapper;
import com.vtmer.microteachingquality.model.dto.GetAllMajorsDTO;
import com.vtmer.microteachingquality.model.dto.MajorDTO;
import com.vtmer.microteachingquality.model.pojo.ReportTemplate;
import com.vtmer.microteachingquality.model.vo.ReportTemplateResult;
import com.vtmer.microteachingquality.service.ClazzService;
import com.vtmer.microteachingquality.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author Colin_Knight
 * @create 2023/11/1 16:40
 */
@SpringBootTest
public class TemplateTest {

    @Resource
    ReportService reportService;

    @Autowired
    ClazzService clazzService;
    @Autowired
    ClazzFileTemplateMapper clazzFileTemplateMapper;

    @Test
    public void test() {
        ReportTemplate reportTemplate = reportService.getReportTemplateByMajor("财务处");
        String fileName = null;
        ReportTemplateResult result = new ReportTemplateResult();
        if (ObjectUtil.isNotNull(reportTemplate)) {
            fileName = StrUtil.subAfter(reportTemplate.getPath(), File.separator, true);
            result.setFileName(fileName);
        }
        if (reportTemplate == null) {
            System.out.println(ResponseMessage.newSuccessInstance(result));
        } else {
            BeanUtils.copyProperties(reportTemplate, result);
        }
        System.out.println(ResponseMessage.newSuccessInstance(result));
    }

    @Test
    public void test1() {
        clazzFileTemplateMapper.selectAllByUserId(1).forEach(System.out::println);
    }

    @Test
    public void test2() {
        MajorDTO majorDTO = new MajorDTO();
        majorDTO.setPageIndex(1);
        majorDTO.setPageLength(10);
        GetAllMajorsDTO allMajors = reportService.getAllMajors(majorDTO);
        allMajors.getMajorsList().forEach(System.out::println);

    }

}
