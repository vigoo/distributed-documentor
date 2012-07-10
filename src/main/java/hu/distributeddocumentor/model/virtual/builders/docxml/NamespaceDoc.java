package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;
import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.IOException;
import java.util.*;
import org.apache.commons.lang3.StringUtils;


public class NamespaceDoc {

    private final String name;
    private final String fullName;
    private final String pageId;
    private final NamespaceDoc parent;
    private final Map<String, NamespaceDoc> childNamespaces;
    private final Map<String, ClassDoc> childClasses;
    private final Function<String, String> idGenerator;
    

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
        
        this.name = name;                
        this.fullName = parent == null ? name : parent.getAsPrefix() + name;
        this.parent = parent;
        this.pageId = pageId;
        this.idGenerator = idGenerator;
        
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
        
        for (NamespaceDoc cl : childNamespaces.values())
            set.add(cl);
        
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
        
        for (ClassDoc cl : childClasses.values())
            set.add(cl);
        
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
            for (int i = 1; i < parts.length; i++)
                subParts[i-1] = parts[i];
            
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
}
