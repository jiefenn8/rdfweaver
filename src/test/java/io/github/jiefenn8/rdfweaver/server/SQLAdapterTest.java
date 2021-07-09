package io.github.jiefenn8.rdfweaver.server;

import io.github.jiefenn8.graphloom.api.inputsource.Entity;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@link SQLAdapter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLAdapterTest extends TestCase {

    @Mock private ResultSet mockResultSet;
    @Mock private ResultSetMetaData mockMetaData;
    private SQLAdapter sqlAdapter;

    @Before
    public void SetUp() throws Exception {
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        sqlAdapter = new SQLAdapter(mockResultSet);
    }

    @Test
    public void GivenResultSetHasNext_WhenHasNext_ThenReturnTrue() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        boolean result = sqlAdapter.hasNext();
        assertThat(result, is(true));
    }

    @Test
    public void GivenResultSetHasNoNext_WhenHasNext_ThenReturnFalse() throws Exception {
        when(mockResultSet.next()).thenReturn(false);
        boolean result = sqlAdapter.hasNext();
        assertThat(result, is(false));
    }

    @Test
    public void GivenResultSetHasException_WhenHasNext_ThenThrowRuntimeException() throws Exception {
        when(mockResultSet.next()).thenThrow(SQLException.class);
        Assert.assertThrows(
                RuntimeException.class,
                () -> sqlAdapter.hasNext()
        );
    }

    @Test
    public void GivenResultSet_WhenNextEntity_ThenReturnThis() {
        Entity result = sqlAdapter.nextEntity();
        assertThat(result, instanceOf(SQLAdapter.class));
    }

    @Test
    public void GivenValidPropertyName_WhenGetPropertyValue_ThenReturnExpected() throws Exception {
        String property = "PROPERTY";
        String expected = "VALUE";
        (when(mockMetaData.getColumnCount())).thenReturn(2);
        when(mockMetaData.getColumnName(2)).thenReturn(property);
        when(mockResultSet.getString(2)).thenReturn(expected);
        sqlAdapter.nextEntity();
        String result = sqlAdapter.getPropertyValue(property);
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void GivenPropertyNameBeforeFirst_WhenGetPropertyValue_ThenReturnNull() throws Exception {
        String property = "PROPERTY";
        String result = sqlAdapter.getPropertyValue(property);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void GivenInvalidPropertyName_WhenGetPropertyValue_ThenReturnNull() throws Exception {
        String result = sqlAdapter.getPropertyValue(null);
        assertThat(result, is(nullValue()));
    }

}