package com.drewmalin.snickerdoodle.engine.ecs.component;

public interface Texture
    extends Material {

    /**
     * Returns the file path of this Texture. The file is expected to exist within the .jar as build-time
     * resources.
     */
    String getFilePath();

    /**
     * The coordinates to sample from the texture file, in their draw order.
     */
    float[] getCoordinates();
}
