package io.github.jiefenn8.rdfweaver.server;

import io.github.jiefenn8.graphloom.api.inputsource.Entity;
import io.github.jiefenn8.graphloom.api.inputsource.EntityResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * This class defines the base methods in wrapping the functionality of SQL
 * for the {@link EntityResult} and {@link Entity} interfaces.
 */
public class SQLAdapter implements EntityResult, Entity {

    private static final Logger LOGGER = LogManager.getLogger(SQLAdapter.class);
    private final ResultSet resultSet;
    private boolean calledNext = false;
    private boolean hasNext = false;
    private final Map<String, String> entity = new LinkedHashMap<>();

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
            if(!calledNext){
                hasNext = resultSet.next();
                calledNext = true;
            }
            return hasNext;
        } catch (SQLException ex) {
            LOGGER.fatal("SQL error checking for the next row.");
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Entity nextEntity() {
        try {
            if(!calledNext){
                resultSet.next();
            }
            calledNext = false;
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            for(int i = 1; i <= columnCount; i++){
                entity.put(metaData.getColumnName(i), resultSet.getString(i));
            }
            return this;
        } catch (SQLException ex) {
            LOGGER.fatal("SQL error retrieving the next row.");
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getPropertyValue(String name) {
        return entity.get(name);
    }
}
