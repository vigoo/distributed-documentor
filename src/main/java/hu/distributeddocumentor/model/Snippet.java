package hu.distributeddocumentor.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Snippet extends Page {

    public Snippet(File source, SnippetCollection snippets) throws FileNotFoundException, IOException {
        super(source, snippets);
    }

    public Snippet(String id, SnippetCollection snippets) {
        super(id, snippets);
    }   
}
