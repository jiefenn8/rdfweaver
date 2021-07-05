package com.github.jiefenn8.rdfweaver.server;

import io.github.jiefenn8.graphloom.api.EntityReference;
import io.github.jiefenn8.graphloom.rdf.r2rml.DatabaseType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines common methods dealing with any SQL related features
 * within this app package.
 */
public class SQLHelper {

    private static final Pattern DB_OBJ_CHAIN_PATTERN = Pattern.compile("([\\w]+[.][\\w]+)");

    /**
     * Enclose all identifiers found in this String with a bracket.
     *
     * @param identifiers the string containing identifiers chained together
     * @return the delimited chain enclosed by brackets: [foo].[foo]
     */
    private static String delimitIdentifier(@NonNull String identifiers) {
        String[] objects = identifiers.split("[.]");
        for (String obj : objects) {
            identifiers = identifiers.replace(obj, "[" + obj + "]");
        }
        return identifiers;
    }

    /**
     * Checks the query string and delimit any objects or identifiers
     * found so that any conflicts with SQL keywords is unlikely during
     * execution.
     *
     * @param query the string to check and delimit
     * @return the delimited query string ready for use
     */
    protected static String delimitQueryIdentifiers(@NonNull String query) {
        Matcher matcher = DB_OBJ_CHAIN_PATTERN.matcher(query);
        while (matcher.find()) {
            String replace = delimitIdentifier(matcher.group(1));
            query = query.replace(matcher.group(1), replace);
        }
        return query;
    }

    /**
     * Returns a query string that is prepared to be executed over a SQL
     * connection.
     *
     * @param entityReference containing information to locate a specific entity
     * @return string of the prepared query
     */
    protected static String prepareQuery(EntityReference entityReference) {
        String query = Objects.requireNonNull(entityReference.getPayload());
        EntityReference.PayloadType type = entityReference.getPayloadType();
        if (type.equals(DatabaseType.TABLE_NAME)) {
            query = "SELECT * FROM " + query;
        }
        return SQLHelper.delimitQueryIdentifiers(query);
    }
}
