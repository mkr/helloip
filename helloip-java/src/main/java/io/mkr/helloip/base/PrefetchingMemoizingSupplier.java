package io.mkr.helloip.base;

import com.google.common.base.Supplier;

/**
 * A dead simple {@link Supplier} creating the value it supplies eagerly in the current thread by calling another {@link Supplier}.
 */
public class PrefetchingMemoizingSupplier<T> implements Supplier<T> {

    private final T value;

    public PrefetchingMemoizingSupplier(Supplier<T> delegate) {
        value = delegate.get();
    }

    @Override
    public T get() {
        return value;
    }
}
