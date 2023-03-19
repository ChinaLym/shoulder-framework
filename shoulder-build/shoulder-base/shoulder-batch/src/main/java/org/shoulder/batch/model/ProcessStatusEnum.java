package org.shoulder.batch.model;

public enum ProcessStatusEnum {

    WAITING("未开始", 0),
    RUNNING("运行中", 1),
    EXCEPTION("异常终止", 2),
    FINISHED("正常结束", 3),

    ;

    private final String name;

    private final int code;

    ProcessStatusEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}
