package io.mkr.helloip.examples;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.mkr.helloip.IpInfoLookup;
import io.mkr.helloip.IpInfoLookupImpl;
import io.mkr.helloip.IpInfos;
import io.mkr.helloip.sources.ApnicAsnIpRangesSource;
import io.mkr.helloip.sources.AwsIpRangesSource;
import io.mkr.helloip.sources.AzureIpRangesSource;
import io.mkr.helloip.sources.GoogleCloudIpRangesSource;
import net.ripe.commons.ip.Ipv4;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import static io.mkr.helloip.Suppliers.*;

public class IpInfoServlet {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        Server server = new Server(8080);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(HelloIpServlet.class, "/*");
        server.start();
        server.join();
    }

    public static class HelloIpServlet extends HttpServlet {

        private final ScheduledExecutorService ipInfoRefreshExecutor = new ScheduledThreadPoolExecutor(1);
        private IpInfoLookup ipInfoLookup;

        @Override
        public void init() throws ServletException {
            this.ipInfoLookup = new IpInfoLookupImpl(Arrays.asList(
                    fetchAsyncWithRefresh(apnic, 1, TimeUnit.DAYS, ipInfoRefreshExecutor),
                    fetchAsyncWithRefresh(aws, 1, TimeUnit.DAYS, ipInfoRefreshExecutor),
                    fetchAsyncWithRefresh(azure, 1, TimeUnit.DAYS, ipInfoRefreshExecutor),
                    fetchAsyncWithRefresh(googleCloud, 1, TimeUnit.DAYS, ipInfoRefreshExecutor)
            ));
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                IOException {
            Ipv4 remoteIp = new RemoteIpDetector().remoteIp(request);
            IpInfos infos = ipInfoLookup.infosFor(remoteIp);

            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("<h1>Hello %s</h1>", remoteIp));
            if (infos.hasAnyFrom(ApnicAsnIpRangesSource.NAME)) {
                sb.append(String.format("A warm welcome to %s, (ASN: %s)<br>",
                        infos.infoFromProvider(ApnicAsnIpRangesSource.NAME, ApnicAsnIpRangesSource.KEY_ASNORG),
                        infos.infoFromProvider(ApnicAsnIpRangesSource.NAME, ApnicAsnIpRangesSource.KEY_ASN)));
            }
            Optional<String> cloud = cloudName(infos);
            if (cloud.isPresent()) {
                sb.append(String.format("Greetings to our friends in the %s cloud.<br>", cloud.get()));
            }
            response.getWriter().println(sb.toString());
        }

        private static Optional<String> cloudName(IpInfos infos) {
            if (infos.hasAnyFrom(GoogleCloudIpRangesSource.NAME)) {
                return Optional.of("Google");
            } else if (infos.hasAnyFrom(AwsIpRangesSource.NAME)) {
                return Optional.of("AWS");
            } else if (infos.hasAnyFrom(AzureIpRangesSource.NAME)) {
                return Optional.of("Azure");
            } else {
                return Optional.empty();
            }
        }
    }


}
