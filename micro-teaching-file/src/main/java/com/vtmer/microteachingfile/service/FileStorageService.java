package com.vtmer.microteachingfile.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface FileStorageService {

    String saveChunk(String sessionCode, Integer chunkIndex, InputStream inputStream) throws IOException;

    String mergeChunks(String storedName, List<Path> chunkPaths, String relativeObjectDir) throws IOException;

    void deletePath(String storagePath) throws IOException;

    Path resolveObjectPath(String storagePath);
}
