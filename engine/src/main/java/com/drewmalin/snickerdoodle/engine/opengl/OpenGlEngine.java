package com.drewmalin.snickerdoodle.engine.opengl;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.camera.Camera;
import com.drewmalin.snickerdoodle.engine.ecs.system.InputSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.RenderSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.ScriptSystem;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.timer.Timer;
import com.drewmalin.snickerdoodle.engine.window.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class OpenGlEngine implements Engine {

    private final Camera camera;
    private final Window window;
    private final Timer timer;

    private final InputSystem.Callback inputHandler;
    private final InputSystem inputSystem;
    private final RenderSystem renderSystem;
    private final ScriptSystem scriptSystem;

    private final int maxFramesPerSecond;
    private final GLFWErrorCallback glfwStderrCallback;

    private Scene scene;
    private State state;

    public OpenGlEngine(final Builder builder) {
        this.state = State.RUNNING;

        this.camera = builder.camera;
        this.window = builder.window;
        this.timer = builder.timer;
        this.inputHandler = builder.inputHandler;
        this.inputSystem = builder.inputSystem;
        this.renderSystem = builder.renderSystem;
        this.scriptSystem = builder.scriptSystem;

        this.maxFramesPerSecond = builder.maxFramesPerSecond;

        this.glfwStderrCallback = GLFW.glfwSetErrorCallback(
                GLFWErrorCallback.createPrint(java.lang.System.err)
        );
    }

    @Override
    public Camera getCamera() {
        return this.camera;
    }

    @Override
    public Window getWindow() {
        return this.window;
    }

    @Override
    public Timer getTimer() {
        return this.timer;
    }

    @Override
    public void run() {
//        if (this.state != State.RUNNING) {
//            return;
//        }
        try {
            while (!this.window.isClosed()) {
                if (this.scene == null) {
                    throw new IllegalStateException("Scene cannot be null");
                }

                this.timer.update();

                this.inputSystem.update(this.window, this.inputHandler);
                this.scriptSystem.update(this.scene);
                this.window.update(() ->
                        this.renderSystem.update(this.scene, this.window, this.camera)
                );

                syncFrameRate();
            }
        } finally {
            this.window.destroy();
            close();
        }
    }

    private void syncFrameRate() {
        if (!this.window.isVerticalSyncEnabled()) {
            return;
        }
        final var maxFrameIntervalSeconds = 1.0 / this.maxFramesPerSecond;
        while (this.timer.getDeltaTime() < maxFrameIntervalSeconds) {
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                // empty
            }
        }
    }

    @Override
    public void close() {
        if (this.glfwStderrCallback != null) {
            this.glfwStderrCallback.close();
        }
        GLFW.glfwTerminate();
    }

    @Override
    public void setState(final State state) {
        this.state = state;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void setScene(final Scene scene) {
        if (scene == null) {
            throw new IllegalArgumentException("Scene cannot be null");
        }
        this.scene = scene;
    }

    public static class Builder {

        private static final int DEFAULT_MAX_FRAMES_PER_SECOND = 60;

        private Camera camera;
        private Window window;
        private Timer timer;
        public InputSystem.Callback inputHandler;
        private InputSystem inputSystem;
        private RenderSystem renderSystem;
        private ScriptSystem scriptSystem;

        private int maxFramesPerSecond = DEFAULT_MAX_FRAMES_PER_SECOND;

        private Builder() {

        }

        public Builder camera(final Camera camera) {
            this.camera = camera;
            return this;
        }

        public Builder window(final Window window) {
            this.window = window;
            return this;
        }

        public Builder timer(final Timer timer) {
            this.timer = timer;
            return this;
        }

        public Builder inputHandler(final InputSystem.Callback inputHandler) {
            this.inputHandler = inputHandler;
            return this;
        }

        public Builder inputSystem(final InputSystem inputSystem) {
            this.inputSystem = inputSystem;
            return this;
        }

        public Builder renderSystem(final RenderSystem renderSystem) {
            this.renderSystem = renderSystem;
            return this;
        }

        public Builder scriptSystem(final ScriptSystem scriptSystem) {
            this.scriptSystem = scriptSystem;
            return this;
        }

        public Builder maxFramesPerSecond(final int maxFramesPerSecond) {
            this.maxFramesPerSecond = maxFramesPerSecond;
            return this;
        }

        public OpenGlEngine build() {
            return new OpenGlEngine(this);
        }


    }
}
