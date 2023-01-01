package com.drewmalin.snickerdoodle.engine;

import com.drewmalin.snickerdoodle.engine.opengl.OpenGlEngine;

public class EngineFactory {

    public static OpenGlEngine.Builder openGlEngineBuilder() {
        return OpenGlEngine.builder();
    }
}
