package com.github.jiefenn8.rdfweaver.r2rml;

import com.github.jiefenn8.rdfweaver.output.OutputOption;
import io.github.jiefenn8.graphloom.rdf.parser.R2RMLBuilder;
import io.github.jiefenn8.graphloom.rdf.r2rml.R2RMLMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.n3.turtle.TurtleParseException;
import org.apache.jena.shared.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "r2rml",
        exitCodeOnInvalidInput = 12,
        exitCodeOnExecutionException = 14,
        subcommands = {OutputOption.class},
        description = "R2RML command in handling the loading of mapping config to mapper.")
public class R2RMLOption implements Callable<R2RMLMap> {

    private static final Logger LOGGER = LogManager.getLogger(R2RMLOption.class);
    private final R2RMLBuilder builder;
    @Option(names = {"-f", "--file"}, required = true, description = "Location and filename of the R2RML file.")
    private final File filename = new File(StringUtils.EMPTY);
    @Spec private CommandSpec spec;

    /**
     * Constructs a {@code R2RMLOption} instance with default
     * {@link R2RMLBuilder}.
     */
    public R2RMLOption() {
        this.builder = new R2RMLBuilder();
    }

    /**
     * Constructs a {@code R2RMLOption} instance with specified
     * {@link R2RMLBuilder}.
     *
     * @param builder the R2RML builder instance to use
     */
    public R2RMLOption(R2RMLBuilder builder) {
        this.builder = builder;
    }

    @Override
    public R2RMLMap call() {
        CommandLine cmd = spec.commandLine();
        String path = filename.getPath();
        try {
            if (!filename.isFile()) {
                String message = String.format("%s not found.", path);
                throw new NotFoundException(message);
            }
            R2RMLMap r2rmlMap = builder.parse(filename.getPath());
            int mapSize = r2rmlMap.getEntityMaps().size();
            LOGGER.info("Loading R2RML file completed, {} TriplesMaps found.", mapSize);
            return r2rmlMap;
        } catch (NotFoundException ex) {
            String msg = String.format("R2RML file '%s' is not valid filename or path.", path);
            cmd.getErr().println(msg);
            LOGGER.fatal(msg, ex);
            throw new ExecutionException(cmd, msg, ex);
        } catch (TurtleParseException ex) {
            String msg = String.format("R2RML file '%s' does not contain a valid R2RML map.", path);
            cmd.getErr().println(msg);
            LOGGER.fatal(msg, ex);
            throw new ExecutionException(cmd, msg, ex);
        }
    }
}
