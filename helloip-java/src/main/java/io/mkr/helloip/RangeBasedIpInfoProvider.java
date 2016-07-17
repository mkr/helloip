package io.mkr.helloip;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import io.mkr.helloip.sources.IpRangeInfos;
import net.ripe.commons.ip.Ipv4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangeBasedIpInfoProvider implements IpInfoProvider {

    private static final Logger log = LoggerFactory.getLogger(RangeBasedIpInfoProvider.class);

    private final String name;
    private final RangeMap<Long, IpRangeInfos> ipInfos;

    public RangeBasedIpInfoProvider(String name, List<IpRangeInfos> ipRangeInfos) {
        this.name = name;
        RangeMap<Long, IpRangeInfos> rangeMap = TreeRangeMap.create();
        for (IpRangeInfos ipRange: ipRangeInfos) {
            Range<Long> range = Range.closed(
                ipRange.getRange().start().asBigInteger().longValue(),
                ipRange.getRange().end().asBigInteger().longValue()
            );
            // get a possibly existing previous entry for that range (some sources have multiple entries for similar ranges)
            Map.Entry<Range<Long>, IpRangeInfos> existing = rangeMap.getEntry(range.lowerEndpoint());
            if (existing != null) {
                Range<Long> existingRange = existing.getKey();
                if (existingRange.encloses(range)) {
                    log.debug("Existing range enclosing new one, skipping (new: {}, existing: {})", range, existingRange);
                } else if (range.encloses(existingRange)) {
                    log.debug("New range enclosing existing one, replacing existing (new: {}, existing: {})", range, existingRange);
                    rangeMap.remove(existingRange);
                    rangeMap.put(range, ipRange);
                } else {
                    log.warn("Range overlap between existing and new range, skipping (new: {}, existing: {})", range, existingRange);
                }
            } else {
                rangeMap.put(range, ipRange);
            }
        }
        this.ipInfos = rangeMap;
    }

    @Override
    public void add(Ipv4 ipv4, IpInfos infos) {
        IpRangeInfos rangeInfo = ipInfos.get(ipv4.asBigInteger().longValue());
        if (rangeInfo != null) {
            infos.add(name, rangeInfo);
        }
    }

}
