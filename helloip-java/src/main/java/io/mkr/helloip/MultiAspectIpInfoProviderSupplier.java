package io.mkr.helloip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.mkr.helloip.sources.IpRangeInfos;
import io.mkr.helloip.sources.IpRangeInfosSource;
import net.ripe.commons.ip.Ipv4;

/**
 *
 */
public class MultiAspectIpInfoProviderSupplier implements Supplier<IpInfoProvider> {

    private final IpRangeInfosSource ipRangesSource;
    private final String aspectField;

    public MultiAspectIpInfoProviderSupplier(IpRangeInfosSource ipRangesSource, String aspectField) {
        this.ipRangesSource = ipRangesSource;
        this.aspectField = aspectField;
    }

    @Override
    public IpInfoProvider get() {
        try {
            List<IpRangeInfos> ipRangeInfos = ipRangesSource.getIpRanges();
            Multimap<String, IpRangeInfos> m = Multimaps.index(ipRangeInfos, new Function<IpRangeInfos, String>() {
                @Override
                public String apply(IpRangeInfos ipRangeInfo) {
                    return ipRangeInfo.getInfos().get(aspectField);
                }
            });
            List<IpInfoProvider> providers = new ArrayList<>();
            for (Map.Entry<String, Collection<IpRangeInfos>> aspectWithIpRangeInfos: m.asMap().entrySet()) {
                IpInfoProvider provider = new RangeBasedIpInfoProvider(ipRangesSource.getName() + ":" + aspectWithIpRangeInfos.getKey(),
                    new ArrayList<>(aspectWithIpRangeInfos.getValue()));
                providers.add(provider);
            }
            return new AggregatingIpInfoProvider(providers);

        } catch (IOException e) {
            // TODO!
            return null;
        }
    }

    private static class AggregatingIpInfoProvider implements IpInfoProvider {

        private final List<IpInfoProvider> delegates;

        public AggregatingIpInfoProvider(List<IpInfoProvider> delegates) {
            this.delegates = delegates;
        }

        @Override
        public void add(Ipv4 ipv4, IpInfos infos) {
            for (IpInfoProvider delegate: delegates) {
                delegate.add(ipv4, infos);
            }
        }
    }

}
