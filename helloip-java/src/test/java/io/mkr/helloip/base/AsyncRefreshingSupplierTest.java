package io.mkr.helloip.base;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Supplier;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AsyncRefreshingSupplierTest {

    @Test
    public void testRefreshWorks() throws Exception {
        int initialDelayMillis = 1000;
        Supplier<Integer> sup = new SequenceSupplier<>(new Integer[]{1, 2, 3, 4}, initialDelayMillis, 0);

        long start = System.nanoTime();
        Supplier<Integer> async = new AsyncRefreshingSupplier<>(sup, 3L, TimeUnit.SECONDS,
                new ScheduledThreadPoolExecutor(1));
        Integer value = async.get();

        // get() returns immediately, i.e. supplier's initial delay sleep does happen async and does not block current thread
        long durationMillis = (System.nanoTime() - start) / 1_000_000L;
        assertTrue(durationMillis < initialDelayMillis);
        assertThat(value, is(nullValue()));

        Thread.sleep(1500);
        assertThat(async.get(), is(1));

        Thread.sleep(3000);
        assertThat(async.get(), is(2));

        Thread.sleep(3000);
        assertThat(async.get(), is(3));

        Thread.sleep(3000);
        assertThat(async.get(), is(4));

        Thread.sleep(3000);
        assertThat(async.get(), is(1));
    }

    @Test
    public void testKeepsRefreshingAfterException() throws Exception {
        Supplier<Integer> sup = new SequenceSupplier<>(new Integer[]{1, 2, 3, 4}, 0, 2);
        Supplier<Integer> async = new AsyncRefreshingSupplier<>(sup, 1L, TimeUnit.SECONDS,
                new ScheduledThreadPoolExecutor(1));

        // initial value at 0 is null because SequenceSupplier throws Exception
        assertThat(async.get(), is(nullValue()));

        // after 1s it should have refreshed
        Thread.sleep(1000);
        assertThat(async.get(), is(2));

        // after another 1s it should have swallowed another Exception an return the old value
        Thread.sleep(1000);
        assertThat(async.get(), is(2));

        Thread.sleep(1000);
        assertThat(async.get(), is(4));
    }

    private static class SequenceSupplier<T> implements Supplier<T> {

        private T[] values;
        private int idx = 0;
        private int preDelay = 0;
        private int throwRate = 0;

        SequenceSupplier(T[] values, int preDelay, int throwRate) {
            this.values = values;
            this.preDelay = preDelay;
            this.throwRate = throwRate;
        }

        @Override
        public T get() {
            if (preDelay != 0)
            try {
                Thread.sleep(preDelay);
            } catch (InterruptedException e) {
            }
            if (throwRate != 0) {
                if (idx % throwRate == 0) {
                    idx++;
                    throw new RuntimeException("Failed");
                }
            }
            return values[idx++ % values.length];
        }
    }

}
