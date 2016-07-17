package io.mkr.helloip.sources;

import java.util.Map;

import net.ripe.commons.ip.Ipv4Range;

/**
 * Contains infos about an IPv4 range. Infos can be arbitrary key value pairs represented as a Java Map.
 */
public class IpRangeInfos {

    private final Ipv4Range range;
    private final Map<String, String> infos;

    public IpRangeInfos(Ipv4Range range, Map<String, String> info) {
        this.range = range;
        this.infos = info;
    }

    public Ipv4Range getRange() {
        return range;
    }

    public Map<String, String> getInfos() {
        return infos;
    }

    @Override
    public String toString() {
        return range.toString() + "[" + infos + "]";
    }
}
