package com.drewmalin.snickerdoodle.engine.ecs.component;

import org.joml.Vector4f;

public interface Material extends Component {

    Vector4f getAmbient();

    Vector4f getDiffuse();

    Vector4f getSpecular();

    float getReflectance();
}
