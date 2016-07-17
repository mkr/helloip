package io.mkr.helloip.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP {@link ReaderProvider} which gets the location it should read from by a regex-match+replace on another
 * {@link ReaderProvider}s fully consumed content.
 */
public class TwoStepHttpReaderProvider implements ReaderProvider {

    private static final Logger logger = LoggerFactory.getLogger(TwoStepHttpReaderProvider.class);

    private final ReaderProvider locationReaderProvider;
    private final Pattern locationExtractionRegex;
    private final String locationExtractionReplace;

    public TwoStepHttpReaderProvider(ReaderProvider locationReaderProvider, Pattern locationExtractionRegex,
        String locationExtractionReplace) {
        this.locationReaderProvider = locationReaderProvider;
        this.locationExtractionRegex = locationExtractionRegex;
        this.locationExtractionReplace = locationExtractionReplace;
    }

    @Override
    public Reader createReader() throws IOException {
        logger.info("Getting location information via {}", locationReaderProvider);
        String location = null;
        try (BufferedReader br = new BufferedReader(locationReaderProvider.createReader())) {
            String line;
            while ((line = br.readLine()) != null && location == null) {
                Matcher matcher = locationExtractionRegex.matcher(line);
                if (matcher.matches()) {
                    location = matcher.replaceFirst(locationExtractionReplace);
                }
            }
        }
        return location != null ? new HttpReaderProvider(location).createReader() : null;
    }
}
