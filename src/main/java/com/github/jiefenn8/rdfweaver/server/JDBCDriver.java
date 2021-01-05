package com.github.jiefenn8.rdfweaver.server;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Enum class that defines all the supported JDBC driver and their classes.
 */
public enum JDBCDriver {

    /**
     * All supported JDBC driver class listed below.
     */
    UNDEFINED(""),
    MSSQL("com.microsoft.sqlserver.jdbc.SQLServerDataSource"),
    MYSQL("com.mysql.cj.jdbc.MysqlDataSource"),
    ORACLE("oracle.jdbc.pool.OracleDataSource");

    private final String driverClass;

    /**
     * Constructs a driver class enum with the specified name of the class that will
     * handle this driver.
     */
    JDBCDriver(@NonNull String driverClass) {
        this.driverClass = driverClass;
    }

    /**
     * Returns the name of the driver class for this ENUM.
     *
     * @return the name of the driver class
     */
    public String getClassName() {
        return this.driverClass;
    }
}