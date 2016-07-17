package io.mkr.helloip.examples;

import com.google.common.base.Supplier;
import io.mkr.helloip.IpInfoLookup;
import io.mkr.helloip.IpInfoLookupImpl;
import io.mkr.helloip.IpInfoProvider;
import io.mkr.helloip.IpInfos;
import io.mkr.helloip.sources.ApnicAsnIpRangesSource;
import net.ripe.commons.ip.Ipv4;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static io.mkr.helloip.Suppliers.*;

/**
 * Loads an Apache access log file (first argument) and prints some stats about the client IPs.
 */
public class ApacheLogIpStats {

    public static void main(String[] args) throws Exception {
        InputStream is = new FileInputStream(args[0]);

        // initialize lookup with pre-fetched / immediately downloaded IP info range information
        IpInfoLookup lookup = new IpInfoLookupImpl(Arrays.asList(
                fetchOnce(apnic),
                fetchOnce(aws),
                fetchOnce(azure),
                fetchOnce(googleCloud))
        );

        int allRows = 0;

        Map<String, Integer> providerCounts = new HashMap<>();
        Map<String, Integer> orgCounts = new HashMap<>();
        List<String> ipsWithNoInfos = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0) {
                    String ip = line.split(" ")[0];
                    IpInfos ipInfos = lookup.infosFor(Ipv4.of(ip));
                    allRows++;
                    for (String provider: ipInfos.getProviderNames()) {
                        Integer count = providerCounts.get(provider);
                        count = count == null ? 1 : ++count;
                        providerCounts.put(provider, count);
                    }
                    if (ipInfos.hasAnyFrom(ApnicAsnIpRangesSource.NAME)) {
                        String org = ipInfos.infoFromProvider(ApnicAsnIpRangesSource.NAME, ApnicAsnIpRangesSource.KEY_ASNORG);
                        Integer count = orgCounts.get(org);
                        count = count == null ? 1 : ++count;
                        orgCounts.put(org, count);
                    }
                    if (ipInfos.getProviderNames().isEmpty()) {
                        ipsWithNoInfos.add(ip);
                    }
                }
            }
        }
        List<Map.Entry<String, Integer>> sortedOrgCounts = new ArrayList<>(orgCounts.entrySet());
        Collections.sort(sortedOrgCounts, Map.Entry.<String, Integer>comparingByValue().reversed());
        System.out.println("Top Organisations:");
        for (int i = 0; i < Math.min(sortedOrgCounts.size(), 50); i++) {
            System.out.println(i + "\t" + sortedOrgCounts.get(i));
        }

        System.out.println("All: " + allRows);
        System.out.println("Infos by providers: " + providerCounts);
        System.out.println("IPs with no infos: " + ipsWithNoInfos);
    }

}
