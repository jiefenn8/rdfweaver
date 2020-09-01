package com.github.jiefenn8.rdfweaver.integrationtest.options;

import com.github.jiefenn8.rdfweaver.output.OutputOption;
import com.github.jiefenn8.rdfweaver.output.RDFFile;
import com.github.jiefenn8.rdfweaver.output.RDFFileFormat;
import org.apache.jena.riot.RDFFormat;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test class for {@code OutputOption}.
 */
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
        RDFFile result = commandLine.getExecutionResult();
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void GivenPathParam_WhenExecute_ThenReturnRDFFileWithExpectedPath() throws Exception {
        Path testDirectory = Files.createTempDirectory(OUTPUT_DIR);
        String[] args = new String[]{"--dir" + DELIMITER + testDirectory};

        commandLine.execute(args);
        RDFFile file = commandLine.getExecutionResult();
        String result = file.getParent();
        assertThat(result, is(equalTo(testDirectory.toString())));
    }

    @Test
    public void GivenRDFFormatParam_WhenExecute_ThenReturnRDFFileWithExpectedFormat() {
        String format = "NTRIPLES";
        String[] args = new String[]{"--format" + DELIMITER + format};

        commandLine.execute(args);
        RDFFile file = commandLine.getExecutionResult();
        RDFFormat result = file.getFormat();
        RDFFormat expectedFormat = RDFFileFormat.valueOf(format).getFormat();
        assertThat(result, is(equalTo(expectedFormat)));
    }

    @Test
    public void GivenFilenameParam_WhenExecute_ThenReturnRDFFileWithExpectedFilename() {
        String filename = "filename.nt";
        String[] args = new String[]{"--filename" + DELIMITER + filename};

        commandLine.execute(args);
        RDFFile file = commandLine.getExecutionResult();
        String result = file.getName();
        assertThat(result, is(equalTo(filename)));
    }
}
