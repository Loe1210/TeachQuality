package com.vtmer.microteachingquality.service.impl;


import com.vtmer.microteachingquality.common.exception.CustomException;
import com.vtmer.microteachingquality.mapper.*;
import com.vtmer.microteachingquality.model.dto.ClazzOpinionRecordDTO;
import com.vtmer.microteachingquality.model.pojo.User;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.vtmer.microteachingquality.common.constant.enums.EvaluationProcessStatus.EVALUATION_PROCESS_MATERIAL_BACK;

/**
 * ClazzEvaluationProcessServiceImpl Tester.
 *
 * @author 陈沛泓
 * @version 1.0
 * @since <pre>6, 5, 2022</pre>
 */
public class ClassClazzEvaluationProcessServiceImplTest {

    @InjectMocks
    private ClazzEvaluationProcessServiceImpl evaluationProcessService;

    @Mock
    private ClazzEvaluationProcessMapper clazzEvaluationProcessMapper;
    @Mock
    private ClazzFileMapper clazzFileMapper;
    @Mock
    private ClazzOpinionRecordMapper clazzOpinionRecordMapper;
    @Mock
    private ClazzOpinionLeaderRecordMapper clazzOpinionLeaderRecordMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RocketMQTemplate rocketMQTemplate;
    @Mock
    private ClazzMapper clazzMapper;

    @BeforeEach
    public void before() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void after() throws Exception {
    }

    /**
     * Method: createEvaluationProcess(Integer clazzId)
     */
    @Test
    public void testCreateEvaluationProcess() throws Exception {
        Mockito.when(clazzMapper.selectByPrimaryKey(1)).thenReturn(null);

        Assertions.assertThrows(CustomException.class, () -> {
            evaluationProcessService.createEvaluationProcess(1);
        });

        Assertions.assertTrue(evaluationProcessService.createEvaluationProcess(1));

    }

    /**
     * Method: getEvaluationProcesses(Integer userId, Integer clazzId)
     */
    @Test
    public void testGetEvaluationProcesses() throws Exception {
        Mockito.when(clazzMapper.selectByPrimaryKey(1)).thenReturn(null);
        Assertions.assertThrows(CustomException.class, () -> {
            evaluationProcessService.getEvaluationProcesses(1, 1, 1, new HashMap<>());
        });

        Assertions.assertNotNull(evaluationProcessService.getEvaluationProcesses(1, 1, 1, new HashMap<>()));
    }

    /**
     * Method: getUploadedFiles(Integer userId, Long evaluationId)
     */
    @Test
    public void testGetUploadedFiles() {
        Mockito.when(clazzEvaluationProcessMapper.selectById(1)).thenReturn(null);
        Assertions.assertThrows(CustomException.class, () -> {
            evaluationProcessService.getUploadedFiles(1, 1L);
        });

        Assertions.assertNotNull(evaluationProcessService.getUploadedFiles(1, 10L));
    }

    /**
     * Method: sendBackEvaluation(Long evaluationId)
     */
    @Test
    public void testSendBackEvaluation() throws Exception {
        Mockito.when(clazzEvaluationProcessMapper.selectById(1)).thenReturn(null);
        Assertions.assertThrows(CustomException.class, () -> {
            evaluationProcessService.sendBackEvaluation(1L);
        });

        Mockito.when(clazzEvaluationProcessMapper.updateEvaluationProcess(2L, EVALUATION_PROCESS_MATERIAL_BACK))
                .thenReturn(-1);

        Assertions.assertThrows(CustomException.class, () -> {
            evaluationProcessService.sendBackEvaluation(2L);
        });

        Assertions.assertTrue(evaluationProcessService.sendBackEvaluation(3L));
    }

    /**
     * Method: getAllFinishedReviews(Long evaluation)
     */
    @Test
    public void testGetAllFinishedReviews() throws Exception {

        Mockito.when(clazzEvaluationProcessMapper.selectById(1)).thenReturn(null);
        Assertions.assertThrows(CustomException.class, () -> evaluationProcessService.getAllFinishedReviews(1L));

        List<ClazzOpinionRecordDTO> list = new ArrayList<>();
        ClazzOpinionRecordDTO opinionRecordDTO = new ClazzOpinionRecordDTO();
        opinionRecordDTO.setUserId(2);
        opinionRecordDTO.setUpdateTime(LocalDateTime.now());
        list.add(opinionRecordDTO);
        Mockito.when(clazzOpinionRecordMapper.getAllReviewInfo(2L))
                .thenReturn(list);
        User user = new User(3, "cph", "dsani", "陈沛泓", "111", "222", 1, "ds1a5", LocalDateTime.now(), LocalDateTime.now());
        Mockito.when(user.selectById()).thenReturn(user);

        Assertions.assertNotNull(evaluationProcessService.getAllFinishedReviews(1L));
    }

    /**
     * Method: postClazzEvaluationLeaderReview(ClazzEvaluationLeaderBO evaluationLeaderBO)
     */
    @Test
    public void testPostClazzEvaluationLeaderReview() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getClazzEvaluationLeaderReviews(Long evaluationId)
     */
    @Test
    public void testGetClazzEvaluationLeaderReviews() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: updateClazzEvaluationStatus(Long evaluationId, String newStatus)
     */
    @Test
    public void testUpdateClazzEvaluationStatus() throws Exception {
//TODO: Test goes here... 
    }


} 
