package com.vtmer.microteachingfile.common.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileTypeDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDetectPdfMagicHeader() throws IOException {
        Path file = tempDir.resolve("sample.pdf");
        Files.write(file, new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D});

        assertEquals("pdf", FileTypeDetector.detectMagicExtension(file));
        assertTrue(FileTypeDetector.matchesExpected("sample.pdf", "pdf"));
    }

    @Test
    void shouldTreatDocxAndXlsxAsZipContainers() {
        assertTrue(FileTypeDetector.matchesExpected("report.docx", "zip"));
        assertTrue(FileTypeDetector.matchesExpected("score.xlsx", "zip"));
        assertFalse(FileTypeDetector.matchesExpected("image.png", "zip"));
    }
}
