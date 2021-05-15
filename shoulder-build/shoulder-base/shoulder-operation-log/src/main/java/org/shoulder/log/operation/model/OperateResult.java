package org.shoulder.log.operation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.shoulder.log.operation.enums.OperationResult;

/**
 * 可以判断操作结果的对象
 * 如批量操作场景下返回的多个DTO，DTO中某个字段表示本次操作是否成功，实现该接口后
 * 可以使用 {@link OperationResult#of} 就可以判断本次批量操作是 成功 or 失败 or 部分成功
 *
 * @author lym
 */
@FunctionalInterface
public interface OperateResult {

    /**
     * 此次操作结果是否成功
     * （接口粒度为单次操作，单次操作只有成功或失败）
     *
     * @return true 成功， false 失败
     */
    @JsonIgnore
    boolean success();
}
