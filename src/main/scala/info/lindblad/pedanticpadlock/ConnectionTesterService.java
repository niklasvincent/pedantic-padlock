package info.lindblad.pedanticpadlock;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.cert.CertPath;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class ConnectionTesterService {

    public static long daysUntilExpiration(String hostname) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, null);
            SSLSocketFactory ssf = sc.getSocketFactory();
            SSLSocket s = (SSLSocket) ssf.createSocket(hostname, 443);
            s.startHandshake();
            SSLSession session = s.getSession();
            java.security.cert.Certificate[] servercerts = session.getPeerCertificates();

            X509Certificate xc = (X509Certificate) servercerts[0];

            Date expirationDate = xc.getNotAfter();
            Date today = new Date();

            long millisecondsUntilExpiration = expirationDate.getTime() - today.getTime();
            long daysUntilExpiration = TimeUnit.DAYS.convert(millisecondsUntilExpiration, TimeUnit.MILLISECONDS);

            System.err.println("ExpirationDate = " + expirationDate.toString());

            return daysUntilExpiration;
        } catch (Exception e) {
            // Do nothing
        }
        return -1;
    }


}
