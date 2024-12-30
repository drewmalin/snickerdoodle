package com.drewmalin.snickerdoodle.engine.scene;

import com.drewmalin.snickerdoodle.engine.ecs.entity.DefaultEntityManager;
import com.drewmalin.snickerdoodle.engine.ecs.entity.EntityManager;
import com.drewmalin.snickerdoodle.engine.light.DefaultLightManager;
import com.drewmalin.snickerdoodle.engine.light.LightManager;

import java.io.File;

public class Scene {

    private final String name;
    private final EntityManager entityManager;
    private final LightManager lightManager;

    public Scene(final String name) {
        this(name, new DefaultEntityManager(), new DefaultLightManager());
    }

    public Scene(final String name, final EntityManager entityManager, final LightManager lightManager) {
        this.name = name;
        this.entityManager = entityManager;
        this.lightManager = lightManager;
    }

    public static Scene fromFile(final File file) {
        return new Scene("Default");
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public LightManager getLightManager() {
        return this.lightManager;
    }

    @Override
    public String toString() {
        return "Scene["
            + "name='" + this.name + "'"
            + ']';
    }
}
