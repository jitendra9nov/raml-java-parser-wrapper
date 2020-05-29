package com.bhadouriya.raml.tools;

import javax.net.ssl.*;
import javax.swing.*;
import java.io.*;
import java.net.URI;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.logging.Logger;

import static java.io.File.separator;
import static java.io.File.separatorChar;
import static java.lang.Integer.parseInt;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.isWritable;
import static java.util.logging.Level.*;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class AccreditorProcessor {
    private static final Logger LOGGER = Logger.getLogger(AccreditorProcessor.class.getName());

    private static final String NO_ACCESS = "You don't have write access rights to Path: \n";

    private static final String KEY_STORE_NOT_CHANGED = "KeyStore not changed.";

    private static final String JKS_DEF_JDK_PATH = System.getenv("JAVA_HOME") + separatorChar + "jre" + separatorChar + "lib" + separatorChar + "security";

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static final String LOCAL_JKS = "jssecacerts";

    public static void main(final String[] args) {
        if (args.length > 1) {
            try {
                install(args[0], false, (args.length > 2 ? args[1] : null), (args.length > 3 ? args[2] : null), (args.length > 4 ? args[3] : null));
            } catch (final Exception e) {
                LOGGER.log(SEVERE, "Main", e
                );
            }
        } else {
            LOGGER.log(SEVERE, "Usage: java AccreditorProcessor <host[:port]> <certFilePath> <trustStorePath> <passphrase>");
            throw new IllegalArgumentException("Usage: java AccreditorProcessor <host[:port]> <certFilePath> <trustStorePath> <passphrase>");
        }
    }


    public static void install(String url, boolean isDel, String certFilePath, String trustStorePath, String password) throws Exception {
        final String host;
        final int port;
        final char[] passphrase;

        final String domain = getDomainWithPort(url);

        final String[] c = domain.split(":");
        host = c[0];
        port = (c.length == 1 || parseInt(c[1]) == -1) ? 443 : parseInt(c[1]);
        final String p = isEmpty(password) ? "changeIt" : password;
        passphrase = p.toCharArray();

        File keyStoreFile = null;

        if (!isEmpty(trustStorePath)) {
            keyStoreFile = new File(trustStorePath);

            if (!isWritable(keyStoreFile.toPath().getParent())) {
                LOGGER.log(SEVERE, NO_ACCESS + keyStoreFile.toPath().getParent());
                throw new IllegalArgumentException(NO_ACCESS + keyStoreFile.toPath().getParent());
            }
        }
        if (isEmpty(System.getenv("JAVA_HOME"))) {
            LOGGER.log(SEVERE, "JAVA_HOME environment variable is not set.");
            throw new IllegalArgumentException("JAVA_HOME environment variable is not set.");
        }

        final File dir = new File(JKS_DEF_JDK_PATH);

        if (!isWritable(dir.toPath())) {
            LOGGER.log(SEVERE, NO_ACCESS + dir);
            throw new IllegalArgumentException(NO_ACCESS + dir);
        }

        File tempFile = null;

        try {
            final InputStream is = AccreditorProcessor.class.getResourceAsStream(separator + "artifacts" + separator + LOCAL_JKS);
            tempFile = File.createTempFile("tempfile", ".tmp");
            try (final OutputStream out = new FileOutputStream(tempFile)) {
                int read;
                final byte[] bytes = new byte[1024];
                while ((read = is.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
            }
            tempFile.deleteOnExit();
            keyStoreFile = tempFile;

        } catch (final Exception e) {
            LOGGER.log(SEVERE, e.getMessage());
            keyStoreFile = new File(dir, LOCAL_JKS);
        }

        if (!keyStoreFile.isFile()) {
            keyStoreFile = new File(dir, LOCAL_JKS);
            if (!keyStoreFile.isFile()) {
                keyStoreFile = copy(new File(dir, "cacerts").toPath(), keyStoreFile.toPath()).toFile();
            }
        } else {
            keyStoreFile = new File(dir, LOCAL_JKS);
            if (!keyStoreFile.isFile()) {
                keyStoreFile = copy(tempFile.toPath(), keyStoreFile.toPath()).toFile();
            }
        }


        final KeyStore keyStore = loadKeyStore(passphrase, keyStoreFile);

        final X509Certificate[] chain = getCertChain(certFilePath, host, port, keyStore);

        if (null != chain) {
            printCert(chain);

            int index = 0;
            Exception ee = null;
            for (final X509Certificate cert : chain
            ) {
                try {
                    if (isDel) {
                        delFromUrl(host, port, index, passphrase, keyStoreFile, keyStore, cert);
                    } else {
                        importCert(host, port, index, passphrase, keyStoreFile, keyStore, cert);
                    }
                } catch (final IllegalArgumentException ex) {
                    ee = ex;
                }
                index++;

            }
            if (null != ee) {
                throw new IllegalArgumentException(ee.getMessage());
            }
        } else {
            if (isDel) {
                try {
                    delFromUrl(host, port, 0, passphrase, keyStoreFile, keyStore, null);
                } catch (final Exception ex) {

                    try {
                        delFromAlias(url, passphrase, keyStoreFile, keyStore);
                    } catch (final Exception ex1) {
                        LOGGER.log(INFO, KEY_STORE_NOT_CHANGED);
                        throw new IllegalArgumentException(KEY_STORE_NOT_CHANGED);
                    }
                }
            } else {
                LOGGER.log(SEVERE, "Could not obtain server certificate.");
                throw new IllegalArgumentException("Could not obtain server certificate.");
            }
        }


    }


    private static X509Certificate[] getCertChain(final String filePath, final String host, final int port, final KeyStore keyStore) throws Exception {
        if (null != filePath) {
            try {
                final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                final X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(new FileInputStream(filePath));
                return new X509Certificate[]{cert};
            } catch (final Exception e) {
                LOGGER.log(WARNING, e.getMessage());
            }
        } else {
            final SavingTrustManager tm = fetchCert(host,
                    port, keyStore);
            return tm.chain;
        }

        return null;
    }

    private static void printCert(final X509Certificate[] chain) throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n\nCertificate " + chain.length + " certificate(s):\n\n");
        final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        final MessageDigest md5 = MessageDigest.getInstance("MD5");

        for (int i = 0; i < chain.length; i++) {
            final X509Certificate cert = chain[i];
            sb.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + (i + 1) + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~").append("\n Subject " +
                    cert.getSubjectDN()).append("\n Issuer " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            sb.append("\n\t sha1\t" + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
            sb.append("\n\t md5\t" + toHexString(sha1.digest())).append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

        }
        LOGGER.log(INFO, sb.toString());

    }


    private static void importCert(final String host, final int port, final int index, final char[] passphrase, final File file, final KeyStore keyStore, final X509Certificate cert) throws Exception {
        final String certAlias = keyStore.getCertificateAlias(cert);
        final boolean hasAlias = !isEmpty(certAlias);

        final String alias = hasAlias ? keyStore.getCertificateAlias(cert) : createAlias(host, port, index);

        final StringBuilder sb = new StringBuilder();

        if (hasAlias) {
            sb.append("\n\nCertificate already exists with alias '" + alias + "' in keystore \n" + file.getPath());
            LOGGER.log(WARNING, sb.toString());
            throw new IllegalArgumentException(sb.toString());
        }
        keyStore.setCertificateEntry(alias, cert);
        try (final OutputStream out = new FileOutputStream(file)) {
            keyStore.store(out, passphrase);
            sb.append("\n\n" + cert).append("\n Added certificate using alias '" + alias + "' to keystore \n" + file.getPath());

            JOptionPane.showMessageDialog(null, sb.toString(), "Import Success", JOptionPane.INFORMATION_MESSAGE, null);
        }
        LOGGER.log(INFO, sb.toString());
    }


    private static void delFromUrl(final String host, final int port, final int index, final char[] passphrase, final File file, final KeyStore
            keyStore, final X509Certificate cert) throws Exception {
        final String alias = isEmpty(keyStore.getCertificateAlias(cert)) ? createAlias(host, port, index) : keyStore.getCertificateAlias(cert);
        deleteCert(passphrase, file, keyStore, alias);
    }


    private static String createAlias(String host, final int port, final int index) {
        if (host.endsWith("/") || host.endsWith("\\")) {
            host = host.substring(0, host.length() - 1);
        }
        return host + (port == 0 ? "" : ":" + port) + "-X509" + (index == 0 ? "" : "-" + index);
    }

    private static void delFromAlias(String url, final char[] passphrase, final File file, final KeyStore keyStore) throws Exception {
        url = url.replaceAll("https://", "");
        final boolean hasAlias = null != keyStore.getCertificate(url);
        final String alias;
        if (hasAlias) {
            alias = url;
        } else if (url.contains(":")) {
            alias = createAlias(url, 0, 0);
        } else {
            alias = createAlias(url, 443, 0);
        }
        deleteCert(passphrase, file, keyStore, alias);
    }

    private static void deleteCert(final char[] passphrase, final File file, final KeyStore keyStore, final String alias) throws Exception {


        final StringBuilder sb = new StringBuilder();

        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias);
        } else {
            sb.append("\n\nCertificate not exists with alias '" + alias + "' in keystore \n" + file.getPath());
            LOGGER.log(WARNING, sb.toString());
            throw new IllegalArgumentException(sb.toString());
        }
        try (final OutputStream out = new FileOutputStream(file)) {
            keyStore.store(out, passphrase);

            sb.append("\n\n").append("\n Deleted certificate using alias '" + alias + "' to keystore \n" + file.getPath());

            JOptionPane.showMessageDialog(null, sb.toString(), "Delete Success", JOptionPane.INFORMATION_MESSAGE, null);
        }
        LOGGER.log(INFO, sb.toString());
    }


    private static SavingTrustManager fetchCert(final String host, final int port, final KeyStore keyStore) throws Exception {
        final StringBuilder sb = new StringBuilder();

        final SSLContext context = SSLContext.getInstance("TLS");

        final TrustManagerFactory managerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        managerFactory.init(keyStore);

        final X509TrustManager defaultTrustManager = (X509TrustManager) managerFactory.getTrustManagers()[0];

        final SavingTrustManager trustManager = new SavingTrustManager(defaultTrustManager);

        context.init(null, new TrustManager[]{trustManager}, null);

        final SSLSocketFactory factory = context.getSocketFactory();
        sb.append("\n\nOpening connection to " + host + ":" + port + "...\n");

        try (final SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
            socket.setSoTimeout(10000);
            socket.startHandshake();
            sb.append("\nNo errors, certificate is already trusted\n");
        } catch (final Exception e) {
            LOGGER.log(SEVERE, e.toString());
        }
        LOGGER.log(INFO, sb.toString());
        return trustManager;

    }


    private static String toHexString(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (int b : bytes
        ) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 5]);
            sb.append(' ');
        }
        return sb.toString();
    }

    private static String getDomainWithPort(final String url) throws Exception {

        final URI uri = new URI(url);

        final String domain = (null == uri.getHost()) ? url : uri.getHost() + ":" + uri.getPort();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }


    private static void printCerts(final KeyStore keyStore) throws Exception {
        LOGGER.log(INFO, "########## Total Certificates: " + keyStore.size());
        final Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            final String alias = aliases.nextElement();
            printCert(new X509Certificate[]{(X509Certificate) keyStore.getCertificate(alias)});
        }


    }

    private static KeyStore loadKeyStore(final char[] passphrase, final File file) {
        LOGGER.log(INFO, "Loading KeyStore " + file + " ...");
        KeyStore keyStore = null;

        try (final InputStream is = new FileInputStream(file)) {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(is, passphrase);
            printCerts(keyStore);

        } catch (final Exception e) {
            LOGGER.log(WARNING, e.toString(), e);
        }
        return keyStore;
    }

    private static class SavingTrustManager implements X509TrustManager {
        private final X509TrustManager tm;
        private X509Certificate[] chain;

        public SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            this.chain = chain;
            this.tm.checkServerTrusted(chain, authType);
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }
    }
}
