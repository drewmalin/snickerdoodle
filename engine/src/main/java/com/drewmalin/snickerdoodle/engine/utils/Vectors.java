package com.drewmalin.snickerdoodle.engine.utils;

import org.joml.Vector3f;

public class Vectors {

    private static final Vector3f UP = new Vector3f(0f, 1f, 0f);

    private Vectors() {
        // utility class
    }

    public static Vector3f up() {
        return UP;
    }
}
