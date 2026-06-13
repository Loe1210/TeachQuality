package com.vtmer.microteachingquality.util;

import com.vtmer.microteachingquality.model.bo.ClazzEvaluationToWordBO;
import com.vtmer.microteachingquality.model.bo.MajorEvaluationToWordBO;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportEvaluationWordUtil {
    //    @Value("${clazzReport.path}")
    private static final String clazzPath = "C:\\Users\\Lenovo\\Desktop\\评审";
    //    @Value("${majorReport.path}")
    private static String majorPath = "C:\\Users\\Lenovo\\Desktop\\评审";

    public static String exportMajorEvaluationWord(MajorEvaluationToWordBO majorEvaluationToWordBO) {
        String[] time = majorEvaluationToWordBO.getDate().split("-");
        // 创建一个新的XWPFDocument
        XWPFDocument document = new XWPFDocument();
        createBigTitle(document);
        // 添加空行
        addEmptyLines(document, 1);

        // 创建副标题段落 “XXXX年广东工业大学本科专业校内评估报告”
        XWPFParagraph subTitleParagraph = document.createParagraph();
        subTitleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun subTitleRun = subTitleParagraph.createRun();
        subTitleRun.setText(time[0] + "年广东工业大学本科专业校内评估报告");
        subTitleRun.setFontSize(16);
        subTitleRun.setFontFamily("SimSun");
        subTitleRun.setBold(true);

        // 添加空行
        addEmptyLines(document, 1);

        // 创建 “一、学院专业” 段落
        createLeftAlignedTitle(document, "一、学院专业");
        createLeftAlignedBody(document, "    " + majorEvaluationToWordBO.getCollegeAndMajor());

        // 创建 “二、评估结论” 段落
        createLeftAlignedTitle(document, "二、评估结论");
        createLeftAlignedBody(document, "    " + majorEvaluationToWordBO.getResult());

        // 创建 “三、评估意见” 段落
        createLeftAlignedTitle(document, "三、评估意见");
        createLeftAlignedBody(document, "    " + majorEvaluationToWordBO.getOpinion());

        // 创建 “评估专家组组长：” 段落
        createLeftAlignedBody(document, "评估专家组组长：" + majorEvaluationToWordBO.getLeader());

        // 创建 “成员：” 段落
        createLeftAlignedBody(document, "成员：" + majorEvaluationToWordBO.getMembers());

        // 创建居右段落 “广东工业大学”
        createRightAlignedBody(document, "广东工业大学");

        // 创建居右段落 “2023年7月1日”
        createRightAlignedBody(document, time[0] + "年" + time[1] + "月" + time[2] + "日");

        // 保存文档
        String filePath = majorPath + File.separator +
                time[0] + "年" + majorEvaluationToWordBO.getCollegeAndMajor() + "专业评审报告.docx";
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            document.write(out);
            System.out.println("Document saved successfully.");
        } catch (IOException e) {
            return null;
        }

        return filePath;
    }

    public static String exportClazzEvaluationWord(ClazzEvaluationToWordBO clazzEvaluationToWordBO) {
        String[] time = clazzEvaluationToWordBO.getDate().split("-");
        // 创建一个新的XWPFDocument
        XWPFDocument document = new XWPFDocument();
        createBigTitle(document);
        // 添加空行
        addEmptyLines(document, 1);

        // 创建副标题段落 “XXXX年广东工业大学本科课程评估报告”
        XWPFParagraph subTitleParagraph = document.createParagraph();
        subTitleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun subTitleRun = subTitleParagraph.createRun();
        subTitleRun.setText(time[0] + "广东工业大学本科课程评估报告");
        subTitleRun.setFontSize(16);
        subTitleRun.setFontFamily("SimSun");
        subTitleRun.setBold(true);

        // 添加空行
        addEmptyLines(document, 1);

        // 创建 “一、课程名称” 段落
        createLeftAlignedTitle(document, "一、课程名称");
        createLeftAlignedBody(document, "    " + clazzEvaluationToWordBO.getName());

        // 创建 “二、评估结论” 段落
        createLeftAlignedTitle(document, "二、评估结论");
        createLeftAlignedBody(document, "    " + clazzEvaluationToWordBO.getResult());

        // 创建 “三、突出优点” 段落
        createLeftAlignedTitle(document, "三、突出优点");
        createLeftAlignedBody(document, "    " + clazzEvaluationToWordBO.getAdvantage());

        // 创建 ”四、存在问题“ 段落
        createLeftAlignedTitle(document, "四、存在问题");
        createLeftAlignedBody(document, "    " + clazzEvaluationToWordBO.getProblem());

        // 创建 ”五、改进建议“ 段落
        createLeftAlignedTitle(document, "五、改进建议");
        createRightAlignedBody(document, "    " + clazzEvaluationToWordBO.getAdvice());

        // 创建 “评估专家组组长：” 段落
        createLeftAlignedBody(document, "评估专家组组长：" + clazzEvaluationToWordBO.getLeader());

        // 创建 “成员：” 段落
        createLeftAlignedBody(document, "成员：" + clazzEvaluationToWordBO.getMembers());

        // 创建居右段落 “广东工业大学”
        createRightAlignedBody(document, "广东工业大学");

        // 创建居右段落 “2023年7月1日”
        createRightAlignedBody(document, time[0] + "年" + time[1] + "月" + time[2] + "日");

        // 保存文档
        String filePath = clazzPath + File.separator +
                time[0] + "年" + clazzEvaluationToWordBO.getName() + "专业评审报告.docx";
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(new File(filePath))) {
            document.write(out);
            System.out.println("Document saved successfully.");
        } catch (IOException e) {
            return null;
        }

        return filePath;
    }


    // 创建标题段落 “广东工业大学”
    private static void createBigTitle(XWPFDocument document) {
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        String title = "广东工业大学";
        for (char c : title.toCharArray()) {
            XWPFRun run = titleParagraph.createRun();
            run.setText(String.valueOf(c));
            run.setBold(true);
            run.setFontSize(48);
            run.setColor("FF0000");
            run.setFontFamily("SimSun");
            run.setUnderline(UnderlinePatterns.DOUBLE);

            // 添加额外的空格以增加字符间距
            XWPFRun spaceRun = titleParagraph.createRun();
            spaceRun.setText("       ");
            spaceRun.setColor("FF0000");
            spaceRun.setUnderline(UnderlinePatterns.DOUBLE);
        }
    }

    // 添加空行
    private static void addEmptyLines(XWPFDocument document, int number) {
        for (int i = 0; i < number; i++) {
            XWPFParagraph emptyParagraph = document.createParagraph();
            XWPFRun run = emptyParagraph.createRun();
            run.setText("\n");
        }
    }

    // 创建左对齐标题
    private static void createLeftAlignedTitle(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(16);
        run.setBold(true);
        run.setFontFamily("SimSun");
    }

    // 创建左对齐正文
    private static void createLeftAlignedBody(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(16);
        run.setFontFamily("SimSun");
    }

    // 创建右对齐正文
    private static void createRightAlignedBody(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(16);
        run.setFontFamily("SimSun");
    }

    // 添加图片方法
    private static void addImage(XWPFDocument document, String imgFile) {
        try {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.addPicture(new FileInputStream(imgFile), XWPFDocument.PICTURE_TYPE_JPEG, imgFile, Units.toEMU(400), Units.toEMU(300));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
