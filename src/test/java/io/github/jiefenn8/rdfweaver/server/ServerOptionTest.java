package io.github.jiefenn8.rdfweaver.server;

import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.pool.HikariPool;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@link ServerOption}.
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

    /**
     * The values of the mock database connection that the test much match to be
     * considered a valid connection.
     */
    private static final String TEST_DRIVER = "MSSQL";
    private static final String TEST_HOST = "localhost";
    private static final String TEST_PORT = "1433";
    private static final String TEST_USER = "sa";
    private static final String TEST_PASS = "YourStrong@Passw0rd";

    /**
     * The Database properties id strings.
     */
    private static final String DRIVER_PROP = "dataSourceClassName";
    private static final String HOST_PROP = "dataSource.serverName";
    private static final String DRIVER_TYPE_PROP = "dataSource.driverType";
    private static final String USER_PROP = "dataSource.user";
    private static final String PASS_PROP = "dataSource.password";
    private static final String PORT_PROP = "dataSource.portNumber";

    /**
     * The immutable list of required options to run this command. This test class
     * will use this list to create an array with arguments that will be executed
     * on the command line.
     * <p>
     * Note: When adding new options, ensure that any test parameter methods with
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

    /**
     * The immutable list of extra options that can be used with this command.
     * This test class will use this list to create an array with arguments that can
     * be executed with the required options on the command line.
     * <p>
     * Note: When adding new options, ensure that any test parameter methods with
     * related test values are updated and aligned in the exact order of this list.
     * Any extra values not matching the size of this list will be ignored.
     */
    private static final List<String> options = ImmutableList.of();

    @Rule public final MockitoRule mockitoRule = MockitoJUnit.rule();
    private final PrintStream consoleOut = System.out;
    private final PrintStream consoleErr = System.err;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final Throwable stubException = new Throwable();
    @Mock private RelationalSource mockRelationalSource;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private RelationalSource.Builder mockRelationalSourceBuilder;
    private CommandLine commandLine;
    private Properties expectedConfig;

    @Before
    public void setUp() {
        out.reset();
        err.reset();
        System.setErr(new PrintStream(err));
        System.setOut(new PrintStream(out));

        ServerOption serverOption = new ServerOption(mockRelationalSourceBuilder);
        commandLine = new CommandLine(serverOption);

        expectedConfig = new Properties();
        expectedConfig.setProperty(DRIVER_PROP, JDBCDriver.valueOf(TEST_DRIVER).getClassName());
        expectedConfig.setProperty(HOST_PROP, TEST_HOST);
        expectedConfig.setProperty(PORT_PROP, TEST_PORT);
        expectedConfig.setProperty(USER_PROP, TEST_USER);
        expectedConfig.setProperty(PASS_PROP, TEST_PASS);
    }

    public String[] createExecutableArguments(List<String> required) {
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
    public void GivenInvalidOptionChoice_WhenExecute_ThenReturnCode_22() {
        String args = "--notaoption=foo";
        int result = commandLine.execute(args);

        assertThat(result, is(22));
    }

    public List<List<String>> invalidRequiredOptionParams() {
        return ImmutableList.of(
                ImmutableList.of("ORACLE", TEST_HOST, TEST_PORT, TEST_USER, TEST_PASS),
                ImmutableList.of(TEST_DRIVER, "192.168.1.234", TEST_PORT, TEST_USER, TEST_PASS),
                ImmutableList.of(TEST_DRIVER, TEST_PORT, "44", TEST_USER, TEST_PASS),
                ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, "invalid_user", TEST_PASS),
                ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, TEST_USER, "invalid_pass")
        );
    }

    @Test
    @Parameters(method = "invalidRequiredOptionParams")
    public void GivenInvalidRequiredOptionParams_WhenExecute_ThenReturnCode24(List<String> params) {
        String[] args = createExecutableArguments(params);
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenAnswer((i) -> {
            Properties config = new Properties();
            config.setProperty(DRIVER_PROP, JDBCDriver.valueOf(params.get(0)).getClassName());
            config.setProperty(HOST_PROP, params.get(1));
            config.setProperty(PORT_PROP, params.get(2));
            config.setProperty(USER_PROP, params.get(3));
            config.setProperty(PASS_PROP, params.get(4));
            if (!config.equals(expectedConfig)) {
                throw new HikariPool.PoolInitializationException(stubException);
            }
            return mockRelationalSource;
        });

        int result = commandLine.execute(args);
        assertThat(result, is(24));
    }

    @Test
    public void GivenInvalidDriverParam_WhenExecute_ThenPrintErrorMessage() {
        String driver = "MYSQL";
        List<String> parameters = ImmutableList.of(driver, TEST_HOST, TEST_PORT, TEST_USER, TEST_PASS);
        String[] args = createExecutableArguments(parameters);
        String expected = "Server error: " + driver + " host " + TEST_HOST + ", port " + TEST_PORT
                + " couldn't be reached." + EOL;
        Exception exception = new HikariPool.PoolInitializationException(new SQLException("", "08S01", 0));
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenThrow(exception);

        commandLine.execute(args);
        String result = out.toString();
        assertThat(result, equalTo(expected));
    }

    @Test
    public void GivenInvalidHostUrlParam_WhenExecute_ThenPrintErrorMessage() {
        String host = "192.168.1.234";
        List<String> parameters = ImmutableList.of(TEST_DRIVER, host, TEST_PORT, TEST_USER, TEST_PASS);
        String[] args = createExecutableArguments(parameters);
        String expected = "Server error: " + TEST_DRIVER + " host " + host + ", port " + TEST_PORT
                + " couldn't be reached." + EOL;
        Exception exception = new HikariPool.PoolInitializationException(new SQLException("", "08S01", 0));
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenThrow(exception);

        commandLine.execute(args);
        String result = out.toString();
        assertThat(result, equalTo(expected));
    }

    @Test
    @Parameters({"-1", "65536"})
    public void GivenInvalidHostPortParam_WhenExecute_ThenPrintErrorMessage(String port) {
        List<String> parameters = ImmutableList.of(TEST_DRIVER, TEST_HOST, port, TEST_USER, TEST_PORT);
        String[] args = createExecutableArguments(parameters);
        String expected = "Server error: " + TEST_DRIVER + " host " + TEST_HOST + ", port " + port
                + " couldn't be reached." + EOL;
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenAnswer((i) -> {
            int value = Integer.parseInt(port);
            if (value < 0 || value > 65535) {
                throw new HikariPool.PoolInitializationException(new SQLException("", "08S01", 0));
            }
            return mockRelationalSource;
        });

        commandLine.execute(args);
        String result = out.toString();
        assertThat(result, equalTo(expected));
    }

    @Test
    public void GivenInvalidLoginUserParam_WhenExecute_ThenPrintErrorMessage() {
        String user = "invalid_user";
        List<String> parameters = ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, user, TEST_PASS);
        String[] args = createExecutableArguments(parameters);
        String expected = "Server error: Login for '" + user + "' failed to access database." + EOL;
        Exception exception = new HikariPool.PoolInitializationException(new SQLException("", "S0001", 18456));
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenThrow(exception);

        commandLine.execute(args);
        String result = out.toString();
        assertThat(result, equalTo(expected));
    }

    @Test
    public void GivenInvalidLoginPassParam_WhenExecute_ThenPrintErrorMessage() {
        String pass = "invalid_pass";
        List<String> parameters = ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, TEST_USER, pass);
        String[] args = createExecutableArguments(parameters);
        String expected = "Server error: Login for '" + TEST_USER + "' failed to access database." + EOL;
        Exception exception = new HikariPool.PoolInitializationException(new SQLException("", "S0001", 18456));
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenThrow(exception);

        commandLine.execute(args);
        String result = out.toString();
        assertThat(result, equalTo(expected));
    }

    @Test
    public void GivenUnhandledSQLError_WhenExecute_ThenPrintErrorMessage() {
        List<String> parameters = ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, TEST_USER, TEST_PASS);
        String[] args = createExecutableArguments(parameters);
        String expected = "Network error: SQLSTATE: XXXX SQLCODE: 0" + EOL;
        Exception exception = new HikariPool.PoolInitializationException(new SQLException("", "XXXX", 0));
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenThrow(exception);

        commandLine.execute(args);
        String result = out.toString();
        assertThat(result, equalTo(expected));
    }

    @Test
    public void GivenUnhandledRuntimeLError_WhenExecute_ThenPrintErrorMessage() {
        List<String> parameters = ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, TEST_USER, TEST_PASS);
        String[] args = createExecutableArguments(parameters);
        String expected = "Unhandled exception occurred during server command execution. Aborting." + EOL;
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenThrow(RuntimeException.class);

        commandLine.execute(args);
        String result = out.toString();
        assertThat(result, equalTo(expected));
    }

    @Test
    @Parameters(method = "invalidRequiredOptionParams")
    public void GivenInvalidServerParams_WhenGetExecutionResult_ThenReturnNull(List<String> params) {
        String[] args = createExecutableArguments(params);
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenAnswer((i) -> {
            Properties config = new Properties();
            config.setProperty(DRIVER_PROP, JDBCDriver.valueOf(params.get(0)).getClassName());
            config.setProperty(HOST_PROP, params.get(1));
            config.setProperty(PORT_PROP, params.get(2));
            config.setProperty(USER_PROP, params.get(3));
            config.setProperty(PASS_PROP, params.get(4));
            if (!config.equals(expectedConfig)) {
                throw new HikariPool.PoolInitializationException(stubException);
            }
            return mockRelationalSource;
        });

        commandLine.execute(args);
        RelationalSource result = commandLine.getExecutionResult();
        assertThat(result, is(nullValue()));
    }

    public List<List<String>> validServerParams() {
        return ImmutableList.of(
                ImmutableList.of(TEST_DRIVER, TEST_HOST, TEST_PORT, TEST_USER, TEST_PASS)
        );
    }

    @Test
    @Parameters(method = "validServerParams")
    public void GivenValidServerParams_WhenExecute_ThenReturnCode0(List<String> params) {
        String[] args = createExecutableArguments(params);
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenAnswer((i) -> {
            Properties config = new Properties();
            config.setProperty(DRIVER_PROP, JDBCDriver.valueOf(params.get(0)).getClassName());
            config.setProperty(HOST_PROP, params.get(1));
            config.setProperty(PORT_PROP, params.get(2));
            config.setProperty(USER_PROP, params.get(3));
            config.setProperty(PASS_PROP, params.get(4));
            if (!config.equals(expectedConfig)) {
                throw new HikariPool.PoolInitializationException(stubException);
            }
            return mockRelationalSource;
        });

        int result = commandLine.execute(args);
        assertThat(result, is(0));
    }

    @Test
    @Parameters(method = "validServerParams")
    public void GivenValidServerParams_WhenGetExecutionResult_ThenReturnRDBSource(List<String> params) {
        String[] args = createExecutableArguments(params);
        when(mockRelationalSourceBuilder.newInstance()
                .serverHost(any(JDBCDriver.class), any(InetAddress.class), anyInt())
                .credential(anyString(), any(char[].class))
                .database(any())
                .build()
        ).thenAnswer((i) -> {
            Properties config = new Properties();
            config.setProperty(DRIVER_PROP, JDBCDriver.valueOf(params.get(0)).getClassName());
            config.setProperty(HOST_PROP, params.get(1));
            config.setProperty(PORT_PROP, params.get(2));
            config.setProperty(USER_PROP, params.get(3));
            config.setProperty(PASS_PROP, params.get(4));
            if (!config.equals(expectedConfig)) {
                throw new HikariPool.PoolInitializationException(stubException);
            }
            return mockRelationalSource;
        });

        commandLine.execute(args);
        RelationalSource result = commandLine.getExecutionResult();
        assertThat(result, is(notNullValue()));
    }
}
