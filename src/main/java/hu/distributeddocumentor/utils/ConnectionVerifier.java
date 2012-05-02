package hu.distributeddocumentor.utils;

import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionVerifier {

    private static final Logger log = LoggerFactory.getLogger(ConnectionVerifier.class.getName());
    private URI uri;
    private String lastError;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getLastError() {
        return lastError;
    }

    public ConnectionVerifier(URI uri) {

        this.uri = uri;
    }

    public boolean verify() {

        try {

            HttpClient client = new DefaultHttpClient();

            SSLSocketFactory socketFactory = new SSLSocketFactory(new TrustSelfSignedStrategy(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme sch = new Scheme("https", 443, socketFactory);
            client.getConnectionManager().getSchemeRegistry().register(sch);

            HttpGet get = new HttpGet(uri);
            if (uri.getUserInfo() != null) {
                get.addHeader(BasicScheme.authenticate(
                        new UsernamePasswordCredentials(uri.getUserInfo()), "US-ASCII", false));
            }

            HttpResponse response = client.execute(get);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                lastError = response.getStatusLine().getReasonPhrase();

                return false;
            } else {
                return true;
            }

        } catch (Exception ex) {

            log.error(null, ex);
            lastError = ex.getMessage();
            return false;
        }
    }
}
