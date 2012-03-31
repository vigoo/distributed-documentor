package hu.distributeddocumentor.controller;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.Page;
import java.awt.Color;
import org.apache.commons.lang3.StringUtils;


public class MediaWikiEditor implements WikiEditor {

    private final Page page;

    public MediaWikiEditor(Page page) {
        this.page = page;
    }
        
    
    @Override
    public void setHeadingLevel(final int row, final int level) {
        
        modifyLine(row,
                new Function<String, String>() {
                    @Override
                    public String apply(String line) {
                        
                        String baseline = StringUtils.strip(line, "= ");
                        String prefix = StringUtils.repeat('=', level);
                        return prefix + " " + baseline + " " + prefix;                        
                    }                    
                });        
    }

    @Override
    public void setBold(int start, int end) {
        
        modifySelection(start, end, true,
            new Function<String, String>() {
                @Override
                public String apply(String selection) {
                                
                    if (selection.startsWith("'''") &&
                        selection.endsWith("'''") &&
                        selection.length() > 6) {
                        // Already set to bold

                        return selection.substring(3, selection.length()-3);
                    } else {
                        // Setting to bold

                        return "'''" + selection + "'''";
                    }
                }});
    }

    @Override
    public void setItalic(int start, int end) {
        
        modifySelection(start, end, true,
            new Function<String, String>() {
                @Override
                public String apply(String selection) {
                                
                    if (selection.startsWith("''") &&
                        selection.endsWith("''") &&
                        selection.length() > 4) {
                        // Already set to italic

                        return selection.substring(2, selection.length()-2);
                    } else {
                        // Setting to italic

                        return "''" + selection + "''";
                    }
                }});        
    }
        
    @Override
    public void setColor(final int start, final int end, final Color color) {
     
        modifySelection(start, end, true,
                new Function<String, String>() {
                    @Override
                    public String apply(String selection) {
                        
                        // TODO: handle case when selection already has <span>
                        return "<span style=\"color:#"+
                                Integer.toHexString(color.getRGB()).substring(2)+
                                "\">"+selection+"</span>";                        
                    }                    
                });
    }
       
    @Override
    public void toggleBullets(int startRow, int endRow) {
        
        toggleGenericBullets(startRow, endRow, '*');
    }
    
    @Override
    public void toggleEnumeration(int startRow, int endRow) {
        
        toggleGenericBullets(startRow, endRow, '#');
    }
    
    @Override
    public void indent(int startRow, int endRow) {
        
        modifyLines(startRow, endRow,
                new Function<String, String>() {
                    @Override
                    public String apply(String line) {
                        
                        if (line.length() > 0) {
                            char firstChar = line.charAt(0);
                            String prefix;
                            
                            if (firstChar != '*' &&
                                firstChar != '#' &&
                                firstChar != ';' &&
                                firstChar != ':')
                                prefix = ": ";
                            else
                                prefix = Character.toString(firstChar);
                            
                            return prefix + line;
                        }
                        else {
                            return line;
                        }
                    }
            });        
    }

    @Override
    public void unindent(int startRow, int endRow) {
        
          
        modifyLines(startRow, endRow,
                new Function<String, String>() {
                    @Override
                    public String apply(String line) {
                        
                        if (line.length() > 0) {
                            char firstChar = line.charAt(0);
                            
                            if (firstChar != '*' &&
                                firstChar != '#' &&
                                firstChar != ';' &&
                                firstChar != ':')
                                return line;
                            else
                                return line.substring(1).trim();
                        }
                        else {
                            return line;
                        }
                    }
            });        
    }
        
    @Override
    public void insertRemoteLink(int pos, String url, String linkText) {
        
        String linkRef;
        if (linkText.length() > 0)
            linkRef = "[" + url + " " + linkText + "]";
        else
            linkRef = "[" + url + "]";
        
        String markup = page.getMarkup();
        StringBuilder builder = new StringBuilder(markup);
        
        builder.insert(pos, linkRef);
        
        page.setMarkup(builder.toString());        
    }
    
    private void toggleGenericBullets(int startRow, int endRow, final char bulletChar) {
        
        final String bulletCharS = Character.toString(bulletChar);
        modifyLines(startRow, endRow,
                new Function<String, String>() {
                    @Override
                    public String apply(String line) {
                        if (line.startsWith("*") ||
                            line.startsWith("#") ||
                            line.startsWith(":") ||
                            line.startsWith(";")) {
                            
                            char originalBulletChar = line.charAt(0);
                            if (bulletChar != originalBulletChar) {
                                                                
                                int count;
                                for (count = 0; count < line.length(); count++) {
                                    if (line.charAt(count) != originalBulletChar)
                                        break;
                                }
                                
                                if (count < line.length()) {
                                    String stripped = line.substring(count).trim();
                                    return StringUtils.repeat(bulletChar, count) + " " + stripped;
                                } else{
                                    return line;
                                }
                                
                            } else {
                                return line.substring(1).trim();
                            }
                        }
                        else {                            
                            return bulletCharS + " " + line;
                        }
                    }
                });
    }
    
    private void modifyLine(int row, Function<String, String> action) {
     
        modifyLines(row, row, action);
    }
    
    private void modifyLines(int startRow, int endRow, Function<String, String> action) {
        String[] lines = page.getMarkup().split("\n");
        
        for (int row = startRow; row <= endRow; row++) {
            String line = lines[row];               
            String modifiedLine = action.apply(line);

            lines[row] = modifiedLine;
        }
        
        page.setMarkup(StringUtils.join(lines, '\n'));          
    }
    
    private void modifySelection(int start, int end, boolean trimLineEndings, Function<String, String> fn) {
        
        String markup = page.getMarkup();
        String selection = markup.substring(start, end); 
        StringBuilder builder = new StringBuilder(markup);
        
        if (trimLineEndings) {
            while (selection.charAt(0) == '\r' ||
                   selection.charAt(0) == '\n') {
                
                start++;
                selection = selection.substring(1);
            }
            
            while (selection.charAt(selection.length()-1) == '\r' ||
                   selection.charAt(selection.length()-1) == '\n') {
                end--;
                selection = selection.substring(0, selection.length()-2);
            }
        }
        
        String replacement = fn.apply(selection);
        
        builder.replace(start, end, replacement);
        page.setMarkup(builder.toString());
    }

}
