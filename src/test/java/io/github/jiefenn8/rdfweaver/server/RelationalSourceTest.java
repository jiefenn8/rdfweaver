package io.github.jiefenn8.rdfweaver.server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test class for {@link RelationalSource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RelationalSourceTest {

    /**
     * The values of the mock database connection that the test much match to be
     * considered a valid connection.
     */
    private static final JDBCDriver TEST_DRIVER = JDBCDriver.MSSQL;
    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 1433;
    private static final String TEST_USER = "test_user";
    private static final String TEST_PASS = "test_pass";

    @Mock private DataSourceFactory mockDataSourceFactory;
    @Mock private InetAddress mockAddress;
    private RelationalSource.Builder relationalSourceBuilder;

    @Before
    public void setUp() {
        when(mockAddress.getHostName()).thenReturn(TEST_HOST);
        relationalSourceBuilder = new RelationalSource.Builder(mockDataSourceFactory);
    }

    @Test
    public void GivenValidParams_WhenBuildAllRequired_ThenReturnRelationalSource() {
        when(mockDataSourceFactory.getDataSource(any(Properties.class))).thenReturn(mock(DataSource.class));
        RelationalSource result = relationalSourceBuilder.newInstance()
                .serverHost(TEST_DRIVER, mockAddress, TEST_PORT)
                .credential(TEST_USER, TEST_PASS.toCharArray())
                .build();

        assertThat(result, is(notNullValue()));
    }

    @Test
    public void GivenInvalidParams_WhenBuild_ThenThrowException() {
        when(mockDataSourceFactory.getDataSource(any(Properties.class))).thenThrow(RuntimeException.class);
        Assert.assertThrows(
                Exception.class,
                ()-> {
                    relationalSourceBuilder.newInstance()
                            .serverHost(TEST_DRIVER, mockAddress, -1)
                            .credential(TEST_USER, TEST_PASS.toCharArray())
                            .build();
                }
        );
    }
}
