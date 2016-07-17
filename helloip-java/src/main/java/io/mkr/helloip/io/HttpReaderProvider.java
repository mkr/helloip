package io.mkr.helloip.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * A {@link ReaderProvider} for HTTP response bodies using {@link org.apache.http.client.HttpClient}. Fully consuming the reader is required
 * to close the underyling connection (according to
 * <a href="https://mail-archives.apache.org/mod_mbox/hc-httpclient-users/200909.mbox/%3C4A9EBECB.3030708@apache.org%3E">the
 * HttpClient authors</a>).
 */
public class HttpReaderProvider implements ReaderProvider {

    private static final int DEFAULT_CONNECT_TIMEOUT = 60000;
    private static final int DEFAULT_SOCKET_TIMEOUT = 60000;

    private final String location;

    public HttpReaderProvider(String location) {
        this.location = location;
    }

    /**
     * Create a {@link Reader} for a HTTP response body. {@code null} is returned on any unexpected circumstances: response code not 200,
     * no response entity. Note: Fully consuming the reader is required to close the underlying connection.
     *
     * @return the {@link Reader} for the HTTP response body or null
     * @throws IOException
     */
    @Override
    public Reader createReader() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(location);
        RequestConfig requestConfig = RequestConfig
            .copy(RequestConfig.DEFAULT)
            .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
            .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
            .build();
        httpget.setConfig(requestConfig);
        CloseableHttpResponse response = httpclient.execute(httpget);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                Charset charset = ContentType.getOrDefault(responseEntity).getCharset();
                return new InputStreamReader(responseEntity.getContent(), charset == null ? StandardCharsets.ISO_8859_1 : charset);
            }
        }
        response.close();
        httpclient.close();
        return null;
    }

    @Override
    public String toString() {
        return getClass().getName() + " from " + location;
    }
}
