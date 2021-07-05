package com.github.jiefenn8.rdfweaver.server;

import io.github.jiefenn8.graphloom.api.inputsource.Entity;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SQLAdapterTest extends TestCase {

    @Mock
    private ResultSet mockResultSet;
    private SQLAdapter SQLAdapter;

    @Before
    public void SetUp() {
        SQLAdapter = new SQLAdapter(mockResultSet);
    }

    @Test
    public void GivenResultSetHasNext_WhenHasNext_ThenReturnTrue() throws Exception {
        when(mockResultSet.isAfterLast()).thenReturn(false);
        boolean result = SQLAdapter.hasNext();
        assertThat(result, is(true));
    }

    @Test
    public void GivenResultSetHasNoNext_WhenHasNext_ThenReturnFalse() throws Exception {
        when(mockResultSet.isAfterLast()).thenReturn(true);
        boolean result = SQLAdapter.hasNext();
        assertThat(result, is(false));
    }

    @Test
    public void GivenResultSetHasException_WhenHasNext_ThenThrowRuntimeException() throws Exception {
        when(mockResultSet.isAfterLast()).thenThrow(SQLException.class);
        Assert.assertThrows(
                RuntimeException.class,
                () -> SQLAdapter.hasNext()
        );
    }

    @Test
    public void GivenResultSetHasEntity_WhenNextEntity_ThenReturnEntity() {
        Entity result = SQLAdapter.nextEntity();
        assertThat(result, instanceOf(Entity.class));
    }

    @Test
    public void GivenPropertyName_WhenGetPropertyValue_ThenReturnExpected() throws Exception {
        String property = "PROPERTY";
        String expected = "VALUE";
        when(mockResultSet.getString(property)).thenReturn(expected);
        String result = SQLAdapter.getPropertyValue(property);
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void GivenInvalidPropertyName_WhenGetPropertyValue_ThenThrowRuntimeException() throws Exception {
        when(mockResultSet.getString(null)).thenThrow(SQLException.class);
        Assert.assertThrows(
                RuntimeException.class,
                () -> SQLAdapter.getPropertyValue(null)
        );
    }

}