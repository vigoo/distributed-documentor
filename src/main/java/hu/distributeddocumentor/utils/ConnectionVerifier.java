package hu.distributeddocumentor.utils;

import java.net.URI;
import javax.net.ssl.SSLContext;
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
    private SSLContext sc;
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
//
//        // Create a trust manager that does not validate certificate chains
//        TrustManager[] trustAllCerts = new TrustManager[]{
//            new X509TrustManager() {
//
//                @Override
//                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                    return null;
//                }
//
//                @Override
//                public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                }
//
//                @Override
//                public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                }
//            }
//        };
//
//        // Install the all-trusting trust manager
//        try {
//            sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//        } catch (KeyManagementException ex) {
//            log.log(Level.SEVERE, null, ex);
//        } catch (NoSuchAlgorithmException ex) {
//            log.log(Level.SEVERE, null, ex);
//        }
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
//    public boolean verify() {
//        
//        boolean failed = false;
//        lastError = null;
//        
//        HostnameVerifier originalVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
//        SSLSocketFactory originalFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
//
//        final String userName;
//        final String password;
//        
//        if (uri.getUserInfo() != null) {
//            String[] userInfoParts = uri.getUserInfo().split(":");
//
//            userName = userInfoParts[0];
//            if (userInfoParts.length > 1) {
//                password = userInfoParts[1];
//            } else {
//                password = "";
//            }
//        }
//        else {
//            userName = "";
//            password = "";
//        }
//
//        try {
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
//
//                @Override
//                public boolean verify(String urlHostName, SSLSession session) {
//                    return true;
//                }
//            });
//
//            Authenticator.setDefault(
//                    new Authenticator() {
//
//                        @Override
//                        protected PasswordAuthentication getPasswordAuthentication() {
//                            return new PasswordAuthentication(userName, password.toCharArray());
//                        }                                                
//                    });
//
//            URLConnection testConnection = uri.toURL().openConnection();
//            testConnection.setAllowUserInteraction(false);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(testConnection.getInputStream()));
//            in.readLine();
//            in.close();
//        } catch (Exception ex) {
//            log.log(Level.SEVERE, null, ex);
//            
//            lastError = ex.getMessage();
//            failed = true;
//        } finally {
//            HttpsURLConnection.setDefaultSSLSocketFactory(originalFactory);
//            HttpsURLConnection.setDefaultHostnameVerifier(originalVerifier);
//            Authenticator.setDefault(null);
//        }
//        
//        return !failed;
//    }
}
