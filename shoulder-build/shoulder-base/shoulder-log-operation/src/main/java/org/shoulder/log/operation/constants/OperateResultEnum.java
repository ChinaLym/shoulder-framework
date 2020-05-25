package org.shoulder.log.operation.constants;

import org.shoulder.log.operation.dto.OperateResult;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

/**
 * 业务操作结果：
 * 成功或失败：1表示成功，0表示失败，2表示部分成功；
 * 正确或不正确：1表示正确，0表示不正确；
 *
 * 操作结果在一个业务中哪怕输入相同结果也可能不确定，常用作函数参数，不在注解中出现，因此通过枚举类使用
 * @author lym
 */
public enum OperateResultEnum {

    /**
     * 成功 | 正确
     */
    SUCCESS("1"),

    /**
     * 失败 | 不正确
     */
    FAIL("0"),

    /**
     * 部分成功
     */
    PARTIAL("2");

    public final String code;

    OperateResultEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static OperateResultEnum of(boolean success) {
        return success ? SUCCESS : FAIL;
    }

    /**
     * 如果 hasSuccess、hasFail 都为 false 则默认为操作成功
     */
    public static OperateResultEnum of(boolean hasSuccess, boolean hasFail) {
        return hasSuccess && hasFail ?
                    OperateResultEnum.PARTIAL :
                hasFail ?
                        OperateResultEnum.FAIL : OperateResultEnum.SUCCESS;
    }

    /**
     * 如果操作结果为空默认为成功
     */
    public static OperateResultEnum of(Collection<? extends OperateResult> results) {
        if(CollectionUtils.isEmpty(results)) {
            return OperateResultEnum.SUCCESS;
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
