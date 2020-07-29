package com.github.jiefenn8.rdfweaver.network;

import com.github.jiefenn8.graphloom.api.EntityRecord;
import com.github.jiefenn8.graphloom.api.InputSource;
import com.github.jiefenn8.graphloom.api.SourceConfig;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.util.Properties;

/**
 * This class implements {@link InputSource} and handles the retrieval of data
 * from relational data source.
 */
public class RelationalSource implements InputSource {

    private final DataSource dataSource;

    /**
     * Constructs a {@code RelationalSource} with the specified Builder containing
     * the properties to populate and initialise this immutable instance.
     *
     * @param builder the database source builder to build from
     */
    private RelationalSource(Builder builder) {
        dataSource = builder.dataSource;
    }

    @Override
    public EntityRecord getEntityRecord(@NonNull SourceConfig sourceConfig, int batchId) {
        return null;
    }

    @Override
    public int calculateNumOfBatches(@NonNull SourceConfig sourceConfig) {
        return 0;
    }

    /**
     * Builder class for {@link RelationalSource}.
     */
    public static class Builder {

        private final DataSourceFactory dataSourceFactory;
        private DataSource dataSource;

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
            public OptionalConfig credential(@NonNull String user, char[] pass) {
                serverConfig.setProperty("dataSource.user", user);
                serverConfig.setProperty("dataSource.password", String.valueOf(pass));
                return this;
            }

            @Override
            public RelationalSource build() {
                builder.dataSource = builder.dataSourceFactory.getDataSource(serverConfig);
                return new RelationalSource(builder);
            }
        }
    }
}
