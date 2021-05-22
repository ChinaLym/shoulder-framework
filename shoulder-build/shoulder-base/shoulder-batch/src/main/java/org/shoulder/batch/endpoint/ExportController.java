package org.shoulder.batch.endpoint;

/**
 * 导出
 *
 * @param <Entity>    实体
 * @param <PageQuery> 分页查询参数
 * @author lym
 * <p>
 * 导出Excel
 * @param params   参数
 * @param request  请求
 * @param response 响应
 * <p>
 * 预览Excel
 * @param params 预览参数
 * @return 预览html
 * <p>
 * 构建导出参数
 * @param params 分页参数
 * @param page   分页
 * @return 导出参数
 */
/*
public interface ExportController<Entity, PageQuery> {

    */
/**
 * 导出Excel
 *
 * @param params   参数
 * @param request  请求
 * @param response 响应
 *//*

    @ApiOperation(value = "导出Excel")
    @RequestMapping(value = "/export", method = RequestMethod.POST, produces = "application/octet-stream")
    @OperationLog(operation = OperationLog.Operations.EXPORT)
    default void exportExcel(@RequestBody @Validated PageParams<PageQuery> params, HttpServletRequest request, HttpServletResponse response) {
        IPage<Entity> page = params.buildPage();
        ExportParams exportParams = getExportParams(params, page);

        Map<String, Object> map = new HashMap<>(7);
        map.put(NormalExcelConstants.DATA_LIST, page.getRecords());
        map.put(NormalExcelConstants.CLASS, getEntityClass());
        map.put(NormalExcelConstants.PARAMS, exportParams);
        Object fileName = params.getExtra().getOrDefault(NormalExcelConstants.FILE_NAME, "临时文件");
        map.put(NormalExcelConstants.FILE_NAME, fileName);
        PoiBaseView.render(map, request, response, NormalExcelConstants.EASYPOI_EXCEL_VIEW);
    }

    */
/**
 * 预览Excel
 *
 * @param params 预览参数
 * @return 预览html
 *//*

    @ApiOperation(value = "预览Excel")
    @OperationLog(operation = OperationLog.Operations.EXPORT)
    @RequestMapping(value = "/preview", method = RequestMethod.POST)
    default BaseResult<String> preview(@RequestBody @Validated PageParams<PageQuery> params) {
        IPage<Entity> page = params.buildPage();
        ExportParams exportParams = getExportParams(params, page);

        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, getEntityClass(), page.getRecords());
        return BaseResult.success(ExcelXorHtmlUtil.excelToHtml(new ExcelToHtmlParams(workbook)));
    }


    */
/**
 * 构建导出参数
 *
 * @param params 分页参数
 * @param page   分页
 * @return 导出参数
 *//*

    default ExportParams getExportParams(PageParams<PageQuery> params, IPage<Entity> page) {
        query(params, page, params.getSize() == -1 ? Convert.toLong(Integer.MAX_VALUE) : params.getSize());

        Object title = params.getExtra().get("title");
        Object type = params.getExtra().getOrDefault("type", ExcelType.XSSF.name());
        Object sheetName = params.getExtra().getOrDefault("sheetName", "SheetName");

        ExcelType excelType = ExcelType.XSSF.name().equals(type) ? ExcelType.XSSF : ExcelType.HSSF;
        return new ExportParams(String.valueOf(title), sheetName.toString(), excelType);
    }

}
*/
