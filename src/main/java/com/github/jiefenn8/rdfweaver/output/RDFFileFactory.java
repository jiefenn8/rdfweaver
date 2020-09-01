package com.github.jiefenn8.rdfweaver.output;

import org.apache.jena.riot.RDFFormat;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;

/**
 * This class handles the preparation and instantiation of {@code RDFFile}.
 */
public class RDFFileFactory {

    /**
     * Constructs a {@code RDFFile} instance with the specified destination
     * {@code Path} and {@code RDFFormat}.
     *
     * @param path   the output destination of the file
     * @param format the RDF format to output data as
     * @return the file to output RDF result into
     */
    protected RDFFile createFile(@NonNull Path path, @NonNull RDFFormat format) {
        return new RDFFile(path, format);
    }
}
