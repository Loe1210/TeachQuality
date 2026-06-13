package com.vtmer.microteachingquality.util;


import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.net.URLEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Hung
 */
public class EncryptUtil {
    public static String encodeBase64(byte[] bytes) {
        String encoded = Base64.getEncoder().encodeToString(bytes);
        return encoded;
    }

    public static byte[] decodeBase64(String str) {
        byte[] bytes = null;
        bytes = Base64.getDecoder().decode(str);
        return bytes;
    }

    public static String encodeUTF8StringBase64(String str) {
        String encoded = null;
        encoded = Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
        return encoded;

    }

    public static String decodeUTF8StringBase64(String str) {
        String decoded = null;
        byte[] bytes = Base64.getDecoder().decode(str);
        decoded = new String(bytes, StandardCharsets.UTF_8);
        return decoded;
    }

    public static String encodeURL(String url) {
        URLEncoder urlEncoder = new URLEncoder();
        String encoded = urlEncoder.encode(url, StandardCharsets.UTF_8);
        return encoded;
    }


    public static String decodeURL(String url) {
        String decoded = URLDecoder.decode(url, StandardCharsets.UTF_8);
        return decoded;
    }
}
