package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NamespaceDoc extends DocItem {
    
    private final static Logger log = LoggerFactory.getLogger(NamespaceDoc.class);

    private final String name;
    private final String fullName;
    private final String pageId;
    private final NamespaceDoc parent;
    private final Map<String, NamespaceDoc> childNamespaces;
    private final Map<String, ClassDoc> childClasses;   

    public Map<String, ClassDoc> getChildClasses() {
        return childClasses;
    }

    public Map<String, NamespaceDoc> getChildNamespaces() {
        return childNamespaces;
    }

    public String getFullName() {
        return fullName;
    }
    
    public String getAsPrefix() {
        return fullName + ".";
    }

    public String getName() {
        return name;
    }

    public NamespaceDoc getParent() {
        return parent;
    }

    public String getPageId() {
        return pageId;
    }
            
    public NamespaceDoc(NamespaceDoc parent, String name, String pageId, Function<String, String> idGenerator) {
        
        super(idGenerator);
        
        this.name = name;                
        this.fullName = parent == null ? name : parent.getAsPrefix() + name;
        this.parent = parent;
        this.pageId = pageId;        
        
        childNamespaces = new HashMap<>();
        childClasses = new HashMap<>();
    }
    
    public NamespaceDoc addChildNamespace(String childName, String pageId) {
        
        NamespaceDoc child = new NamespaceDoc(this, childName, pageId, idGenerator);
        childNamespaces.put(childName, child);
        return child;
    }
    
    public ClassDoc addChildClass(String childName, String pageId) {
        
        ClassDoc child = new ClassDoc(this, childName, pageId, idGenerator);
        childClasses.put(childName, child);
        return child;
    }
    
    public SortedSet<NamespaceDoc> getSortedNamespaces() {
        
        // TODO: cache
        
        SortedSet<NamespaceDoc> set = new TreeSet<>(
                new Comparator<NamespaceDoc>() {

                    @Override
                    public int compare(NamespaceDoc o1, NamespaceDoc o2) {
                        return o1.getFullName().compareTo(o2.getFullName());
            }});
        
        for (NamespaceDoc cl : childNamespaces.values()) {
            set.add(cl);
        }
        
        return set;
    }
    
    public SortedSet<ClassDoc> getSortedClasses() {
        
        // TODO: cache
        
        SortedSet<ClassDoc> set = new TreeSet<>(
                new Comparator<ClassDoc>() {

                    @Override
                    public int compare(ClassDoc o1, ClassDoc o2) {
                        return o1.getName().compareTo(o2.getName());
            }});
        
        for (ClassDoc cl : childClasses.values()) {
            set.add(cl);
        }
        
        return set;
    }
    
    public NamespaceDoc buildSubtree(String childName) {
        
        String[] parts = childName.split("\\.");
        
        NamespaceDoc newChild;
        if (childNamespaces.containsKey(parts[0])) {
            newChild = childNamespaces.get(parts[0]);
        }
        else {
            String newFullPath = getAsPrefix()+parts[0];
            String newId = idGenerator.apply(newFullPath);
            
            newChild = addChildNamespace(parts[0], newId);
        }
        
        if (parts.length == 1) {
            
            return newChild;
        }
        else {                        
            String[] subParts = new String[parts.length - 1];
            for (int i = 1; i < parts.length; i++) {
                subParts[i-1] = parts[i];
            }
            
            return newChild.buildSubtree(StringUtils.join(subParts, '.'));
        }
    }

    public void writeBullet(WikiWriter writer, int level) throws IOException {
        
        writer.beginBullet(level);
        writer.internalLink(pageId, name);
        writer.text("\n");
        
        for (NamespaceDoc child : getSortedNamespaces()) {
            child.writeBullet(writer, level+1);
        }
    }
    
    public void renderPage(final WikiWriter writer) {                
        
        try {
            writer.heading(1, getFullName()); 
            writer.newParagraph();
            
            writer.internalLink(getParent().getPageId(), "Parent namespace: " + getParent().getFullName());
            writer.newParagraph();
            
            if (getChildNamespaces().size() > 0) {
            
                writer.heading(2, "Namespaces");
                writer.text("This namespace contains the following ");
                writer.italic("sub-namespaces");
                writer.text(":\n");
                writer.newParagraph();

                for (NamespaceDoc child : getSortedNamespaces()) {
                    child.writeBullet(writer, 1);
                }
                writer.newParagraph();
            }
            
            if (getChildClasses().size() > 0) {
                writer.heading(2, "Classes");
                writer.text("This namespace contains the following ");            
                writer.italic("classes");
                writer.text(":\n");
                writer.newParagraph();

                for (ClassDoc cl : getSortedClasses()) {
                    cl.writeBullet(writer, 1);
                }
                writer.newParagraph();
            }
        }
        catch (IOException ex) {
            log.error("Failed to render namespace page: " + getFullName() + " because of: " + ex.getMessage());
            // TODO: write exception to output
        }            
    }    
}
