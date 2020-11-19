package com.github.jiefenn8.rdfweaver.output;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.jena.riot.RDFFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@code OutputOption}.
 */
@RunWith(JUnitParamsRunner.class)
public class OutputOptionTest {

    private static final String EOL = System.getProperty("line.separator");
    private static final String DELIMITER = "=";
    private static final String OUTPUT_DIR = "rdfweaver-test-";

    private final PrintStream consoleOut = System.out;
    private final PrintStream consoleErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock private RDFFileFactory mockRDFFileFactory;
    private CommandLine commandLine;
    private Path testDir;

    @Before
    public void setUp() throws Exception {
        out.reset();
        err.reset();
        System.setErr(new PrintStream(err));
        System.setOut(new PrintStream(out));

        testDir = Files.createTempDirectory(OUTPUT_DIR);
        OutputOption outputOption = new OutputOption(mockRDFFileFactory);
        commandLine = new CommandLine(outputOption);
    }

    @After
    public void tearDown() {
        System.setOut(consoleOut);
        System.setErr(consoleErr);
    }

    @Test
    public void GivenNoParams_WhenExecute_ThenReturnDefaultRDFFile() {
        when(mockRDFFileFactory.createFile(any(Path.class), any(RDFFormat.class)))
                .thenReturn(mock(RDFFile.class));
        commandLine.execute();
        RDFFile result = commandLine.getExecutionResult();
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void GivenInvalidOptionChoice_WhenExecute_ThenReturnCode_32() {
        String args = "--notaoption" + DELIMITER + "foo";
        int result = commandLine.execute(args);
        assertThat(result, is(32));
    }

    @Test
    public void GivenAnyParams_WhenExecuteTriggerException_ThenReturnCode_34() {
        when(mockRDFFileFactory.createFile(any(Path.class), any(RDFFormat.class)))
                .thenThrow(RuntimeException.class);
        int result = commandLine.execute();
        assertThat(result, is(34));
    }

    @Test
    public void GivenAnyParams_WhenExecuteTriggerException_ThenPrintErrorMessage() {
        String arg = "--dir" + DELIMITER + testDir;
        String expected = "Unhandled exception occurred during runtime. Aborting." + EOL;
        when(mockRDFFileFactory.createFile(any(Path.class), any(RDFFormat.class)))
                .thenThrow(RuntimeException.class);

        commandLine.execute(arg);
        String result = out.toString();
        assertThat(result, equalTo(expected));
    }

    @Test
    public void GivenPathParam_WhenExecute_ThenReturnRDFFileWithExpectedPath() {
        Path testDirectory = Paths.get(OUTPUT_DIR);
        String[] args = new String[]{"--dir" + DELIMITER + testDirectory};
        RDFFile mockRDFFile = mock(RDFFile.class);
        when(mockRDFFileFactory.createFile(any(Path.class), any(RDFFormat.class))).thenReturn(mockRDFFile);
        when(mockRDFFile.getParent()).thenReturn(testDirectory.toString());

        commandLine.execute(args);
        RDFFile result = commandLine.getExecutionResult();
        assertThat(result.getParent(), is(equalTo(testDirectory.toString())));
    }

    public List<String> validFormatParameters(){
        return Arrays.asList("TURTLE", "NTRIPLES", "NQUADS", "TRIG", "JSONLD", "RDFXML", "RDFJSON");
    }

    @Test
    @Parameters(method = "validFormatParameters")
    public void GivenRDFFormatParam_WhenExecute_ThenReturnRDFFileWithExpectedFormat(String value) {
        String[] args = new String[]{"--format" + DELIMITER + value};
        RDFFile mockRDFFile = mock(RDFFile.class);
        RDFFormat expected = RDFFileFormat.valueOf(value).getFormat();
        when(mockRDFFileFactory.createFile(any(Path.class), any(RDFFormat.class))).thenReturn(mockRDFFile);
        when(mockRDFFile.getFormat()).thenReturn(expected);

        commandLine.execute(args);
        RDFFile result = commandLine.getExecutionResult();
        assertThat(result.getFormat(), is(equalTo(expected)));
    }

    @Test
    public void GivenFilenameParam_WhenExecute_ThenReturnRDFFileWithExpectedFilename() {
        String filename = "filename.nt";
        String[] args = new String[]{"--filename" + DELIMITER + filename};
        RDFFile mockRDFFile = mock(RDFFile.class);
        when(mockRDFFile.getName()).thenReturn(filename);
        when(mockRDFFileFactory.createFile(any(Path.class), any(RDFFormat.class))).thenReturn(mockRDFFile);

        commandLine.execute(args);
        RDFFile result = commandLine.getExecutionResult();
        assertThat(result.getName(), is(equalTo(filename)));
    }
}