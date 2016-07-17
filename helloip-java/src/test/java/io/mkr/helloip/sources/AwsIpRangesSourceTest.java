package io.mkr.helloip.sources;

import io.mkr.helloip.base.StringReaderProvider;
import io.mkr.helloip.io.ReaderProvider;
import net.ripe.commons.ip.Ipv4Range;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AwsIpRangesSourceTest {

    @Test
    public void testAwsRangeParsing() throws Exception {
        String json = "{" +
                "  \"syncToken\": \"ignored\"," +
                "  \"createDate\": \"ignored\"," +
                "  \"prefixes\": [" +
                "    {" +
                "      \"ip_prefix\": \"23.20.0.0/14\"," +
                "      \"region\": \"us-east-1\"," +
                "      \"service\": \"AMAZON\"" +
                "    }," +
                "    {" +
                "      \"ip_prefix\": \"27.0.0.0/22\"," +
                "      \"region\": \"ap-northeast-1\"," +
                "      \"service\": \"AMAZON\"" +
                "    }," +
                "    {" +
                "      \"ip_prefix\": \"23.20.0.0/14\"," +
                "      \"region\": \"us-east-1\"," +
                "      \"service\": \"EC2\"" +
                "    }," +
                "    {" +
                "      \"ip_prefix\": \"46.51.128.0/18\"," +
                "      \"region\": \"eu-west-1\"," +
                "      \"service\": \"EC2\"" +
                "    }" +
                "]}";
        IpRangeInfosSource source = new AwsIpRangesSource(new StringReaderProvider(json));
        List<IpRangeInfos> ranges = source.getIpRanges();
        assertThat(ranges.size(), is(4));
        assertThat(ranges.get(0).getRange(), equalTo(Ipv4Range.parseCidr("23.20.0.0/14")));
        assertThat(ranges.get(0).getInfos().size(), is(2));
        assertThat(ranges.get(0).getInfos().get(AwsIpRangesSource.KEY_REGION), equalTo("us-east-1"));
        assertThat(ranges.get(0).getInfos().get(AwsIpRangesSource.KEY_SERVICE), equalTo("AMAZON"));
    }

}
