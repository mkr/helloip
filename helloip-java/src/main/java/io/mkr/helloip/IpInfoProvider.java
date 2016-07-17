package io.mkr.helloip;

import net.ripe.commons.ip.Ipv4;

/**
 * An {@link IpInfoProvider} can add information about an IP to {@link IpInfos}.
 */
public interface IpInfoProvider {

    /**
     * Adds further information about an {@link Ipv4} to an {@link IpInfos} accumulator
     * @param ipv4 the IP
     * @param infos accumulator for infos
     */
    void add(Ipv4 ipv4, IpInfos infos);

}
