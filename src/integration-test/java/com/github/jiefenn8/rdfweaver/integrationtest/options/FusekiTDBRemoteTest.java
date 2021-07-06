package com.github.jiefenn8.rdfweaver.integrationtest.options;

import com.github.jiefenn8.rdfweaver.output.FusekiTDBRemote;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FusekiTDBRemoteTest {

    private final static String HOST = "127.0.0.1";
    private final static int PORT = 3030;
    private final static String BASE = "ds";
    private FusekiTDBRemote fusekiTDBRemote;

    @Before
    public void setUp() throws Exception {
        fusekiTDBRemote = new FusekiTDBRemote.Builder(InetAddress.getByName(HOST), PORT, BASE).build();
    }

    @Test
    public void GivenPopulatedModel_WhenSave_ThenSaveModel() throws Exception {
        Resource res = ResourceFactory.createResource("TEST");
        Model model = ModelFactory.createDefaultModel();
        model.add(res, ResourceFactory.createProperty("PROPERTY"), ResourceFactory.createStringLiteral("TEST"));

        fusekiTDBRemote.save(model);

        String connStr = new URIBuilder()
                .setScheme("http")
                .setHost(HOST)
                .setPort(PORT)
                .setPath(BASE)
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
        assertThat(count, is(1));
    }
}
