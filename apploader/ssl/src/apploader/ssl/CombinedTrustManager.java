package apploader.ssl;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class CombinedTrustManager implements X509TrustManager {

    private final List<X509TrustManager> tms;

    CombinedTrustManager(List<X509TrustManager> tms) {
        this.tms = tms;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (tms.isEmpty())
            return;
        CertificateException error = null;
        for (X509TrustManager tm : tms) {
            try {
                tm.checkClientTrusted(chain, authType);
                return;
            } catch (CertificateException ex) {
                if (error == null) {
                    error = ex;
                } else {
                    error.addSuppressed(ex);
                }
            }
        }
        throw error;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (tms.isEmpty())
            return;
        CertificateException error = null;
        for (X509TrustManager tm : tms) {
            try {
                tm.checkServerTrusted(chain, authType);
                return;
            } catch (CertificateException ex) {
                if (error == null) {
                    error = ex;
                } else {
                    error.addSuppressed(ex);
                }
            }
        }
        throw error;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        List<X509Certificate> all = new ArrayList<>();
        for (X509TrustManager tm : tms) {
            X509Certificate[] certs = tm.getAcceptedIssuers();
            all.addAll(Arrays.asList(certs));
        }
        return all.toArray(new X509Certificate[0]);
    }

    static TrustManager combine(TrustManager[]... tmArrays) {
        List<X509TrustManager> xtms = new ArrayList<>();
        for (TrustManager[] tmArray : tmArrays) {
            for (TrustManager tm : tmArray) {
                if (tm instanceof X509TrustManager) {
                    X509TrustManager xtm = (X509TrustManager) tm;
                    xtms.add(xtm);
                }
            }
        }
        return new CombinedTrustManager(xtms);
    }
}
