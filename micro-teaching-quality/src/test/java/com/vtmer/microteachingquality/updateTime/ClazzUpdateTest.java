package com.vtmer.microteachingquality.updateTime;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vtmer.microteachingquality.mapper.ClazzEvaluationProcessMapper;
import com.vtmer.microteachingquality.mapper.ClazzMapper;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.Clazz;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author Colin_Knight
 * @create 2023/12/27 15:27
 */
@SpringBootTest
public class ClazzUpdateTest {

    @Autowired
    private ClazzEvaluationProcessMapper processMapper;

    @Autowired
    private ClazzMapper clazzMapper;

    /**
     *
     */
    @Test
    public void update() {


        // 创建新的工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Results");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("序号");
        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("课程");
        Cell headerCell3 = headerRow.createCell(2);
        headerCell3.setCellValue("专业");
        Cell headerCell4 = headerRow.createCell(3);
        headerCell4.setCellValue("课程名称");
        Cell headerCell5 = headerRow.createCell(4);
        headerCell5.setCellValue("是否提交");


        // 创建一个CellStyle，并设置其填充色
        CellStyle cellStyle = workbook.createCellStyle();

        // 将Fill对象应用到CellStyle
        cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        // 填充数据
        int rowNum = 1;

        QueryWrapper<ClassEvaluationProcess> wrapper = new QueryWrapper<>();
        //大于某个时间点的
        wrapper.gt("update_time", "2023-07-04 20:08:24");
        List<ClassEvaluationProcess> classEvaluationProcesses =
                processMapper.selectList(wrapper);

        for (ClassEvaluationProcess classEvaluationProcess : classEvaluationProcesses) {
            Clazz clazz = clazzMapper.selectById(classEvaluationProcess.getClazzId());
            if (clazz == null) {
                continue;
            }
            Row row = sheet.createRow(rowNum);
            Cell cell1 = row.createCell(0);
            cell1.setCellValue(rowNum);
            Cell cell2 = row.createCell(1);
            cell2.setCellValue(clazz.getCollege());
            Cell cell3 = row.createCell(2);
            cell3.setCellValue(clazz.getMajor());
            Cell cell4 = row.createCell(3);
            cell4.setCellValue(clazz.getName());
            Cell cell5 = row.createCell(4);
            if (classEvaluationProcess.getPrincipalMaterialStatus() != 2) {
                cell5.setCellStyle(cellStyle);
            } else {
                cell5.setCellValue("✅");
            }
            rowNum++;
            System.out.println(clazz);
        }

        try (FileOutputStream fileOut = new FileOutputStream("课程查询结果.xlsx")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            // 处理异常
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                // 处理异常
            }

        }

    }


    @Test
    public void getReviewResult() {
        QueryWrapper<ClassEvaluationProcess> wrapper = new QueryWrapper<>();


    }
}