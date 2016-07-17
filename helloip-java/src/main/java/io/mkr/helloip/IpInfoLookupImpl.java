package io.mkr.helloip;

import com.google.common.base.Supplier;
import net.ripe.commons.ip.Ipv4;

import java.util.List;

/**
 * An {@link IpInfoLookup} accumulating info using a list of supplied {@link IpInfoProvider}s.
 */
public class IpInfoLookupImpl implements IpInfoLookup {

    private final List<Supplier<IpInfoProvider>> providerSuppliers;

    public IpInfoLookupImpl(List<Supplier<IpInfoProvider>> providerSuppliers) {
        this.providerSuppliers = providerSuppliers;
    }

    @Override
    public IpInfos infosFor(Ipv4 ipv4) {
        IpInfos ipInfos = new IpInfos(ipv4);
        for (Supplier<IpInfoProvider> providerSupplier: this.providerSuppliers) {
            IpInfoProvider provider = providerSupplier.get();
            // provider can be null if not yet initialized
            if (provider != null) {
                provider.add(ipv4, ipInfos);
            }
        }
        return ipInfos;
    }
        
}
