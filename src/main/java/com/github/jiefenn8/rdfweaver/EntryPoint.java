package com.github.jiefenn8.rdfweaver;

public class EntryPoint {

    /**
     * Main program execution entry.
     *
     * @param args the arguments given to app
     */
    public static void main(String[] args) {
        System.setProperty("log4j2.configurationFile", "configs/log4j2-rdfweaver.xml");
        RDFWeaver RDFWeaver = new RDFWeaver();
        RDFWeaver.init(args);
    }
}
