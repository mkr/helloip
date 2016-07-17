package io.mkr.helloip;

import net.ripe.commons.ip.Ipv4;

/**
 * The {@link IpInfoLookup} accumulates all {@link IpInfos} about an {@link Ipv4}
 */
public interface IpInfoLookup {

    /**
     * Returns accumulated information about an IPv4
     *
     * @param ipv4 the IP address
     * @return accumulated information about an IPv4
     */
    IpInfos infosFor(Ipv4 ipv4);
}
