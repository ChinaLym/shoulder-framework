package org.shoulder.core.concurrent.enhancer;

import org.junit.Test;
import org.shoulder.core.concurrent.enhance.EnhancedRunnable;

public class EnhancerRunnableTest {

    @Test
    public void testRunnableEnhancer() {
        testEnhancer(new OriginRunnable());
    }

    @Test
    public void testThreadEnhancer() {
        testEnhancer(new OriginThread());
    }

    private void testEnhancer(Runnable origin) {
        Class<? extends Runnable> originClass = origin.getClass();

        MyRunnableEnhancer1 myRunnableEnhancer1 = new MyRunnableEnhancer1(origin);
        MyRunnableEnhancer2 myRunnableEnhancer2 = new MyRunnableEnhancer2(myRunnableEnhancer1);
        MyRunnableEnhancer3 myRunnableEnhancer3 = new MyRunnableEnhancer3(myRunnableEnhancer2);

        // decorators.size test

        assert myRunnableEnhancer1.getDecorators().size() == 1;
        assert myRunnableEnhancer2.getDecorators().size() == 2;
        assert myRunnableEnhancer3.getDecorators().size() == 3;

        // isInstanceOf test

        assert myRunnableEnhancer1.isInstanceOf(originClass);
        assert myRunnableEnhancer1.isInstanceOf(MyRunnableEnhancer1.class);

        assert myRunnableEnhancer2.isInstanceOf(originClass);
        assert myRunnableEnhancer2.isInstanceOf(MyRunnableEnhancer1.class);
        assert myRunnableEnhancer2.isInstanceOf(MyRunnableEnhancer2.class);

        assert myRunnableEnhancer3.isInstanceOf(originClass);
        assert myRunnableEnhancer3.isInstanceOf(MyRunnableEnhancer1.class);
        assert myRunnableEnhancer3.isInstanceOf(MyRunnableEnhancer2.class);
        assert myRunnableEnhancer3.isInstanceOf(MyRunnableEnhancer3.class);

        // as test

        assert origin == myRunnableEnhancer1.as(originClass);
        assert origin == myRunnableEnhancer2.as(originClass);
        assert origin == myRunnableEnhancer3.as(originClass);

        assert myRunnableEnhancer1 == myRunnableEnhancer1.as(MyRunnableEnhancer1.class);
        assert myRunnableEnhancer1 == myRunnableEnhancer2.as(MyRunnableEnhancer1.class);
        assert myRunnableEnhancer1 == myRunnableEnhancer3.as(MyRunnableEnhancer1.class);

        assert myRunnableEnhancer2 == myRunnableEnhancer2.as(MyRunnableEnhancer2.class);
        assert myRunnableEnhancer2 == myRunnableEnhancer3.as(MyRunnableEnhancer2.class);

        assert myRunnableEnhancer3 == myRunnableEnhancer3.as(MyRunnableEnhancer3.class);
    }


    static class OriginRunnable implements Runnable {

        @Override
        public void run() {
            System.out.println("OriginRunnable running.........");
        }
    }

    static class OriginThread extends Thread {

        @Override
        public void run() {
            System.out.println("OriginThread running.........");
        }
    }

    static class MyRunnableEnhancer1 extends EnhancedRunnable {

        MyRunnableEnhancer1(Runnable delegate) {
            super(delegate);
        }

        @Override
        public void run() {
            System.out.println("MyRunnableEnhancer1 before");
            delegate.run();
            System.out.println("MyRunnableEnhancer1 after");
        }
    }


    static class MyRunnableEnhancer2 extends EnhancedRunnable {

        MyRunnableEnhancer2(Runnable delegate) {
            super(delegate);
        }

        @Override
        public void run() {
            System.out.println("MyRunnableEnhancer2 before");
            delegate.run();
            System.out.println("MyRunnableEnhancer2 after");
        }
    }

    static class MyRunnableEnhancer3 extends EnhancedRunnable {

        MyRunnableEnhancer3(Runnable delegate) {
            super(delegate);
        }

        @Override
        public void run() {
            System.out.println("MyRunnableEnhancer3 before");
            delegate.run();
            System.out.println("MyRunnableEnhancer3 after");
        }
    }

}
