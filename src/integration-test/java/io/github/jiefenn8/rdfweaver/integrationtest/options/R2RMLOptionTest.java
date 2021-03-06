package io.github.jiefenn8.rdfweaver.integrationtest.options;

import io.github.jiefenn8.rdfweaver.r2rml.R2RMLOption;
import com.google.common.collect.ImmutableList;
import io.github.jiefenn8.graphloom.rdf.parser.R2RMLBuilder;
import io.github.jiefenn8.graphloom.rdf.r2rml.R2RMLMap;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import picocli.CommandLine;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test class for {@link R2RMLOption}.
 */
@RunWith(JUnitParamsRunner.class)
public class R2RMLOptionTest {

    private R2RMLOption r2rmlOption;
    private CommandLine commandLine;

    @Before
    public void setUp() {
        r2rmlOption = new R2RMLOption(new R2RMLBuilder());
        commandLine = new CommandLine(r2rmlOption);
    }

    public List<String> invalidR2RMLPathParameters() {
        return ImmutableList.of("", "mock_r2rml.ttl", "/invalid_path/mock_r2rml.ttl");
    }

    public List<String> validR2RMLPathParameters() {
        return ImmutableList.of("/r2rml/empty_r2rml.ttl", "/r2rml/valid_r2rml.ttl");
    }

    @Test
    @Parameters(method = "invalidR2RMLPathParameters")
    public void GivenInvalidR2RMLFile_WhenExecute_ThenReturnNull(String value) {
        String args = String.format("--file=%s", value);

        commandLine.execute(args);
        R2RMLMap result = commandLine.getExecutionResult();

        assertThat(result, is(nullValue()));
    }

    @Test
    @Parameters(method = "validR2RMLPathParameters")
    public void GivenValidR2RMLFile_WhenExecute_ThenReturnR2RMLMap(String value) {
        String path = getClass().getResource(value).getPath();
        String args = String.format("--file=%s", path);

        commandLine.execute(args);
        R2RMLMap result = commandLine.getExecutionResult();

        assertThat(result, is(notNullValue()));
    }

}
