package com.github.jiefenn8.rdfweaver.server;

import io.github.jiefenn8.graphloom.api.inputsource.Entity;
import io.github.jiefenn8.graphloom.api.inputsource.EntityResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class defines the base methods in wrapping the functionality of SQL
 * for the {@link EntityResult} and {@link Entity} interfaces.
 */
public class SQLAdapter implements EntityResult, Entity {

    private static final Logger LOGGER = LogManager.getLogger(SQLAdapter.class);
    private final ResultSet resultSet;

    /**
     * Constructs an instance of SQLAdapter with the given ResultSet.
     *
     * @param resultSet to wrap its functionality
     */
    protected SQLAdapter(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public boolean hasNext() {
        try {
            return !resultSet.isAfterLast();
        } catch (SQLException ex) {
            LOGGER.fatal("Error retrieving next row of entity result.");
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Entity nextEntity() {
        return this;
    }

    @Override
    public String getPropertyValue(String name) {
        try {
            return resultSet.getString(name);
        } catch (SQLException ex) {
            LOGGER.fatal("Error retrieving property value {}.", name);
            throw new RuntimeException(ex);
        }
    }
}
