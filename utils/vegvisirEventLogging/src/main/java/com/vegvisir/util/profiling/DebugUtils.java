package com.vegvisir.util.profiling;

import java.util.Base64;

public class DebugUtils {

    public static String utf82base64(String utf8) {
        return Base64.getEncoder().encodeToString(utf8.getBytes());
    }
}
