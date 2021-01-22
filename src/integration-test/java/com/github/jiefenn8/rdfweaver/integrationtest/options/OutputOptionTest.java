package com.github.jiefenn8.rdfweaver.integrationtest.options;

import com.github.jiefenn8.rdfweaver.output.OutputOption;
import com.github.jiefenn8.rdfweaver.output.RDFFileFormat;
import com.github.jiefenn8.rdfweaver.output.RDFFileSystem;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.jena.riot.RDFFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test class for {@code OutputOption}.
 */
@RunWith(JUnitParamsRunner.class)
public class OutputOptionTest {

    private static final String OUTPUT_DIR = "rdfweaver-test-";
    private static final String DELIMITER = "=";
    private CommandLine commandLine;

    @Before
    public void setUp() {
        OutputOption outputOption = new OutputOption();
        commandLine = new CommandLine(outputOption);
    }

    @Test
    public void GivenNoParams_WhenExecute_ThenReturnDefaultRDFFile() {
        commandLine.execute();
        RDFFileSystem result = commandLine.getExecutionResult();
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void GivenPathParam_WhenExecute_ThenReturnRDFFileWithExpectedPath() throws Exception {
        Path testDirectory = Files.createTempDirectory(OUTPUT_DIR);
        String[] args = new String[]{"--dir" + DELIMITER + testDirectory};

        commandLine.execute(args);
        RDFFileSystem file = commandLine.getExecutionResult();
        String result = file.getParent();
        assertThat(result, is(equalTo(testDirectory.toString())));
    }

    public List<String> validFormatParameters() {
        return Arrays.asList("TURTLE", "NTRIPLES", "NQUADS", "TRIG", "JSONLD", "RDFXML", "RDFJSON");
    }

    @Test
    @Parameters(method = "validFormatParameters")
    public void GivenRDFFormatParam_WhenExecute_ThenReturnRDFFileWithExpectedFormat(String value) {
        String[] args = new String[]{"--format" + DELIMITER + value};

        commandLine.execute(args);
        RDFFileSystem file = commandLine.getExecutionResult();
        RDFFormat result = file.getFormat();
        RDFFormat expected = RDFFileFormat.valueOf(value).getFormat();
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void GivenFilenameParam_WhenExecute_ThenReturnRDFFileWithExpectedFilename() {
        String filename = "filename.nt";
        String[] args = new String[]{"--file" + DELIMITER + filename};

        commandLine.execute(args);
        RDFFileSystem file = commandLine.getExecutionResult();
        String result = file.getName();
        assertThat(result, is(equalTo(filename)));
    }
}
