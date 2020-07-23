package org.shoulder.log.operation.dto;

/**
 * 一次操作记录（包含所有操作日志中不可预料的值）
 * 常用于批量场景
 *
 * @author lym
 */
public interface OperateRecord extends Operable, OperationDetailAble, OperateResult {

}
