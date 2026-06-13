package com.vtmer.microteachingquality.controller;

import com.vtmer.microteachingquality.common.ResponseMessage;
import com.vtmer.microteachingquality.model.bo.SelectClazzEvaluationProcessListBO;
import com.vtmer.microteachingquality.model.bo.SelectMajorEvaluationProcessListBO;
import com.vtmer.microteachingquality.model.pojo.clazzevaluation.ClassEvaluationProcess;
import com.vtmer.microteachingquality.model.pojo.majorevaluation.MajorEvaluationProcess;
import com.vtmer.microteachingquality.model.vo.ClazzEvaluationProcessVO;
import com.vtmer.microteachingquality.model.vo.MajorEvaluationProcessVO;
import com.vtmer.microteachingquality.service.EvaluationProcessFileService;
import com.vtmer.microteachingquality.service.EvaluationProcessService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author batstroke
 */
@Api(tags = "流程管理相关接口")
@RestController
@RequestMapping("/evaluationProcess")
@Slf4j
@PreAuthorize("hasAnyAuthority('all')")
public class EvaluationProcessController {

    @Resource
    EvaluationProcessService evaluationProcessService;
    @Resource
    EvaluationProcessFileService evaluationProcessFileService;

    @ApiOperation("查看所有专业的评审流程")
    @PostMapping("showMajorProcess")
    public ResponseMessage<List<MajorEvaluationProcessVO>> getMajorProcessInfo(@RequestBody @Validated SelectMajorEvaluationProcessListBO selectMajorEvaluationProcessListBO) {
        List<MajorEvaluationProcessVO> processes = evaluationProcessService.getMajorEvaluationByName(selectMajorEvaluationProcessListBO);
        return processes != null ? ResponseMessage.newSuccessInstance(processes) : ResponseMessage.newErrorInstance("加载失败");
    }

    @ApiOperation("查看所有课程的评审流程")
    @PostMapping("showClazzProcess")
    public ResponseMessage<List<ClazzEvaluationProcessVO>> getClazzProcessInfo(@RequestBody @Validated SelectClazzEvaluationProcessListBO selectClazzEvaluationProcessListBO) {
        List<ClazzEvaluationProcessVO> processes = evaluationProcessService.getClazzEvaluationByName(selectClazzEvaluationProcessListBO);
        return processes != null ? ResponseMessage.newSuccessInstance(processes) : ResponseMessage.newErrorInstance("加载失败");
    }


    @ApiOperation("【管理流程】修改课程评审状态")
    @PostMapping("changeClazzStatus")
    public ResponseMessage<String> changeClazzProcessStatus(@RequestBody ClassEvaluationProcess classEvaluationProcess) {
        return evaluationProcessService.changeClazzProcessStatus(classEvaluationProcess) ?
                ResponseMessage.newSuccessInstance("修改成功") :
                ResponseMessage.newErrorInstance("修改失败");
    }

    @ApiOperation("【管理流程】修改专业评审状态")
    @PostMapping("changeMajorStatus")
    public ResponseMessage<String> changeMajorProcessStatus(@RequestBody MajorEvaluationProcess majorEvaluationProcess) {
        return evaluationProcessService.changeMajorProcessStatus(majorEvaluationProcess) ?
                ResponseMessage.newSuccessInstance("修改成功") :
                ResponseMessage.newErrorInstance("修改失败");
    }

    @ApiOperation("【管理流程】删除评审流程")
    @PostMapping("deleteProcess")
    public ResponseMessage<String> changeClazzProcessStatus(@RequestBody T process) throws NoSuchFieldException, IllegalAccessException {
        return evaluationProcessService.deleteProcess(process) ?
                ResponseMessage.newSuccessInstance("删除成功") :
                ResponseMessage.newErrorInstance("删除失败");
    }

    @ApiOperation("导出专业评估报告")
    @GetMapping("/exportReport/{majorEvaluationProcessId}")
    public ResponseMessage<List<String>> exportMajorReport(@PathVariable Long majorEvaluationProcessId) {
        List<String> paths = evaluationProcessFileService.generateMajorEvaluationReport(majorEvaluationProcessId);
        return ResponseMessage.newSuccessInstance(paths);
    }

    @ApiOperation("导出课程评估报告")
    @GetMapping("/exportReport/{clazzEvaluationProcessId}")
    public ResponseMessage<List<String>> exportClazzReport(@PathVariable Long clazzEvaluationProcessId) {
        List<String> paths = evaluationProcessFileService.generateClazzEvaluationReport(clazzEvaluationProcessId);
        return ResponseMessage.newSuccessInstance(paths);
    }
}
