package io.mkr.helloip.examples;

import net.ripe.commons.ip.Ipv4;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Tries to find the remote IPv4 of an {@link javax.servlet.http.HttpServletRequest} following the approach of
 * <a href="https://tomcat.apache.org/tomcat-7.0-doc/api/org/apache/catalina/valves/RemoteIpValve.html">Tomcat's
 * RemoteIpValve</a>, i.e. using the right-most non-trusted IP in a given header.
 */
public class RemoteIpDetector {

    private Pattern trustedProxyIpsPattern = Pattern.compile(
                    "10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|" +
                    "192\\.168\\.\\d{1,3}\\.\\d{1,3}|" +
                    "169\\.254\\.\\d{1,3}\\.\\d{1,3}|" +
                    "127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|" +
                    "172\\.1[6-9]{1}\\.\\d{1,3}\\.\\d{1,3}|" +
                    "172\\.2[0-9]{1}\\.\\d{1,3}\\.\\d{1,3}|" +
                    "172\\.3[0-1]{1}\\.\\d{1,3}\\.\\d{1,3}");

    private String remoteHeaderName = "X-Forwarded-For";

    public Ipv4 remoteIp(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();
        Ipv4 result = null;
        if (trustedProxyIpsPattern !=null && trustedProxyIpsPattern.matcher(remoteAddress).matches()) {
            List<String> ips = valuesFromCsvHeaders(request, remoteHeaderName);
            // scan for the first non-trusted IP in the headers
            for (int i = ips.size()-1; i >= 0; i--) {
                String value = ips.get(i);
                if (!trustedProxyIpsPattern.matcher(value).matches()) {
                    try {
                        // try parsing the header value as IPv4
                        result = Ipv4.parse(value);
                        break;
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
        return result != null ? result : Ipv4.of(remoteAddress);
    }

    /**
     * Parse header values from multiple headers. CSV Values can be spread across multiple field-name:field-value rows
     * according to RFC 2616 (section 4.2). Splitting into multiple headers should be equivalent to having multiple
     * values in one CSV.
     *
     * @param request the request
     * @param headerName the header name
     * @return all header values for the given name interpreted as CSV
     */
    private List<String> valuesFromCsvHeaders(HttpServletRequest request, String headerName) {
        Enumeration<String> headers = request.getHeaders(headerName);
        // according to doc headers can be null "if the container does not allow access to header information"
        return headers != null
                ? Collections.list(request.getHeaders(headerName))
                        .stream()
                        .flatMap(header -> Arrays.stream(header.split(",")))
                        .map(String::trim)
                        .collect(Collectors.toList())
                : Collections.emptyList();
    }

    public void setRemoteHeaderName(String remoteHeaderName) {
        this.remoteHeaderName = remoteHeaderName;
    }

    public void setTrustedProxyIpsPattern(String trustedProxyIps) {
        if (trustedProxyIps == null || trustedProxyIps.length() == 0) {
            this.trustedProxyIpsPattern = null;
        } else {
            this.trustedProxyIpsPattern = Pattern.compile(trustedProxyIps);
        }
    }
}
