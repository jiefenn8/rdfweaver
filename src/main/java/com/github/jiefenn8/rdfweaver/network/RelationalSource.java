package com.github.jiefenn8.rdfweaver.network;

import com.github.jiefenn8.graphloom.api.*;
import com.github.jiefenn8.graphloom.api.SourceConfig.PayloadType;
import com.github.jiefenn8.graphloom.rdf.r2rml.DatabaseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements {@link InputSource} and handles the retrieval of data
 * from relational data source.
 */
public class RelationalSource implements InputSource {

    private static final Logger LOGGER = LogManager.getLogger(RelationalSource.class);
    private static final int MAX_BATCH_ROWS = 1000;
    private static final int SLEEP_DURATION = 6000;
    private static final int TIMEOUT_DURATION = 60000;
    private final Map<SourceConfig, Map<Integer, EntityRecord>> sourceConfigMap = new HashMap<>();
    private final DataSource dataSource;
    private final String database;

    /**
     * Constructs a {@code RelationalSource} instance with the specified Builder
     * containing the properties to populate and initialise this instance.
     *
     * @param builder the builder to construct this instance
     */
    private RelationalSource(@NonNull Builder builder) {
        database = builder.database;
        dataSource = builder.dataSource;
    }

    /**
     * Register any new {@link SourceConfig} and map to its own entity record
     * collection; And initialise the retrieval of data specified by the SQL
     * query in the {@code SourceConfig}.
     */
    private void initRetrieval(@NonNull SourceConfig sourceConfig) {
        sourceConfigMap.put(sourceConfig, new HashMap<>());
        EntityRecordRetrievalTask task = new EntityRecordRetrievalTask(sourceConfig);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(task);
    }

    /**
     * Initialise and run a thread to get all the SQL query data into batches
     * while returning the specified batch requested.
     */
    @Override
    public EntityRecord getEntityRecord(@NonNull SourceConfig sourceConfig, int batchId) {
        LOGGER.info("Searching for records of batch " + batchId);
        if (!sourceConfigMap.containsKey(sourceConfig)) {
            LOGGER.info("Source not found, starting retrieval task.");
            initRetrieval(sourceConfig);
        }
        return waitForBatch(sourceConfigMap.get(sourceConfig), batchId);
    }

    /**
     * Put the main thread to sleep until it the batch wanted is available, else
     * reach the {@code TIMEOUT_DURATION} and abort the program.
     */
    private EntityRecord waitForBatch(@NonNull Map<Integer, EntityRecord> entityStore, int batchId) {
        int threadDuration = 0;
        while (threadDuration < TIMEOUT_DURATION) {
            if (entityStore.containsKey(batchId)) {
                LOGGER.info("Batch " + batchId + " received.");
                return entityStore.get(batchId);
            }
            threadDuration += sleepAndUpdate();
        }
        //Temp handling
        LOGGER.debug("Timed out waiting for response or batch does not exist");
        throw new RuntimeException();
    }

    /**
     * Take and nap and return the duration of the nap afterward.
     */
    private int sleepAndUpdate() {
        long start = System.nanoTime();
        long end;
        try {
            Thread.sleep(SLEEP_DURATION);
            end = System.nanoTime();
        } catch (InterruptedException ie) {
            end = System.nanoTime();
            LOGGER.debug("Thread sleep interrupted.", ie);
        }
        long elapsed = (end - start) / 1000000;
        return Math.toIntExact(elapsed);
    }

    @Override
    public int calculateNumOfBatches(@NonNull SourceConfig sourceConfig) {
        try (Connection conn = dataSource.getConnection()) {
            if (!database.isEmpty()) {
                conn.setCatalog(database);
            }
            try (Statement stmt = conn.createStatement()) {
                String query = prepareCountQuery(sourceConfig);
                return calculateResults(stmt, query);
            }
        } catch (SQLException ex) {
            //Temp handling
            LOGGER.debug("RuntimeException thrown while calculating number of batches.");
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the count results and calculate the number of batch needed for a
     * whole result set.
     */
    private int calculateResults(Statement stmt, String query) throws SQLException {
        try (ResultSet results = stmt.executeQuery(query)) {
            results.next();
            int tableSize = results.getInt("total_rows");
            double maxBatch = ((double) tableSize / MAX_BATCH_ROWS);
            return ((int) Math.ceil(maxBatch));
        }
    }

    /**
     * Alter and prepare the query to count the results instead.
     */
    private String prepareCountQuery(@NonNull SourceConfig config) {
        String query = config.getPayload();
        PayloadType type = config.getPayloadType();
        if (type.equals(DatabaseType.TABLE_NAME)) {
            query = "SELECT COUNT(*) total_rows FROM " + query;
        } else if (type.equals(DatabaseType.QUERY)) {
            query = query.replaceAll(";", "");
            query = "SELECT COUNT(*) total_rows FROM (" + query + ") as t1";
        }
        return SQLHelper.delimitQueryIdentifiers(query);
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

    /**
     * This class contains the methods required to retrieve query data in the
     * background during main thread execution.
     */
    public class EntityRecordRetrievalTask implements Callable<Integer> {

        private final SourceConfig sourceConfig;

        /**
         * Constructs an {@code EntityRecordRetrievalTask} instance with the
         * specified {@link SourceConfig}.
         */
        public EntityRecordRetrievalTask(@NonNull SourceConfig sourceConfig) {
            this.sourceConfig = sourceConfig;
        }

        /**
         * Start retrieval of SQL results from connection.
         */
        private void startRetrieval(@NonNull Connection conn) throws SQLException {
            if (!database.isEmpty()) {
                conn.setCatalog(database);
            }
            try (Statement stmt = conn.createStatement()) {
                String query = prepareSelectQuery();
                handleResults(stmt, query);
            }
        }

        /**
         * Additional preparation of query for use.
         */
        private String prepareSelectQuery() {
            String query = sourceConfig.getPayload();
            PayloadType type = sourceConfig.getPayloadType();
            if (type.equals(DatabaseType.TABLE_NAME)) {
                query = "SELECT * FROM " + query;
            }
            return SQLHelper.delimitQueryIdentifiers(query);
        }

        /**
         * Get results and handle the data.
         */
        private void handleResults(@NonNull Statement stmt, @NonNull String query) throws SQLException {
            try (ResultSet results = stmt.executeQuery(query)) {
                int id = 0;
                ResultSetMetaData meta = results.getMetaData();
                MutableEntityRecord entityBatch = new MutableEntityRecord();
                Map<Integer, EntityRecord> entityBatches = sourceConfigMap.get(sourceConfig);
                while (results.next()) {
                    entityBatch.addRecord(buildRecord(meta, results));
                    if (entityBatch.size() >= MAX_BATCH_ROWS) {
                        entityBatches.put(id, entityBatch);
                        id++;
                    }
                }
                entityBatches.put(id, entityBatch);
            }
        }

        /**
         * Create and return a {@code MutableRecord} containing data of one row.
         */
        private MutableRecord buildRecord(@NonNull ResultSetMetaData meta, @NonNull ResultSet results) throws SQLException {
            int columnCount = meta.getColumnCount();
            String column = meta.getColumnName(1);
            String value = results.getString(1);
            MutableRecord record = new MutableRecord(column, value);
            for (int i = 1; i <= columnCount; i++) {
                record.addProperty(meta.getColumnName(i), results.getString(i));
            }
            return record;
        }

        /**
         * Start retrieval execution of source data.
         *
         * @return exit code 0 if successful, otherwise -1
         */
        @Override
        public Integer call() {
            LOGGER.traceEntry("Thread starting retrieval of data.");
            try (Connection conn = dataSource.getConnection()) {
                conn.setCatalog(database);
                startRetrieval(conn);
                return LOGGER.traceExit("Thread finished retrieving data.", 0);
            } catch (SQLException ex) {
                LOGGER.error("SQLException occurred during data retrieval", ex);
                return LOGGER.traceExit("Aborting thread.", -1);
            }
        }
    }
}
