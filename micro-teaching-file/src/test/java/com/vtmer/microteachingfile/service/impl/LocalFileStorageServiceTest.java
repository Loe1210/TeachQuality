package com.vtmer.microteachingfile.service.impl;

import com.vtmer.microteachingfile.config.properties.FileStorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndMergeChunksWithStreaming() throws IOException {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setRootDir(tempDir.toString());
        properties.setTempDir(tempDir.resolve("temp").toString());
        properties.setObjectDir(tempDir.resolve("object").toString());

        LocalFileStorageService storageService = new LocalFileStorageService();
        ReflectionTestUtils.setField(storageService, "fileStorageProperties", properties);

        String chunk0 = storageService.saveChunk("session-1", 0,
                new ByteArrayInputStream("hello ".getBytes(StandardCharsets.UTF_8)));
        String chunk1 = storageService.saveChunk("session-1", 1,
                new ByteArrayInputStream("world".getBytes(StandardCharsets.UTF_8)));

        String storagePath = storageService.mergeChunks(
                "merged.txt",
                List.of(
                        Path.of(properties.getTempDir(), chunk0),
                        Path.of(properties.getTempDir(), chunk1)
                ),
                "20260624"
        );

        Path mergedPath = Path.of(properties.getObjectDir(), storagePath);
        assertTrue(Files.exists(mergedPath));
        assertEquals("hello world", Files.readString(mergedPath));

        storageService.deletePath("session-1");
        assertFalse(Files.exists(Path.of(properties.getTempDir(), "session-1")));
    }
}
