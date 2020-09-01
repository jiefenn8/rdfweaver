package com.github.jiefenn8.rdfweaver.output;

import org.apache.jena.riot.RDFFormat;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.nio.file.Path;

/**
 * This class extends the {@code File} class with RDF specific methods.
 */
public class RDFFile extends File {

    private final RDFFormat format;

    /**
     * Constructs a {@code RDFFile} instance with the specified {@code Path}
     * and {@link RDFFormat} of the file.
     */
    protected RDFFile(@NonNull Path path, @NonNull RDFFormat format) {
        super(path.toString());
        this.format = format;
    }

    /**
     * Return this instance as a File type.
     *
     * @return this instance as a File type
     */
    public File asFile() {
        return this;
    }

    /**
     * Returns the {@code RDFFormat} that this file instance is set as.
     *
     * @return the format of this file instance
     */
    public RDFFormat getFormat() {
        return format;
    }
}
