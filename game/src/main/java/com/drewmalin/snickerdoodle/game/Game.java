package com.drewmalin.snickerdoodle.game;

import com.drewmalin.snickerdoodle.engine.EngineFactory;
import com.drewmalin.snickerdoodle.engine.camera.DefaultCamera;
import com.drewmalin.snickerdoodle.engine.ecs.component.Color;
import com.drewmalin.snickerdoodle.engine.ecs.component.Cube;
import com.drewmalin.snickerdoodle.engine.ecs.component.Plane;
import com.drewmalin.snickerdoodle.engine.ecs.component.Transform;
import com.drewmalin.snickerdoodle.engine.ecs.entity.DefaultEntityManager;
import com.drewmalin.snickerdoodle.engine.ecs.entity.EntityManager;
import com.drewmalin.snickerdoodle.engine.ecs.system.DefaultInputSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.DefaultRenderSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.DefaultScriptSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.InputSystem;
import com.drewmalin.snickerdoodle.engine.light.*;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.script.Script;
import com.drewmalin.snickerdoodle.engine.timer.OpenGlTimer;
import com.drewmalin.snickerdoodle.engine.window.OpenGlWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Game {

    private static final Logger LOGGER = LogManager.getLogger(Game.class);

    public static void main(final String[] args) {
        LOGGER.info("Instantiating Engine");

        final var camera = new DefaultCamera();

        final var engine = EngineFactory.openGlEngineBuilder()
                .camera(camera)
                .window(newWindow())
                .timer(new OpenGlTimer())
                .inputHandler(newInputHandler(camera))
                .inputSystem(new DefaultInputSystem())
                .renderSystem(new DefaultRenderSystem())
                .scriptSystem(new DefaultScriptSystem())
                .maxFramesPerSecond(60)
                .build();

        final var scene = newScene(new DefaultEntityManager(), new DefaultLightManager());

        engine.setScene(scene);

        LOGGER.info("Starting Engine");
        engine.run();
    }

    private static Scene newScene(final EntityManager entityManager, final LightManager lightManager) {
        final var scene = new Scene(entityManager, lightManager);

        /*
         * Set up lights.
         */
        final var lightMngr = scene.getLightManager();
        lightMngr.setSpecularPower(10f);
        lightMngr.setAmbient(new Light.Ambient(new Vector3f(0.3f, 0.3f, 0.3f)));
        final var light = PointLight.builder()
                .color(new Vector3f(1f, 1f, 1f))
                .position(new Vector3f(0f, 0f, 0f))
                .intensity(10f)
                .attenuation(new Attenuation(0f, 0f, 1f))
                .build();
        lightMngr.addPositionalLight(light);


        /*
         * Set up entities.
         */
        final var entityMngr = scene.getEntityManager();

        final var planeMesh = new Plane();
        final var cubeMesh = new Cube();

        final var yellowMaterial = Color.builder().red(1.0f).green(1.0f).build();
        final var redMaterial = Color.builder().red(1.0f).build();
        final var blueMaterial = Color.builder().blue(1.0f).build();
        final var grayMaterial = Color.builder().red(0.4f).green(0.4f).blue(0.4f).build();


        final Script rotateScript = (e, em) -> {
            final var transform = em.getComponent(e, Transform.class).orElseThrow();
            var rot = transform.getRotation().x() + 1.5f;
            if (rot > 360) {
                rot = 0;
            }
            transform.setRotation(rot, rot, rot);
        };

        final var yellowCube = entityMngr.newEntity();
        entityMngr.putComponent(yellowCube, cubeMesh);
        entityMngr.putComponent(yellowCube, yellowMaterial);
        entityMngr.putComponent(yellowCube, rotateScript);
        entityMngr.putComponent(yellowCube, Transform.builder()
                .position(new Vector3f(3f, 0f, 3f))
                .build()
        );

        final var redCube = entityMngr.newEntity();
        entityMngr.putComponent(redCube, cubeMesh);
        entityMngr.putComponent(redCube, redMaterial);
        entityMngr.putComponent(redCube, rotateScript);
        entityMngr.putComponent(redCube, Transform.builder()
                .position(new Vector3f(-3f, 0f, 3f))
                .build()
        );

        final var blueCube = entityMngr.newEntity();
        entityMngr.putComponent(blueCube, cubeMesh);
        entityMngr.putComponent(blueCube, blueMaterial);
        entityMngr.putComponent(blueCube, rotateScript);
        entityMngr.putComponent(blueCube, Transform.builder()
                .position(new Vector3f(0f, 0f, -3f))
                .build()
        );

        // "ground"
        final var ground = entityMngr.newEntity();
        entityMngr.putComponent(ground, planeMesh);
        entityMngr.putComponent(ground, grayMaterial);
        entityMngr.putComponent(ground, Transform.builder()
                .rotation(new Vector3f(270f, 0f, 0f))
                .position(new Vector3f(0f, -1f, 0f))
                .scale(new Vector3f(10f, 10f, 1f))
                .build()
        );

        return scene;
    }

    private static OpenGlWindow newWindow() {
        return new OpenGlWindow(500, 500, "Snickerdoodle", true);
    }

    private static InputSystem.Callback newInputHandler(DefaultCamera camera) {
        return (window, mouseCursorDelta) -> {
            if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
                camera.panForward(1);
            } else if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
                camera.panBackward(1);
            }
            if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
                camera.panLeft(1);
            } else if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
                camera.panRight(1);
            }
            if (window.isKeyPressed(GLFW.GLFW_KEY_Z)) {
                camera.panUp(1);
            } else if (window.isKeyPressed(GLFW.GLFW_KEY_X)) {
                camera.panDown(1);
            }
        };
    }

}
