package io.mkr.helloip;

import com.google.common.base.Supplier;
import io.mkr.helloip.base.AsyncRefreshingSupplier;
import io.mkr.helloip.base.PrefetchingMemoizingSupplier;
import io.mkr.helloip.sources.ApnicAsnIpRangesSource;
import io.mkr.helloip.sources.AwsIpRangesSource;
import io.mkr.helloip.sources.AzureIpRangesSource;
import io.mkr.helloip.sources.GoogleCloudIpRangesSource;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Suppliers {

    /**
     * A supplier of AWS IP range infos
     */
    public static final Supplier<IpInfoProvider> aws = new MultiAspectIpInfoProviderSupplier(new AwsIpRangesSource(), AwsIpRangesSource.KEY_SERVICE);

    /**
     * A supplier of MS Azure IP range infos
     */
    public static final Supplier<IpInfoProvider> azure = new SourcingIpInfoProviderSupplier(new AzureIpRangesSource());

    /**
     * A supplier of Google Cloud IP range infos
     */
    public static final Supplier<IpInfoProvider> googleCloud = new SourcingIpInfoProviderSupplier(new GoogleCloudIpRangesSource());

    /**
     * A supplier of APNIC IP range infos
     */
    public static final Supplier<IpInfoProvider> apnic = new SourcingIpInfoProviderSupplier(new ApnicAsnIpRangesSource());

    public static Supplier<IpInfoProvider> fetchOnce(Supplier<IpInfoProvider> supplier) {
        return new PrefetchingMemoizingSupplier<>(supplier);
    }

    public static Supplier<IpInfoProvider> fetchAsyncWithRefresh(Supplier<IpInfoProvider> supplier, long period, TimeUnit unit,
                                                                 ScheduledExecutorService scheduledExecutorService) {
        return new AsyncRefreshingSupplier<>(supplier, period, unit, scheduledExecutorService);
    }
}
