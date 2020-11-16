package org.shoulder.batch.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.shoulder.batch.dto.param.ExecuteOperationParam;
import org.shoulder.batch.dto.param.QueryImportResultDetailParam;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.core.dto.response.RestResult;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * - 导入、导出功能
 * - 导入模板下载
 * - 上传导入文件 / 解析
 * - 查询导入校验进度
 * - 批量操作
 * - 查询数据操作进度
 * - 数据导入记录，分页查询、导出 todo 数据类型、业务操作类型
 *
 * @author lym
 */
@Api(tags = {"数据批量操作"})
@RequestMapping("batch")
public interface ImportRestfulApi {


    /**
     * 上传数据导入文件 / 提交校验
     */
    @ApiOperation(value = "上传数据导入文件", consumes = "text/html", httpMethod = "POST")
    @ApiImplicitParams({
        @ApiImplicitParam(value = "对应的区域ID", name = "regionIndexCode", example = "312312312312",
            defaultValue = "3123412312321", required = true, paramType = "body"),
        @ApiImplicitParam(value = "文件编码", name = "charsetLanguage", example = "gbk",
            defaultValue = "gbk", required = true, paramType = "body")
    })
    @RequestMapping(value = "template/upload", method = {RequestMethod.POST})
    RestResult<String> doValidate(MultipartFile file,
                                  @RequestParam(name = "charsetLanguage", required = false) String charsetLanguage)
        throws Exception;

    /**
     * 查询数据导入数据校验进度
     */
    /*@ApiOperation(value = "查询数据导入数据校验进度", produces = MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "GET")
    @ApiImplicitParam(value = "批次ID", name = "taskId", example = "452f5wq6", defaultValue = "452f5wq6",
            required = true, paramType = "path")
    @RequestMapping(value = "progress/{taskId}", method = GET)
    RestResult<BatchProcessResult> queryValidateProcess(String taskId);*/

    /**
     * 批量操作
     *
     * @param executeOperationParam 操作参数
     * @return result
     */
    @ApiOperation(value = "批量操作", produces = MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "POST")
    @RequestMapping(value = "import", method = RequestMethod.POST)
    RestResult<String> doImport(ExecuteOperationParam executeOperationParam);

    /**
     * 查询数据操作进度，todo 考虑 查进度和结果是否为同一个接口？
     *
     * @param taskId 任务标识
     * @return 操作进度 / 结果
     */
    @ApiOperation(value = "查询数据操作进度", produces = MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "GET")
    @ApiImplicitParam(value = "批次ID", name = "taskId", example = "312312312312", defaultValue = "3123412312321",
        required = true, paramType = "path")
    @RequestMapping(value = "progress/{taskId}", method = GET)
    RestResult<BatchProcessResult> queryOperationProcess(@PathVariable("taskId") String taskId);


    // ===================================  导入记录查询  =====================================


    /**
     * 查询处理记录
     * 可用于界面展示，（当前用户）最近一次导入记录
     * todo 查询条件
     *
     * @return 分页-批量处理进度 / 结果
     */
    @ApiOperation(value = "查询处理记录", notes = "支持分页", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "GET")
    @RequestMapping(value = "record/list", method = RequestMethod.GET)
    RestResult<PageResult<BatchProcessResult>> queryImportRecord();


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
    @RequestMapping(value = "record/detail", method = RequestMethod.POST)
    RestResult<BatchProcessResult> queryImportRecordDetail(QueryImportResultDetailParam condition);

    // ===================================  导出相关  =====================================


    /**
     * 数据导入模板下载
     * <p>
     * todo 数据类型、业务操作类型
     */
    @ApiOperation(value = "数据导入模板下载", consumes = "text/csv", httpMethod = "GET")
    @ApiImplicitParam(value = "文件编码", name = "charsetLanguage", example = "gbk",
        defaultValue = "gbk", required = true, paramType = "query")
    @RequestMapping(value = "template/{businessType}/download", method = RequestMethod.GET)
    void exportImportTemplate(HttpServletResponse response,
                              @PathVariable(value = "businessType") String businessType) throws IOException;


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
    void exportRecordDetail(HttpServletResponse response, QueryImportResultDetailParam condition) throws IOException;

    /**
     * 导出数据
     * todo 查询条件 + Request
     *
     * @throws IOException 数据流错误
     */
    /*@ApiOperation(value = "导出", notes = "搜索结果全部导出" +
            "注意：文件名对于中文已进行UTF-8编码，前端需进行URL.decode解码操作", produces = "text/csv", consumes =
            MimeTypeUtils.APPLICATION_JSON_VALUE, httpMethod = "GET")
    @RequestMapping(value = "export", method = GET)
    void export(HttpServletResponse response, @PathVariable(value = "businessType") String businessType) throws IOException;*/

}
