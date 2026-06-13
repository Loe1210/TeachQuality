package com.vtmer.microteachingquality.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

@Component
public class WriteExcelUtils {

    public static void write(Map<String, Map<String, String>> data) {
        Workbook workbook = new XSSFWorkbook(); // 创建工作簿
        Sheet sheet = workbook.createSheet("Example Sheet"); // 创建工作表
        Row row = sheet.createRow(0); // 创建行
        row.createCell(0).setCellValue("学院");
        row.createCell(1).setCellValue("课程名称");
        row.createCell(2).setCellValue("小组评审结论");

        int index = 1;
        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            for (Map.Entry<String, String> stringEntry : entry.getValue().entrySet()) {
                Row sheetRow = sheet.createRow(index);
                sheetRow.createCell(0).setCellValue(entry.getKey());
                sheetRow.createCell(1).setCellValue(stringEntry.getKey());
                sheetRow.createCell(2).setCellValue(stringEntry.getValue());
                index++;
            }
        }

        // 将工作簿写入文件
        try (FileOutputStream outputStream = new FileOutputStream("example_with_wrap_text.xlsx")) {
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeAllClazzDetails(Map<String, List<Map<String, Object>>> data) {
        Workbook workbook = new XSSFWorkbook(); // 创建工作簿
        Sheet sheet = workbook.createSheet("2024年咋课程评审记录"); // 创建工作表
        Row row = sheet.createRow(0); // 创建行
        row.createCell(0).setCellValue("学院");
        row.createCell(1).setCellValue("课程名称");
        row.createCell(2).setCellValue("评审专家");
        row.createCell(3).setCellValue("评审结论");
        row.createCell(4).setCellValue("课程优点");
        row.createCell(5).setCellValue("存在问题");
        row.createCell(6).setCellValue("改进建议");
        row.createCell(7).setCellValue("评价时间");

        int index = 1;
        for (Map.Entry<String, List<Map<String, Object>>> collegeEntry : data.entrySet()) {
            String college = collegeEntry.getKey();
            for (Map<String, Object> object : collegeEntry.getValue()) {
                String name = object.get("name").toString();
                String teacher = object.get("real_name").toString();
                String remark = object.get("clazz_remark").toString();
                String advantage = object.get("clazz_advantage").toString();
                String problem = object.get("clazz_problem").toString();
                String advice = object.get("clazz_advice").toString();
                String time = object.get("create_time").toString();

                Row sheetRow = sheet.createRow(index);
                sheetRow.createCell(0).setCellValue(college);
                sheetRow.createCell(1).setCellValue(name);
                sheetRow.createCell(2).setCellValue(teacher);
                sheetRow.createCell(3).setCellValue(remark);
                sheetRow.createCell(4).setCellValue(advantage);
                sheetRow.createCell(5).setCellValue(problem);
                sheetRow.createCell(6).setCellValue(advice);
                sheetRow.createCell(7).setCellValue(time);
                index++;
            }
        }

        // 将工作簿写入文件
        try (FileOutputStream outputStream = new FileOutputStream("2024" +
                "课程评审.xlsx")) {
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeAllMajorDetails(Map<String, List<Map<String, Object>>> data) {
        Workbook workbook = new XSSFWorkbook(); // 创建工作簿
        Sheet sheet = workbook.createSheet("2024年专业评审记录"); // 创建工作表
        Row row = sheet.createRow(0); // 创建行
        row.createCell(0).setCellValue("学院");
        row.createCell(1).setCellValue("专业名称");
        row.createCell(2).setCellValue("评审专家");
        row.createCell(3).setCellValue("评估结论");
        row.createCell(4).setCellValue("评估意见");
        row.createCell(5).setCellValue("评价时间");

        int index = 1;
        for (Map.Entry<String, List<Map<String, Object>>> collegeEntry : data.entrySet()) {
            String college = collegeEntry.getKey();
            for (Map<String, Object> object : collegeEntry.getValue()) {
                String name = object.get("name").toString();
                String teacher = object.get("real_name").toString();
                String result = object.get("remark").toString();
                String opinion = object.get("opinion").toString();
                String time = object.get("create_time").toString();

                Row sheetRow = sheet.createRow(index);
                sheetRow.createCell(0).setCellValue(college);
                sheetRow.createCell(1).setCellValue(name);
                sheetRow.createCell(2).setCellValue(teacher);
                sheetRow.createCell(3).setCellValue(result);
                sheetRow.createCell(4).setCellValue(opinion);
                sheetRow.createCell(5).setCellValue(time);
                index++;
            }
        }

        // 将工作簿写入文件
        try (FileOutputStream outputStream = new FileOutputStream("2024" +
                "专业评审.xlsx")) {
            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
