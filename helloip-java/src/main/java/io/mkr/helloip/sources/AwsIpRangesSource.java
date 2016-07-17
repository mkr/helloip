package io.mkr.helloip.sources;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.mkr.helloip.io.HttpReaderProvider;
import io.mkr.helloip.io.ReaderProvider;
import net.ripe.commons.ip.Ipv4Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects the AWS IP ranges as recommended in <a href="http://docs.aws.amazon.com/general/latest/gr/aws-ip-ranges.html">their document</a>.
 */
public class AwsIpRangesSource implements IpRangeInfosSource {

    private static final Logger logger = LoggerFactory.getLogger(AwsIpRanges.class);

    public static final String NAME = "AWS";

    public static final String KEY_SERVICE = "service";
    public static final String KEY_REGION = "region";

    private static final String AMAZON_URL = "https://ip-ranges.amazonaws.com/ip-ranges.json";

    private final ReaderProvider awsJsonReaderProvider;

    public AwsIpRangesSource(ReaderProvider awsJsonReaderProvider) {
        this.awsJsonReaderProvider = awsJsonReaderProvider;
    }

    public AwsIpRangesSource() {
        this.awsJsonReaderProvider = new HttpReaderProvider(AMAZON_URL);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Retrieves the current Amazon AWS IP ranges by requesting the JSON file from the AWS server. Amazon provides sometimes overlapping ranges
     * for different types of services ("AMAZON", "EC2", "CLOUDFRONT"...).
     *
     * @return a set of IP ranges
     */
    @Override
    public List<IpRangeInfos> getIpRanges() throws IOException {
        List<IpRangeInfos> result = new ArrayList<>();
        logger.info("Retrieving AWS IP ranges with {}", awsJsonReaderProvider);
        try (Reader reader = awsJsonReaderProvider.createReader()) {
            AwsIpRanges awsIpRanges = new ObjectMapper().readValue(reader, AwsIpRanges.class);
            for (AwsIpRanges.AwsIpRange awsIpRange: awsIpRanges.getPrefixes()) {
                Ipv4Range range = Ipv4Range.parseCidr(awsIpRange.getIp_prefix());
                Map<String, String> infos = ImmutableMap.of(KEY_SERVICE, awsIpRange.getService(), KEY_REGION, awsIpRange.getRegion());
                result.add(new IpRangeInfos(range, infos));
            }
        }
        return result;
    }

    /**
     * JSON data transfer object representing an IP range in AWS.
     */
    public static class AwsIpRanges {

        @JsonProperty
        private String syncToken;

        @JsonProperty
        private String createDate;

        @JsonProperty
        private List<AwsIpRange> prefixes = new ArrayList<>();

        public String getSyncToken() {
            return syncToken;
        }

        public void setSyncToken(String syncToken) {
            this.syncToken = syncToken;
        }

        public String getCreateDate() {
            return createDate;
        }

        public void setCreateDate(String createDate) {
            this.createDate = createDate;
        }

        public List<AwsIpRange> getPrefixes() {
            return prefixes;
        }

        public void setPrefixes(List<AwsIpRange> prefixes) {
            this.prefixes = prefixes;
        }

        public static class AwsIpRange {

            @JsonProperty
            private String ip_prefix;

            @JsonProperty
            private String region;

            @JsonProperty
            private String service;


            public String getIp_prefix() {
                return ip_prefix;
            }

            public void setIp_prefix(String ip_prefix) {
                this.ip_prefix = ip_prefix;
            }

            public String getRegion() {
                return region;
            }

            public void setRegion(String region) {
                this.region = region;
            }

            public String getService() {
                return service;
            }

            public void setService(String service) {
                this.service = service;

            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Current AWS IP Ranges: " + new AwsIpRangesSource().getIpRanges());
    }

}
