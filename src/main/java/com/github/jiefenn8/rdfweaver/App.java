package com.github.jiefenn8.rdfweaver;

import com.github.jiefenn8.graphloom.rdf.parser.R2RMLBuilder;
import com.github.jiefenn8.graphloom.rdf.r2rml.R2RMLMap;
import com.github.jiefenn8.rdfweaver.options.R2RMLOption;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

@Command(name = "rdfweaver",
        mixinStandardHelpOptions = true,
        synopsisSubcommandLabel = "COMMAND",
        subcommands = {R2RMLOption.class},
        version = "0.1.0")
public class App implements Runnable {

    @Spec private CommandSpec spec;
    private R2RMLBuilder builder;
    private R2RMLMap r2rmlMap;

    public static void main(String... args) {
    }

    @Override
    public void run() {
        ParseResult parseResult = spec.commandLine().getParseResult();

        if (parseResult.hasSubcommand()) {
            for (ParseResult pr : parseResult.subcommands()) {
                pr.commandSpec().commandLine().getCommandName();
            }
        }
    }
}
