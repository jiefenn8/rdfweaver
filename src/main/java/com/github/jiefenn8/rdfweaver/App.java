package com.github.jiefenn8.rdfweaver;

import com.github.jiefenn8.graphloom.api.ConfigMaps;
import com.github.jiefenn8.graphloom.api.InputSource;
import com.github.jiefenn8.graphloom.rdf.RDFMapper;
import com.github.jiefenn8.rdfweaver.output.RDFOutput;
import com.github.jiefenn8.rdfweaver.server.ServerOption;
import org.apache.jena.rdf.model.Model;
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

@Command(mixinStandardHelpOptions = true,
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
     * Checks that the required subcommands is given as argument. Throws an
     * exception if the requirements are not met.
     */
    @Override
    public void run() {
        LOGGER.debug("Checking for all required commands before mapping execution.");
        cmd = spec.commandLine();
        if (!hasRequiredCommands()) {
            throw new ParameterException(cmd, "server, r2rml, output subcommands required.");
        }
    }

    /**
     * Main execution method of the mapping program.
     *
     * @param cmd  the root command line to start program with
     * @param args the arguments to execute the program with
     * @return status code of the execution
     */
    public int start(@NonNull CommandLine cmd, @NonNull String... args) {
        LOGGER.info("Starting RDFWeaver...");
        cmd.addSubcommand(serverOption);
        cmd.setExecutionStrategy(new RunAll());
        int code = processResults(cmd.execute(args));
        LOGGER.info("End of process reached. Exiting with code {}.", code);
        return code;
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
            LOGGER.debug("Execution code {} is not 0. Aborting results processing.", code);
            return code;
        }

        collectAllSubResults(cmd).forEach((obj) -> {
            if (obj instanceof InputSource) {
                inputSource = ((InputSource) obj);
                LOGGER.debug("InputSource result found and set for mapping usage.");
            }
            if (obj instanceof ConfigMaps) {
                configMaps = ((ConfigMaps) obj);
                LOGGER.debug("ConfigMaps result found and set for mapping usage.");
            }
            if (obj instanceof RDFOutput) {
                rdfOutput = ((RDFOutput) obj);
                LOGGER.debug("RDFOutput result found and set for mapping usage.");
            }
        });

        try {
            LOGGER.info("Starting RDF mapping process.");
            Model result = rdfMapper.mapToGraph(inputSource, configMaps);
            LOGGER.info("RDF model generated from mapping, total triples: " + result.size());
            rdfOutput.save(result);
            LOGGER.info("RDF model successfully serialised to output.");
            return spec.exitCodeOnSuccess();
        } catch (IOException ex) {
            LOGGER.fatal("Error while outputting RDF results.", ex);
            return spec.exitCodeOnExecutionException();
        }
    }

    /**
     * Recursively collect all results from top commands and any nested
     * subcommands that has been executed.
     *
     * @param cmd the root command to start collection from
     * @return list of object containing all the command results
     */
    private List<Object> collectAllSubResults(@NonNull CommandLine cmd) {
        LOGGER.debug("Collecting results from command executions.");
        List<Object> resultList = new ArrayList<>();
        ParseResult parseResult = cmd.getParseResult();
        if (parseResult.hasSubcommand()) {
            CommandLine subCommandLine = parseResult.subcommand()
                    .commandSpec()
                    .commandLine();
            resultList.add(subCommandLine.getExecutionResult());
            resultList.addAll(collectAllSubResults(subCommandLine));
            LOGGER.debug("Result collected from '{}' subcommand.", subCommandLine.getCommandName());
        }
        return resultList;
    }
}
