package com.example.hrms.utils;

import java.util.Set;

public class FileFormatConstants {

    public static final Set<String> VALID_FORMATS = Set.of(
            "PDF",
            "JPG",
            "PNG"
    );

    private FileFormatConstants() {
        // prevent instantiation
    }
}
