package com.github.jiefenn8.rdfweaver.output;

import org.apache.jena.riot.RDFFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.concurrent.Callable;


/**
 * This class handles the user input of output related commands and parameters
 * to prepare all the necessary directory and system to output RDF results.
 */
@Command(name = "output",
        exitCodeOnInvalidInput = 32,
        exitCodeOnExecutionException = 34,
        description = "Output command in handling the output of RDF result to persistent storage.")
public class OutputOption implements Callable<RDFOutput> {

    private static final Logger LOGGER = LogManager.getLogger(OutputOption.class);
    private final RDFOutputFactory rdfOutputFactory;
    @ArgGroup(exclusive = false)
    private final FileSystem fileSystem = new FileSystem();
    @ArgGroup(exclusive = false)
    private FusekiTDB fuseki;
    @Spec
    private CommandSpec spec;

    /**
     * Constructs an {@code OutputOption} instance with default
     * {@link RDFOutputFactory}.
     */
    public OutputOption() {
        this.rdfOutputFactory = new RDFOutputFactory();
    }

    /**
     * Construct any {@code OutputOption} instance with specified
     * {@link RDFOutputFactory}.
     *
     * @param rdfOutputFactory the factory to use
     */
    public OutputOption(@NonNull RDFOutputFactory rdfOutputFactory) {
        this.rdfOutputFactory = rdfOutputFactory;
    }

    @Override
    public RDFOutput call() {
        CommandLine cmd = spec.commandLine();
        cmd.setCaseInsensitiveEnumValuesAllowed(false);
        try {
            RDFOutput output;
            if (fuseki != null) {
                InetAddress host = fuseki.host;
                int port = fuseki.port;
                output = rdfOutputFactory.createFusekiBuilder(host, port, fuseki.baseName)
                        .graphName(fuseki.graphName)
                        .build();
                LOGGER.info("Output as Fuseki remote set to '{}:{}'.", host.getHostName(), port);
                return output;
            }
            Path path = fileSystem.path.toPath();
            String filename = fileSystem.filename;
            RDFFormat format = fileSystem.format.getFormat();
            output = rdfOutputFactory.createFileSystem(path, filename, format);
            LOGGER.info("Output as file set to '{}' path, name '{}', format '{}'.", path, filename, format);
            return output;
        } catch (IOException ex) {
            String msg = "I/O error: Failed to create directory path: " + fileSystem.path.toPath();
            cmd.getOut().println(msg);
            LOGGER.fatal(msg, ex);
            throw new ExecutionException(cmd, msg, ex);
        } catch (Exception ex) {
            String msg = "Unhandled exception occurred during runtime. Aborting.";
            LOGGER.fatal(msg, ex);
            cmd.getOut().println(msg);
            throw new ExecutionException(cmd, msg, ex);
        }
    }

    /**
     * FileSystem related options.
     * <p>
     * This is the default if no other arg group is initialised.
     */
    static class FileSystem {
        private static final String DEFAULT_DIR = "output";
        private static final String DIR_DESC = "Output directory for RDF output. (default: ${DEFAULT-VALUE})";
        private static final String DEFAULT_FORMAT = "NTRIPLES";
        private static final String FORMAT_DESC = "File format to serialise RDF result. (default: ${DEFAULT-VALUE})";
        private static final String DEFAULT_FILENAME = "rdfOutput.nt";
        private static final String FILENAME_DESC = "File name for RDF output. (default: ${DEFAULT-VALUE})";
        @Option(names = {"-d", "--dir"}, defaultValue = DEFAULT_DIR, description = DIR_DESC)
        private File path;
        @Option(names = {"-n", "--filename"}, defaultValue = DEFAULT_FILENAME, description = FILENAME_DESC)
        private String filename;
        @Option(names = {"-f", "--format"}, defaultValue = DEFAULT_FORMAT, description = FORMAT_DESC)
        private RDFFileFormat format;
    }

    /**
     * Fuseki Remote TDB remote options.
     */
    static class FusekiTDB {
        private static final String HOST_DESC = "IP address or host name to connect to Fuseki server.";
        private static final String PORT_DESC = "Port that the Fuseki server is listening to.";
        private static final String BASE_DESC = "Base name (Dataset) that exists on Fuseki that can be used.";
        private static final String GRAPH_DESC = "Graph name to upload the mapped RDF result under.";
        @Option(names = {"-h", "--host"}, required = true, description = HOST_DESC)
        private InetAddress host;
        @Option(names = {"-p", "--port"}, required = true, description = PORT_DESC)
        private int port;
        @Option(names = {"-b", "--baseName"}, required = true, description = BASE_DESC)
        private String baseName;
        @Option(names = {"-g", "--graphName"}, description = GRAPH_DESC)
        private String graphName;
    }
}