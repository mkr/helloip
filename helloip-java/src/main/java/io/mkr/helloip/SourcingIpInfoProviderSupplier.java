package io.mkr.helloip;

import com.google.common.base.Supplier;
import io.mkr.helloip.sources.IpRangeInfosSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Supplier which loads constructs a new {@link IpInfoProvider} on any call to {@link #get()} using the ranges
 * from a given {@link IpRangeInfosSource}.
 */
public class SourcingIpInfoProviderSupplier implements Supplier<IpInfoProvider> {

    private static final Logger logger = LoggerFactory.getLogger(SourcingIpInfoProviderSupplier.class);

    private final IpRangeInfosSource ipRangesSource;

    public SourcingIpInfoProviderSupplier(IpRangeInfosSource ipRangesSource) {
        this.ipRangesSource = ipRangesSource;
    }

    @Override
    public IpInfoProvider get() {
        try {
            return new RangeBasedIpInfoProvider(ipRangesSource.getName(), ipRangesSource.getIpRanges());
        } catch (IOException e) {
            logger.error("Error creating IP info provider", e);
            throw new RuntimeException(e);
        }
    }

}
