package hu.distributeddocumentor.gui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JTable;


public class JTableWithImagePreview extends JTable {

    private File root;
    private Dimension maxToolTipSize = new Dimension(300, 300);

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
               
        int index = rowAtPoint(me.getPoint());
        if (index > -1) {
            
            String name = (String)getValueAt(index, 0);
            File img = new File(root, name);
            
            ImageIcon icon = new ImageIcon(img.getAbsolutePath());
            double horizontalPercentage = (double)maxToolTipSize.width / (double)icon.getIconWidth();
            double verticalPercentage = (double)maxToolTipSize.height / (double)icon.getIconHeight();
            double percentage = Math.min(1.0, Math.min(horizontalPercentage, verticalPercentage));
            
            int targetWidth = (int)Math.round((double)icon.getIconWidth() * percentage);
            int targetHeight = (int)Math.round((double)icon.getIconHeight() * percentage);
            
            String html = "<html><body><h3>" + name + "</h3><img src='" + img.toURI() + "' width='"+ Integer.toString(targetWidth) + "' height='" + Integer.toString(targetHeight) + "'></body></html>";
            return html;
        }
        
        return null;
    }
    
}
