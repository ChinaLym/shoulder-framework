package org.shoulder.core.concurrent;

import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.util.ArrayUtils;
import org.springframework.core.GenericTypeResolver;

import java.util.Optional;

/**
 * 双 buffer 缓冲，无锁化技术
 * 该类封装内部两个 cacheObject 构成的 buffer 数组的操作，无需关注 index
 * <p>
 * 双buffer技术，其实就是准备两个Obj，一个用来读，一个用来写。
 * 写完成之后，原子交换两个Obj；
 * 之后的读操作，都放在交换后的读对象上，而原来的读对象，在原有的“读操作”完成之后，又可以进行写操作了。
 * </p>
 *
 * @author lym
 */
public class DoubleBufferCache<T> {

    /**
     * 所有的缓存的更新操作均由 Semaphore 控制，线程安全
     */
    @Getter
    @Setter
    private volatile int index = 0;

    private T[] buffer = (T[]) ArrayUtils.newInstance(Optional.ofNullable(GenericTypeResolver.resolveTypeArguments(getClass(), DoubleBufferCache.class)).orElseThrow()[1], 2);

    /**
     * @return 当前 object
     */
    public T getCurrent() {
        return buffer[index];
    }

    /**
     * @return 另一个 object
     */
    public T getNext() {
        return buffer[(index + 1) % 2];
    }

    /**
     * @return 获得并切换到另一个 object
     */
    public T switchNextAndGet() {
        index = (index + 1) % 2;
        return getCurrent();
    }

    /**
     * 设置另一个 object
     */
    public void setNext(T object) {
        buffer[(index + 1) % 2] = object;
    }

    /**
     * 设置并切换另一个 object
     */
    public void setAndSwitchNext(T object) {
        index = (index + 1) % 2;
        buffer[index] = object;
    }

}
