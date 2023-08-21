package org.shoulder.core.concurrent;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发测试
 */
public class FastPriorityBlockingQueueTest {

    @Test
    public void testQueue() throws InterruptedException {
        testQueue(true);
        testQueue(false);
    }

    public void testQueue(boolean alwaysAcquireHighPriorityFirst) throws InterruptedException {
        int priorityCount = 3;
        FastPriorityBlockingQueue<TestRequest> queue = new FastPriorityBlockingQueue<>(LinkedBlockingQueue::new,
                TestRequest::getPriority, priorityCount, alwaysAcquireHighPriorityFirst);
        int threadNum = 50;
        // 10w
        int addPerThread = 10_0000;


        AtomicInteger[] atomicIntegers = new AtomicInteger[priorityCount];
        for (int i = 0; i < priorityCount; i++) {
            atomicIntegers[i] = new AtomicInteger(0);
        }

        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < addPerThread; j++) {
                        int thisPriority = ThreadLocalRandom.current().nextInt(priorityCount);
                        atomicIntegers[thisPriority].incrementAndGet();
                        if (ThreadLocalRandom.current().nextBoolean()) {
                            queue.add(new TestRequest(thisPriority));
                        } else if (ThreadLocalRandom.current().nextBoolean()) {
                            queue.offer(new TestRequest(thisPriority));
                        } else {
                            queue.put(new TestRequest(thisPriority));
                        }
                    }
                    countDownLatch.countDown();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        }
        countDownLatch.await(30, TimeUnit.SECONDS);
        // ============


        // 总数、各等级总数
        Assertions.assertThat(queue.size()).isEqualTo(threadNum * addPerThread);
        for (int i = 0; i < priorityCount; i++) {
            Assertions.assertThat(queue.queuesArray[i].size()).isEqualTo(atomicIntegers[i].get());
        }

        // peek / element 不会取出
        Assertions.assertThat(queue.peek() == queue.peek()).isEqualTo(true);
        Assertions.assertThat(queue.element() == queue.element()).isEqualTo(true);

        // 迭代器总数
        int count = 0;
        for (TestRequest testRequest : queue) {
            count++;
        }
        Assertions.assertThat(count).isEqualTo(threadNum * addPerThread);

        // toArray正确
        Assertions.assertThat(queue.toArray().length).isEqualTo(threadNum * addPerThread);

        // 总是从第一优先级拿
        int takePerThread = atomicIntegers[0].get() / threadNum;
        int left = atomicIntegers[0].get() % threadNum;
        CountDownLatch takeCountDownLatch = new CountDownLatch(threadNum);
        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < takePerThread; j++) {
                        if (ThreadLocalRandom.current().nextBoolean()) {
                            queue.take();
                        } else if (ThreadLocalRandom.current().nextBoolean()) {
                            queue.poll();
                        } else {
                            queue.remove();
                        }
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    takeCountDownLatch.countDown();
                }
            }).start();

        }
        takeCountDownLatch.await(30, TimeUnit.SECONDS);

        // 严格按照先处理高优先，再处理低优先的，高优先处理不完，低优先永远得不到处理
        if (alwaysAcquireHighPriorityFirst) {
            Assertions.assertThat(queue.queuesArray[0].size()).isEqualTo(left);
            for (int i = 1; i < priorityCount; i++) {
                Assertions.assertThat(queue.queuesArray[i].size())
                        .describedAs("index=" + i)
                        .isEqualTo(atomicIntegers[i].get());
            }
        } else {
            // 整体会先处理高优先，但实际低优先也不至于不会处理
            int totalLeft = Arrays.stream(atomicIntegers).map(AtomicInteger::get).reduce(left, Integer::sum) - atomicIntegers[0].get();
            int actualLeft = Arrays.stream(queue.queuesArray).map(Collection::size).reduce(0, Integer::sum);
            Assertions.assertThat(actualLeft)
                    .describedAs("abstract fair check")
                    .isEqualTo(totalLeft);
        }

        // 清空
        queue.clear();
        Assertions.assertThat(queue.size()).isEqualTo(0);
    }

    public static class TestRequest {
        // 0：生产流量
        // 1: 性能回归流量（压测）
        // 2: 项目测试流量
        int priority;

        public TestRequest(int priority) {
            this.priority = priority;
        }

        /**
         * Getter method for property <tt>priority</tt>.
         *
         * @return property value of priority
         */
        public int getPriority() {
            return priority;
        }
    }

}
