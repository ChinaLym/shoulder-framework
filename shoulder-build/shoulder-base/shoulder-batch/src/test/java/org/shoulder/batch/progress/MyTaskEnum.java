package org.shoulder.batch.progress;

public enum MyTaskEnum implements BatchActivityEnum<MyTaskEnum> {
    TASK_BLOCK1_MAIN_1("x", "任务1", 0, 0),

    TASK_BLOCK1_MAIN_2("2", "任务2", 0, 0),
    TASK_BLOCK1_MAIN_3("3", "任务3", 0, 0),
    ;

    private final String taskKey;
    private final String description;
    private final int displayBlockNum;
    private final int getDisplayColumnNum;
    private final String displayEmoji;

    MyTaskEnum(String displayEmoji, String description, int displayBlockNum, int getDisplayColumnNum) {
        this.taskKey = name();
        this.displayEmoji = displayEmoji;
        this.description = description;
        this.displayBlockNum = displayBlockNum;
        this.getDisplayColumnNum = getDisplayColumnNum;
    }

    @Override
    public String getKey() {
        return taskKey;
    }

    @Override
    public boolean hasSubTask() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return description;
    }

    @Override
    public String getDisplayEmoji() {
        return displayEmoji;
    }

    @Override
    public int displayBlockNum() {
        return displayBlockNum;
    }

    @Override
    public int getDisplayColumnNum() {
        return getDisplayColumnNum;
    }
}