package io.github.jiefenn8.rdfweaver.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * This class handles the base configuration and creation of {@link DataSource} using
 * connection pooling for SQL databases.
 */
public class DataSourceFactory {

    private static final int POOL_SIZE = 5;
    private static final int TIMEOUT_DURATION = 10000;

    /**
     * Returns a DataSource with a established connection with a database with the
     * given server configuration or throw an exception if the configuration is not
     * valid or the database could not be reached.
     *
     * @param config the configuration to establish connection
     * @return the data source of a successful database connection
     * @throws PoolInitializationException if configuration given to data source is
     *                                     invalid or connection could not be established
     */
    protected DataSource getDataSource(@NonNull Properties config) {
        HikariConfig serverConfig = new HikariConfig(config);
        serverConfig.setMaximumPoolSize(POOL_SIZE);
        serverConfig.setConnectionTimeout(TIMEOUT_DURATION);
        return new HikariDataSource(serverConfig);
    }
}
