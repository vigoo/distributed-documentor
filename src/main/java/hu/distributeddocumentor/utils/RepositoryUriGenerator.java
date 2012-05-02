package hu.distributeddocumentor.utils;

import java.net.URI;

public abstract class RepositoryUriGenerator {        
    
    public static String addCredentials(final String uri, 
                                        final String userName, 
                                        final String password) {
        
        if (userName == null || 
            password == null ||
            userName.length() == 0 ||
            password.length() == 0) {
            return uri;
        } else {
            
            final URI parsed = URI.create(uri);
            
            String scheme = parsed.getScheme();
            if (scheme == null)
                scheme = "http";
            
            final StringBuilder result = new StringBuilder();
            result.append(scheme);
            result.append("://");
            result.append(userName);
            result.append(':');
            result.append(password);
            result.append('@');
                    
            if (parsed.getAuthority() != null)
                result.append(parsed.getAuthority());
            
            if (parsed.getPath() != null)
                result.append(parsed.getPath());
            
            if (parsed.getQuery() != null) {
                result.append('?');
                result.append(parsed.getQuery());
            }
            
            return result.toString();
            
        }        
    }
    
}
