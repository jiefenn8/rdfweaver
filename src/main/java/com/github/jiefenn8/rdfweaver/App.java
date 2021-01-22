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
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.RunAll;
import picocli.CommandLine.Spec;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(mixinStandardHelpOptions = true,
        synopsisSubcommandLabel = "COMMAND",
        version = "0.1.0")
public class App implements Callable<Boolean> {

    private static final Logger LOGGER = LogManager.getLogger(App.class);
    private final RDFMapper rdfMapper;
    private final ServerOption serverOption;
    @Spec private CommandSpec spec;
    private CommandLine cmd;

    /**
     * Constructs an App instance with default {@link RDFMapper}.
     */
    public App() {
        cmd = new CommandLine(this);
        rdfMapper = new RDFMapper();
        serverOption = new ServerOption();
    }

    /**
     * Constructs an App instance with specified {@link RDFMapper}.
     *
     * @param rdfMapper    the mapper to use
     * @param serverOption the subcommand to use
     */
    public App(@NonNull RDFMapper rdfMapper, @NonNull ServerOption serverOption) {
        cmd = new CommandLine(this);
        this.rdfMapper = rdfMapper;
        this.serverOption = serverOption;
    }

    /**
     * Checks that the required subcommands is given as argument. Returns false
     * if the requirements are not met.
     *
     * @return false when the required subcommand does not exist
     */
    @Override
    public Boolean call() {
        LOGGER.debug("Checking for all required commands before mapping execution.");
        cmd = spec.commandLine();
        if (hasRequiredSubcommands()) {
            return true;
        }
        LOGGER.info("SERVER, R2RML and OUTPUT subcommands required. Check the README for usage information.");
        recursivelyPrintUsage(cmd);
        return false;
    }

    /**
     * Recursively print the given commandline and its subcommands' usage
     * message.
     *
     * @param cmd the commandline and its subcommand to print usage
     */
    private void recursivelyPrintUsage(CommandLine cmd){
        PrintWriter writer = cmd.getOut();
        cmd.getOut().println();
        LOGGER.info("Printing usage for {}", cmd.getCommandName());
        cmd.usage(writer);
        cmd.getSubcommands().forEach((k,v)->{
            recursivelyPrintUsage(v);
        });
    }

    /**
     * Main execution method of the mapping program.
     *
     * @param args the arguments to execute the program with
     * @return exit code of the program
     */
    public int start(@NonNull String... args) {
        LOGGER.info("Starting RDFWeaver...");
        cmd.addSubcommand(serverOption);
        cmd.setExecutionStrategy(new RunAll());
        int code = cmd.execute(args);
        if (hasMappingPrerequisite(code)) {
            code = processResults(recursiveCollectResults(cmd));
            LOGGER.info("End of mapping process reached.");
        }
        LOGGER.info("Program exited with code {}.", code);
        return code;
    }

    /**
     * Returns true when required subcommands have been given to this
     * application, otherwise return false.
     *
     * @return true if required commands is given, otherwise false
     */
    private boolean hasRequiredSubcommands() {
        List<String> commands = cmd.getParseResult().originalArgs();
        List<String> required = Arrays.asList("server", "r2rml", "output");
        return commands.containsAll(required);
    }

    /**
     * Checks if the command line meets all the prerequisites to begin the
     * mapping process. Returns true if the command line is ready to start
     * the mapping process, otherwise returns false.
     *
     * @param code the exit code of the command execute method
     * @return true if all prerequisite is met, otherwise false
     */
    private boolean hasMappingPrerequisite(int code) {
        //Handling errors during running all commands.
        if (code != spec.exitCodeOnSuccess()) {
            LOGGER.debug("Received error code {} from running all commands.", code);
            return false;
        }
        //If other commands like --version or --help is given.
        boolean hasAnySubcommands = cmd.getParseResult().hasSubcommand();
        if (!hasAnySubcommands) {
            return false;
        }
        //Check if all required commands exists before processing.
        return cmd.getExecutionResult();
    }

    /**
     * Retrieves relevant results for mapping and output of result from the map
     * containing all the results collected from the command line executions.
     * Returns exit code for the program after completion or failure.
     *
     * @return the generated RDF model from mapping
     */
    private int processResults(Map<String, Object> results) {
        LOGGER.info("Starting RDF mapping process.");
        InputSource source = ((InputSource) results.get("server"));
        LOGGER.debug("InputSource result found and set for mapping usage.");
        ConfigMaps config = ((ConfigMaps) results.get("r2rml"));
        LOGGER.debug("ConfigMaps result found and set for mapping usage.");
        RDFOutput rdfOutput = ((RDFOutput) results.get("output"));
        LOGGER.debug("RDFOutput result found and set for output usage.");
        Model rdfModel = initMapping(source, config);
        return outputRDFModel(rdfOutput, rdfModel);
    }

    /**
     * Returns a RDF {@link Model} generated from the mapping of data from the
     * {@link InputSource} using the configurations from {@link ConfigMaps}.
     *
     * @param source the input source for the mapper
     * @param config the config data for the mapper
     * @return the RDF result of mapping the data
     */
    private Model initMapping(InputSource source, ConfigMaps config) {
        LOGGER.debug("ConfigMaps result found and set for mapping usage.");
        Model rdfModel = rdfMapper.mapToGraph(source, config);
        LOGGER.info("RDF model generated from mapping, total triples: " + rdfModel.size());
        return rdfModel;
    }

    /**
     * Outputs the generated RDF model with the given {@link RDFOutput}
     * implementations that will store the RDF model in a persistent state.
     *
     * @param output the implementation that will store the RDF model
     * @param model  the RDF result from the mapping process
     * @return exit code of this method
     */
    private int outputRDFModel(RDFOutput output, Model model) {
        try {
            output.save(model);
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
    private Map<String, Object> recursiveCollectResults(@NonNull CommandLine cmd) {
        LOGGER.debug("Collecting results from command executions.");
        Map<String, Object> resultMap = new HashMap<>();
        ParseResult parseResult = cmd.getParseResult();
        if (parseResult.hasSubcommand()) {
            CommandLine subcommand = parseResult.subcommand()
                    .commandSpec()
                    .commandLine();
            resultMap.put(subcommand.getCommandName(), subcommand.getExecutionResult());
            resultMap.putAll(recursiveCollectResults(subcommand));
            LOGGER.debug("Result collected from '{}' subcommand.", subcommand.getCommandName());
        }
        return resultMap;
    }
}
