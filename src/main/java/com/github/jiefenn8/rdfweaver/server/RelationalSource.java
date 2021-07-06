package com.github.jiefenn8.rdfweaver.server;

import io.github.jiefenn8.graphloom.api.EntityReference;
import io.github.jiefenn8.graphloom.api.InputSource;
import io.github.jiefenn8.graphloom.api.inputsource.EntityResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * This class implements {@link InputSource} and handles the retrieval of data
 * from relational data source.
 */
public class RelationalSource implements InputSource {

    private static final Logger LOGGER = LogManager.getLogger(RelationalSource.class);
    private final DataSource dataSource;
    private final String database;

    /**
     * Constructs a {@code RelationalSource} instance with the specified Builder
     * containing the properties to populate and initialise this instance.
     *
     * @param builder the builder to construct this instance
     */
    protected RelationalSource(@NonNull Builder builder) {
        database = builder.database;
        dataSource = builder.dataSource;
    }

    @Override
    public void executeEntityQuery(EntityReference entityReference, Consumer<EntityResult> action) {
        try (Connection conn = dataSource.getConnection()) {
            LOGGER.debug("Starting retrieval task for records.");
            conn.setCatalog(database);
            if (!database.isEmpty()) {
                LOGGER.debug("Database property found. Setting catalog as {}.", database);
                conn.setCatalog(database);
            }
            try (Statement stmt = conn.createStatement()) {
                String query = SQLHelper.prepareQuery(entityReference);
                handleResults(stmt, query, action);
            }
            LOGGER.debug("Finished retrieving all data.");
        } catch (SQLException ex) {
            LOGGER.error("SQLException occurred during data retrieval.", ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get results and handle the data.
     */
    private void handleResults(@NonNull Statement stmt, @NonNull String query, Consumer<EntityResult> action) throws SQLException {
        try (ResultSet results = stmt.executeQuery(query)) {
            LOGGER.debug("Applying actions to query results.");
            EntityResult entityResult = new SQLAdapter(results);
            action.accept(entityResult);
        }
    }


    /**
     * Builder class for {@link RelationalSource}.
     */
    public static class Builder {

        private final DataSourceFactory dataSourceFactory;
        private DataSource dataSource;
        private String database;

        /**
         * Constructs a {@code Builder} with default {@link DataSourceFactory}.
         */
        public Builder() {
            this.dataSourceFactory = new DataSourceFactory();
        }

        /**
         * Constructs a {@code Builder} with specified {@link DataSourceFactory}.
         *
         * @param dataSourceFactory the factory to create {@link DataSource}
         */
        public Builder(@NonNull DataSourceFactory dataSourceFactory) {
            this.dataSourceFactory = dataSourceFactory;
        }

        /**
         * Returns an step builder instance to start configuring the builder to
         * build the instance.
         *
         * @return the step builder to start building instance
         */
        public ServerConfig newInstance() {
            return new InstanceConfig(this);
        }

        /**
         * Step building interface for base methods that will handle the server
         * configurations on builder.
         */
        public interface ServerConfig {
            ServerConfig serverHost(@NonNull JDBCDriver driver, @NonNull InetAddress host, int port);

            OptionalConfig credential(@NonNull String user, char[] pass);
        }

        /**
         * Step building interface for base methods that will handle optional
         * configurations on builder.
         */
        public interface OptionalConfig extends BuildConfig {
            OptionalConfig database(@NonNull String name);
        }

        /**
         * Step building interface for base methods that will handle the final
         * configurations and building of instance.
         */
        public interface BuildConfig {

            /**
             * Returns a {@code RelationalSource} instance containing the properties
             * given to its builder.
             *
             * @return instance created with the properties in this builder
             */
            RelationalSource build();
        }

        public static class InstanceConfig implements ServerConfig, OptionalConfig {

            private final Builder builder;
            private final Properties serverConfig = new Properties();
            private String database;

            /**
             * Constructs an {@link InstanceConfig} step builder with its parent
             * builder instance.
             *
             * @param builder the parent that created this instance
             */
            private InstanceConfig(@NonNull Builder builder) {
                this.builder = builder;
            }

            @Override
            public ServerConfig serverHost(@NonNull JDBCDriver driver, @NonNull InetAddress host, int port) {
                serverConfig.setProperty("dataSourceClassName", driver.getClassName());
                serverConfig.setProperty("dataSource.serverName", host.getHostName());
                serverConfig.setProperty("dataSource.portNumber", String.valueOf(port));
                return this;
            }

            @Override
            public OptionalConfig database(@NonNull String name) {
                database = name;
                return this;
            }

            @Override
            public OptionalConfig credential(@NonNull String user, char[] pass) {
                serverConfig.setProperty("dataSource.user", user);
                serverConfig.setProperty("dataSource.password", String.valueOf(pass));
                return this;
            }

            @Override
            public RelationalSource build() {
                builder.dataSource = builder.dataSourceFactory.getDataSource(serverConfig);
                builder.database = database;
                return new RelationalSource(builder);
            }
        }
    }
}
