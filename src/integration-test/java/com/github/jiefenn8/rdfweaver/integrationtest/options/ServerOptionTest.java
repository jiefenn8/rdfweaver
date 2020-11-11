package com.github.jiefenn8.rdfweaver.integrationtest.options;

import com.github.jiefenn8.graphloom.api.InputSource;
import com.github.jiefenn8.rdfweaver.network.JDBCDriver;
import com.github.jiefenn8.rdfweaver.options.ServerOption;
import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import junitparams.JUnitParamsRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import picocli.CommandLine;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test class for {@link ServerOption}.
 */
@RunWith(JUnitParamsRunner.class)
public class ServerOptionTest {

    private static final String EOL = System.getProperty("line.separator");
    private static final String DELIMITER = "=";

    /**
     * The available options to use for this command.
     */
    private static final String DRIVER_OPTION = "--driver";
    private static final String HOST_OPTION = "--host";
    private static final String PORT_OPTION = "--port";
    private static final String USER_OPTION = "--user";
    private static final String PASS_OPTION = "--pass";
    private static final String DATABASE_OPTION = "--database";

    /**
     * The values of the mock database connection that the test much match to be
     * considered a valid connection.
     */
    private static final String TEST_DRIVER = JDBCDriver.MSSQL.toString();
    private static final String TEST_HOST = "localhost";
    private static final String TEST_PORT = "1433";
    private static final String TEST_USER = "sa";
    private static final String TEST_PASS = "YourStrong@Passw0rd";

    /**
     * List of required options needed to run this command.
     * <p>
     * Note: When adding new options, ensure that any test values aligns parameter methods with
     * related test values are updated and aligned in the exact order of this list.
     * Any extra value not matching the size of this list will be ignored.
     */
    private static final List<String> REQUIRED_OPTIONS = ImmutableList.of(
            DRIVER_OPTION,
            HOST_OPTION,
            PORT_OPTION,
            USER_OPTION,
            PASS_OPTION
    );

    private final PrintStream consoleOut = System.out;
    private final PrintStream consoleErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private CommandLine commandLine;

    @Before
    public void setUp() {
        out.reset();
        err.reset();
        System.setErr(new PrintStream(err));
        System.setOut(new PrintStream(out));

        ServerOption serverOption = new ServerOption();
        commandLine = new CommandLine(serverOption);
    }

    private String[] createExecutableArguments(List<String> required) {
        int size = REQUIRED_OPTIONS.size();
        String[] args = new String[size];
        for (int i = 0; i < size; i++) {
            args[i] = REQUIRED_OPTIONS.get(i) + DELIMITER + required.get(i);
        }
        return args;
    }

    @After
    public void tearDown() {
        System.setOut(consoleOut);
        System.setErr(consoleErr);
    }

    @Test
    public void GivenInvalidHostParams_WhenExecute_ThenPrintErrorMessage() {
        String host = "192.168.1.234";
        List<String> parameters = ImmutableList.of(TEST_DRIVER, host, TEST_PORT, TEST_USER, TEST_PASS);
        String[] args = createExecutableArguments(parameters);
        String expected = "Server error: " + TEST_DRIVER + " host " + host + ", port " + TEST_PORT
                + " couldn't be reached." + EOL;

        commandLine.execute(args);
        String result = out.toString();
        assertThat(result, is(containsString(expected)));
    }

    @Test
    public void GivenInvalidLoginParams_WhenExecute_ThenPrintErrorMessage() {
        String user = "invalid_user";
        List<String> parameters = ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, user, TEST_PASS);
        String[] args = createExecutableArguments(parameters);
        String expected = "Server error: Login for '" + user + "' failed to access database." + EOL;

        commandLine.execute(args);
        String result = out.toString();
        assertThat(result, is(containsString(expected)));
    }

    @Test
    public void GivenInvalidServerParams_WhenExecute_ThenReturnNull() {
        List<String> parameters = ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, "invalid_user", TEST_PASS);
        String[] args = createExecutableArguments(parameters);

        commandLine.execute(args);
        InputSource result = commandLine.getExecutionResult();
        assertThat(result, is(nullValue()));
    }

    @Test
    public void GivenValidServerParams_WhenExecute_ThenReturnRDBSource() {
        List<String> parameters = ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, TEST_USER, TEST_PASS);
        String[] args = createExecutableArguments(parameters);

        commandLine.execute(args);
        InputSource result = commandLine.getExecutionResult();
        assertThat(result, is(notNullValue()));
    }

    @Ignore
    @Test
    public void IntegrationDatabaseDirectTest() {
        Properties config = new Properties();
        config.setProperty("dataSourceClassName", JDBCDriver.valueOf(TEST_DRIVER).getClassName());
        config.setProperty("dataSource.serverName", TEST_HOST);
        config.setProperty("dataSource.portNumber", TEST_PORT);
        config.setProperty("dataSource.user", TEST_USER);
        config.setProperty("dataSource.password", TEST_PASS);
        HikariConfig serverConfig = new HikariConfig(config);
        serverConfig.setMaximumPoolSize(5);
        serverConfig.setConnectionTimeout(10000);
        DataSource result = new HikariDataSource(serverConfig);
        assertThat(result, is(notNullValue()));
    }
}
