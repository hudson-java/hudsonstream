package com.hudsonstream.storage.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class FileUtils {

    public FileUtils() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static DataOutputStream openDataOutputStream(Path path, StandardOpenOption... standardOpenOptions) throws IOException {
        OutputStream outputStream = Files.newOutputStream(path, standardOpenOptions);
        BufferedOutputStream  bufferedOutputStream = new BufferedOutputStream(outputStream);
        return new DataOutputStream(bufferedOutputStream);
    }

    public static DataInputStream openDataInputStream(Path path, StandardOpenOption... standardOpenOptions) throws IOException {
        InputStream inputStream = Files.newInputStream(path, standardOpenOptions);
        BufferedInputStream  bufferedInputStream = new BufferedInputStream(inputStream);
        return new DataInputStream(bufferedInputStream);
    }
}
