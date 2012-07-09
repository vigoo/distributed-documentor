package hu.distributeddocumentor.model.virtual.builders.docxml;


public class ClassDoc {

    private final String name;
    private final NamespaceDoc parent;

    public String getName() {
        return name;
    }

    public NamespaceDoc getParent() {
        return parent;
    }   
    
    public ClassDoc(NamespaceDoc parent, String name) {
        this.name = name;
        this.parent = parent;
    }        
}
