package hu.distributeddocumentor.vcs.mercurial;

import com.aragost.javahg.Repository;
import com.aragost.javahg.RepositoryConfiguration;
import com.aragost.javahg.commands.*;
import com.google.inject.Inject;
import hu.distributeddocumentor.model.Change;
import hu.distributeddocumentor.prefs.DocumentorPreferences;
import hu.distributeddocumentor.vcs.VersionControl;
import java.io.File;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mercurial specific implementation of the VersionControl interface, using JavaHG
 * 
 * @author Daniel Vigovszky
 */
public class MercurialVersionControl implements VersionControl {

    private static final Logger log = LoggerFactory.getLogger(MercurialVersionControl.class.getName());
    
    private final DocumentorPreferences prefs;
    private Repository repository;
    
    @Inject
    public MercurialVersionControl(DocumentorPreferences prefs) {
        this.prefs = prefs;
    }
    
    @Override
    public File getRoot() {
        return repository.getDirectory();
    }

    @Override
    public void create(File repositoryRoot) {
        repository = Repository.create(createRepositoryConfiguration(), repositoryRoot);
    }

    @Override
    public void add(File item) {                
        AddCommand add = new AddCommand(repository);
        add.execute(item);
    }

    @Override
    public void remove(File item, boolean force) {
        RemoveCommand remove = new RemoveCommand(repository);
        
        if (force)
            remove.force();
        
        remove.execute(item);
    }
        
    @Override
    public void remove(File[] items, boolean force, boolean after) {
        RemoveCommand remove = new RemoveCommand(repository);
        
        if (after)
            remove = remove.after();
        if (force)
            remove = remove.force();        
        
        remove.execute(items);
    }
            
    @Override
    public void remove(String[] items, boolean force, boolean after) {
        RemoveCommand remove = new RemoveCommand(repository);
        
        if (after)
            remove = remove.after();
        if (force)
            remove = remove.force();        
        
        remove.execute(items);
    }
    
    @Override
    public void open(File repositoryRoot) {
        repository = Repository.open(createRepositoryConfiguration(), repositoryRoot);
    }

    @Override
    public void clone(File localRepositoryRoot, String uri) {
        repository = Repository.clone(createRepositoryConfiguration(), localRepositoryRoot, uri);
    }
        
    private RepositoryConfiguration createRepositoryConfiguration() {

        RepositoryConfiguration conf = new RepositoryConfiguration();
        conf.setHgBin(prefs.getMercurialPath());
        conf.setCodingErrorAction(CodingErrorAction.REPLACE);

        return conf;
    }

    @Override
    public void add(File[] items) {
        AddCommand add = new AddCommand(repository);
        add.execute(items);
    }

    @Override
    public boolean hasChanges() {                
        StatusCommand status = new StatusCommand(repository);
        StatusResult result = status.execute();

        return result.getAdded().size() > 0
                || result.getCopied().size() > 0
                || result.getModified().size() > 0
                || result.getRemoved().size() > 0;
    }
       
    @Override
    public Map<String, Change> getChanges() {

        StatusCommand status = new StatusCommand(repository);
        StatusResult result = status.execute();

        Map<String, Change> changes = new HashMap<>();

        for (String change : result.getAdded()) {
            changes.put(change, Change.Added);
        }

        for (String change : result.getCopied().values()) {
            changes.put(change, Change.Copied);
        }

        for (String change : result.getModified()) {
            changes.put(change, Change.Modified);
        }

        for (String change : result.getRemoved()) {
            changes.put(change, Change.Removed);
        }

        return changes;
    }

    @Override
    public void commitChanges(String message, List<String> files) {

        CommitCommand commit = new CommitCommand(repository);
        commit.message(message);
        commit.user(System.getProperty("user.name"));

        String[] items = Arrays.copyOf(files.toArray(), files.size(), String[].class);
        commit.execute(items);
    }

    @Override
    public void revert(String[] items) {
        RevertCommand revert = new RevertCommand(repository);        
        revert.execute(items);
    }
          
    @Override
    public void rename(File source, File target, boolean force) {
        RenameCommand rename = new RenameCommand(repository);
        
        if (force)
            rename = rename.force();
                    
        rename.execute(source, target);
        
        log.debug("Rename return code " + rename.getReturnCode() + ", error message: " + rename.getErrorString());
    }

    @Override
    public Iterable<String> getMissingFiles() {        
        StatusCommand status = new StatusCommand(repository);
        StatusResult result = status.execute();

        return result.getMissing();
    }

    @Override
    public boolean isAdded(File item) {                
        StatusCommand status = new StatusCommand(repository);        
        StatusResult result = status.execute(item);
                
        return result.getUnknown().isEmpty();
    }

    public Repository getRepository() {
        return repository;
    }

}
