package com.github.jiefenn8.rdfweaver.server;

import com.github.jiefenn8.rdfweaver.r2rml.R2RMLOption;
import com.zaxxer.hikari.pool.HikariPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * This class handles the user input of server related commands and parameters to
 * establish a connection to desired database.
 */
@Command(name = "server",
        exitCodeOnInvalidInput = 22,
        exitCodeOnExecutionException = 24,
        subcommands = {R2RMLOption.class},
        description = "Server command in handling the connection to relational database to map.")
public class ServerOption implements Callable<RelationalSource> {

    private static final Logger LOGGER = LogManager.getLogger(ServerOption.class);
    private static final String DRIVER_DESC = "Driver type to connect to specific database vendor.";
    private static final String HOST_DESC = "IP address or host name to connect to relational database.";
    private static final String DB_DESC = "Name of database to use (if multiple instances within host.";
    private static final String PORT_DESC = "Port that the relational database is listening to.";
    private static final String USER_DESC = "User/login to access the database as.";
    private static final String PASS_DESC = "Password to authenticate access with given user/login.";
    private final RelationalSource.Builder rdbSourceBuilder;
    @Option(names = {"-d", "--driver"}, required = true, description = DRIVER_DESC)
    private JDBCDriver driver;
    @Option(names = {"-h", "--host"}, required = true, description = HOST_DESC)
    private InetAddress host;
    @Option(names = {"-db", "--database"}, defaultValue = "", description = DB_DESC)
    private String database;
    @Option(names = {"-p", "--port"}, required = true, description = PORT_DESC)
    private int port;
    @Option(names = {"-u", "--user"}, required = true, description = USER_DESC)
    private String user;
    @Option(names = {"-pw", "--pass"}, required = true, description = PASS_DESC, interactive = true)
    private char[] pass;
    @Spec private CommandSpec spec;

    /**
     * Constructs a {@code ServerOption} instance with default
     * {@link RelationalSource} builder.
     */
    public ServerOption() {
        rdbSourceBuilder = new RelationalSource.Builder();
    }

    /**
     * Constructs a {@code ServerOption} instance with specified
     * {@link RelationalSource} builder.
     *
     * @param rdbSourceBuilder the builder to build {@code RelationalSource}
     */
    protected ServerOption(RelationalSource.Builder rdbSourceBuilder) {
        this.rdbSourceBuilder = rdbSourceBuilder;
    }

    @Override
    public RelationalSource call() {
        CommandLine cmd = spec.commandLine();
        cmd.setCaseInsensitiveEnumValuesAllowed(false);
        try {
            LOGGER.info("Connecting to database...");
            RelationalSource source = rdbSourceBuilder.newInstance()
                    .serverHost(driver, host, port)
                    .credential(user, pass)
                    .database(database)
                    .build();
            LOGGER.info("Connection to database '{}:{}' as '{}' established.", host.getHostName(), port, user);
            return source;
        } catch (HikariPool.PoolInitializationException ex) {
            String msg = "Server exception occurred during connection attempt.";
            Throwable cause = ex.getCause();
            if (cause instanceof SQLException) {
                SQLException sqlException = (SQLException) cause;
                msg = handleSqlException(sqlException);
            }
            LOGGER.fatal(msg, ex);
            cmd.getOut().println(msg);
            throw new ExecutionException(cmd, msg, ex);
        } catch (Exception ex) {
            String msg = "Unhandled exception occurred during server command execution. Aborting.";
            LOGGER.fatal(msg, ex);
            cmd.getOut().println(msg);
            throw new ExecutionException(cmd, msg, ex);
        } finally {
            Arrays.fill(pass, '*');
        }
    }

    /**
     * Handle given SQL code and return appropriate error message of the cause.
     *
     * @param ex the SQLException containing the SQL code of the driver
     * @return error message of the cause
     */
    private String handleSqlException(SQLException ex) {
        String msg;
        int code = ex.getErrorCode();
        switch (code) {
            case 18456:
                msg = "Server error: Login for '" + user + "' failed to access database.";
                break;
            case 0:
            default:
                msg = handleSqlState(ex);
                break;
        }
        return msg;
    }

    /**
     * Handle given SQLSTATE and return appropriate error message if there is no
     * vendor code to identify the cause.
     *
     * @param ex the SQLException containing the SQLSTATE of the driver
     * @return error message of the cause
     */
    private String handleSqlState(SQLException ex) {
        String msg;
        String state = ex.getSQLState();
        switch (state) {
            case "08S01": {
                msg = "Server error: " + driver.toString() + " host " + host.getHostName() + ", port " + port
                        + " couldn't be reached.";
                break;
            }
            default:
                msg = "Network error: SQLSTATE: " + ex.getSQLState() + " SQLCODE: " + ex.getErrorCode();
                break;
        }
        return msg;
    }

}
