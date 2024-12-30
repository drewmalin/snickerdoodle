package com.drewmalin.snickerdoodle.engine.utils;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Vectors {

    private static final Vector3f UP = new Vector3f(0f, 1f, 0f);

    private Vectors() {
        // utility class
    }

    public static Vector3f up() {
        return UP;
    }

    public static String toString(final Vector4f vector4f) {
        return "("
            + vector4f.x + ", "
            + vector4f.y + ", "
            + vector4f.z + ", "
            + vector4f.w
            + ")";
    }
}
