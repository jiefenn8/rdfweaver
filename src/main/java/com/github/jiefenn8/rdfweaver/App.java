package com.github.jiefenn8.rdfweaver;

import com.github.jiefenn8.graphloom.api.ConfigMaps;
import com.github.jiefenn8.graphloom.api.InputSource;
import com.github.jiefenn8.graphloom.rdf.RDFMapper;
import com.github.jiefenn8.rdfweaver.options.ServerOption;
import com.github.jiefenn8.rdfweaver.output.RDFOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import picocli.CommandLine;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Command(name = "rdfweaver",
        mixinStandardHelpOptions = true,
        synopsisSubcommandLabel = "COMMAND",
        version = "0.1.0")
public class App implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private final RDFMapper rdfMapper;
    private final ServerOption serverOption;
    @Spec private CommandSpec spec;
    private CommandLine cmd;
    private InputSource inputSource;
    private ConfigMaps configMaps;
    private RDFOutput rdfOutput;

    /**
     * Constructs an App instance with default {@link RDFMapper}.
     */
    public App() {
        rdfMapper = new RDFMapper();
        serverOption = new ServerOption();
    }

    /**
     * Constructs an App instance with specified {@link RDFMapper}.
     *
     * @param rdfMapper the mapper to use
     */
    public App(@NonNull RDFMapper rdfMapper, @NonNull ServerOption serverOption) {
        this.rdfMapper = rdfMapper;
        this.serverOption = serverOption;
    }

    /**
     * Main program execution entry.
     *
     * @param args the arguments given to app
     */
    public static void main(@NonNull String... args) {
        LOGGER.trace("Entering application.");
        App app = new App();
        int code = app.start(new CommandLine(app), args);
        LOGGER.trace("Exiting application with code " + code + ".");
        LOGGER.traceExit();
    }

    /**
     * Checks that the required subcommands is given as argument. Throws an
     * exception if the requirements are not met.
     */
    @Override
    public void run() {
        cmd = spec.commandLine();
        if (!hasRequiredCommands()) {
            throw new ParameterException(cmd, "server, r2rml, output subcommands required.");
        }
    }

    protected int start(@NonNull CommandLine cmd, @NonNull String... args) {
        cmd.addSubcommand(serverOption);
        cmd.setExecutionStrategy(new RunAll());
        return processResults(cmd.execute(args));
    }

    /**
     * Returns true when required commands have been given to this application,
     * otherwise return false.
     *
     * @return true if required commands is given, otherwise false
     */
    private boolean hasRequiredCommands() {
        List<String> commands = cmd.getParseResult().originalArgs();
        List<String> required = Arrays.asList("server", "r2rml", "output");
        return commands.containsAll(required);
    }

    /**
     * Takes the execution code from command line execution and checks if all
     * the commands have be executed successfully. If so, process relevant
     * results to map input data to a RDF model for output using
     * {@link RDFMapper#mapToGraph(InputSource, ConfigMaps)}.
     *
     * @param code the execution result from application
     * @return the execution code of the result processing
     */
    private int processResults(int code) {
        if (code != 0) {
            return code;
        }

        cmd.getOut().println("Starting RDF mapping process.");
        collectAllSubResults(cmd).forEach((obj) -> {
            if (obj instanceof InputSource) inputSource = ((InputSource) obj);
            if (obj instanceof ConfigMaps) configMaps = ((ConfigMaps) obj);
            if (obj instanceof RDFOutput) rdfOutput = ((RDFOutput) obj);
        });

        try {
            rdfOutput.save(rdfMapper.mapToGraph(inputSource, configMaps));
            cmd.getOut().println("RDF mapping complete. Output: " + rdfOutput);
        } catch (IOException ex) {
            ex.printStackTrace(cmd.getErr());
        }
        return spec.exitCodeOnSuccess();
    }

    /**
     * Recursively collect all results from top commands and any nested
     * subcommands that has been executed.
     *
     * @param cmd the root command to start collection from
     * @return list of object containing all the command results
     */
    private List<Object> collectAllSubResults(@NonNull CommandLine cmd) {
        List<Object> resultList = new ArrayList<>();
        ParseResult parseResult = cmd.getParseResult();
        if (parseResult.hasSubcommand()) {
            CommandLine subCommandLine = parseResult.subcommand()
                    .commandSpec()
                    .commandLine();
            resultList.add(subCommandLine.getExecutionResult());
            resultList.addAll(collectAllSubResults(subCommandLine));
        }
        return resultList;
    }
}
