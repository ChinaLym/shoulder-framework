package org.shoulder.batch.endpoint;

import jakarta.validation.constraints.NotBlank;
import lombok.SneakyThrows;
import org.shoulder.batch.dto.BatchActivityDTO;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.model.BatchActivityBlock;
import org.shoulder.batch.model.BatchActivityRoot;
import org.shoulder.batch.progress.BatchActivityEnum;
import org.shoulder.batch.progress.BatchActivityRepository;
import org.shoulder.core.concurrent.Threads;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogConfig;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.validate.exception.ParamErrorCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

@Controller
@Validated
@RequestMapping("activity")
@OperationLogConfig(objectType = "objectType.activity")
public class ActivityController {

    @Autowired
    private BatchActivityRepository dynamicProgressActivityRepository;

    @Autowired
    private ConversionService conversionService;

    @RequestMapping("showProgress")
    public Object showProgress(Model model, 
                              @RequestParam @NotBlank String activityId,
                              @RequestParam @NotBlank String progressId) {
        BatchActivityRoot activityRoot = dynamicProgressActivityRepository.queryActivity(activityId);
        model.addAttribute("activityStruct", activityRoot);
        model.addAttribute("progressId", progressId);
        return "progress/progress";
    }

    @ResponseBody
    @RequestMapping("progress")
    public Map<String, BatchProcessResult> queryActivity(
            @RequestParam @NotBlank String activityId,
            @RequestParam @NotBlank String progressId) {
        BatchActivityRoot activityRoot = dynamicProgressActivityRepository.queryActivity(activityId);
        AssertUtils.notNull(activityRoot, ParamErrorCodeEnum.PARAM_ILLEGAL, activityId);
        Map<String, BatchProcessResult> mergedProgress = new HashMap<>();
        for (BatchActivityEnum<?> value : activityRoot.getOriginalClass().getEnumConstants()) {
            mergedProgress.put(value.getKey(), 
                    conversionService.convert(value.findProgressOrCreate(progressId).toRecord(), BatchProcessResult.class));
        }
        return mergedProgress;
    }

    @ResponseBody
    @RequestMapping("cleanCache")
    @OperationLog(operation = OperationLog.Operations.DELETE)
    public String cleanCache() {
        BatchActivityEnum.progressCache.clear();
        OpLogContextHolder.getLog().setObjectId("all").setObjectName("progressCache");
        return "success";
    }

    // ------ 演示用 -----------
    @RequestMapping("testProgress")
    public Object testProgress(Model model, 
                              @RequestParam @NotBlank String activityId) throws ClassNotFoundException {
        BatchActivityRoot activityRoot = dynamicProgressActivityRepository.queryActivity(activityId);
        String progressId = "demo";
        model.addAttribute("activityStruct", activityRoot);
        model.addAttribute("progressId", progressId);
        
        // 清理演示用的进度
        BatchActivityEnum<?>[] steps = activityRoot.getOriginalClass().getEnumConstants();
        for (BatchActivityEnum<?> step : steps) {
            BatchActivityEnum.progressCache.evict(step.genCacheKey(progressId));
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
        return "progress/progress";
    }

    @SneakyThrows
    private Semaphore mockExecuteBlocks(Semaphore dependency, 
                                       BatchActivityBlock activityBlock,
                                       String progressId,
                                       Class<? extends Enum> originalClass) {
        Semaphore current = new Semaphore(0);
        Threads.execute(() -> {
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
                    Threads.execute(() ->
                        triggerRunSteps(progressId, originalClass, parallelSemaphore, parallelActivityDTOList));
                }
                Threads.execute(() -> {
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

    // 其他辅助方法...
}
