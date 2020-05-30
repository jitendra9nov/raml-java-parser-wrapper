package com.bhadouriya.raml.tools;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static com.bhadouriya.raml.efficacies.Efficacy.printToOut;
import static java.io.File.separator;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;

public class ProgressProcessor {
    public static final String PATH = ".." + separator + "logs";
    public static final String LOG_FILE_REG = "co-app[-]?\\d*\\.log";
    public static final String LOG_FILE = "co-app.log";
    private static final Logger LOGGER = Logger.getLogger(ProgressProcessor.class.getName());
    private final ProgressPanel progressPanel;
    private final List<String> appLogList = new CopyOnWriteArrayList<>();
    private final List<Future<?>> taskInit = new CopyOnWriteArrayList<>();
    public boolean isPrinted;
    public int timeout = 60 * 1000;
    public ExecutorService executor = Executors.newFixedThreadPool(5);
    private boolean isCustom;
    private boolean isRaml;
    private boolean isExpt;

    public ProgressProcessor(final ProgressPanel progressPanel) {
        this.progressPanel = progressPanel;
    }

    private static SSLContext trustEveryOne() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = null;

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(final String s, final SSLSession sslSession) {
                return true;
            }
        });
        context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[]{new X509TrustManager() {

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        return context;
    }

    public boolean isPrinted() {
        return this.isPrinted;
    }

    public void extricateNow(final String timestamp, final boolean isCustom, final boolean isRaml, final boolean isExpt) {
        this.isCustom = isCustom;
        this.isRaml = isRaml;
        this.isExpt = isExpt;

        this.init(10, "https://api-for-log/something");
        try {
            this.checkTasks();
            Thread.sleep(1000);
            this.checkTasks();
        } catch (final Exception e) {
            printToOut("Exception has been caught!" + e);
        }
        this.printLogs(timestamp);

        try {
            Thread.sleep(1000);
            this.taskInit.forEach(future -> {
                try {
                    future.get();
                } catch (final Exception e) {
                    printToOut("Exception has been caught!" + e);
                }
            });
        } catch (final Exception e) {
            printToOut("Exception has been caught!" + e);
        }
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(this.timeout, TimeUnit.SECONDS);
            printToOut("Shutting down Executor>>Normally");
        } catch (final Exception e) {
            printToOut("Shutting down Executor>>ABNormally");
            this.executor.shutdown();
        }
    }

    private void init(final int ind, final String url) {
        for (int i = 0; i <= ind; i++) {
            this.taskInit.add(this.executor.submit(() ->
            {
                try {
                    this.extractLog(url);
                } catch (final URISyntaxException e) {
                    e.printStackTrace();
                }
            }));
        }
    }

    private void extractLog(final String url) throws URISyntaxException {
        if (this.loadCerts(url)) {
            this.callLogViewer(url);
        }
    }

    private void callLogViewer(final String url) throws URISyntaxException {
        final HttpUriRequest request = RequestBuilder.create("GET").setUri(new URI(url)).build();
        try (final CloseableHttpClient httpClient = this.httpClient()) {
            final CloseableHttpResponse response = httpClient.execute(request);
            if (null != response && response.getStatusLine().getStatusCode() == 200) {
                String respTxt = null;
                try {
                    respTxt = EntityUtils.toString(response.getEntity());
                } catch (final Exception e) {
                    printToOut(e.getMessage());
                }
            }
        } catch (final IOException | KeyManagementException | NoSuchAlgorithmException e) {
            printToOut(e.getMessage());
        }
    }

    private synchronized boolean loadCerts(final String url) {
        boolean isBool = true;
        try {
            AccreditorProcessor.install(url, false, null, null, null);
        } catch (final Exception e) {
            if ("Could not obtain server Certificate.".equalsIgnoreCase(e.getMessage())) {
                isBool = false;
            } else if (containsIgnoreCase(e.getMessage(), "You Don't have access to Path") || containsIgnoreCase(e.getMessage(), "JAVA_HOME environment variable is not set.")) {
                throw new IllegalArgumentException((e.getMessage()));
            }
        }
        return isBool;
    }

    private CloseableHttpClient httpClient() throws KeyManagementException, NoSuchAlgorithmException {
        try {
            SSLContext sslContext;
            try {
                sslContext = trustEveryOne();
            } catch (final Exception e) {
                e.printStackTrace();
                sslContext = SSLContexts.custom().build();
            }

            final SSLConnectionSocketFactory socketFactory =
                    new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"}, null,
                            SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            return HttpClients.custom().setSSLHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER).setSSLSocketFactory(socketFactory).build();

        } catch (final Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private void printLogs(final String timestamp) {
        this.taskInit.forEach(future -> {
            try {
                printToOut(future.toString());
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void checkTasks() {
        this.taskInit.forEach(future -> {
            try {
                future.get();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
    }
}


