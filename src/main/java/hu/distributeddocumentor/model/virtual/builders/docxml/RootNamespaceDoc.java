package hu.distributeddocumentor.model.virtual.builders.docxml;


public class RootNamespaceDoc extends NamespaceDoc {

    public RootNamespaceDoc() {
        super(null, "");
    }

    @Override
    public String getAsPrefix() {
        return "";
    }
    
    
}
