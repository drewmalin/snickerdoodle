package com.drewmalin.snickerdoodle.game;

import com.drewmalin.snickerdoodle.engine.EngineFactory;
import com.drewmalin.snickerdoodle.engine.camera.DefaultCamera;
import com.drewmalin.snickerdoodle.engine.ecs.component.Color;
import com.drewmalin.snickerdoodle.engine.ecs.component.Transform;
import com.drewmalin.snickerdoodle.engine.ecs.entity.DefaultEntityManager;
import com.drewmalin.snickerdoodle.engine.ecs.system.DefaultScriptSystem;
import com.drewmalin.snickerdoodle.engine.light.Attenuation;
import com.drewmalin.snickerdoodle.engine.light.DefaultLightManager;
import com.drewmalin.snickerdoodle.engine.light.Light;
import com.drewmalin.snickerdoodle.engine.light.PointLight;
import com.drewmalin.snickerdoodle.engine.model.ColoredCube;
import com.drewmalin.snickerdoodle.engine.model.ColoredPlane;
import com.drewmalin.snickerdoodle.engine.model.TexturedCube;
import com.drewmalin.snickerdoodle.engine.opengl.OpenGlEngine;
import com.drewmalin.snickerdoodle.engine.opengl.OpenGlInputSystem;
import com.drewmalin.snickerdoodle.engine.opengl.OpenGlRenderSystem;
import com.drewmalin.snickerdoodle.engine.opengl.OpenGlWindow;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.script.Script;
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
            .inputSystem(new OpenGlInputSystem((e, w, mouseCursorDelta, dt) -> {
                if (e.getState() == RUNNING) {
                    final var distance = (float) (60f * dt);

                    // WASD
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

                    // Raise / Lower
                    if (w.isKeyPressed(GLFW.GLFW_KEY_Z)) {
                        camera.panUp(distance);
                    }
                    else if (w.isKeyPressed(GLFW.GLFW_KEY_X)) {
                        camera.panDown(distance);
                    }

                    // Use arrow keys to move lights
                    final var lightDistance = distance * .05f;
                    var lightForward = 0f;
                    var lightleft = 0f;
                    var lightUp = 0f;

                    if (w.isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                        if (w.isKeyPressed(GLFW.GLFW_KEY_UP)) {
                            lightUp = lightDistance;
                        }
                        else if (w.isKeyPressed(GLFW.GLFW_KEY_DOWN)) {
                            lightUp = -lightDistance;
                        }
                    }
                    else {
                        if (w.isKeyPressed(GLFW.GLFW_KEY_UP)) {
                            lightForward = -lightDistance;
                        }
                        else if (w.isKeyPressed(GLFW.GLFW_KEY_DOWN)) {
                            lightForward = lightDistance;
                        }
                    }
                    if (w.isKeyPressed(GLFW.GLFW_KEY_LEFT)) {
                        lightleft = -lightDistance;
                    }
                    else if (w.isKeyPressed(GLFW.GLFW_KEY_RIGHT)) {
                        lightleft = lightDistance;
                    }

                    for (var light : e.getScene().getLightManager().getPositionalLights()) {
                        final var position = light.getPosition();
                        light.setPosition(
                            new Vector3f(
                                position.x + lightleft,
                                position.y + lightUp,
                                position.z + lightForward)
                        );
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

        final Script rotateScript = (e, em, dt) -> {
            var speed = 50f;
            var delta = speed * dt;
            final var transform = em.getComponent(e, Transform.class).orElseThrow();
            var rot = transform.getRotation().x() + delta;
            if (rot > 360) {
                rot = 0;
            }
            transform.setRotation((float) rot, (float) rot, (float) 0);
        };

        /*
         * A yellow cube
         */
        final var yellowCube = entityManager.newEntity("yellowCube");
        final var yellowCubeModel = new ColoredCube(Color.yellow());
        entityManager.putComponent(yellowCube, yellowCubeModel.getMesh());
        entityManager.putComponent(yellowCube, yellowCubeModel.getMaterial());
        entityManager.putComponent(yellowCube, rotateScript);
        entityManager.putComponent(yellowCube, Transform.builder()
            .position(new Vector3f(3f, 0f, 3f))
            .build()
        );

        /*
         * A red cube
         */
        final var redCube = entityManager.newEntity("redCube");
        final var redCubeModel = new ColoredCube(Color.red());
        entityManager.putComponent(redCube, redCubeModel.getMesh());
        entityManager.putComponent(redCube, redCubeModel.getMaterial());
        entityManager.putComponent(redCube, rotateScript);
        entityManager.putComponent(redCube, Transform.builder()
            .position(new Vector3f(-3f, 0f, 3f))
            .build()
        );

        /*
         * A textured cube
         */
        final var texturedCube = entityManager.newEntity("texturedCube");
        final var texturedCubeModel = new TexturedCube("/textures/debug.png");
        entityManager.putComponent(texturedCube, texturedCubeModel.getMesh());
        entityManager.putComponent(texturedCube, texturedCubeModel.getMaterial());
        entityManager.putComponent(texturedCube, rotateScript);
        entityManager.putComponent(texturedCube, Transform.builder()
            .position(new Vector3f(0f, 0f, -3f))
            .build()
        );

        /*
         * The "ground"
         */
        final var ground = entityManager.newEntity("ground");
        final var groundModel = new ColoredPlane(Color.gray());
        entityManager.putComponent(ground, groundModel.getMesh());
        entityManager.putComponent(ground, groundModel.getMaterial());
        entityManager.putComponent(ground, Transform.builder()
            .rotation(new Vector3f(270f, 0f, 0f))
            .position(new Vector3f(0f, -1f, 0f))
            .scale(new Vector3f(10f, 10f, 1f))
            .build()
        );

        return new Scene("Hello, world!", entityManager, lightManager);
    }
}
