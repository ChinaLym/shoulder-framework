package org.shoulder.validate.util;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.StringUtils;

import java.util.Collection;
import java.util.Date;

/**
 * 接口参数校验类
 * 附带常用的校验方法，若不满足条件，则抛出参数非法异常，通常暗示返回 400
 *
 * 集合大小2，日期，枚举、字符串长度、数组范围3、正则
 * 
 * @author lym
 */
public class ParamCheck {

    /**
     * 非 null
     *
     * @param param 被校验的参数
     * @param paramName 参数名，如 userId，也可以为可翻译的
     */
    public static void notNull(Object param, Object paramName) {
        if (param == null) {
            CommonErrorCodeEnum.PARAM_BLANK.throwRuntime(paramName);
        }
    }


    /**
     * 如果参数为空，则抛带错误码的异常，此方法不支持多语言
     *
     * @param coll   被校验的参数
     * @param paramName 参数名，如 userId，也可以为可翻译的
     */
    public static <T> void notEmpty(Collection<T> coll, Object paramName) {
        if (CollectionUtils.isEmpty(coll)) {
            CommonErrorCodeEnum.PARAM_BLANK.throwRuntime(paramName);
        }
    }


    /**
	 * 非空串
	 *
	 * @param param 被校验的参数
     * @param paramName 参数名，如 userId，也可以为可翻译的
	 */
	public static void notBlank(String param, Object paramName) {
		if (StringUtils.isBlank(param)) {
            CommonErrorCodeEnum.PARAM_BLANK.throwRuntime(paramName);
		}
	}

    /**
     * 不为空
     *
     * @param param 被校验的参数
     * @param paramName 参数名，如 userId，也可以为可翻译的
     */
    public static void notEmpty(String param, Object paramName) {
        if (StringUtils.isEmpty(param)) {
            CommonErrorCodeEnum.PARAM_BLANK.throwRuntime(paramName);
        }
    }

    /**
     * 正数检查
     *
     * @param param 被校验的参数
     * @param paramName 参数名，如 userId，也可以为可翻译的
     */
    public static void assertPositive(Integer param, Object paramName) {
        if(param != null && param <= 0){
            CommonErrorCodeEnum.PARAM_OUT_RANGE.throwRuntime(paramName);
        }
    }

    /**
     * 闭区间检查
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable> void greater(T param, T min, Object paramName) {
        if(param != null && min.compareTo(param) > 0){
            CommonErrorCodeEnum.PARAM_OUT_RANGE.throwRuntime(paramName);
        }
    }


    public static <T extends Comparable> void assertRange(T param, T min, T max, Object paramName) {
        if(param != null && (min.compareTo(param) > 0 || max.compareTo(param) < 0)){
            CommonErrorCodeEnum.PARAM_OUT_RANGE.throwRuntime(paramName);
        }
    }

    public static void assertPositive(Long param, Object paramName) {
        if(param != null && param <= 0){
            CommonErrorCodeEnum.PARAM_OUT_RANGE.throwRuntime(paramName);
        }
    }


	/**
	 * 检查分页查询参数是否符合要求
	 *
	 * @param pageSize 分页大小
	 * @param pageNo   页码
	 */
	public static void pageParam(Integer pageSize, Integer pageNo) {
        notNull(pageSize, "pageSize");
        notNull(pageNo, "pageNo");
        assertRange(pageSize, 1, 1000, "pageSize");
        assertPositive(pageNo, "pageNo");
    }


	/**
	 * 校验数据个数是否超过限制
	 *
	 * @param size   数据个数
	 * @param limit  最大限制
     * @param paramName 参数名，如 userId，也可以为可翻译的
	 */
	public static void assert(int size, int limit, Object paramName) {
		if (size > 0 && size > limit) {
		    
		}
	}


    /**
     * 校验集合元素是否超过限制个数
     * @param paramName 参数名，如 userId，也可以为可翻译的
     */
    public static <T> void sizeLimit(Collection<T> coll, Integer num, Object paramName) {
        if (coll != null && coll.size() > num) {

        }
    }

	/**
	 * 校验参数param在集合limits指定的元素范围中
	 *
	 * @param param  待校验的参数
	 * @param allowData 范围
     * @param paramName 参数名，如 userId，也可以为可翻译的
	 */
	public static <T> void assertIn(T param, Collection<? extends T> allowData, Object paramName) {
		if (allowData != null && !allowData.contains(param)) {
		    
		}
	}

	/**
	 * 校验updateTime是否晚于或等于createTime
	 *
	 * @param createTime 创建时间
	 * @param updateTime 更新时间
     * @param paramName 参数名，如 userId，也可以为可翻译的
	 */
	public static void dateLater(Date createTime, Date updateTime, Object paramName) {
		if (createTime != null && updateTime != null && updateTime.before(createTime)) {
			
		}
	}

}
