package io.mkr.helloip;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.mkr.helloip.sources.IpRangeInfos;
import net.ripe.commons.ip.Ipv4;

/**
 * Holds accumulated information for IPs.
 */
public class IpInfos {

    private final Ipv4 ipv4;

    private final Map<String, IpRangeInfos> infos = new HashMap<>();

    public IpInfos(Ipv4 ipv4) {
        this.ipv4 = ipv4;
    }

    public boolean hasAnyFrom(String providerName) {
        return infos.containsKey(providerName);
    }

    public Set<String> getProviderNames() {
        return infos.keySet();
    }

    public Collection<IpRangeInfos> getInfos() {
        return infos.values();
    }

    public IpRangeInfos infosFromProvider(String providerName) {
        return infos.get(providerName);
    }

    public String infoFromProvider(String providerName, String key) {
        return infos.get(providerName).getInfos().get(key);
    }

    public IpInfos add(String providerName, IpRangeInfos infos) {
        this.infos.put(providerName, infos);
        return this;
    }

    @Override
    public String toString() {
        return ipv4.toString() + "[" + infos + "]";
    }
}
