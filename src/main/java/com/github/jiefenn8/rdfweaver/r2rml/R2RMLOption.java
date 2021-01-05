package com.github.jiefenn8.rdfweaver.r2rml;

import com.github.jiefenn8.graphloom.rdf.parser.R2RMLBuilder;
import com.github.jiefenn8.graphloom.rdf.r2rml.R2RMLMap;
import com.github.jiefenn8.rdfweaver.output.OutputOption;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.n3.turtle.TurtleParseException;
import org.apache.jena.shared.NotFoundException;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.File;
import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

@Command(name = "r2rml",
        exitCodeOnInvalidInput = 12,
        exitCodeOnExecutionException = 14,
        subcommands = {OutputOption.class},
        description = "Load a R2RML file to configure the mapping of data to RDF.")
public class R2RMLOption implements Callable<R2RMLMap> {

    private final R2RMLBuilder builder;
    @Option(names = {"-f", "--file"}, required = true, description = "R2RML config file.")
    private final File r2rmlFile = new File(StringUtils.EMPTY);
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
        String path = r2rmlFile.getPath();
        try {
            if (!r2rmlFile.isFile()) {
                String message = String.format("%s not found.", path);
                throw new NotFoundException(message);
            }
            return builder.parse(r2rmlFile.getPath());
        } catch (NotFoundException e) {
            String message = String.format("R2RML file '%s' is not valid filename or path.", path);
            cmd.getErr().println(message);
            throw new ExecutionException(cmd, message, e);
        } catch (TurtleParseException e) {
            String message = String.format("R2RML file '%s' does not contain a valid R2RML map.", path);
            cmd.getErr().println(message);
            throw new ExecutionException(cmd, message, e);
        }
    }
}
