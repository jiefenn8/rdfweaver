package com.github.jiefenn8.rdfweaver.output;

import org.apache.jena.riot.RDFFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test class for {@link RDFFileSystem}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RDFFileSystemTest {

    private static final String OUTPUT_DIR = "rdfweaver-test-";
    @Mock
    private RDFFormat mockFormat;
    private Path testDir;
    private RDFFileSystem rdfFileSystem;

    @Before
    public void setUp() {
        testDir = Paths.get(OUTPUT_DIR);
        rdfFileSystem = new RDFFileSystem(testDir, mockFormat);
    }

    @Test
    public void GivenPath_WhenGetInstanceAsFile_ThenReturnExpectedFile() {
        File result = rdfFileSystem.asFile();
        assertThat(result.toPath(), is(equalTo(testDir)));
    }

    @Test
    public void GivenRDFFormat_WhenGetFormat_ThenReturnExpectedRDFFormat() {
        RDFFormat result = rdfFileSystem.getFormat();
        assertThat(result, is(equalTo(mockFormat)));
    }
}
