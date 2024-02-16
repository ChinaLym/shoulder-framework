package org.shoulder.batch.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.shoulder.batch.dto.param.AdvanceBatchParam;
import org.shoulder.batch.dto.param.QueryImportResultDetailParam;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.dto.result.BatchRecordResult;
import org.shoulder.core.dto.request.PageQuery;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.validate.annotation.FileType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * - 导入、导出功能
 * - 导入模板下载
 * - 上传导入文件 / 解析
 * - 查询导入校验进度
 * - 批量操作
 * - 查询数据操作进度
 * - 数据导入记录，分页查询、导出 todo 【开发】数据类型、业务操作类型
 *  /batch-operation/{dataType}/{operationType}
 *  如 /batch-operation/user/add-validate
 *  如 /batch-operation/user/add
 *  如 /batch-operation/user/update-validate
 *  如 /batch-operation/user/update
 *  带默认 api 请求路径，实际中可以通过继承复写来替换
 *
 * @author lym
 */
@Api(tags = {"数据批量操作"})
@RequestMapping(value = "${shoulder.web.ext.batch.apiPath:/api/v1/batch/{dataType}}")
@Validated
public interface ImportRestfulApi {


    /**
     * 上传数据导入文件 / 提交校验
     * 校验不写操作日志，已经有批处理记录
     *
     * @see ImportController#queryProcess 查询进度
     */
    @ApiOperation(value = "上传数据导入文件", consumes = "text/html", httpMethod = "POST")
    @ApiImplicitParam(value = "文件编码", name = "charsetLanguage", example = "gbk",
            defaultValue = "gbk", required = true, paramType = "body")
    @RequestMapping(value = "validate", method = {RequestMethod.POST})
    BaseResult<String> validate(@PathVariable(value = "dataType") String businessType,
                                @NotNull @FileType(allowSuffix = "csv", maxSize = "10M", allowEmpty = false) MultipartFile file,
                                @RequestParam(name = "charsetLanguage", required = false) String charsetLanguage)
            throws Exception;

    /**
     * 推进批处理阶段
     *
     * @param advanceBatchParam 操作参数
     * @return result
     */
    @ApiOperation(value = "批量操作", produces = MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "POST")
    @RequestMapping(value = "advance", method = RequestMethod.POST)
    BaseResult<String> advance(@Validated @RequestBody AdvanceBatchParam advanceBatchParam);

    /**
     * 查询数据操作进度，todo 【开发】考虑 查进度和结果是否为同一个接口？进度不需要每行信息
     *
     * @param batchId 批处理任务id
     * @return 操作进度 / 结果
     */
    @ApiOperation(value = "查询数据操作进度", produces = MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "GET")
    @ApiImplicitParam(value = "批次ID", name = "batchId", example = "312312312312", defaultValue = "3123412312321",
            required = true, paramType = "path")
    @RequestMapping(value = "progress/{batchId}", method = GET)
    BaseResult<BatchProcessResult> queryProcess(@PathVariable("batchId") String batchId);


    // ===================================  导入记录查询  =====================================


    /**
     * 查询处理记录
     * 可用于界面展示，（当前用户）最近一次导入记录
     * todo 【开发】查询条件 暂不支持分页
     *
     * @return 分页-批量处理进度 / 结果
     */
    @ApiOperation(value = "查询处理记录", notes = "暂不支持分页", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "GET")
    @RequestMapping(value = "record/list", method = RequestMethod.GET)
    BaseResult<ListResult<BatchRecordResult>> pageQueryImportRecord(@PathVariable(value = "dataType") String dataType);


    /**
     * 查询某次处理记录详情
     * 场景： 数导入后，查看导入校验 / 处理结果
     * 历史导入，查看详情
     *
     * @param condition 过滤条件
     * @return 批量处理进度 / 结果
     */
    @ApiOperation(value = "查询某次处理记录详情", notes = "支持分页", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE
            , produces = MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "POST")
    @RequestMapping(value = "record/detail/list", method = RequestMethod.POST)
    BaseResult<BatchRecordResult> pageQueryImportRecordDetail(@RequestBody @NotNull @Valid QueryImportResultDetailParam condition);

    // ===================================  导出相关  =====================================


    /**
     * 数据导入模板下载
     * <p>
     * todo 【开发】数据类型、业务操作类型
     *
     * @return
     */
    @ApiOperation(value = "数据导入模板下载", consumes = "text/csv", httpMethod = "GET")
    @ApiImplicitParam(value = "文件编码", name = "charsetLanguage", example = "gbk",
        defaultValue = "gbk", required = true, paramType = "query")
    @RequestMapping(value = "template/download", method = RequestMethod.GET)
    void exportImportTemplate(HttpServletResponse response,
                                                  @PathVariable(value = "dataType") String businessType) throws IOException;


    /**
     * 数据导入记录详情导出
     *
     * @param condition 过滤条件
     * @throws IOException 流异常
     */
    @ApiOperation(value = "数据处理记录详情导出", notes = "搜索结果全部导出" +
        "注意：文件名对于中文已进行UTF-8编码，前端需进行URL.decode解码操作", produces = "text/csv", consumes =
        MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "GET")
    @ApiImplicitParam(value = "文件编码", name = "charsetLanguage", example = "gbk",
        defaultValue = "gbk", required = true, paramType = "query")
    @RequestMapping(value = "record/detail/export", method = GET)
    void exportRecordDetail(HttpServletResponse response, @RequestBody @NotNull @Valid QueryImportResultDetailParam condition) throws IOException;

    /**
     * 导出数据
     * todo 【开发】查询条件 + Request
     *
     * @throws IOException 数据流错误
     */
    @ApiOperation(value = "导出", notes = "搜索结果全部导出" +
            "注意：文件名对于中文已进行UTF-8编码，前端需进行URL.decode解码操作", produces = "text/csv", consumes =
            MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "GET")
    @RequestMapping(value = "export", method = GET)
    void export(HttpServletResponse response,
                @PathVariable(value = "dataType") String businessType,
                @RequestBody PageQuery<Map> exportCondition
    ) throws IOException;

}
