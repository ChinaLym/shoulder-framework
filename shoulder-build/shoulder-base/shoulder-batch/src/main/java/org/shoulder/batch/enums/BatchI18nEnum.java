package org.shoulder.batch.enums;


/**
 * 导入导出国际化
 *
 * @author lym
 */
public enum BatchI18nEnum {


    // ============================== 页面 ==================================
    /**
     * 行号
     * @zh_CN 行号
     */
    LINE_NUM("shoulder.batch.common.label.linNum"),
    /**
     * 第几行
     */
    SPECIAL_LINE("shoulder.batch.common.label.lineNum.special"),
    /**
     * 导出数据-校验详情-导入结果
     */
    VALIDATE_RESULT("shoulder.batch.common.label.result"),
    /**
     * 导出数据-校验详情-详情
     */
    VALIDATE_DETAIL("shoulder.batch.common.label.detail"),



    // ============================== 上传导入文件提示 ==================================

    /**
     * csv导入模板下载-csv文件名
     */
    EXPORT_MODE_CSVNAME("shoulder.batch.import.template.download.csvName"),
    /**
     * csv导入模板下载开始提示
     */
    EXPORT_MODE_MSG("shoulder.batch.import.template.download.msg"),


    /**
     * 导入时候有其他导入正在进行
     */
    OTHER_IMPORTPROCESS("shoulder.batch.import.other.importProcess"),
    // ============================== 上传导入文件提示 ==================================
    /**
     * 上传文件模板不正确
     */
    UPLOADFILE_MODE_ERROR("shoulder.batch.upload.template.error"),
    /**
     * 上传文件行数超出限制
     */
    UPLOADFILE_ROW_LIMIT("shoulder.batch.upload.row.limit"),
    /**
     * 上传文件太大
     */
    UPLOADFILE_SIZE_LIMIT("shoulder.batch.upload.size.limit"),


    // ============================== 导入结果 ==================================
    /**
     * @zh_CN 未知
     */
    RESULT_UNKNOWN("shoulder.batch.result.unknown"),
    /**
     * @zh_CN 校验成功
     */
    RESULT_VAIDATE_SUCCESS("shoulder.batch.result.validate.success"),
    /**
     * @zh_CN 校验失败
     */
    RESULT_VAIDATE_FAILED("shoulder.batch.result.validate.failed"),
    /**
     * @zh_CN 导入成功
     */
    RESULT_IMPORT_SUCCESS("shoulder.batch.result.import.success"),
    /**
     * @zh_CN 导入失败
     */
    RESULT_IMPORT_FAILED("shoulder.batch.result.import.failed"),
    /**
     * @zh_CN 更新
     */
    RESULT_IMPORT_UPDATE("shoulder.batch.result.import.update"),
    /**
     * @zh_CN 重复跳过
     */
    RESULT_IMPORT_SKIP("shoulder.batch.result.import.skip"),


    // ==================== 校验提示 =====================
    /**
     * 重复校验-和DB重复提示
     */
    VALIDATE_REPEAT_DB("shoulder.batch.validate.repeat.db"),
    /**
     * 校验未知错误提示
     */
    VALIDATE_UNKOWERROR("shoulder.batch.validate.unknownError"),
    /**
     * 存在相同行
     */
    VALIDATE_REGIONPATH_REPEAT_LINE("shoulder.batch.validate.repeat"),


    /**
     * 导出历史导入详情不包含敏感信息提示
     */
    RESULT_EXPORT_HEADER_SENSITIVE_TIP("shoulder.batch.import.header.sensitive.tip"),

    /**
     * 导入数量：%1，成功：%2，失败：%3
     */
    BATCH_DEVICE_MESSAGE("shoulder.batch.import.batch.device.detail"),
    /**
     * 导出数量：%1
     */
    EXPORT_BATCH_DEVICE_MESSAGE("shoulder.batch.export.batch.device.detail"),


    // -------------------------- 非多语言，而是允许格式可通过资源包形式扩展---------------
    /**
     * 键值对，用于行较重时拼接 {0}:{1}
     */
    REPEAT_DIGEST("shoulder.batch.import.repeat.digest"),
    /**
     * 多个键值对的分隔符
     */
    REPEAT_DIGEST_BREAK("shoulder.batch.import.repeat.break"),
    ;



    private String code;

    BatchI18nEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
