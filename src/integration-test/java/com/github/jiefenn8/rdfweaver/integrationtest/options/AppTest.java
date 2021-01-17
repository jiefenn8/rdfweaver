package com.github.jiefenn8.rdfweaver.integrationtest.options;

import com.github.jiefenn8.rdfweaver.EntryPoint;
import com.github.jiefenn8.rdfweaver.server.JDBCDriver;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test class for {@code App}.
 */
public class AppTest {

    private static final String DELIMITER = "=";

    /**
     * The RDB input options to use for this test.
     */
    private static final String DRIVER_OPTION = "--driver";
    private static final String HOST_OPTION = "--host";
    private static final String PORT_OPTION = "--port";
    private static final String USER_OPTION = "--user";
    private static final String PASS_OPTION = "--pass";
    private static final String DB_OPTION = "--database";

    /**
     * The common values used for testing.
     */
    private static final String RDB_DRIVER = JDBCDriver.MSSQL.toString();
    private static final String RDB_HOST = "127.0.0.1";
    private static final String RDB_PORT = "1433";
    private static final String RDB_USER = "sa";
    private static final String RDB_PASS = "YourStrong@Passw0rd";
    private static final String RDB_DB = "testDb";


    private Path expectedOutput;

    @Before
    public void setUp() {
        expectedOutput = Paths.get("output/rdfOutput.nt").toAbsolutePath();
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(expectedOutput);
    }

    @Test
    public void GivenRequiredParams_WhenExecute_ThenGenerateRDFFile() {
        //Input params
        String driver = DRIVER_OPTION + DELIMITER + RDB_DRIVER;
        String host = HOST_OPTION + DELIMITER + RDB_HOST;
        String port = PORT_OPTION + DELIMITER + RDB_PORT;
        String db = DB_OPTION + DELIMITER + RDB_DB;
        String user = USER_OPTION + DELIMITER + RDB_USER;
        String pass = PASS_OPTION + DELIMITER + RDB_PASS;

        //R2RML params
        String r2rmlFile = getClass().getResource("/r2rml/valid_r2rml.ttl").getPath();
        String r2rml = "--file=" + r2rmlFile;

        //Full arg array assembly
        String[] args = new String[]{"server", driver, db, host, port, user, pass, "r2rml", r2rml, "output"};

        EntryPoint.main(args);
        boolean result = Files.exists(expectedOutput);
        assertThat(result, is(true));
    }

    @Test
    public void GivenParamsForFusekiOutput_WhenExecute_ThenMapResultToFusekiDB() throws Exception {
        //Input params
        String driver = DRIVER_OPTION + DELIMITER + RDB_DRIVER;
        String host = HOST_OPTION + DELIMITER + RDB_HOST;
        String port = PORT_OPTION + DELIMITER + RDB_PORT;
        String db = DB_OPTION + DELIMITER + RDB_DB;
        String user = USER_OPTION + DELIMITER + RDB_USER;
        String pass = PASS_OPTION + DELIMITER + RDB_PASS;

        //R2RML params
        String r2rmlFile = getClass().getResource("/r2rml/valid_r2rml.ttl").getPath();
        String r2rml = "--file=" + r2rmlFile;

        //Output params
        String fHost = RDB_HOST;
        String fusekiHost = "--host" + DELIMITER + fHost; //Should be in the same local machine (TravisCI).
        int fPort = 3030;
        String fusekiPort = "--port" + DELIMITER + fPort;
        String fBase = "ds";
        String fusekiBase = "--baseName" + DELIMITER + fBase;

        //Full arg array assembly
        String[] args = new String[]{
                "server", driver, db, host, port, user, pass,
                "r2rml", r2rml,
                "output", fusekiHost, fusekiPort, fusekiBase
        };

        EntryPoint.main(args);
        String connStr = new URIBuilder()
                .setScheme("http")
                .setHost(fHost)
                .setPort(fPort)
                .setPath(fBase)
                .build()
                .toASCIIString();

        ResultSet copy;
        try (RDFConnection conn = RDFConnectionFactory.connect(connStr)) {
            copy = Txn.calculateRead(conn, () -> {
                String query = "SELECT (COUNT(*) as ?Triples) WHERE { ?s ?p ?o }";
                ResultSet rs = conn.query(query).execSelect();
                return ResultSetFactory.copyResults(rs);
            });
        }

        RDFNode node = copy.next().get("Triples");
        int count = node.asLiteral().getInt();
        assertThat(count, is(2));
    }
}
