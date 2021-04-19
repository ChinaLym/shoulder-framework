package com.example.demo2.controller.batch;

import com.example.demo2.dto.PersonRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.dto.param.ExecuteOperationParam;
import org.shoulder.batch.dto.param.QueryImportResultDetailParam;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.dto.result.BatchRecordResult;
import org.shoulder.batch.enums.BatchConstants;
import org.shoulder.batch.enums.BatchResultEnum;
import org.shoulder.batch.model.*;
import org.shoulder.batch.model.convert.BatchModelConvert;
import org.shoulder.batch.service.BatchService;
import org.shoulder.batch.service.ExportService;
import org.shoulder.batch.service.RecordService;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * 使用 mybatis-plus，基本不需要写基础代码
 *
 * @author lym
 */
@RestController
@RequestMapping("batch")
public class BatchController {

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
     * 实现举例：上传一个 csv，准备导入一批人员信息
     * http://localhost:8080/batch/validate
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
     * 实现举例：批量导入
     */

    public BaseResult<String> doImport(@RequestBody ExecuteOperationParam executeOperationParam) {
        // 示例：从缓存中拿出校验结果，根据校验结果组装为 BatchData，执行导入

        BatchData batchData = new BatchData();
        return BaseResult.success(
                batchService.doProcess(batchData)
        );
    }

    /**
     * 查询数据导入进度
     * http://localhost:8080/batch/progress?taskId=
     */
    @RequestMapping(value = "progress", method = GET)
    public BaseResult<BatchProcessResult> queryOperationProcess(@Nullable String taskId) {
        BatchProgress process = batchService.queryBatchProgress(taskId);
        return BaseResult.success(BatchModelConvert.CONVERT.toDTO(process));
    }


    /**
     * 查询数据导入记录
     */

    public BaseResult<ListResult<BatchRecordResult>> queryImportRecord() {
        return BaseResult.success(
                Stream.of(recordService.findLastRecord("dataType", AppContext.getUserName()))
                        .map(BatchModelConvert.CONVERT::toDTO).collect(Collectors.toList())
        );
    }

    /**
     * 查询某次处理记录详情
     */

    public BaseResult<BatchRecordResult> queryImportRecordDetail(
            @RequestBody QueryImportResultDetailParam condition) {
        BatchRecord record = recordService.findRecordById("xxx");
        List<BatchRecordDetail> details = recordService.findAllRecordDetail(condition.getTaskId());
        record.setDetailList(details);
        BatchRecordResult result = BatchModelConvert.CONVERT.toDTO(record);
        return BaseResult.success(result);
    }


    /**
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
     */

    public void exportRecordDetail(HttpServletResponse response, QueryImportResultDetailParam condition) throws IOException {
        exportService.exportBatchDetail(response.getOutputStream(), BatchConstants.CSV, condition.getBusinessType(),
                condition.getTaskId(), CollectionUtils.emptyIfNull(condition.getStatusList())
                        .stream().map(BatchResultEnum::of).collect(Collectors.toList()));
    }


}
