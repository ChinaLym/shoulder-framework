package org.shoulder.core.concurrent.enhance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * 被增强过的 runnable，可以保存所有的增强器
 *
 * @author lym
 */
public class EnhancedRunnable implements Runnable {

    /**
     * 被装饰的原生 runnable
     */
    protected Runnable delegate;

    @Override
    public void run() {
        delegate.run();
    }

    EnhancedRunnable() {

    }

    public EnhancedRunnable(Runnable delegate) {
        this.delegate = delegate;
    }

    /**
     * 是否是一个 xxx
     *
     * @param clazz 目标类
     * @return 结果
     */
    public boolean isInstanceOf(Class<?> clazz) {
        if (clazz.isInstance(this)) {
            return true;
        }
        Runnable result = this;
        while (result instanceof EnhancedRunnable) {
            result = ((EnhancedRunnable) result).delegate;
            if (clazz.isInstance(result)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 所有包装过的装饰器
     * origin 不再内
     *
     * @return List class
     */
    public List<Runnable> getDecorators() {
        List<Runnable> runnableList = new LinkedList<>();
        Runnable result = this;
        while (result instanceof EnhancedRunnable) {
            runnableList.add(result);
            result = ((EnhancedRunnable) result).delegate;
        }
        return runnableList;
    }

    /**
     * 最原生的 runnable 是什么类
     *
     * @return class
     */
    public Runnable getOrigin() {
        Runnable result = this;
        while (result instanceof EnhancedRunnable) {
            result = ((EnhancedRunnable) result).delegate;
        }
        return result;
    }

    /**
     * 把本 runnable 当作 clazz
     *
     * @throws ClassCastException clazz 错误，未被目标类包装过
     */
    @Nonnull
    public <T> T as(Class<T> clazz) {
        T result = unwrap(clazz);
        if (result != null) {
            return result;
        }
        throw new ClassCastException("can't cast to " + clazz.getName());
    }

    /**
     * 寻找到最外层的 clazz 包装器，返回
     * <p>
     * 与 lettuce.core 类似
     *
     * @return 找不到则返回 null
     * @see io.lettuce.core.protocol.CommandWrapper#unwrap
     */
    @Nullable
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isInstance(this)) {
            return clazz.cast(this);
        }
        Runnable result = this;
        while (result instanceof EnhancedRunnable) {
            result = ((EnhancedRunnable) result).delegate;
            if (clazz.isInstance(result)) {
                return clazz.cast(result);
            }
        }
        return null;
    }

}
