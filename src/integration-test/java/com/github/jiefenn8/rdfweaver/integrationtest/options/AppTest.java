package com.github.jiefenn8.rdfweaver.integrationtest.options;

import com.github.jiefenn8.rdfweaver.App;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test class for {@code App}.
 */
public class AppTest {

    private Path expectedOutput;

    @Before
    public void setUp() {
        expectedOutput = Paths.get("output/rdfOutput.nt").toAbsolutePath();
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(expectedOutput);
    }

    @Test
    public void GivenRequiredParams_WhenExecute_ThenGenerateRDFFile() {
        String driver = "--driver=MSSQL";
        String host = "--host=127.0.0.1";
        String port = "--port=1433";
        String database = "--database=testDb";
        String user = "--user=sa";
        String pass = "--pass=YourStrong@Passw0rd";
        String r2rmlFile = getClass().getResource("/r2rml/valid_r2rml.ttl").getPath();
        String r2rml = "--file=" + r2rmlFile;
        String[] args = new String[]{"server", driver, database, host, port, user, pass, "r2rml", r2rml, "output"};

        App.main(args);
        boolean result = Files.exists(expectedOutput);
        assertThat(result, is(true));
    }
}
