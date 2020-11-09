package org.shoulder.batch.service;


/**
 * <p>导入<p>
 *
 * @author lym
 */
public interface ImportService {


    /**
    * <p>判断是否允许导入<p>
    *
    * @return boolean
    */
    boolean canImport();

    /**
    * <p>导入信息<p>
    *
    * @param importDto 导入/更新
    * @param userIndexCode 用户信息
    * @param languageId 语言标识
    * @return 导入任务标识
    */
    default String doImport(ImportPersistentDto importDto, String userIndexCode, String languageId) {
        return this.doImport(importDto, userIndexCode, languageId, null);
    }


    /**
    * <p>导入<p>
    *
    * @param importDto 导入入参
    * @param userIndexCode 用户信息
    * @param languageId 语言标识
    * @param moduleSpecialHandler 特殊业务处理器
    * @return 导入任务编码
    */
    String doImportData(ImportPersistentDto importDto, String userIndexCode, String languageId,
                        ModuleSpecialHandler moduleSpecialHandler);

    /**
     * 获取导入进度与结果
     *
     * @param taskId 用户信息
     * @return Object 导入进度或者结果
     */
    ImportResult findImportResult(String taskId);

}
