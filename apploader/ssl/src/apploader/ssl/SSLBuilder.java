package apploader.ssl;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;
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
    private boolean hasTrustAll = false;

    public void trustAll() {
        this.hasTrustAll = true;
    }

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

    public TrustManager buildTrustManager() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        if (certs.isEmpty()) {
            if (hasTrustAll) {
                return new CombinedTrustManager(Collections.emptyList());
            } else {
                return null;
            }
        }

        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManager[] myManagers;
        {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            for (X509Certificate cert : certs) {
                String name = cert.getSubjectX500Principal().getName(X500Principal.RFC1779);
                ks.setCertificateEntry(name, cert);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init(ks);
            myManagers = tmf.getTrustManagers();
        }
        TrustManager[] defManagers;
        {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
            tmf.init((KeyStore) null);
            defManagers = tmf.getTrustManagers();
        }

        return CombinedTrustManager.combine(defManagers, myManagers);
    }

    public SSLContext buildSSLContext() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, KeyManagementException {
        TrustManager tm = buildTrustManager();
        if (tm == null)
            return null;
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] {tm}, null);
        return sslContext;
    }
}
