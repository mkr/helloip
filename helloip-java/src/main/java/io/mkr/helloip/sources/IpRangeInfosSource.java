package io.mkr.helloip.sources;

import java.io.IOException;
import java.util.List;

/**
 * A source of information about ip ranges.
 */
public interface IpRangeInfosSource {

    /**
     * Returns the name of the source
     *
     * @return the name of the source
     */
    String getName();

    /**
     * Returns all {@link IpRangeInfos} about all IP ranges this source knows of
     *
     * @return all {@link IpRangeInfos} about all IP ranges this source knows of
     * @throws IOException
     */
    List<IpRangeInfos> getIpRanges() throws IOException;

}
