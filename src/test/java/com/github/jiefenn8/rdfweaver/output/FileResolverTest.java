package com.github.jiefenn8.rdfweaver.output;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Unit test class for {@link FileResolver}.
 */
public class FileResolverTest {


    private static final String OUTPUT_DIR = "rdfweaver-test-";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private Path testDir;

    @Before
    public void setUp() throws Exception {
        testDir = Files.createTempDirectory(OUTPUT_DIR);
    }

    @Test
    public void GivenPath_WhenPrepareDir_ThenReturnExpectedPath() throws Exception {
        Path result = FileResolver.prepareDir(testDir);
        assertThat(result, is(equalTo(testDir)));
    }

    @Test
    public void GivenPath_WhenPrepareDir_ThenDirectoryExists() throws Exception {
        FileResolver.prepareDir(testDir);
        boolean result = Files.exists(testDir);
        assertThat(result, is(true));
    }

    @Test
    public void GivenUnusedPaths_WhenResolveFilename_ThenReturnCombinedPath() {
        String filename = "rdfFile.nt";
        Path expected = Paths.get(testDir.toString(), filename);

        Path result = FileResolver.resolveFilename(testDir, filename);
        assertThat(result, is(equalTo(expected)));
    }

    @Test
    public void GivenExistingPaths_WhenResolvedFilename_ThenReturnNewPath() throws Exception {
        String filename = "rdfFile.nt";
        String incrementedFilename = "rdfFile1.nt";
        Files.createFile(testDir.resolve(filename));
        Path expected = testDir.resolve(incrementedFilename);

        Path result = FileResolver.resolveFilename(testDir, filename);
        assertThat(result, is(equalTo(expected)));
    }
}
