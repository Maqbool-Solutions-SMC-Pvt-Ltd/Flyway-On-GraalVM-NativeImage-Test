package com.maqboolsolutions.flywaygraalvmtest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {

    public static File createTempDirectory(String directoryName) {
        try {
            Path tempDir = Files.createTempDirectory(directoryName);
            return tempDir.toFile();
        } catch (IOException e) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, "Failed to create temporary directory", e);
            return null;
        }
    }
}



