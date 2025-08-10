
package org.shoulder.core.concurrent;

import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 装饰器-阻塞队列 base
 * 队列内不能有多个 null 元素
 *
 * @author lym
 */
public abstract class BaseDecorateableBlockingQueue<E> implements BlockingQueue<E> {

    private final BlockingQueue<E> delegateBlockingQueue;

    public BaseDecorateableBlockingQueue(BlockingQueue<E> delegateBlockingQueue) {
        this.delegateBlockingQueue = delegateBlockingQueue;
    }

    public BlockingQueue<E> getQueue() {
        return delegateBlockingQueue;
    }


    protected void beforeInQueue(E e) {

    }

    protected E afterOutQueue(E e) {
        return e;
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        boolean added = delegateBlockingQueue.add(e);
        if (added) {
            beforeInQueue(e);
        }
        return added;
    }

    @Override
    public boolean add(E e) {
        boolean added = delegateBlockingQueue.add(e);
        if (added) {
            beforeInQueue(e);
        }
        return added;
    }

    @Override
    public boolean offer(E e) {
        boolean added = delegateBlockingQueue.offer(e);
        if (added) {
            beforeInQueue(e);
        }
        return added;
    }

    @Override
    public void put(E e) throws InterruptedException {
        beforeInQueue(e);
        delegateBlockingQueue.put(e);
    }

    @Override
    public E peek() {
        return delegateBlockingQueue.peek();
    }

    @Override
    public E poll() {
        return afterOutQueue(delegateBlockingQueue.poll());
    }

    @Override
    public E take() throws InterruptedException {
        return afterOutQueue(delegateBlockingQueue.take());
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return afterOutQueue(delegateBlockingQueue.poll(timeout, unit));
    }

    @Override
    public E remove() {
        return afterOutQueue(delegateBlockingQueue.remove());
    }

    @Override
    public E element() {
        return delegateBlockingQueue.element();
    }

    @Override
    public boolean remove(Object e) {
        if (delegateBlockingQueue.remove(e)) {
            afterOutQueue((E) e);
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(Object e) {
        return delegateBlockingQueue.contains(e);
    }

    @Override
    public boolean addAll(Collection c) {
        Optional.ofNullable(c).ifPresent(o -> beforeInQueue((E) o));
        return delegateBlockingQueue.addAll(c);
    }

    @Override
    public void clear() {
        drainTo(new ArrayList<>(size()));
    }

    @Override
    public boolean retainAll(Collection c) {
        // 删除指定集合中不存在的那些元素
        List<E> all = new ArrayList<>(size());
        // 跳过增强
        getQueue().drainTo(all);
        AtomicBoolean hasRemove = new AtomicBoolean(false);
        all.iterator().forEachRemaining(e -> {
            if (c.contains(e)) {
                // 跳过增强
                boolean put = getQueue().offer(e);
                if (!put) {
                    throw new RuntimeException("Unexcepted case!");
                }
            } else {
                // 触发增强
                afterOutQueue(e);
                hasRemove.compareAndSet(false, true);
            }
        });
        return hasRemove.get();
    }

    @Override
    public Iterator<E> iterator() {
        // 【默认不支持remove增强，故new Itr 包装，禁用 iterator.remove，避免误使用】
        return new DisableRemoveIterator<>(delegateBlockingQueue.iterator());
    }

    @Override
    public Object[] toArray() {
        // 【默认不支持toArray后续操作增强，但 array[]引用是java基础，使用频率低，也不容易出错，不做拦截】
        return delegateBlockingQueue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // 【默认不支持toArray后续操作增强，但 array[]引用是java基础，使用频率低，也不容易出错，不做拦截】
        return delegateBlockingQueue.toArray(a);
    }

    @Override
    public boolean removeAll(Collection c) {
        // 删除指定集合中元素
        boolean hasDelete = false;
        for (Object o : c) {
            boolean d = remove(o);
            if (d) {
                afterOutQueue((E) o);
            }
            hasDelete = hasDelete || d;
        }
        return hasDelete;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return delegateBlockingQueue.containsAll(c);
    }

    @Override
    public int remainingCapacity() {
        return delegateBlockingQueue.remainingCapacity();
    }

    @Override
    public int size() {
        return delegateBlockingQueue.size();
    }

    @Override
    public boolean isEmpty() {
        return delegateBlockingQueue.isEmpty();
    }

    @Override
    public int drainTo(Collection c) {
        // 非阻塞的批量获取元素，等于 pool 多个，性能更好点
        int i = delegateBlockingQueue.drainTo(c);
        for (Object o : c) {
            afterOutQueue((E) o);
        }
        return i;
    }

    @Override
    public int drainTo(Collection c, int maxElements) {
        int i = delegateBlockingQueue.drainTo(c, maxElements);
        for (Object o : c) {
            afterOutQueue((E) o);
        }
        return i;
    }

    public static class DisableRemoveIterator<X> implements Iterator<X> {

        private final Iterator<X> delegateIt;

        public DisableRemoveIterator(Iterator<X> delegateIt) {
            this.delegateIt = delegateIt;
        }

        @Override
        public boolean hasNext() {
            return delegateIt.hasNext();
        }

        @Override
        public synchronized X next() {
            return delegateIt.next();
        }

    }

}
