package org.shoulder.core.concurrent;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class FastPriorityBlockingQueueTest {

    @Test
    public void testQueue() throws InterruptedException {
        int priorityCount = 3;
        FastPriorityBlockingQueue<TestRequest> queue = new FastPriorityBlockingQueue<>(LinkedBlockingQueue::new, TestRequest::getPriority,
            priorityCount);
        int threadNum = 50;
        // 100w
        int addPerThread = 1_000_000;
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
        countDownLatch.await();
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

        // 总是从第一优先级拿 fixme bug
        CountDownLatch takeCountDownLatch = new CountDownLatch(atomicIntegers[0].get());
        int takePerThread = atomicIntegers[0].get() / threadNum;
        int left = atomicIntegers[0].get() % threadNum;
        for (int i = 0; i < threadNum; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < takePerThread; j++) {
                        int thisPriority = ThreadLocalRandom.current().nextInt(priorityCount);
                        atomicIntegers[thisPriority].incrementAndGet();
                        if (ThreadLocalRandom.current().nextBoolean()) {
                            queue.take();
                        } else if (ThreadLocalRandom.current().nextBoolean()) {
                            queue.poll();
                        } else {
                            queue.remove();
                        }
                    }
                    countDownLatch.countDown();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        }
        takeCountDownLatch.await();
        Assertions.assertThat(queue.queuesArray[0].size()).isEqualTo(left);
        for (int i = 1; i < priorityCount; i++) {
            Assertions.assertThat(queue.queuesArray[i].size()).isEqualTo(atomicIntegers[i].get());
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
