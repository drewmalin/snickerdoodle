package com.drewmalin.snickerdoodle.game;

import com.drewmalin.snickerdoodle.engine.EngineFactory;
import com.drewmalin.snickerdoodle.engine.camera.DefaultCamera;
import com.drewmalin.snickerdoodle.engine.ecs.component.Color;
import com.drewmalin.snickerdoodle.engine.ecs.component.Cube;
import com.drewmalin.snickerdoodle.engine.ecs.component.Plane;
import com.drewmalin.snickerdoodle.engine.ecs.component.Transform;
import com.drewmalin.snickerdoodle.engine.ecs.entity.DefaultEntityManager;
import com.drewmalin.snickerdoodle.engine.ecs.system.OpenGlInputSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.OpenGlRenderSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.DefaultScriptSystem;
import com.drewmalin.snickerdoodle.engine.light.Attenuation;
import com.drewmalin.snickerdoodle.engine.light.DefaultLightManager;
import com.drewmalin.snickerdoodle.engine.light.Light;
import com.drewmalin.snickerdoodle.engine.light.PointLight;
import com.drewmalin.snickerdoodle.engine.opengl.OpenGlEngine;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.script.Script;
import com.drewmalin.snickerdoodle.engine.window.OpenGlWindow;
import com.drewmalin.snickerdoodle.engine.window.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import static com.drewmalin.snickerdoodle.engine.Engine.State.PAUSED;
import static com.drewmalin.snickerdoodle.engine.Engine.State.RUNNING;

public class Game {

    private static final Logger LOGGER = LogManager.getLogger(Game.class);

    public static void main(final String[] args) {
        final var engine = EngineFactory.openGlEngineBuilder()
            .scriptSystem(new DefaultScriptSystem())
            .maxFramesPerSecond(60)
            .maxUpdatesPerSecond(120)
            .build();

        final var window = newWindow(engine);
        final var scene = newScene();

        engine.setWindow(window);
        engine.setScene(scene);
        engine.run();
    }

    private static Window newWindow(final OpenGlEngine engine) {
        final var camera = new DefaultCamera();

        final var window = OpenGlWindow.builder()
            .title("Snickerdoodle")
            .width(1000)
            .height(1000)
            .vSync(true)
            .camera(camera)
            .renderSystem(new OpenGlRenderSystem())
            .inputSystem(new OpenGlInputSystem((w, mouseCursorDelta, dt) -> {
                if (engine.getState() == RUNNING) {
                    float distance = (float) (60f * dt);

                    if (w.isKeyPressed(GLFW.GLFW_KEY_W)) {
                        camera.panForward(distance);
                    }
                    else if (w.isKeyPressed(GLFW.GLFW_KEY_S)) {
                        camera.panBackward(distance);
                    }
                    if (w.isKeyPressed(GLFW.GLFW_KEY_A)) {
                        camera.panLeft(distance);
                    }
                    else if (w.isKeyPressed(GLFW.GLFW_KEY_D)) {
                        camera.panRight(distance);
                    }
                    if (w.isKeyPressed(GLFW.GLFW_KEY_Z)) {
                        camera.panUp(distance);
                    }
                    else if (w.isKeyPressed(GLFW.GLFW_KEY_X)) {
                        camera.panDown(distance);
                    }
                }
            }))
            .keyUpEventHandler(GLFW.GLFW_KEY_P, () -> {
                switch (engine.getState()) {
                    case RUNNING -> engine.setState(PAUSED);
                    case PAUSED -> engine.setState(RUNNING);
                }
            })
            .keyUpEventHandler(GLFW.GLFW_KEY_I, () -> {
                LOGGER.debug("state: {}, FPS: {}, UPS: {}",
                    engine.getState(), engine.getFramesPerSecond(), engine.getUpdatesPerSecond());
            })
            .build();

        return window;
    }

    private static Scene newScene() {
        /*
         * Set up lights.
         */
        final var lightManager = new DefaultLightManager();
        lightManager.setSpecularPower(10f);
        lightManager.setAmbient(new Light.Ambient(new Vector3f(0.3f, 0.3f, 0.3f)));
        final var light = PointLight.builder()
            .color(new Vector3f(1f, 1f, 1f))
            .position(new Vector3f(0f, 0f, 0f))
            .intensity(10f)
            .attenuation(new Attenuation(0f, 0f, 1f))
            .build();
        lightManager.addPositionalLight(light);


        /*
         * Set up entities.
         */
        final var entityManager = new DefaultEntityManager();

        final var planeMesh = new Plane();
        final var cubeMesh = new Cube();

        final var yellowMaterial = Color.builder().red(1.0f).green(1.0f).build();
        final var redMaterial = Color.builder().red(1.0f).build();
        final var blueMaterial = Color.builder().blue(1.0f).build();
        final var grayMaterial = Color.builder().red(0.4f).green(0.4f).blue(0.4f).build();

        final Script rotateScript = (e, em, dt) -> {
            var speed = 5f;
            var delta = speed * dt;
            final var transform = em.getComponent(e, Transform.class).orElseThrow();
            var rot = transform.getRotation().x() + delta;
            if (rot > 360) {
                rot = 0;
            }
            transform.setRotation((float) rot, (float) rot, (float) rot);
        };

        final var yellowCube = entityManager.newEntity("yellowCube");
        entityManager.putComponent(yellowCube, cubeMesh);
        entityManager.putComponent(yellowCube, yellowMaterial);
        entityManager.putComponent(yellowCube, rotateScript);
        entityManager.putComponent(yellowCube, Transform.builder()
            .position(new Vector3f(3f, 0f, 3f))
            .build()
        );

        final var redCube = entityManager.newEntity("redCube");
        entityManager.putComponent(redCube, cubeMesh);
        entityManager.putComponent(redCube, redMaterial);
        entityManager.putComponent(redCube, rotateScript);
        entityManager.putComponent(redCube, Transform.builder()
            .position(new Vector3f(-3f, 0f, 3f))
            .build()
        );

        final var blueCube = entityManager.newEntity("blueCube");
        entityManager.putComponent(blueCube, cubeMesh);
        entityManager.putComponent(blueCube, blueMaterial);
        entityManager.putComponent(blueCube, rotateScript);
        entityManager.putComponent(blueCube, Transform.builder()
            .position(new Vector3f(0f, 0f, -3f))
            .build()
        );

        // "ground"
        final var ground = entityManager.newEntity("ground");
        entityManager.putComponent(ground, planeMesh);
        entityManager.putComponent(ground, grayMaterial);
        entityManager.putComponent(ground, Transform.builder()
            .rotation(new Vector3f(270f, 0f, 0f))
            .position(new Vector3f(0f, -1f, 0f))
            .scale(new Vector3f(10f, 10f, 1f))
            .build()
        );

        return new Scene("Hello, world!", entityManager, lightManager);
    }
}
