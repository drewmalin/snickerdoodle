package com.drewmalin.snickerdoodle.engine.opengl;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.camera.Camera;
import com.drewmalin.snickerdoodle.engine.ecs.system.RenderSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.ScriptSystem;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.timer.Timer;
import com.drewmalin.snickerdoodle.engine.window.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class OpenGlEngine
    implements Engine {

    private static final Logger LOGGER = LogManager.getLogger(OpenGlEngine.class);

    private final RenderSystem renderSystem;
    private final ScriptSystem scriptSystem;

    private final int maxUpdatesPerSecond;
    private final int maxUpdatesPerFrame;
    private final int maxFramesPerSecond;
    private final GLFWErrorCallback glfwStderrCallback;

    private double secondsElapsedSinceLastRender;
    private double secondsElapsedSinceLastUpdate;
    private double lastRenderTimeSeconds;
    private double lastUpdateTimeSeconds;

    private Scene scene;
    private State state;
    private Window window;

    public OpenGlEngine(final Builder builder) {
        this.state = State.RUNNING;

        this.renderSystem = builder.renderSystem;
        this.scriptSystem = builder.scriptSystem;

        this.maxUpdatesPerSecond = builder.maxUpdatesPerSecond;
        this.maxUpdatesPerFrame = builder.maxUpdatesPerFrame;
        this.maxFramesPerSecond = builder.maxFramesPerSecond;

        this.glfwStderrCallback = GLFW.glfwSetErrorCallback(
            GLFWErrorCallback.createPrint(java.lang.System.err)
        );
    }

    @Override
    public void setWindow(final Window window) {
        this.window = window;
    }

    @Override
    public Window getWindow() {
        return this.window;
    }

    @Override
    public void run() {
        try {

            if (this.scene == null) {
                throw new IllegalStateException("Scene cannot be null");
            }

            /*
             * Calculate the value of the seconds per update and seconds per render -- these will be used to more easily
             * compare elapsed time versus these intervals.
             */
            final var secondsPerUpdate = 1.0 / this.maxUpdatesPerSecond;
            final var secondsPerRender = 1.0 / this.maxFramesPerSecond;

            /*
             * Set the last update and render time to "now" to seed the loop with some realistic value.
             */
            var lastUpdateTimeSeconds = GLFW.glfwGetTime();
            var lastRenderTimeSeconds = lastUpdateTimeSeconds;
            var updateTimeBufferSeconds = 0D;

            while (!this.window.isClosed()) {

                final var now = GLFW.glfwGetTime();

                /*
                 * Update the "time buffer" by adding the elapsed time that has occurred since the last loop. Once
                 * complete, set the "last updated time" to now. This time buffer is the accumulation of time elapsed,
                 * meaning that we need our game to "catch up" and perform a number of updates equal to the number
                 * of 'secondsPerUpdate' that fit into the time buffer. Note that this may result in a small remainder
                 * which will be left over and passed to the next loop.
                 */
                updateTimeBufferSeconds += now - lastUpdateTimeSeconds;

                /*
                 * While the time buffer contains update intervals, update the game state. This guarantees that each
                 * "tick" is consistent. Note that this means that the time delta passed to each update here is fixed.
                 */
                var updatesPerformed = 0;
                while (updateTimeBufferSeconds >= secondsPerUpdate) {
                    this.scriptSystem.update(this, this.scene, secondsPerUpdate);
                    updateTimeBufferSeconds -= secondsPerUpdate;

                    recordUpdateTick();

                    /*
                     * Question: what if a given update loop below exceeds a "tick"? This could result in this loop
                     * taking so long that the next render update is delayed, and the next iteration of the full main
                     * loop calculates a large time buffer. In the degenerate case, if each update iteration takes
                     * significantly longer than the update interval, then this problem compounds.
                     *
                     * So as a band-aid, the below will only perform some maximum number of updates per frame.
                     */
                    updatesPerformed++;
                    if (updatesPerformed > this.maxUpdatesPerFrame) {
                        break;
                    }
                }

                /*
                 * We should render only if the amount of time elapsed since the last loop meets or exceeds the
                 * established rendering rate.
                 */
                var renderDeltaTimeSeconds = now - lastRenderTimeSeconds;
                var shouldRender = renderDeltaTimeSeconds >= secondsPerRender;

                if (shouldRender) {
                    this.window.update(this, this.scene, renderDeltaTimeSeconds);

                    recordFrameRender();
                    lastRenderTimeSeconds = now;
                }

                /*
                 * Reset the last updated time to "now" to prepare for the next loop. We do not set this value to the
                 * actual value of GLFW.lfwGetTime() as we want a more realistic difference between two subsequent
                 * loops.
                 */
                lastUpdateTimeSeconds = now;
            }
        }
        finally {
            this.window.destroy();
            close();
        }
    }

    private void recordFrameRender() {
        this.secondsElapsedSinceLastRender = GLFW.glfwGetTime() - this.lastRenderTimeSeconds;
        this.lastRenderTimeSeconds = GLFW.glfwGetTime();
    }

    private void recordUpdateTick() {
        this.secondsElapsedSinceLastUpdate = GLFW.glfwGetTime() - this.lastUpdateTimeSeconds;
        this.lastUpdateTimeSeconds = GLFW.glfwGetTime();
    }

    @Override
    public double getFramesPerSecond() {
        return this.secondsElapsedSinceLastRender == 0 ? 0 : 1 / this.secondsElapsedSinceLastRender;
    }

    @Override
    public double getUpdatesPerSecond() {
        return this.secondsElapsedSinceLastUpdate == 0 ? 0 : 1 / this.secondsElapsedSinceLastUpdate;
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

    @Override
    public State getState() {
        return this.state;
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

        private static final int DEFAULT_MAX_UPDATES_PER_SECOND = 60;
        private static final int DEFAULT_MAX_FRAMES_PER_SECOND = 60;
        private static final int DEFAULT_MAX_UPDATES_PER_FRAME = 500;

        private RenderSystem renderSystem;
        private ScriptSystem scriptSystem;

        private int maxUpdatesPerSecond = DEFAULT_MAX_UPDATES_PER_SECOND;
        private int maxUpdatesPerFrame = DEFAULT_MAX_UPDATES_PER_FRAME;
        private int maxFramesPerSecond = DEFAULT_MAX_FRAMES_PER_SECOND;

        private Builder() {

        }

        public Builder scriptSystem(final ScriptSystem scriptSystem) {
            this.scriptSystem = scriptSystem;
            return this;
        }

        public Builder maxFramesPerSecond(final int maxFramesPerSecond) {
            this.maxFramesPerSecond = maxFramesPerSecond;
            return this;
        }

        public Builder maxUpdatesPerSecond(final int maxUpdatesPerSecond) {
            this.maxUpdatesPerSecond = maxUpdatesPerSecond;
            return this;
        }

        public Builder maxUpdatesPerFrame(final int maxUpdatesPerFrame) {
            this.maxUpdatesPerFrame = maxUpdatesPerFrame;
            return this;
        }

        public OpenGlEngine build() {
            return new OpenGlEngine(this);
        }

    }
}
