package hu.distributeddocumentor.controller;

import hu.distributeddocumentor.model.Change;

/**
 *
 * @author vigoo
 */
public class CommittableItem {
    
    private final String path;
    private final Change change;

    public Change getChange() {
        return change;
    }

    public String getPath() {
        return path;
    }
        

    public CommittableItem(String path, Change change) {
        this.path = path;
        this.change = change;
    }

    @Override
    public String toString() {
        
        String changeIndicator = "";
        
        switch (change) {
            case Added:
                changeIndicator = "[Added]";
                break;
                
            case Copied:
                changeIndicator = "[Copied]";
                break;
                
            case Modified:
                changeIndicator = "[Modified]";
                break;
                
            case Removed:
                changeIndicator = "[Removed]";
                break;
        }
        
        String friendlyPath = path;
        
        if (friendlyPath.equals("toc.xml"))
            friendlyPath = "Table of Contents";
        else {
            int lastDot = friendlyPath.lastIndexOf(".");
            String id = friendlyPath.substring(0, lastDot);
            String markupLanguage = friendlyPath.substring(lastDot + 1);
            
            friendlyPath = id + " (" + markupLanguage + ")";
        }
        
        return changeIndicator + " " + friendlyPath;
    }        
}
