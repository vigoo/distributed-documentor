package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.Documentation;


public class PageIdGenerator {
    private final Documentation doc;

    public PageIdGenerator(Documentation doc) {
        this.doc = doc;        
    }
    
    public String generate(String pageTitle) {

        String defid = generateDefaultId(pageTitle);
        if (doc.getPage(defid) == null)
            return defid;
        else {
         
            int counter = 1;
            String id = addCounter(defid, counter);
            while (doc.getPage(id) != null) {
                counter++;
                id = addCounter(defid, counter);
            }                
            
            return id;
        }        
    }

    private String generateDefaultId(String pageTitle) {
        if (pageTitle == null || pageTitle.length() == 0)
            pageTitle = "Untitled";
        
        String[] words = pageTitle.split(" ");
        StringBuilder builder = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            
            String capitalized = capitalize(words[i]);
            for (int j = 0; j < capitalized.length(); j++) {
                
                char c = capitalized.charAt(j);
                if (Character.isLetterOrDigit(c))
                    builder.append(c);
            }
        }
        
        return builder.toString();
    }
    
    private String capitalize(String word) {
        
        if (word.length() == 0)
            return "";
        else
            return Character.toString(Character.toUpperCase(word.charAt(0))) + word.substring(1);
    }

    private String addCounter(String id, int counter) {
        return id + Integer.toString(counter);
    }
}
