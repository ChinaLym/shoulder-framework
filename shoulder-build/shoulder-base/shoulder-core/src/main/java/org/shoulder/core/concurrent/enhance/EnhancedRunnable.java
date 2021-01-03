package org.shoulder.core.concurrent.enhance;

import java.util.LinkedList;
import java.util.List;

/**
 * 被增强过的 runnable，可以保存所有的增强器
 *
 * @author lym
 */
public class EnhancedRunnable implements Runnable {

    /**
     * 被装饰的原生 runnable，todo 约束不出意外，一定是 EnhancedRunnable
     */
    protected Runnable delegate;

    @Override
    public void run() {
        delegate.run();
    }

    /**
     * 血统（装饰器的类）
     */
    private List<Runnable> decorators = new LinkedList<>();


    EnhancedRunnable() {

    }

    public EnhancedRunnable(Runnable delegate) {
        this.delegate = delegate;
        if (delegate instanceof EnhancedRunnable) {
            EnhancedRunnable enhancedRunnable = (EnhancedRunnable) delegate;
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
        for (Runnable decorator : decorators) {
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
    public List<Runnable> getDecorators() {
        return decorators;
    }

    /**
     * 最原生的 runnable 是什么类
     *
     * @return class
     */
    public Runnable getOrigin() {
        return decorators.get(0);
    }

    /**
     * 添加血脉
     */
    public void addDecorator(Runnable ancestry) {
        decorators.add(ancestry);
    }

    /**
     * 把本 runnable 当作
     * 寻找到最外层的包装器，返回
     *
     * @throws ClassCastException clazz 错误，未被目标类包装过
     */
    public <T> T as(Class<T> clazz) {
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
        Runnable target = this;
        do {
            // todo 这里认为一定是 EnhancedRunnable；
            target = ((EnhancedRunnable) target).delegate;
            warpCount--;
        } while (warpCount >= 0);
        return (T) target;
    }

}
