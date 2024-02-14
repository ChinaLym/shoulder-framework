package org.shoulder.log.operation.enums;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.dictionary.model.IntDictionaryItemEnum;
import org.shoulder.log.operation.model.OperateResult;

import java.util.Collection;

/**
 * 业务操作结果：
 * 成功或失败：1表示成功，0表示失败，2表示部分成功；
 * 正确或不正确：1表示正确，0表示不正确；
 * <p>
 * 操作结果在一个业务中哪怕输入相同结果也可能不确定，常用作函数参数，不在注解中出现，因此通过枚举类使用
 *
 * @author lym
 */
@Getter public enum OperationResult implements IntDictionaryItemEnum<OperationResult> {

    /**
     * 成功 | 正确
     */
    SUCCESS(0),

    /**
     * 失败 | 不正确
     */
    FAIL(1),

    /**
     * 部分成功（适用于批量操作，如批量保存10条数据，5条成功，5条失败）
     */
    PARTIAL(2);

    public final Integer itemId;

    OperationResult(int itemId) {
        this.itemId = itemId;
    }

    public static OperationResult of(boolean success) {
        return success ? SUCCESS : FAIL;
    }

    /**
     * 如果 hasSuccess、hasFail 都为 false 则默认为操作成功
     */
    public static OperationResult of(boolean hasSuccess, boolean hasFail) {
        return hasSuccess && hasFail ?
            OperationResult.PARTIAL :
            hasFail ?
                OperationResult.FAIL : OperationResult.SUCCESS;
    }

    /**
     * 如果操作结果为空默认为成功
     */
    public static OperationResult of(Collection<? extends OperateResult> results) {
        if (CollectionUtils.isEmpty(results)) {
            return OperationResult.SUCCESS;
        }

        boolean hasSuccess = false;
        boolean hasFail = false;
        for (OperateResult dto : results) {
            if (hasSuccess && hasFail) {
                break;
            }
            if (dto.success()) {
                hasSuccess = true;
            } else {
                hasFail = true;
            }
        }
        return of(hasSuccess, hasFail);
    }

}
