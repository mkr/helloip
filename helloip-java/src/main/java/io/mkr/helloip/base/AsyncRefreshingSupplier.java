package io.mkr.helloip.base;

import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Supplier} which asynchronously refreshes the value it supplies. The value is immediately scheduled to be
 * pre-fetched asynchronously on instantiation of the supplier. {@link #get()} will return the last successfully
 * retrieved value or {@code null} if no value has been retrieved yet.
 *
 * @param <T> the type of value to supply
 */
public class AsyncRefreshingSupplier<T> implements Supplier<T> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncRefreshingSupplier.class);

    private volatile T value = null;

    public AsyncRefreshingSupplier(final Supplier<T> delegate, long period, TimeUnit unit, ScheduledExecutorService scheduledExecutorService) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    value = delegate.get();
                } catch (Exception e) {
                    logger.error("Error getting value", e);
                }

            }
        };
        scheduledExecutorService.scheduleAtFixedRate(runnable, 0, period, unit);
    }

    public T get() {
        return value;
    }


}
