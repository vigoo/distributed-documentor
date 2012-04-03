package hu.distributeddocumentor.model;

import java.io.IOException;
import java.util.Collection;

public interface SnippetCollection {

    Collection<Snippet> getSnippets();
    Snippet getSnippet(String id);
    void addSnippet(Snippet snippet) throws IOException, PageAlreadyExistsException;
    void removeSnippet(String id);
}
