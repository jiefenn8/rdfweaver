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
public class RDFOutputFactoryTest {

    private static final String OUTPUT_DIR = "rdfweaver-test-";
    private Path testDir;
    private RDFOutputFactory RDFOutputFactory;

    @Before
    public void setUp() throws Exception {
        testDir = Files.createTempDirectory(OUTPUT_DIR);
        RDFOutputFactory = new RDFOutputFactory();
    }

    @Test
    public void GivenParams_WhenCreateFile_ThenReturnRDFile() throws Exception {
        String filename = "rdfFile.nt";
        RDFFileSystem result = RDFOutputFactory.createFileSystem(testDir, filename, mock(RDFFormat.class));
        assertThat(result, is(notNullValue()));
    }
}
