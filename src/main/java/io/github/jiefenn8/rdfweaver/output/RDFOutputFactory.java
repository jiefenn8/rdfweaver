package io.github.jiefenn8.rdfweaver.output;

import org.apache.jena.riot.RDFFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;

/**
 * This class handles the preparation and instantiation of implementations using
 * {@link RDFOutput}.
 */
public class RDFOutputFactory {

    private static final Logger LOGGER = LogManager.getLogger(RDFOutputFactory.class);

    /**
     * Constructs a {@code RDFFileSystem} instance with the specified
     * destination, filename and format.
     *
     * @param dir    the output destination of the file
     * @param name   the string name of the filename
     * @param format the RDF format to output data as
     * @return the file to output RDF result into
     * @throws IOException if an I/O error occurs or the parent directory
     *                     does not exist
     */
    protected RDFFileSystem createFileSystem(@NonNull Path dir, @NonNull String name, @NonNull RDFFormat format) throws IOException {
        LOGGER.debug("Creating RDFFileSystem for output.");
        Path filePath = FileResolver.resolveFilename(dir, name);
        RDFFileSystem rdfOutput = new RDFFileSystem(filePath, format);
        FileResolver.prepareDir(dir);
        LOGGER.debug(rdfOutput);
        return rdfOutput;
    }

    /**
     * Constructs a {@code FusekiTDBRemote.Builder} instance with the specified
     * host, port and graph base name.
     *
     * @param host     the remote address to the Fuseki database
     * @param port     the port to access database
     * @param baseName the base name of the graph
     * @return the builder instance with the provided parameters
     */
    protected FusekiTDBRemote.Builder createFusekiBuilder(@NonNull InetAddress host, int port, @NonNull String baseName) {
        return new FusekiTDBRemote.Builder(host, port, baseName);
    }
}
