package hu.distributeddocumentor.gui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.JList;

public class JListWithImagePreview extends JList {
    
    private File root;
    private Dimension maxToolTipSize = new Dimension(200, 200);

    public File getRoot() {
        return root;
    }

    public void setRoot(File root) {
        this.root = root;
    }

    public Dimension getMaxToolTipSize() {
        return maxToolTipSize;
    }

    public void setMaxToolTipSize(Dimension maxToolTipSize) {
        this.maxToolTipSize = maxToolTipSize;
    }
    
    

    @Override
    public String getToolTipText(MouseEvent me) {
               
        int index = locationToIndex(me.getPoint());
        if (index > -1) {
            
            String name = (String)getModel().getElementAt(index);
            File img = new File(root, name);
            
            return "<html><body><h3>" + name + "</h3><img src='" + img.toURI() + "' width='"+ Integer.toString(maxToolTipSize.width) + "' height='" + Integer.toString(maxToolTipSize.height) + "'></body></html>";
        }
        
        return null;
    }
    
    
    
}
