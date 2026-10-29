package apploader.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SSLBuilder {

    private CertificateFactory cf = null;
    private final List<X509Certificate> certs = new ArrayList<>();

    public void addTrustCertificate(X509Certificate cert) {
        certs.add(cert);
    }

    public void addTrustCertificate(Path path) throws IOException, CertificateException {
        try (InputStream is = Files.newInputStream(path)) {
            if (cf == null) {
                cf = CertificateFactory.getInstance("X.509");
            }
            X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
            addTrustCertificate(cert);
        }
    }

    public static KeyStore storeWithCertificates(List<X509Certificate> certs) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        for (int i = 0; i < certs.size(); i++) {
            X509Certificate cert = certs.get(i);
            ks.setCertificateEntry("ca-" + i, cert);
        }
        return ks;
    }

    public static X509TrustManager trustAllManager() {
        return new CombinedTrustManager(Collections.emptyList());
    }

    private static TrustManager[] trustManagers(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }

    public X509TrustManager buildTrustManager() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        TrustManager[] myManagers = certs.isEmpty() ? new TrustManager[0] : trustManagers(storeWithCertificates(certs));
        TrustManager[] defManagers = trustManagers(null);
        return CombinedTrustManager.combine(defManagers, myManagers);
    }

    public static SSLContext createSSLContext(TrustManager tm) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] {tm}, null);
        return sslContext;
    }

    public SSLContext buildSSLContext() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, KeyManagementException {
        if (certs.isEmpty())
            return null;
        TrustManager tm = buildTrustManager();
        return createSSLContext(tm);
    }
}
