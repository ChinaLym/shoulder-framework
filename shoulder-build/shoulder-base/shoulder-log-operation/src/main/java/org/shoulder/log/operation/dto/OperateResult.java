package org.shoulder.log.operation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.shoulder.log.operation.constants.OperationResult;

/**
 * 可以判断操作结果的对象
 *  如批量操作有多个字段需要返回，返回结果使用 DTO 接收，DTO某个字段表示本次操作是否成功
 *      使用 {@link OperationResult#of} resultList 就可以判断本次批量操作是 成功 or 失败 or 部分成功
 *
 * @author lym
 */
@FunctionalInterface
public interface OperateResult {

    /**
     * 此次操作结果是否成功
     *  （接口粒度为单次操作，单次操作只有成功或失败）
     * @return true 成功， false 失败
     */
    @JsonIgnore
    boolean success();
}
