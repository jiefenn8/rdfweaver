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
public class RDFFileSystemFactoryTest {

    private static final String OUTPUT_DIR = "rdfweaver-test-";
    private Path testDir;
    private RDFFileSystemFactory rdfFileSystemFactory;

    @Before
    public void setUp() throws Exception {
        testDir = Files.createTempDirectory(OUTPUT_DIR);
        rdfFileSystemFactory = new RDFFileSystemFactory();
    }

    @Test
    public void GivenParams_WhenCreateFile_ThenReturnRDFile() {
        String filename = "rdfFile.nt";
        RDFFileSystem result = rdfFileSystemFactory.createFile(testDir.resolve(filename), mock(RDFFormat.class));
        assertThat(result, is(notNullValue()));
    }
}
