package hu.distributeddocumentor.model.virtual.builders.docxml;

import hu.distributeddocumentor.model.virtual.WikiWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;


public class NamespaceDoc {

    private final String name;
    private final String fullName;
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
    
    public NamespaceDoc(NamespaceDoc parent, String name) {
        
        this.name = name;                
        this.fullName = parent == null ? name : parent.getAsPrefix() + name;
        this.parent = parent;
        
        childNamespaces = new HashMap<>();
        childClasses = new HashMap<>();
    }
    
    public NamespaceDoc addChildNamespace(String childName) {
        
        NamespaceDoc child = new NamespaceDoc(this, childName);
        childNamespaces.put(childName, child);
        return child;
    }
    
    public ClassDoc addChildClass(String childName) {
        
        ClassDoc child = new ClassDoc(this, childName);
        childClasses.put(childName, child);
        return child;
    }
    
    public NamespaceDoc buildSubtree(String childName) {
        
        String[] parts = childName.split("\\.");
        
        NamespaceDoc newChild;
        if (childNamespaces.containsKey(parts[0]))
            newChild = childNamespaces.get(parts[0]);
        else
            newChild = addChildNamespace(parts[0]);
        
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

    void writeBullet(WikiWriter writer, int level) throws IOException {
        
        writer.beginBullet(level);
        writer.text(name);
        writer.text("\n");
        
        for (NamespaceDoc child : childNamespaces.values()) {
            child.writeBullet(writer, level+1);
        }
    }
}
