package org.shoulder.core.concurrent.enhance;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 被增强过的 runnable
 *
 * @author lym
 */
public class EnhancedCallable<V> implements Callable<V> {

    /**
     * 被装饰的原生 runnable
     */
    protected Callable<V> delegate;

    @Override
    public V call() throws Exception {
        return delegate.call();
    }

    /**
     * 血统（装饰器的类）
     */
    private List<Callable<V>> decorators = new LinkedList<>();

    public EnhancedCallable(Callable<V> delegate) {
        this.delegate = delegate;
        if (delegate instanceof EnhancedCallable) {
            EnhancedCallable<V> enhancedRunnable = (EnhancedCallable<V>) delegate;
            this.decorators.addAll(enhancedRunnable.getDecorators());
            this.decorators.add(enhancedRunnable);
        } else {
            this.decorators.add(delegate);
        }
    }

    /**
     * 是否是一个 xxx
     *
     * @param clazz 目标类
     * @return 结果
     */
    public boolean isInstanceOf(Class<?> clazz) {
        if (clazz.isAssignableFrom(getClass())) {
            return true;
        }
        for (Callable<V> decorator : decorators) {
            if (clazz.isAssignableFrom(decorator.getClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 所有包装过的装饰器
     *
     * @return List class
     */
    public List<Callable<V>> getDecorators() {
        return decorators;
    }

    /**
     * 最原生的 runnable 是什么类
     *
     * @return class
     */
    public Callable<V> getOrigin() {
        return decorators.get(0);
    }

    /**
     * 添加血脉
     */
    public void addDecorator(Callable<V> ancestry) {
        decorators.add(ancestry);
    }

    /**
     * 把本 runnable 当作
     * 寻找到最外层的包装器，返回
     *
     * @throws ClassCastException clazz 错误，未被目标类包装过
     */
    @SuppressWarnings("unchecked")
    public <T> T as(Class<T> clazz) throws ClassCastException {
        if (clazz.isAssignableFrom(getClass())) {
            return (T) this;
        }
        // 计算这个类被包装了多少次
        int warpCount = 0;
        for (int i = decorators.size() - 1; i >= 0; i--, warpCount++) {
            if (clazz.isAssignableFrom(decorators.get(i).getClass())) {
                break;
            }
        }
        if (warpCount > decorators.size()) {
            // 一定没有被目标类增强过
            throw new ClassCastException("can't cast to " + clazz.getName());
        }
        // 开始剥皮
        Callable<V> target = this;
        do {
            // 这里认为一定是 EnhancedCallable；若不是则抛出 classCastEx，其实也符合需求
            target = ((EnhancedCallable<V>) target).delegate;
            warpCount--;
        } while (warpCount >= 0);
        return (T) target;
    }

}
