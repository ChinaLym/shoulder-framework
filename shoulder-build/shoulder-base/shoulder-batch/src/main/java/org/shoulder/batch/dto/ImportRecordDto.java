package org.shoulder.batch.dto;

import java.io.Serializable;
import java.util.Date;

/**
 *
 *
 * @author lym
 */
public class ImportRecordDto implements Serializable {

    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 1682515371888642445L;

    /**
     * 导入时间
     */
    private Date importDate;

    /**
     * 结果
     */
    private String result;

    /**
     * 账号
     */
    private String accountNum;

    /**
     * 成功数量
     */
    private Integer successNum;

    /**
     * 失败数量
     */
    private Integer failNum;

    /**
     * 主键
     */
    private String importId;

    /**
     * 总数量
     */
    private Integer totalNum;


    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAccountNum() {
        return accountNum;
    }

    public void setAccountNum(String accountNum) {
        this.accountNum = accountNum;
    }

    public Integer getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(Integer successNum) {
        this.successNum = successNum;
    }

    public Integer getFailNum() {
        return failNum;
    }

    public void setFailNum(Integer failNum) {
        this.failNum = failNum;
    }

    public String getImportId() {
        return importId;
    }

    public void setImportId(String importId) {
        this.importId = importId;
    }

    public Integer getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }
}
