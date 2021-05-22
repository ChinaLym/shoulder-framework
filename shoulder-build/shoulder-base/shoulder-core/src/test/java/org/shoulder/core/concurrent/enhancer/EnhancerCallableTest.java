package org.shoulder.core.concurrent.enhancer;

import org.junit.jupiter.api.Test;
import org.shoulder.core.concurrent.enhance.EnhancedCallable;

import java.util.concurrent.Callable;

public class EnhancerCallableTest {

    @Test
    public void testCallableEnhancer() {
        testEnhancer(new OriginCallable<>());
    }

    private <T> void testEnhancer(Callable<T> origin) {
        Class<? extends Callable> originClass = origin.getClass();

        MyCallableEnhancer1<T> myCallableEnhancer1 = new MyCallableEnhancer1<>(origin);
        MyCallableEnhancer2<T> myCallableEnhancer2 = new MyCallableEnhancer2<>(myCallableEnhancer1);
        MyCallableEnhancer3<T> myCallableEnhancer3 = new MyCallableEnhancer3<>(myCallableEnhancer2);

        // decorators.size test

        assert myCallableEnhancer1.getDecorators().size() == 1;
        assert myCallableEnhancer2.getDecorators().size() == 2;
        assert myCallableEnhancer3.getDecorators().size() == 3;

        // isInstanceOf test

        assert myCallableEnhancer1.isInstanceOf(originClass);
        assert myCallableEnhancer1.isInstanceOf(MyCallableEnhancer1.class);

        assert myCallableEnhancer2.isInstanceOf(originClass);
        assert myCallableEnhancer2.isInstanceOf(MyCallableEnhancer1.class);
        assert myCallableEnhancer2.isInstanceOf(MyCallableEnhancer2.class);

        assert myCallableEnhancer3.isInstanceOf(originClass);
        assert myCallableEnhancer3.isInstanceOf(MyCallableEnhancer1.class);
        assert myCallableEnhancer3.isInstanceOf(MyCallableEnhancer2.class);
        assert myCallableEnhancer3.isInstanceOf(MyCallableEnhancer3.class);

        // as test

        assert origin == myCallableEnhancer1.as(originClass);
        assert origin == myCallableEnhancer2.as(originClass);
        assert origin == myCallableEnhancer3.as(originClass);

        assert myCallableEnhancer1 == myCallableEnhancer1.as(MyCallableEnhancer1.class);
        assert myCallableEnhancer1 == myCallableEnhancer2.as(MyCallableEnhancer1.class);
        assert myCallableEnhancer1 == myCallableEnhancer3.as(MyCallableEnhancer1.class);

        assert myCallableEnhancer2 == myCallableEnhancer2.as(MyCallableEnhancer2.class);
        assert myCallableEnhancer2 == myCallableEnhancer3.as(MyCallableEnhancer2.class);

        assert myCallableEnhancer3 == myCallableEnhancer3.as(MyCallableEnhancer3.class);
    }


    static class OriginCallable<V> implements Callable<V> {

        @Override
        public V call() {
            System.out.println("OriginCallable<V> calling.........");
            return null;
        }
    }

    static class MyCallableEnhancer1<V> extends EnhancedCallable<V> {

        MyCallableEnhancer1(Callable<V> delegate) {
            super(delegate);
        }

        @Override
        public V call() throws Exception {
            System.out.println("MyCallableEnhancer1 before");
            return delegate.call();
        }
    }


    static class MyCallableEnhancer2<V> extends EnhancedCallable<V> {

        MyCallableEnhancer2(Callable<V> delegate) {
            super(delegate);
        }

        @Override
        public V call() throws Exception {
            System.out.println("MyCallableEnhancer2 before");
            return delegate.call();
        }
    }

    static class MyCallableEnhancer3<V> extends EnhancedCallable<V> {

        MyCallableEnhancer3(Callable<V> delegate) {
            super(delegate);
        }

        @Override
        public V call() throws Exception {
            System.out.println("MyCallableEnhancer3 before");
            return delegate.call();
        }
    }

}
