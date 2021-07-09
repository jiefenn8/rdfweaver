package io.github.jiefenn8.rdfweaver.server;

import io.github.jiefenn8.graphloom.api.EntityReference;
import io.github.jiefenn8.graphloom.rdf.r2rml.DatabaseType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@link SQLHelper}.
 */
public class SQLHelperTest {

    @Test
    public void GivenObjectChain_WhenDelimitQuery_ThenReturnExpected() {
        String value = "object.table";
        String expected = "[object].[table]";
        String result = SQLHelper.delimitQueryIdentifiers(value);
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void GivenValidTableReference_WhenPrepareQuery_ThenReturnExpected() {
        EntityReference mockEntityRef = mock(EntityReference.class);
        String payload = "OBJECT.TABLE";
        when(mockEntityRef.getPayload()).thenReturn(payload);
        EntityReference.PayloadType payloadType = DatabaseType.TABLE_NAME;
        when(mockEntityRef.getPayloadType()).thenReturn(payloadType);
        String expected = "SELECT * FROM [OBJECT].[TABLE]";
        String result = SQLHelper.prepareQuery(mockEntityRef);
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void GivenValidViewReference_WhenPrepareQuery_ThenReturnExpected() {
        EntityReference mockEntityRef = mock(EntityReference.class);
        String payload = "SELECT COL (SELECT COUNT(*) FROM OBJ1 WHERE OBJ1.COL=OBJ2.COL) AS S1 FROM OBJ2;";
        when(mockEntityRef.getPayload()).thenReturn(payload);
        EntityReference.PayloadType payloadType = DatabaseType.QUERY;
        when(mockEntityRef.getPayloadType()).thenReturn(payloadType);
        String expected = "SELECT COL (SELECT COUNT(*) FROM OBJ1 WHERE [OBJ1].[COL]=[OBJ2].[COL]) AS S1 FROM OBJ2;";
        String result = SQLHelper.prepareQuery(mockEntityRef);
        assertThat(result, is(equalTo(expected)));
    }
}
