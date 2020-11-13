package org.shoulder.batch.dto;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;


/**
 * 导入结果dto
 *
 * @author lym
 */
public class ImportCsvResultDto implements Serializable, Comparable<ImportCsvResultDto> {

    private static final long serialVersionUID = 1L;

    private Integer lineNum;

    private Integer result;

    private String reason;

    private Object repeatData;

    private String[] multiLangParam; //多语言参数

    private List<String[]> langParamList;

    private String importName;

    private String dataSource;

    public ImportCsvResultDto() {
        super();
    }

    public ImportCsvResultDto(Integer lineNum, Integer result, String reason) {
        super();
        this.lineNum = lineNum;
        this.result = result;
        if (StringUtils.isEmpty(reason)) {
            this.reason = "";
        } else {
            this.reason = reason;
        }
    }

    public ImportCsvResultDto(Integer lineNum, String reason) {
        super();
        this.lineNum = lineNum;
        if (StringUtils.isEmpty(reason)) {
            this.result = 3;
        } else {
            this.result = 4;
        }
        if (StringUtils.isEmpty(reason)) {
            this.reason = "";
        } else {
            this.reason = reason;
        }
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }


    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        if (StringUtils.isEmpty(reason)) {
            this.reason = "";
        } else {
            this.reason = reason;
        }
    }

    public Object getRepeatData() {
        return repeatData;
    }

    public void setRepeatData(Object repeatData) {
        this.repeatData = repeatData;
    }

    public String[] getMultiLangParam() {
        if (null != multiLangParam) {
            String[] arrays = new String[multiLangParam.length];
            System.arraycopy(multiLangParam, 0, arrays, 0, multiLangParam.length);
            return arrays;
        }
        return new String[]{};
    }

    public void setMultiLangParam(String[] multiLangParam) {
        if (null != multiLangParam) {
            this.multiLangParam = new String[multiLangParam.length];
            System.arraycopy(multiLangParam, 0, this.multiLangParam, 0, multiLangParam.length);
        } else {
            this.multiLangParam = null;
        }
    }

    public List<String[]> getLangParamList() {
        return langParamList;
    }

    public void setLangParamList(List<String[]> langParamList) {
        this.langParamList = langParamList;
    }

    public String getImportName() {
        return importName;
    }

    public void setImportName(String importName) {
        this.importName = importName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ImportCsvResultDto [lineNum=");
        builder.append(lineNum);
        builder.append(", success=");
        builder.append(result);
        builder.append(", reason=");
        builder.append(reason);
        builder.append(", repeatData=");
        builder.append(repeatData);
        builder.append(", multiLangParam=");
        builder.append(Arrays.toString(multiLangParam));
        builder.append(", langParamList=");
        builder.append(langParamList);
        builder.append(", importName=");
        builder.append(importName);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((lineNum == null) ? 0 : lineNum.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImportCsvResultDto other = (ImportCsvResultDto) obj;
        if (lineNum == null) {
            if (other.lineNum != null) {
                return false;
            }
        } else if (!lineNum.equals(other.lineNum)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ImportCsvResultDto o) {
        Integer order1 = this.getLineNum() == null ? Integer.valueOf(0) : this.getLineNum();
        Integer order2 = o.getLineNum() == null ? Integer.valueOf(0) : o.getLineNum();
        return order1.compareTo(order2);
    }
}
