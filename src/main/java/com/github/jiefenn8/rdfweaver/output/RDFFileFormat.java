package com.github.jiefenn8.rdfweaver.output;

import org.apache.jena.riot.RDFFormat;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Enum class that contains supported {@code RDFFormat} constants mapped to
 * their own enum.
 * <p>
 * This class maps defined {@code RDFFormat} constants in this class to their
 * own enum equivalent; to be used when their original constants usage is not
 * desired.
 */
public enum RDFFileFormat {

    /**
     * List of all supported  RDF file format below.
     */
    TURTLE(RDFFormat.TURTLE),
    NTRIPLES(RDFFormat.NT),
    NQUADS(RDFFormat.NQUADS),
    TRIG(RDFFormat.TRIG),
    JSONLD(RDFFormat.JSONLD),
    RDFXML(RDFFormat.RDFXML),
    RDFJSON(RDFFormat.RDFJSON);

    private final RDFFormat format;

    /**
     * Constructs an instance of this enum that is mapped to a specified
     * {@link RDFFormat}.
     */
    RDFFileFormat(@NonNull RDFFormat format) {
        this.format = format;
    }

    /**
     * Returns the {@code RDFFormat} constant that is mapped to this enum.
     *
     * @return the format constant mapped of this enum
     */
    public RDFFormat getFormat() {
        return format;
    }
}