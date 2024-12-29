package com.drewmalin.snickerdoodle.engine.ecs.system;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.script.Script;

public class DefaultScriptSystem
    implements ScriptSystem {

    @Override
    public void update(final Engine engine, final Scene scene, final double dt) {
        if (engine.getState() == Engine.State.PAUSED) {
            return;
        }

        final var entityManager = scene.getEntityManager();
        for (final var entity : entityManager.getEntitiesWithComponent(Script.class)) {
            entityManager.getComponent(entity, Script.class).get().invoke(entity, entityManager, dt);
        }
    }

    @Override
    public void destroy() {

    }
}
