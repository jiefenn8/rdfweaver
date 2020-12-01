package com.github.jiefenn8.rdfweaver.output;

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
 * to prepare all the necessary directory and file to output RDF results.
 */
@Command(name = "output",
        exitCodeOnInvalidInput = 32,
        exitCodeOnExecutionException = 34,
        description = "Output of mapped RDF result.")
public class OutputOption implements Callable<RDFFile> {

    private static final Logger LOGGER = LogManager.getLogger(OutputOption.class);
    private final RDFFileFactory factory;
    @ArgGroup
    private final FileSystem fileSystem = new FileSystem();
    @Spec
    private CommandSpec spec;

    public OutputOption() {
        this.factory = new RDFFileFactory();
    }

    public OutputOption(@NonNull RDFFileFactory factory) {
        this.factory = factory;
    }

    @Override
    public RDFFile call() {
        CommandLine cmd = spec.commandLine();
        cmd.setCaseInsensitiveEnumValuesAllowed(false);
        try {
            Path directoryPath = fileSystem.path.toPath();
            Path filePath = FileResolver.resolveFilename(directoryPath, fileSystem.filename);
            RDFFile file = factory.createFile(filePath, fileSystem.format.getFormat());
            FileResolver.prepareDir(directoryPath);
            return file;
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

    static class FileSystem {
        private static final String DEFAULT_DIR = "output";
        private static final String DIR_DESC = "RDF output directory. (default: ${DEFAULT-VALUE})";
        private static final String DEFAULT_FORMAT = "NTRIPLES";
        private static final String FORMAT_DESC = "RDF output file format. (default: ${DEFAULT-VALUE})";
        private static final String DEFAULT_FILENAME = "rdfOutput.nt";
        private static final String FILENAME_DESC = "RDF output file name. (default: ${DEFAULT-VALUE})";
        @Option(names = {"-d", "--dir"}, defaultValue = DEFAULT_DIR, description = DIR_DESC)
        private File path;
        @Option(names = {"-n", "--filename"}, defaultValue = DEFAULT_FILENAME, description = FILENAME_DESC)
        private String filename;
        @Option(names = {"-f", "--format"}, defaultValue = DEFAULT_FORMAT, description = FORMAT_DESC)
        private RDFFileFormat format;
    }

    static class FusekiTDB {
        private static final String HOST_DESC = "Fuseki server host name";
        private static final String PORT_DESC = "Fuseki server port";
        private static final String BASE_DESC = "RDF dataset base name";
        private static final String GRAPH_DESC = "RDF graph name";
        @Option(names = {"-h", "--host"}, required = true, description = HOST_DESC)
        private InetAddress host;
        @Option(names = {"-p", "--port"}, required = true, description = PORT_DESC)
        private int port;
        @Option(names = {"-b", "--baseName"}, description = BASE_DESC)
        private String baseName;
        @Option(names = {"-g", "--graphName"}, description = GRAPH_DESC)
        private String graphName;
    }
}
