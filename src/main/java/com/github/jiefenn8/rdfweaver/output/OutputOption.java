package com.github.jiefenn8.rdfweaver.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.IOException;
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
    private static final String DEFAULT_DIR = "output";
    private static final String DIR_DESC = "RDF output directory. (default: ${DEFAULT-VALUE})";
    private static final String DEFAULT_FORMAT = "NTRIPLES";
    private static final String FORMAT_DESC = "RDF output file format. (default: ${DEFAULT-VALUE})";
    private static final String DEFAULT_FILENAME = "rdfOutput.nt";
    private static final String FILENAME_DESC = "RDF output file name. (default: ${DEFAULT-VALUE})";

    private final RDFFileFactory factory;
    @Option(names = {"-d", "--dir"}, defaultValue = DEFAULT_DIR, description = DIR_DESC)
    private File path;
    @Option(names = {"-n", "--filename"}, defaultValue = DEFAULT_FILENAME, description = FILENAME_DESC)
    private String filename;
    @Option(names = {"-f", "--format"}, defaultValue = DEFAULT_FORMAT, description = FORMAT_DESC)
    private RDFFileFormat format;
    @Spec private CommandSpec spec;

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
            Path directoryPath = path.toPath();
            Path filePath = FileResolver.resolveFilename(directoryPath, filename);
            RDFFile file = factory.createFile(filePath, format.getFormat());
            FileResolver.prepareDir(directoryPath);
            return file;
        } catch (IOException ex) {
            String msg = "I/O error: Failed to create directory path: " + path;
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
}
