package options;

import com.github.jiefenn8.graphloom.rdf.parser.R2RMLBuilder;
import com.github.jiefenn8.graphloom.rdf.r2rml.R2RMLMap;
import com.google.common.collect.ImmutableList;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

    public List<String> invalidR2RMLPathParameters(){
        return ImmutableList.of("", "mock_r2rml.ttl", "/invalid_path/mock_r2rml.ttl");
    }

    public List<String> validR2RMLPathParameters(){
        return ImmutableList.of("/r2rml/empty_r2rml.ttl", "/r2rml/valid_r2rml.ttl");
    }

    @Test
    @Parameters(method = "invalidR2RMLPathParameters")
    public void GivenInvalidR2RMLFile_WhenGetR2RMLMap_ThenReturnNull(String value) {
        //String path = getClass().getResource(value).getPath();
        String args = String.format("--file=%s", value);

        commandLine.execute(args);
        R2RMLMap result = commandLine.getExecutionResult();

        assertThat(result, is(nullValue()));
    }

    @Test
    @Parameters(method = "validR2RMLPathParameters")
    public void GivenValidR2RMLFile_WhenGetR2RMLMap_ThenReturnValidR2RMLMap(String value) {
        String path = getClass().getResource(value).getPath();
        String args = String.format("--file=%s", path);

        commandLine.execute(args);
        R2RMLMap result = commandLine.getExecutionResult();

        assertThat(result, is(notNullValue()));
    }

}
