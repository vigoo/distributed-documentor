package hu.distributeddocumentor.model.virtual.builders.docxml;

import com.google.common.base.Function;


public class RootNamespaceDoc extends NamespaceDoc {

    public RootNamespaceDoc(String pageId, Function<String, String> idGenerator) {
        super(null, "", pageId, idGenerator);
    }

    @Override
    public String getAsPrefix() {
        return "";
    }
    
    
}
