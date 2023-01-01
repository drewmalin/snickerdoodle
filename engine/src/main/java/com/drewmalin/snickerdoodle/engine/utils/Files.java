package com.drewmalin.snickerdoodle.engine.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Files {

    public static String loadResource(final String fileName) {
        String result;
        try (final InputStream in = Files.class.getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
            result = scanner.useDelimiter("\\A").next();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
