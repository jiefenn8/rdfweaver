package com.github.jiefenn8.rdfweaver.server;

import org.checkerframework.checker.nullness.qual.NonNull;

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
    public static String delimitIdentifier(@NonNull String identifiers) {
        String[] objects = identifiers.split("[.]");
        for (String obj : objects) {
            identifiers = identifiers.replace(obj, "[" + obj + "]");
        }
        return identifiers;
    }

    /**
     * Checks the query String and delimit any objects or identifiers
     * found so that any conflicts with SQL keywords is unlikely during
     * execution.
     *
     * @param query the string to check and delimit
     * @return the delimited query string ready for use
     */
    public static String delimitQueryIdentifiers(@NonNull String query) {
        Matcher matcher = DB_OBJ_CHAIN_PATTERN.matcher(query);
        while (matcher.find()) {
            String replace = delimitIdentifier(matcher.group(1));
            query = query.replace(matcher.group(1), replace);
        }
        return query;
    }
}
