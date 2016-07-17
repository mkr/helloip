package io.mkr.helloip.sources;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.mkr.helloip.io.TwoStepHttpReaderProvider;
import io.mkr.helloip.io.HttpReaderProvider;
import io.mkr.helloip.io.ReaderProvider;
import net.ripe.commons.ip.Ipv4Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A {@link IpRangeInfosSource} for the MS Azure IP ranges.
 */
public class AzureIpRangesSource implements IpRangeInfosSource {

    private static final Logger logger = LoggerFactory.getLogger(AzureIpRangesSource.class);

    public static final String NAME = "AZURE";

    private static final String KEY_REGION = "region";
    private static final String AZURE_IP_URL = "https://www.microsoft.com/en-us/download/confirmation.aspx?id=41653";
    private static final Pattern METAPATTERN_REGEX = Pattern.compile(
        ".*href=\"(https://download.microsoft.com/download/0/1/8/018E208D-54F8-44CD-AA26-CD7BC9524A8C/)(.*?)(.xml).*");
    private static final String METAPATTERN_REPLACE = "$1$2$3";

    private ReaderProvider readerProvider;

    public AzureIpRangesSource() {
        this.readerProvider = new TwoStepHttpReaderProvider(new HttpReaderProvider(AZURE_IP_URL), METAPATTERN_REGEX, METAPATTERN_REPLACE);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<IpRangeInfos> getIpRanges() throws IOException {
        logger.info("Retrieving Microsoft Azure Datacenter IP ranges with {}", readerProvider);
        Reader reader = readerProvider.createReader();
        if (reader != null) {
            try {
                SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                AzureIpRangesHandler saxHandler = new AzureIpRangesHandler();
                saxParser.parse(new InputSource(reader), saxHandler);
                return saxHandler.getRangeInfos();
            } catch (Exception e) {
                throw new IOException(e);
            } finally {
                reader.close();
            }
        }
        return Collections.emptyList();
    }

    private static class AzureIpRangesHandler extends DefaultHandler {

        private static final String EL_REGION = "Region";
        private static final String ATT_NAME = "Name";
        private static final String EL_IPRANGE = "IpRange";
        private static final String ATT_SUBNET = "Subnet";


        private List<IpRangeInfos> rangeInfos = new ArrayList<>();
        private String currentRegion = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(EL_REGION)) {
                currentRegion = attributes.getValue(ATT_NAME);
            } else if (qName.equals(EL_IPRANGE)) {
                Map<String, String> azureIpRangeInfos = new HashMap<>();
                azureIpRangeInfos.put(KEY_REGION, currentRegion);
                rangeInfos.add(new IpRangeInfos(Ipv4Range.parseCidr(attributes.getValue(ATT_SUBNET)), azureIpRangeInfos));
            }
        }

        public List<IpRangeInfos> getRangeInfos() {
            return rangeInfos;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new AzureIpRangesSource().getIpRanges());
    }
}
