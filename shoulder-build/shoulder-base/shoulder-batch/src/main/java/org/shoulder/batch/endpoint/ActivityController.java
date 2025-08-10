package org.shoulder.batch.endpoint;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.SneakyThrows;
import org.shoulder.batch.dto.BatchActivityDTO;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.model.BatchActivityBlock;
import org.shoulder.batch.model.BatchActivityRoot;
import org.shoulder.batch.progress.*;
import org.shoulder.core.concurrent.Threads;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogConfig;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.validate.exception.ParamErrorCodeEnum;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

@Tag(name = "ActivityController", description = "自定义流程进度管理")
@RestController
@Validated
@RequestMapping("${shoulder.batch.activity.path:/api/v1/activities}")
@OperationLogConfig(objectType = "objectType.activity")
public class ActivityController {

    private final BatchActivityRepository dynamicProgressActivityRepository;

    private final ConversionService conversionService;


    public ActivityController(BatchActivityRepository dynamicProgressActivityRepository, ConversionService conversionService) {
        this.dynamicProgressActivityRepository = dynamicProgressActivityRepository;
        this.conversionService = conversionService;
    }

    /**
     * 查找动态流程任务定义 todo body包装
     *
     * @param activityId 流程 id
     * @return 流程定义
     */
    @ResponseBody
    @GetMapping("definition")
    @OperationLog(operation = OperationLog.Operations.QUERY, objectType = "obj.batch.activity.definition", logAllParams = true)
    public BatchActivityRoot definition(@RequestParam @NotBlank String activityId) {
        BatchActivityRoot batchActivityRoot = dynamicProgressActivityRepository.queryActivity(activityId);
        AssertUtils.notNull(batchActivityRoot, ParamErrorCodeEnum.DATA_NON_EXIST, activityId);
        return batchActivityRoot;
    }

    /**
     * 查找动态流程任务定义 todo body包装
     *
     * @param activityId 流程 id
     * @return 流程定义
     */
    @ResponseBody
    @GetMapping("progress")
    public Map<String, BatchProcessResult> queryProgress(
            @RequestParam @NotBlank String activityId,
            @RequestParam @NotBlank String progressId) {
        BatchActivityRoot activityRoot = dynamicProgressActivityRepository.queryActivity(activityId);
        AssertUtils.notNull(activityRoot, ParamErrorCodeEnum.DATA_NON_EXIST, activityId);

        BatchProgressCache progressCache = BatchActivityEnum.progressCache();
        Map<String, BatchProcessResult> mergedProgress = new HashMap<>();

        BatchActivityEnum<?>[] steps = activityRoot.getOriginalClass().getEnumConstants();
        Progress firstStep = progressCache.findProgress(steps[0].genCacheKey(progressId));
        AssertUtils.notNull(firstStep, ParamErrorCodeEnum.DATA_NON_EXIST, progressId + ":" + steps[0].getKey());

        for (BatchActivityEnum<?> value : steps) {
            Progress progress = progressCache.findProgress(value.genCacheKey(progressId));
            progress = progress == null ? new BatchProgress() : progress;
            mergedProgress.put(value.getKey(),
                    conversionService.convert(progress.toProgressRecord(), BatchProcessResult.class));
        }
        return mergedProgress;
    }
    // ------ 演示用 -----------

    /**
     * 短期方案，后续引入鉴权、是否完成等判断
     */
    @ResponseBody
    @PostMapping("cleanCache")
    @OperationLog(operation = OperationLog.Operations.DELETE)
    public String cleanCache() {
        BatchActivityEnum.progressCache().clear();
        OpLogContextHolder.getLog().setObjectId("all").setObjectName("progressCache");
        return "success";
    }

    @OperationLog(operation = OperationLog.Operations.CREATE, objectType = "obj.batch.activity.mock", logAllParams = true)
    @PostMapping("testProgress")
    public String testProgress(@RequestParam @NotBlank String activityId,
                               @RequestParam(defaultValue = "_shoulderMockAndTest") String progressId
    ) throws ClassNotFoundException {
        BatchActivityRoot activityRoot = dynamicProgressActivityRepository.queryActivity(activityId);
        AssertUtils.notNull(activityRoot, ParamErrorCodeEnum.DATA_NON_EXIST, activityId);

        // 清理并重建演示用的进度缓存
        BatchActivityEnum<?>[] steps = activityRoot.getOriginalClass().getEnumConstants();
        for (BatchActivityEnum<?> step : steps) {
            BatchActivityEnum.progressCache().evict(step.genCacheKey(progressId));
            step.findProgressOrCreate(progressId);
        }

        Semaphore s = null;
        for (BatchActivityBlock activityBlock : activityRoot.getActivityBlocks()) {
            // 解决跨 classLoader
            Class<? extends Enum> originalClass = (Class<? extends Enum>) activityRoot.getOriginalClass();
            String classFullName = originalClass.getName();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            originalClass = (Class<? extends Enum>) classLoader.loadClass(classFullName);
            s = mockExecuteBlocks(s, activityBlock, progressId, originalClass);
        }
        return "ok";
    }

    @SneakyThrows
    private Semaphore mockExecuteBlocks(Semaphore dependency,
                                        BatchActivityBlock activityBlock,
                                        String progressId,
                                        Class<? extends Enum> originalClass) {
        Semaphore current = new Semaphore(0);
        Threads.execute("t_batchBlocks" + progressId, () -> {
            if (dependency != null) {
                try {
                    dependency.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (activityBlock.getType().equals("PARALLEL")) {
                // 并行处理
                List<List<BatchActivityDTO>> parallelActivities = activityBlock.getList();
                int parallelSize = parallelActivities.size();
                Semaphore parallelSemaphore = new Semaphore(0);
                for (List<BatchActivityDTO> parallelActivityDTOList : parallelActivities) {
                    Threads.execute("t_batchRun" + progressId, () ->
                            triggerRunSteps(progressId, originalClass, parallelSemaphore, parallelActivityDTOList));
                }
                Threads.execute("t_batchWithRun" + progressId, () -> {
                    try {
                        parallelSemaphore.acquire(parallelSize);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        current.release();
                    }
                });
            } else {
                List<BatchActivityDTO> serialActivities = activityBlock.getList();
                triggerRunSteps(progressId, originalClass, current, serialActivities);
            }
        });
        return current;
    }

    private void triggerRunSteps(String progressId,
                                 Class<? extends Enum> activityClass,
                                 Semaphore semaphore,
                                 List<BatchActivityDTO> activityDTOList) {
        BatchActivityEnum<?>[] steps = new BatchActivityEnum[activityDTOList.size()];
        for (int i = 0; i < activityDTOList.size(); i++) {
            steps[i] = (BatchActivityEnum<?>) Enum.valueOf(activityClass, activityDTOList.get(i).getId());
        }
        mockExecuteStepWithOrder(0, steps, progressId);
        semaphore.release();
    }

    @SneakyThrows
    private void mockExecuteStepWithOrder(int index,
                                          BatchActivityEnum<?>[] steps,
                                          String progressId) {
        if (index >= steps.length) {
            return;
        }
        BatchActivityEnum<?> step = steps[index];
        int total = ThreadLocalRandom.current().nextInt(1, 3);
        step.setTotalAndStart(progressId, total);
        Semaphore allTaskFinished = new Semaphore(0);
        for (int i = 0; i < total; i++) {
            int cost = ThreadLocalRandom.current().nextInt(10, 3000);
            Threads.delay("PROGRESS_DEMO_" + step.getKey() + i, () -> {
                step.addSuccess(progressId);
                allTaskFinished.release();
            }, Duration.ofMillis(cost));
        }
        allTaskFinished.acquire(total);
        mockExecuteStepWithOrder(index + 1, steps, progressId);
    }

}
