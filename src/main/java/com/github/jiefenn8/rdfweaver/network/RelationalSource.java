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
    private final Map<Integer, EntityRecord> entityRecordMap = new HashMap<>();
    private final DataSource dataSource;
    private final String database;
    private int batchCount;
    private boolean retrievalStarted = false;
    private boolean timedOut = false;

    /**
     * Constructs a {@code RelationalSource} instance with the specified Builder
     * containing the properties to populate and initialise this instance.
     *
     * @param builder the builder to construct this instance
     */
    private RelationalSource(Builder builder) {
        database = builder.database;
        dataSource = builder.dataSource;
    }

    /**
     * Initialise the retrieval of data specified by the SQL query in
     * {@link SourceConfig}.
     */
    private void initRetrieval(SourceConfig sourceConfig) {
        EntityRecordRetrievalTask errt = new EntityRecordRetrievalTask(sourceConfig);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(errt);
    }

    /**
     * Initialise and run a thread to get all the SQL query data into batches
     * while returning the specified batch requested.
     */
    @Override
    public EntityRecord getEntityRecord(@NonNull SourceConfig sourceConfig, int batchId) {
        if (entityRecordMap.isEmpty()) {
            initRetrieval(sourceConfig);
            int sleepCycleCount = 0;
            int totalSleepDuration = 0;
            while (!retrievalStarted && !timedOut) {
                try {
                    if (totalSleepDuration >= TIMEOUT_DURATION) {
                        timedOut = true;
                    }
                    Thread.sleep(SLEEP_DURATION);
                    totalSleepDuration = totalSleepDuration + SLEEP_DURATION;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                sleepCycleCount++;
            }
        }

        EntityRecord result = entityRecordMap.get(batchId);
        entityRecordMap.remove(batchId);
        if (batchId >= batchCount) {
            entityRecordMap.clear();
        }
        return result;
    }

    @Override
    public int calculateNumOfBatches(@NonNull SourceConfig sourceConfig) {
        try (Connection conn = dataSource.getConnection()) {
            if (!database.isEmpty()) {
                conn.setCatalog(database);
            }
            conn.setCatalog(database);
            try (Statement stmt = conn.createStatement()) {
                String query = "SELECT COUNT(*) over () total_rows FROM " + sourceConfig.getPayload() + ";";
                try (ResultSet rs = stmt.executeQuery(query)) {
                    rs.next();
                    int size = rs.getInt("total_rows");
                    return batchCount = (int) Math.ceil((double) size / MAX_BATCH_ROWS);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
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
            private InstanceConfig(Builder builder) {
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
     * This class contains the methods required to
     */
    public class EntityRecordRetrievalTask implements Callable<Integer> {

        private final SourceConfig sourceConfig;

        /**
         * Constructs an {@code EntityRecordRetrievalTask} instance with the
         * specified {@link SourceConfig}.
         */
        public EntityRecordRetrievalTask(SourceConfig sourceConfig) {
            this.sourceConfig = sourceConfig;
        }

        /**
         * Retrieval relevant SQL query data from connection.
         */
        private void retrieveTableData(Connection conn) throws SQLException {
            if (!database.isEmpty()) {
                conn.setCatalog(database);
            }
            try (Statement stmt = conn.createStatement()) {
                String query = "SELECT * FROM " + sourceConfig.getPayload() + ";";
                try (ResultSet rs = stmt.executeQuery(query)) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    MutableEntityRecord mer = new MutableEntityRecord();

                    int count = 0;
                    int rowCount = 0;
                    while (rs.next()) {
                        mer.addRecord(buildRecord(rsmd, rs));
                        if (rowCount >= MAX_BATCH_ROWS) {
                            entityRecordMap.put(count, mer);
                            retrievalStarted = true;
                            rowCount = 0;
                            count++;
                        }

                        rowCount++;

                    }
                    if (!mer.isEmpty()) {
                        entityRecordMap.put(count, mer);
                        retrievalStarted = true;
                    }
                }
            }
        }

        /**
         * Create and return a {@code MutableRecord} containing data of one row.
         */
        private MutableRecord buildRecord(ResultSetMetaData rsmd, ResultSet rs) throws SQLException {
            int columnCount = rsmd.getColumnCount();
            String colName = rsmd.getColumnName(1);
            String colValue = rs.getString(1);
            MutableRecord mr = new MutableRecord(colName, colValue);
            for (int i = 1; i <= columnCount; i++) {
                mr.addProperty(rsmd.getColumnName(i), rs.getString(i));
            }
            return mr;
        }

        /**
         * Start retrieval execution
         *
         * @return exit code 0 if successful, otherwise -1
         */
        @Override
        public Integer call() {
            try (Connection conn = dataSource.getConnection()) {
                conn.setCatalog(database);
                PayloadType payloadType = sourceConfig.getPayloadType();
                if (payloadType.equals(DatabaseType.TABLE_NAME)) {
                    retrieveTableData(conn);
                }
                return 0;
            } catch (SQLException ex) {
                LOGGER.error(ex);
                return -1;
            }
        }
    }
}
