package com.vtmer.microteachingfile.common.util;

import cn.hutool.crypto.digest.DigestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileHashUtil {

    private FileHashUtil() {
    }

    public static String sha256(Path filePath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return DigestUtil.sha256Hex(inputStream);
        }
    }
}
