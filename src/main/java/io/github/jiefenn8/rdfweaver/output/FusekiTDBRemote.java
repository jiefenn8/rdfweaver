package io.github.jiefenn8.rdfweaver.output;

import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.system.Txn;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.InetAddress;
import java.net.URISyntaxException;

/**
 * This class implements {@link RDFOutput} for mapping of RDF mapping results
 * to a remote Fuseki TDB database.
 */
public class FusekiTDBRemote implements RDFOutput {

    private final String connStr;
    private final String graphName;

    /**
     * Constructs a {@code FusekiTDBRemote} instance with the specified Builder
     * containing the properties to populate and initialise this instance.
     *
     * @param builder the builder to construct this instance
     */
    public FusekiTDBRemote(Builder builder) {
        connStr = builder.connStr;
        graphName = builder.graphName;
    }

    @Override
    public void save(@NonNull Model model) {
        try (RDFConnection conn = RDFConnectionFactory.connect(connStr)) {
            Txn.executeWrite(conn, () -> conn.put(graphName, model));
        }
    }

    /**
     * Builder class for {@link FusekiTDBRemote}.
     */
    public static class Builder {

        private final URIBuilder uriBuilder = new URIBuilder();
        private String graphName;
        private String connStr;

        public Builder(@NonNull InetAddress host, int port, @NonNull String baseName) {
            uriBuilder.setScheme("http")
                    .setHost(host.getHostName())
                    .setPort(port)
                    .setPath(baseName);
        }

        public Builder graphName(String name) {
            graphName = name;
            return this;
        }

        public FusekiTDBRemote build() throws URISyntaxException {
            connStr = uriBuilder.build().toASCIIString();
            return new FusekiTDBRemote(this);
        }
    }
}
