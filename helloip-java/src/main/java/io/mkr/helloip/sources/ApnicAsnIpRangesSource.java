package io.mkr.helloip.sources;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mkr.helloip.io.HttpReaderProvider;
import io.mkr.helloip.io.ReaderProvider;
import net.ripe.commons.ip.Ipv4Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A source for the IP ranges, their ASNs and the organisations behind the ASN as offered by APNIC.
 */
public class ApnicAsnIpRangesSource implements IpRangeInfosSource {

    private static final Logger logger = LoggerFactory.getLogger(ApnicAsnIpRangesSource.class);

    /** Name of this source */
    public static final String NAME = "THYME";

    /** Map key for entry holding the autonomous system number (ASN) */
    public static final String KEY_ASN = "ASN";

    /** Map key for entry holding the org behind the ASN */
    public static final String KEY_ASNORG = "ASNORG";

    /** Value for an ASN with an unknown org */
    public static final String VALUE_UNKNOWN = "unknown";

    /** URL to retrieve ASN to company mapping*/
    private static final String ASNTOCOMPANY_URL = "http://thyme.apnic.net/current/data-used-autnums";

    /** URL to retrieve ip range to ASN mapping */
    private static final String ASN_URL = "http://thyme.apnic.net/current/data-raw-table";

    private final ReaderProvider asnToCompanyReaderProvider;

    private final ReaderProvider asnReaderProvider;

    /**
     * Constructor configuring HTTP retrieval
     */
    public ApnicAsnIpRangesSource() {
        this.asnToCompanyReaderProvider = new HttpReaderProvider(ASNTOCOMPANY_URL);
        this.asnReaderProvider = new HttpReaderProvider(ASN_URL);
    }

    /**
     * Constructor to be used when passing non HTTP {@link ReaderProvider}s, e.g. for accessing a filesystem copies of the ASN lists.
     *
     * @param asnToCompanyReaderProvider the {@link ReaderProvider} for accessing the ASN to company list
     * @param asnReaderProvider the {@link ReaderProvider} for accessing the IP range to ASN list
     */
    public ApnicAsnIpRangesSource(ReaderProvider asnToCompanyReaderProvider, ReaderProvider asnReaderProvider) {
        this.asnToCompanyReaderProvider = asnToCompanyReaderProvider;
        this.asnReaderProvider = asnReaderProvider;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<IpRangeInfos> getIpRanges() throws IOException {
        Map<Integer, String> autnumOrg = new HashMap<>();
        logger.info("Retrieving ASN to company information with {}", asnToCompanyReaderProvider);
        try (BufferedReader br = new BufferedReader(asnToCompanyReaderProvider.createReader())) {
            String line;
            while ((line = br.readLine()) != null) {
                String autNum = line.substring(0, 5).trim();
                String organisation = line.substring(6).trim();
                autnumOrg.put(Integer.parseInt(autNum), organisation);
            }
        }
        List<IpRangeInfos> ipRangeInfos = new ArrayList<>();
        logger.info("Retrieving ASNs for IP ranges with {}", asnReaderProvider);
        try (BufferedReader br = new BufferedReader(asnReaderProvider.createReader())) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] cidrAndAsn = line.split("\t");
                Ipv4Range ipv4Range = Ipv4Range.parseCidr(cidrAndAsn[0]);
                Integer asn = Integer.parseInt(cidrAndAsn[1].trim());
                String company = autnumOrg.get(asn);
                Map<String, String> infos = new HashMap<>();
                infos.put(KEY_ASN, asn.toString());
                infos.put(KEY_ASNORG, company == null ? VALUE_UNKNOWN : company);
                ipRangeInfos.add(new IpRangeInfos(ipv4Range, infos));
            }
        }
        return ipRangeInfos;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Current APNIC IP ranges: " + new ApnicAsnIpRangesSource().getIpRanges());
    }

}
