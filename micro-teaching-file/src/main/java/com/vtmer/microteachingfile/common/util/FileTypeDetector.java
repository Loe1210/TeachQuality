package com.vtmer.microteachingfile.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class FileTypeDetector {

    private FileTypeDetector() {
    }

    public static String detectMagicExtension(Path filePath) throws IOException {
        byte[] header = new byte[16];
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            int read = inputStream.read(header);
            if (read <= 0) {
                return "";
            }
        }
        if (startsWith(header, new byte[]{0x25, 0x50, 0x44, 0x46})) {
            return "pdf";
        }
        if (startsWith(header, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47})) {
            return "png";
        }
        if (startsWith(header, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return "jpg";
        }
        if (startsWith(header, new byte[]{0x50, 0x4B, 0x03, 0x04})) {
            return "zip";
        }
        if (startsWith(header, new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0})) {
            return "ole";
        }
        return "";
    }

    public static boolean matchesExpected(String originalName, String magicExtension) {
        if (magicExtension == null || magicExtension.isEmpty()) {
            return false;
        }
        String extension = getExtension(originalName);
        if (extension.isEmpty()) {
            return false;
        }
        if ("zip".equals(magicExtension)) {
            return extension.equals("zip") || extension.equals("docx") || extension.equals("xlsx");
        }
        if ("ole".equals(magicExtension)) {
            return extension.equals("doc") || extension.equals("xls");
        }
        if ("jpg".equals(magicExtension)) {
            return extension.equals("jpg") || extension.equals("jpeg");
        }
        return extension.equals(magicExtension);
    }

    public static String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean startsWith(byte[] source, byte[] target) {
        if (source.length < target.length) {
            return false;
        }
        for (int i = 0; i < target.length; i++) {
            if (source[i] != target[i]) {
                return false;
            }
        }
        return true;
    }
}
