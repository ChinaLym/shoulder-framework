package org.shoulder.core.concurrent.enhance;

/**
 * 被增强过的 runnable，可以保存所有的增强器
 *
 * @author lym
 */
public class OriginEnhancedRunnable extends EnhancedRunnable {

    /**
     * 被装饰的原生 runnable，todo 约束不出意外，一定是 EnhancedRunnable
     */
    private Runnable origin;

    @Override
    public void run() {
        origin.run();
    }

    public OriginEnhancedRunnable(Runnable delegate) {
        this.origin = delegate;
    }

    /**
     * 是否是一个 xxx
     *
     * @param clazz 目标类
     * @return 结果
     */
    @Override
    public boolean isInstanceOf(Class<?> clazz) {
        return clazz.isAssignableFrom(origin.getClass()) || clazz.isAssignableFrom(getClass());
    }

    /**
     * 最原生的 runnable 是什么类
     *
     * @return class
     */
    @Override
    public Runnable getOrigin() {
        return origin;
    }

    /*@Override
    public void addDecorator(Runnable ancestry) {
        throw new IllegalCallerException("origin");
    }*/

    /**
     * 把本 runnable 当作
     * 寻找到最外层的包装器，返回
     *
     * @throws ClassCastException clazz 错误，未被目标类包装过
     */
    public <T> T as(Class<T> clazz) {
        if (clazz.isAssignableFrom(origin.getClass()) || clazz.isAssignableFrom(getClass())) {
            return (T) this;
        } else {
            throw new ClassCastException("can't cast to " + clazz.getName());
        }
    }

}
