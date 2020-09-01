package com.github.jiefenn8.rdfweaver.output;

import org.apache.commons.io.FilenameUtils;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class contains static methods to handle the preparation of path
 * directory and filename strings for usage in the output package.
 */
public class FileResolver {

    /**
     * Checks if the {@code Path} parameter already exist as a directory.
     * If the directory does not exist, create the directory and any missing
     * parent directories. Returns the {@code Path} parameter if the
     * operation is successful.
     *
     * @param path the directory to create
     * @return the path of the directory created
     * @throws IOException if an I/O error occurs or the parent directory
     *                     does not exist
     */
    protected static Path prepareDir(@NonNull Path path) throws IOException {
        if (Files.exists(path)) return path;
        return Files.createDirectories(path);
    }

    /**
     * Checks of the file with the given path and filename parameter exists.
     * Returns a {@code Path} of combined path and filename if the file
     * exists, otherwise increment the filename to unused name before returning
     * the combined path.
     *
     * @param path     the output directory of the file
     * @param filename the name of the output file
     * @return the combined {@code Path} of the path and fileName
     */
    protected static Path resolveFilename(@NonNull Path path, @NonNull String filename) {
        Path absolute = path.resolve(filename);
        int increment = 1;
        while (Files.exists(absolute)) {
            absolute = path.resolve(incrementFileName(filename, increment));
            increment++;
        }
        return absolute;
    }

    /**
     * Returns a new filename {@code String} by combining the filename with the
     * increment parameter.
     *
     * @param filename  the name of the file to change
     * @param increment the number to add to the filename
     * @return the {@code String} of the new filename
     */
    private static String incrementFileName(@NonNull String filename, @NonNegative int increment) {
        String extension = FilenameUtils.getExtension(filename);
        if (!extension.isEmpty()) extension = "." + extension;
        return FilenameUtils.getBaseName(filename) + increment + extension;
    }
}
