package org.shoulder.batch.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * - 导入、导出功能
 * - 导入模板下载
 * - 上传导入文件 / 解析
 * - 查询导入校验进度
 * - 批量操作
 * - 查询数据操作进度
 * - 数据导入记录，分页查询、导出
 * /batch-operation/{dataType}/{operationType}
 * 如 /batch-operation/user/add-validate
 * 如 /batch-operation/user/add
 * 如 /batch-operation/user/update-validate
 * 如 /batch-operation/user/update
 * 带默认 api 请求路径，实际中可以通过继承复写来替换
 *
 * @author lym
 */
@Tag(name = "ImportRestfulApi", description = "数据批处理接口")
@RequestMapping(value = "${shoulder.web.ext.batch.path:/api/v1/batch/{dataType}}")
@Validated
public interface ImportRestfulApi {

    /**
     * 上传数据导入文件 / 提交校验
     * 校验不写操作日志，已经有批处理记录
     *
     * @see ImportController#queryProcess 查询进度
     */
    @Operation(summary = "上传并校验数据导入文件",
        description = "上传CSV文件进行数据导入并执行校验。",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)),
        parameters = {
            @Parameter(name = "dataType", in = ParameterIn.PATH, required = true,
                description = "正在导入的数据类型",
                schema = @Schema(type = "string", example = "orders")),
            @Parameter(name = "file", style = ParameterStyle.FORM, required = true,
                description = "待导入的CSV文件",
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE),
                extensions = @Extension(properties = {
                    @ExtensionProperty(name = "allowSuffix", value = "csv"),
                    @ExtensionProperty(name = "maxSize", value = "10M"),
                    @ExtensionProperty(name = "allowEmpty", value = "false")
                })),
            @Parameter(name = "encoding", in = ParameterIn.QUERY, required = false,
                description = "文件字符集编码（不传采用系统默认编码）",
                schema = @Schema(type = "string", example = "gbk"))
        }, method = "POST")
    @RequestMapping(value = "validate", method = { POST })
    BaseResult<String> validate(@PathVariable(value = "dataType") String businessType,
                                @NotNull @FileType(allowSuffix = "csv", maxSize = "10M", allowEmpty = false) MultipartFile file,
                                @RequestParam(name = "encoding", required = false) String encoding)
        throws Exception;

    /**
     * 推进批处理阶段
     *
     * @param advanceBatchParam 操作参数
     * @return result
     */
    @Operation(summary = "推进批处理阶段",
        description = "推进批处理至下一个阶段",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "AdvanceBatchParam 阶段推进参数",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), required = true, useParameterTypeSchema = true
        ),
        method = "POST")
    @RequestMapping(value = "advance", method = POST)
    BaseResult<String> advance(@Validated @RequestBody @NotNull AdvanceBatchParam advanceBatchParam);

    /**
     * 查询数据操作进度
     *
     * @param batchId 批处理任务id
     * @return 操作进度 / 结果
     */
    @Operation(summary = "查询数据操作进度",
        description = "获取指定批处理任务的操作进度与结果",
        parameters = {
            @Parameter(name = "batchId", in = ParameterIn.PATH, required = true,
                description = "批次ID",
                schema = @Schema(type = "string", example = "12341234"))
        }, method = "GET")
    @RequestMapping(value = "progress/{batchId}", method = { GET, POST })
    BaseResult<BatchProcessResult> queryProcess(@PathVariable("batchId") String batchId);

    // ===================================  导入记录查询  =====================================

    /**
     * 查询最近一次处理记录
     * 可用于界面展示，（当前用户）最近一次导入记录
     * todo 【进阶】支持分页查询，查所有历史
     *
     * @return 分页-批量处理进度 / 结果
     */
    @Operation(summary = "查询最近一次处理记录",
        description = "查询最近一次导入记录，暂不支持分页",
        parameters = {
            @Parameter(name = "dataType", in = ParameterIn.PATH, required = true,
                description = "数据类型",
                schema = @Schema(type = "string", example = "user"))
        },
        method = "GET")
    @RequestMapping(value = "record/list", method = { GET, POST })
    BaseResult<ListResult<BatchRecordResult>> pageQueryImportRecord(@PathVariable("dataType") String dataType);

    /**
     * 查询某次处理记录详情
     * 场景：数导入后，查看导入校验 / 处理结果
     * 历史导入，查看详情
     *
     * @param condition 过滤条件
     * @return 批量处理进度 / 结果
     */
    @Operation(summary = "查询某次处理记录详情",
        description = "查询指定条件下的导入记录详情，支持分页",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "QueryImportResultDetailParam 查询导入记录详情参数",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), required = true, useParameterTypeSchema = true
        )
    )
    @RequestMapping(value = "record/detail/list", method = { GET, POST })
    BaseResult<BatchRecordResult> pageQueryImportRecordDetail(@NotNull @Valid @RequestBody QueryImportResultDetailParam condition);

    // ===================================  导出相关  =====================================

    /**
     * 数据导入模板下载
     * <p>
     * todo 【开发】数据类型、业务操作类型
     */
    @Operation(summary = "数据导入模板下载",
        description = "下载数据导入模板",
        responses = { @ApiResponse(responseCode = "200", description = "下载成功",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) },
        parameters = {
            @Parameter(name = "encoding", in = ParameterIn.QUERY,
                description = "文件字符集编码（不传采用系统默认编码）",
                schema = @Schema(type = "string", example = "gbk"))
        })
    @RequestMapping(value = "template/download", method = { GET, POST })
    void exportImportTemplate(HttpServletResponse response,
                              @PathVariable("dataType") String businessType,
                              @RequestParam(name = "encoding", required = false) String encoding) throws IOException;

    /**
     * 数据导入记录详情导出
     * 注意响应 header 字符集要与实际导出字符集一致，否则可能乱码
     * excel 默认字符集为 Unicode，可能乱码（用户自行处理）
     *
     * @param condition 过滤条件
     * @throws IOException 流异常
     */
    @Operation(summary = "数据导入记录详情导出",
        description = "搜索结果全部导出。注意：文件名对于中文已进行UTF-8编码，前端需进行URL.decode解码操作",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "QueryImportResultDetailParam 导出筛选条件参数",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), required = true, useParameterTypeSchema = true
        ),
        responses = { @ApiResponse(responseCode = "200", description = "下载成功",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) },
        method = "POST")
    @PostMapping(value = "record/detail/export")
    void exportRecordDetail(HttpServletResponse response, @NotNull @Valid @RequestBody QueryImportResultDetailParam condition)
        throws IOException;

    /**
     * 导出数据
     * todo 【开发】查询条件 + Request
     *
     * @throws IOException 数据流错误
     */
    @Operation(summary = "导出",
        description = "搜索结果全部导出。注意：文件名对于中文已进行UTF-8编码，前端需进行URL.decode解码操作",
        responses = { @ApiResponse(responseCode = "200", description = "下载成功",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "PageQuery<Map> 导出筛选条件参数",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE), required = true, useParameterTypeSchema = true
        ),
        parameters = {
            @Parameter(name = "dataType", in = ParameterIn.PATH, required = true, description = "数据类型",
                schema = @Schema(type = "string", example = "user"))
        }, method = "POST")
    @PostMapping("export")
    void export(HttpServletResponse response,
                @PathVariable(value = "dataType") String businessType,
                @RequestBody PageQuery<Map> exportCondition
    ) throws IOException;

}
