package hu.distributeddocumentor.utils;

import java.net.URI;

public class RepositoryUriGenerator {
    
    public static String addCredentials(String uri, String userName, String password) {
        
        if (userName == null || password == null ||
            userName.length() == 0 ||
            password.length() == 0) {
            return uri;
        } else {
            
            URI parsed = URI.create(uri);
            
            String scheme = parsed.getScheme();
            if (scheme == null)
                scheme = "http";
            
            String result = scheme + "://" + userName + ":" + password + "@";
                    
            if (parsed.getAuthority() != null)
                result = result + parsed.getAuthority();
            
            if (parsed.getPath() != null)
                result = result + parsed.getPath();
            
            if (parsed.getQuery() != null)
                result = result + "?" + parsed.getQuery();
            
            return result;
            
        }        
    }
    
}
