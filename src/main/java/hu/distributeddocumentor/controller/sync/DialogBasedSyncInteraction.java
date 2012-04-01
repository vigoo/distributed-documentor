package hu.distributeddocumentor.controller.sync;

import hu.distributeddocumentor.gui.*;
import hu.distributeddocumentor.model.Documentation;
import java.io.IOException;
import javax.swing.JOptionPane;


public class DialogBasedSyncInteraction implements SyncInteraction {

    private final PageEditorHost host;
    private final Documentation doc;

    public DialogBasedSyncInteraction(PageEditorHost host, Documentation doc) {
        this.host = host;
        this.doc = doc;
    }
        
    
    @Override
    public CommitOrRevert askCommitOrRevert() {
        
        
        String commitS = "Commit";
        String revertS = "Revert";
        String cancelS = "Cancel";
        
        int result = JOptionPane.showOptionDialog(host.getMainFrame(),
                                                 "There are uncommitted changes. What do you do?",
                                                 "Warning",
                                                 JOptionPane.DEFAULT_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 new Object[] { commitS, revertS, cancelS },        
                                                 commitS);            
        
        switch (result) {
            case 0:
                return CommitOrRevert.Commit;
            case 1:
                return CommitOrRevert.Revert;
            default:
                return CommitOrRevert.Cancel;
        }                 
    }

    @Override
    public boolean showCommitDialog() {
    
        CommitDialog dlg = new CommitDialog(host.getMainFrame(), doc);
        dlg.setVisible(true);
        
        return true; // TODO: cancel support
    }

    @Override
    public boolean showRevertDialog() {
        
        RevertDialog dlg = new RevertDialog(host.getMainFrame(), doc, host);
        dlg.setVisible(true);
        
        return true; // TODO: cancel support
    }

    @Override
    public boolean showPullDialog(RepositoryQuery query, RepositorySynchronizer syncer) {
        
        SyncDialog dlg = new SyncDialog(host.getMainFrame(), query.incomingChangesets(), false);
        dlg.setVisible(true);
        
        if (dlg.getReturnStatus() == SyncDialog.RET_OK) {
            try {
                syncer.pull();            
                return true;
            }
            catch (IOException ex) {
                
                ErrorDialog.show(host.getMainFrame(), "Failed to pull changes", ex);
                return false;
            }
        } else {
            return false;
        }        
    }

    @Override
    public boolean askMerge() {
        
        return JOptionPane.showConfirmDialog(host.getMainFrame(), 
                                     "The remote and local changes has to be merged.", 
                                     "Merging is required", 
                                     JOptionPane.OK_CANCEL_OPTION,
                                     JOptionPane.PLAIN_MESSAGE, null)
                == JOptionPane.OK_OPTION;                                             
    }

    @Override
    public boolean showPushDialog(RepositoryQuery query, RepositorySynchronizer syncer) {
        
        SyncDialog dlg = new SyncDialog(host.getMainFrame(), query.outgoingChangesets(), true);
        dlg.setVisible(true);
        
        if (dlg.getReturnStatus() == SyncDialog.RET_OK) {
            try {
                syncer.push();
                
                return true;
            }
            catch (IOException ex) {
                
                ErrorDialog.show(host.getMainFrame(), "Failed to push changes", ex);
                return false;
            }
        } else {
            return false;
        }        
    }

}
