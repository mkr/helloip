package io.mkr.helloip.sources;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.ImmutableMap;
import net.ripe.commons.ip.Ipv4Range;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the Google Compute Engine IP range lookup via DNS TXT record requests as recommended in
 * their <a href="https://cloud.google.com/compute/docs/faq#ipranges">FAQ</a>.
 */
public class GoogleCloudIpRangesSource implements IpRangeInfosSource {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudIpRangesSource.class);

    public static final String NAME = "GoogleCloud";

    public static final String KEY_GROUP = "group";

    private static final String INITIAL_QUERY_DOMAIN = "_cloud-netblocks.googleusercontent.com";

    private static final String SPF_PREFIX = "v=spf1";
    private static final String INCLUDE_PREFIX = "include:";
    private static final String IP4_PREFIX = "ip4:";

    private DnsTextRecordRetriever textRecordRetriever = new JdkDnsTextRecordRetriever();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<IpRangeInfos> getIpRanges() throws IOException {
        List<IpRangeInfos> ipRangeInfos = new ArrayList<>();
        Collection<String> level1Domains = retrieveSpfRule(INITIAL_QUERY_DOMAIN, INCLUDE_PREFIX);
        for (String level1Domain: level1Domains) {
            Collection<String> cidrs = retrieveSpfRule(level1Domain, IP4_PREFIX);
            for (String cidr: cidrs) {
                Ipv4Range range = Ipv4Range.parseCidr(cidr);
                Map<String, String> infos = ImmutableMap.of(KEY_GROUP, level1Domain);
                ipRangeInfos.add(new IpRangeInfos(range, infos));
            }
        }
        return ipRangeInfos;
    }

    private Collection<String> retrieveSpfRule(String domainName, String rulePrefix) throws IOException {
        Collection<String> includeDomains = new ArrayList<>();
        logger.info("Retrieving TXT records record for {}", domainName);
        try {
            Collection<String> textRecords = textRecordRetriever.retrieveTextRecords(domainName);
            for (String textRecord: textRecords) {
                logger.debug("Text record: {}", textRecord);
                if (textRecord.startsWith(SPF_PREFIX)) {
                    includeDomains.addAll(scanSpfForStatements(textRecord, rulePrefix));
                }
            }
            return includeDomains;
        } catch (NamingException e) {
            logger.error("Could not retrieve TXT record from {}", domainName, e);
            throw new IOException(e);
        }
    }

    private static List<String> scanSpfForStatements(String spf, String statement) {
        List<String> includes = new ArrayList<>();
        String[] tokens = spf.split(" ");
        for (String token: tokens) {
            if (token.startsWith(statement)) {
                String include = token.substring(statement.length());
                includes.add(include);
            }
        }
        return includes;
    }

    /**
     * Internal interface for DNS requests for the TXT record. Accessible for the provider to be more testable.
     */
    interface DnsTextRecordRetriever {

        /**
         * Retrieves the DNS TXT record entries for the given domain. Returns an empty collection if there were no TXT records, or there was
         * any error in the DNS request (domain does not exist), DNS request timeouts etc.
         *
         * @param domainName the domain name
         * @return the DNS TXT records
         */
        Collection<String> retrieveTextRecords(String domainName) throws NamingException;
    }

    static class JdkDnsTextRecordRetriever implements DnsTextRecordRetriever {

        @Override
        public Collection<String> retrieveTextRecords(String domainName) throws NamingException {
            List<String> txtEntries = new ArrayList<>();
            Properties env = new Properties();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext dirContext = new InitialDirContext(env);
            Attribute txt = dirContext.getAttributes(domainName, new String[] { "TXT" }).get("TXT");
            if (txt != null) {
                NamingEnumeration e = txt.getAll();
                while (e.hasMore()) {
                    // strip any leading / closing quotes which were observed in TXT records
                    String entry = StringUtils.strip(e.next().toString(), "\"'");
                    txtEntries.add(entry);
                }
            }
            return txtEntries;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new GoogleCloudIpRangesSource().getIpRanges());
    }

}
