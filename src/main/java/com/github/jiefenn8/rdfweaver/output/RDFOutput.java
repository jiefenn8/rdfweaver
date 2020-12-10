package com.github.jiefenn8.rdfweaver.output;

import org.apache.jena.rdf.model.Model;

import java.io.IOException;

public interface RDFOutput {

    void save(Model model) throws IOException;
}
