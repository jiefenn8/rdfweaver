package com.github.jiefenn8.rdfweaver.output;

import org.apache.jena.riot.RDFFormat;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit test class for {@code RDFFileFactory}.
 */
public class RDFFileFactoryTest {

    private static final String OUTPUT_DIR = "rdfweaver-test-";
    private Path testDir;
    private RDFFileFactory rdfFileFactory;

    @Before
    public void setUp() throws Exception {
        testDir = Files.createTempDirectory(OUTPUT_DIR);
        rdfFileFactory = new RDFFileFactory();
    }

    @Test
    public void GivenParams_WhenCreateFile_ThenReturnRDFile() {
        String filename = "rdfFile.nt";
        RDFFile result = rdfFileFactory.createFile(testDir.resolve(filename), mock(RDFFormat.class));
        assertThat(result, is(notNullValue()));
    }
}
