package com.github.jiefenn8.rdfweaver.r2rml;

import com.github.jiefenn8.graphloom.api.EntityMap;
import com.github.jiefenn8.graphloom.rdf.parser.R2RMLBuilder;
import com.github.jiefenn8.graphloom.rdf.r2rml.R2RMLMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.jena.n3.turtle.TurtleParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@link R2RMLOption}.
 */
@RunWith(JUnitParamsRunner.class)
public class R2MRLOptionTest {

    private static final String EOL = System.getProperty("line.separator");
    private static final String R2RML_PATH = "/r2rml/mock_r2rml.ttl";
    private final PrintStream consoleOut = System.out;
    private final PrintStream consoleErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Rule public ExpectedException expectedException = ExpectedException.none();
    private CommandLine commandLine;
    private File r2rmlFile;
    @Mock private R2RMLBuilder mockBuilder;
    @Mock private R2RMLMap mockMap;

    @Before
    public void setUp() {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        R2RMLOption r2rmlOption = new R2RMLOption(mockBuilder);
        commandLine = new CommandLine(r2rmlOption);
        r2rmlFile = new File(getClass().getResource(R2RML_PATH).getPath());
    }

    @After
    public void tearDown() {
        System.setOut(consoleOut);
        System.setErr(consoleErr);
    }

    public List<File> invalidFilePathParameters() {
        return ImmutableList.of(
                new File(""),
                new File("mock_r2rml.ttl"),
                new File("/invalid_path/mock_r2rml.ttl")
        );
    }

    @Test
    public void GivenInvalidOption_WhenExecute_ThenReturnCode_12() {
        String args = "--notaoption=foo";
        int result = commandLine.execute(args);

        assertThat(result, is(12));
    }

    @Test
    @Parameters(method = "invalidFilePathParameters")
    public void GivenInvalidR2RMLPath_WhenExecute_ThenReturnCode_14(File value) {
        String args = String.format("--file=%s", value.getPath());
        int result = commandLine.execute(args);

        assertThat(result, is(14));
    }

    @Test
    @Parameters(method = "invalidFilePathParameters")
    public void GivenInvalidR2RMLPath_WhenExecute_ThenPrintErrMessage(File value) {
        String args = String.format("--file=%s", value.getPath());
        String expectedMessage = String.format("R2RML file '%s' is not valid filename or path.%s", value, EOL);

        commandLine.execute(args);
        String result = err.toString();

        assertThat(result, containsString(expectedMessage));
    }

    @Test
    @Parameters(method = "invalidFilePathParameters")
    public void GivenInvalidR2RMLPath_WhenExecute_ThenReturnNull(File value) {
        String args = String.format("--file=%s", value.getPath());

        commandLine.execute(args);
        R2RMLMap result = commandLine.getExecutionResult();

        assertThat(result, is(nullValue()));
    }

    @Test
    public void GivenValidR2RMLFile_WhenExecute_ThenReturnCode_0() {
        when(mockBuilder.parse(anyString())).thenReturn(mockMap);
        String value = String.format("--file=%s", r2rmlFile);

        int result = commandLine.execute(value);

        assertThat(result, is(0));
    }

    @Test
    public void GivenValidR2RMLFile_WhenExecute_ThenReturnR2RMLMap() {
        when(mockBuilder.parse(anyString())).thenReturn(mockMap);
        when(mockMap.getEntityMaps()).thenReturn(ImmutableSet.of(mock(EntityMap.class)));
        String value = String.format("--file=%s", r2rmlFile);

        commandLine.execute(value);
        R2RMLMap r2rmlMap = commandLine.getExecutionResult();
        boolean result = r2rmlMap.getEntityMaps().isEmpty();

        assertThat(result, is(not(true)));
    }

    @Test
    public void GivenInvalidR2RMLData_WhenExecute_ThenReturnCode_14() {
        when(mockBuilder.parse(anyString())).thenThrow(TurtleParseException.class);
        String value = String.format("--file=%s", r2rmlFile);

        int result = commandLine.execute(value);

        assertThat(result, is(14));
    }

    @Test
    public void GivenInvalidR2RMLData_WhenExecute_ThenReturnNull() {
        when(mockBuilder.parse(anyString())).thenThrow(TurtleParseException.class);
        String value = String.format("--file=%s", r2rmlFile);

        commandLine.execute(value);
        R2RMLMap result = commandLine.getExecutionResult();

        assertThat(result, is(nullValue()));
    }

    @Test
    public void GivenInvalidR2RMLData_WhenExecute_ThenPrintErrMessage() {
        when(mockBuilder.parse(anyString())).thenThrow(TurtleParseException.class);
        String value = String.format("--file=%s", r2rmlFile);
        String expected = String.format("R2RML file '%s' does not contain a valid R2RML map.%s", r2rmlFile, EOL);

        commandLine.execute(value);
        String result = err.toString();

        assertThat(result, containsString(expected));
    }
}
