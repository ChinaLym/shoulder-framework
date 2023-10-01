package com.example.demo2.controller.batch;

import com.example.demo2.dto.PersonRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.constant.BatchConstants;
import org.shoulder.batch.dto.param.ExecuteOperationParam;
import org.shoulder.batch.dto.param.QueryImportResultDetailParam;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.dto.result.BatchRecordResult;
import org.shoulder.batch.enums.ProcessStatusEnum;
import org.shoulder.batch.model.*;
import org.shoulder.batch.model.convert.BatchModelConvert;
import org.shoulder.batch.service.BatchService;
import org.shoulder.batch.service.ExportService;
import org.shoulder.batch.service.RecordService;
import org.shoulder.batch.service.ext.BatchTaskSliceHandler;
import org.shoulder.batch.service.impl.BatchManager;
import org.shoulder.batch.service.impl.BatchProcessor;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * 批处理 api 学习&测试
 * shoulder.Batch 不仅仅提供了一个自动适应且支持单机/集群模式的 进度条查询能力，且提供了一套较完整的批处理框架、并在导入导出这里提供了较完整的解决方案：
 *
 * 使用：
 * 1. new BatchData();                               // new BatchData 并设置值
 * 2. taskId = batchService.doProcess(batchData)     // 提交处理，拿到 taskId
 * 3. batchService.queryBatchProgress(taskId)        // 根据 taskId 查询实施进度
 *
 * 内部细节原理：
 * @see BatchData 整体任务，会由 {@link BatchManager} 封装成 {@link BatchProcessor}(Runnable)，其会根据 {@link BatchTaskSliceHandler} 拆成多个 BatchDataSlice
 * @see BatchDataSlice 是任务的一个分片，但一个分片可能含有多个原子数据 {@link DataItem}
 * @see DataItem 被处理的原子数据（可能是一行数据、也可能是一个对象等）
 *
 * @author lym
 */
@RestController
@RequestMapping("batch")
public class BatchController {

    /**
     * 一般是业务类，这里注入的是框架自带的 csv 导入导出能力
     */
    @Autowired
    private BatchService batchService;

    /**
     * 导出
     */
    @Autowired
    private ExportService exportService;

    /**
     * 批量记录查询
     */
    @Autowired
    private RecordService recordService;


    /**
     * 模拟场景：上传一个 csv，导入一批数据（这里为 person 信息），真正导入数据库前会先校验，而因为数据很多，校验比较慢，需要返回给前端一个进度条
     * 1. http://localhost:8080/batch/validate  调用完该接口会快速同步返回一个任务id
     * 2. 可以在控制台日志看到查询的请求如何发 http://localhost:8080/batch/progress?taskId=xxxxx_change_me
     *
     */
    @RequestMapping(value = "validate")
    public BaseResult<String> doValidate() throws Exception {
        List<? extends DataItem> mockUploadData = randomData(10);
        AppContext.setUserId(ThreadLocalRandom.current().nextLong(10));

        BatchData batchData = new BatchData();
        Map<String, List<? extends DataItem>> allWantProcessData = new HashMap<>();
        allWantProcessData.put(DemoBatchConstants.OPERATION_VALIDATE, mockUploadData);
        batchData.setDataType(DemoBatchConstants.DATA_TYPE_PERSON);
        batchData.setOperation(DemoBatchConstants.OPERATION_VALIDATE);
        batchData.setBatchListMap(allWantProcessData);
        // 只放到内存，不做持久化
        batchData.setPersistentRecord(false);

        // 示例：解析文件，然后校验，返回校验任务标识
        String taskId = batchService.doProcess(batchData);
        System.out.println("可以在这里查询批处理进度:  http://localhost:8080/batch/progress?taskId=" + taskId);
        return BaseResult.success(taskId);
    }

    private List<PersonRecord> randomData(int num) {
        List<PersonRecord> randomDataList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            PersonRecord fakerData = new PersonRecord();
            fakerData.setIndex(i);
            fakerData.setName(UUID.randomUUID().toString().substring(0, 6));
            fakerData.setAge(ThreadLocalRandom.current().nextInt(30) + 10);
            fakerData.setSex((ThreadLocalRandom.current().nextInt(10) % 2) == 0 ? "男" : "女");
            randomDataList.add(fakerData);
        }
        return randomDataList;
    }


    /**
     * 模拟场景：批量导入
     * http://localhost:8080/batch/validate?taskId=
     */
    @RequestMapping(value = "import")
    public BaseResult<String> doImport(@RequestBody ExecuteOperationParam executeOperationParam) {
        // 示例：从缓存中拿出校验结果，根据校验结果组装为 BatchData，执行导入

        BatchData batchData = new BatchData();
        return BaseResult.success(
                batchService.doProcess(batchData)
        );
    }

    /**
     * 查询数据导入进度，注意调用时候记得传入tastId
     * http://localhost:8080/batch/progress?taskId=
     */
    @RequestMapping(value = "progress", method = GET)
    public BaseResult<BatchProcessResult> queryOperationProcess(@Nullable String taskId) {
        BatchProgressRecord process = batchService.queryBatchProgress(taskId);
        return BaseResult.success(BatchModelConvert.CONVERT.toDTO(process));
    }

    // ----------------------------------- 更完整的功能__进度管理 --------------------------------
    // 为了保证即使遇到意外宕机也可以让没进行的任务继续完成，我们往往会将任务执行记录保存在 DB，shoulder 提供了保存记录、和查询记录的能力

    /**
     * 获取某个用户的最后一次的批量处理记录
     *
     * @see RecordService 点看查看更多功能
     */
    public BaseResult<ListResult<BatchRecordResult>> queryImportRecord() {
        return BaseResult.success(
                Stream.of(recordService.findLastRecord("dataType", AppContext.getUserName()))
                        .map(BatchModelConvert.CONVERT::toDTO).collect(Collectors.toList())
        );
    }

    /**
     * 查询某次处理记录详情，提供整体视角（成功、失败数目，进度，耗时等）以及详细视角的信息（每条数据的处理状态）
     *
     * 不仅仅整体进度会保存，每条数据({@link DataItem})的处理状态会保存，并可以查询到处理时间，状态是成功/失败，失败原因等
     */
    public BaseResult<BatchRecordResult> queryImportRecordDetail(
            @RequestBody QueryImportResultDetailParam condition) {
        BatchRecord record = recordService.findRecordById("xxx");
        List<BatchRecordDetail> details = recordService.findAllRecordDetail(condition.getTaskId());
        record.setDetailList(details);
        BatchRecordResult result = BatchModelConvert.CONVERT.toDTO(record);
        return BaseResult.success(result);
    }

    // ----------------------------------- 更完整的功能__导入导出生态 todo 添加更完善示例 --------------------------------

    /**
     * 让用户导入数据前，为了方便用户，我们往往会提供一个数据导入模板的下载功能，这个能力shoulder 也已经内置了！
     * 数据导入模板下载
     * 示例： 导出 导入数据模板
     */
    public void exportImportTemplate(HttpServletResponse response, String businessType) throws IOException {
        exportService.export(response.getOutputStream(), BatchConstants.CSV, Collections.emptyList(), businessType);

    }

    /**
     * 导出数据
     */
    public void export(HttpServletResponse response, String businessType) throws IOException {
        String templateId = "testExport";
        /*exportService.export(response.getOutputStream(), BatchConstants.CSV, new Collections.singletonList(()->{
            List<Map<String, String>> dataList = new ArrayList<>();
            dataList.add()
        }), templateId);*/
    }

    /**
     * 数据导入记录详情导出
     *
     * 为了审计，导入导出往往需要有记录，shoulder甚至提供了完善的导入导出管理能力！
     */
    public void exportRecordDetail(HttpServletResponse response, QueryImportResultDetailParam condition) throws IOException {
        exportService.exportBatchDetail(response.getOutputStream(), BatchConstants.CSV, condition.getBusinessType(),
                condition.getTaskId(), CollectionUtils.emptyIfNull(condition.getStatusList())
                        .stream().map(ProcessStatusEnum::of).collect(Collectors.toList()));
    }


}
