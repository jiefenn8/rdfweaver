package io.github.jiefenn8.rdfweaver.output;

import org.apache.jena.rdf.model.Model;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

/**
 * This interface defines the base methods that manages the output of
 * {@link Model} type RDF graph into persistent filesystem or database.
 */
public interface RDFOutput {

    /**
     * Saves the given RDF {@code Model} graph into an FileSystem or Database
     * implementation for persistent storage.
     *
     * @param model the RDF graph to save
     * @throws IOException if any IO error occur for FileSystem implementation
     */
    void save(@NonNull Model model) throws IOException;
}
