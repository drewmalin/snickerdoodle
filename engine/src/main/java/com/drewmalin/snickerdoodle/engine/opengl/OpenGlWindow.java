package com.drewmalin.snickerdoodle.engine.opengl;

import com.drewmalin.snickerdoodle.engine.Engine;
import com.drewmalin.snickerdoodle.engine.camera.Camera;
import com.drewmalin.snickerdoodle.engine.ecs.system.InputSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.NoopInputSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.NoopRenderSystem;
import com.drewmalin.snickerdoodle.engine.ecs.system.RenderSystem;
import com.drewmalin.snickerdoodle.engine.scene.Scene;
import com.drewmalin.snickerdoodle.engine.window.Window;
import org.joml.Vector2d;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OpenGlWindow
    implements Window {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGlWindow.class);
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final String DEFAULT_TITLE = "OpenGL";
    private static final InputSystem DEFAULT_INPUT_SYSTEM = NoopInputSystem.INSTANCE;
    private static final RenderSystem DEFAULT_RENDER_SYSTEM = NoopRenderSystem.INSTANCE;

    private final long windowHandle;
    private final String title;
    private final Vector2d mousePosition;
    private final boolean vSync;
    private final Map<Integer, Integer> mouseButtonStatus;
    private final Set<Callback> callbacks;
    private final InputSystem inputSystem;
    private final RenderSystem renderSystem;

    private Camera camera;
    private int width;
    private int height;
    private boolean shouldResize;

    private OpenGlWindow(final Builder builder) {
        this.vSync = builder.vSync;
        this.inputSystem = builder.inputSystem == null ? DEFAULT_INPUT_SYSTEM : builder.inputSystem;
        this.renderSystem = builder.renderSystem == null ? DEFAULT_RENDER_SYSTEM : builder.renderSystem;
        this.camera = builder.camera;

        this.width = builder.width == 0 ? DEFAULT_WIDTH : builder.width;
        this.height = builder.height == 0 ? DEFAULT_HEIGHT : builder.height;
        this.title = builder.title == null ? DEFAULT_TITLE : builder.title;

        this.mouseButtonStatus = new HashMap<>();
        this.mousePosition = new Vector2d();

        this.windowHandle = initializeGLFWWindow();
        this.callbacks = new HashSet<>();


        /*
         * Set up Input system
         */
        final var keyUpCallbacks = new HashMap<Integer, Runnable>();
        // Special case callback (can be removed later): close the window on "ESC"
        keyUpCallbacks.put(GLFW.GLFW_KEY_ESCAPE, () -> GLFW.glfwSetWindowShouldClose(this.windowHandle, true));
        keyUpCallbacks.putAll(builder.keyUpCallbacks);
        this.callbacks.add(setKeyUpCallbacks(this.windowHandle, keyUpCallbacks));

        this.callbacks.add(setResizeCallback(this.windowHandle));
        this.callbacks.add(setMousePositionCallback(this.windowHandle));
        this.callbacks.add(setMouseButtonCallback(this.windowHandle));
    }

    private long initializeGLFWWindow() {
        // GLFW requires initialization prior to almost any usage of the library.
        if (!GLFW.glfwInit()) {
            LOGGER.error("Failed to initialize GLFW");
            throw new IllegalStateException("Failed to initialize GLFW");
        }

        // Reset window characteristics
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        // Initialize window
        final var window = GLFW.glfwCreateWindow(this.width, this.height, this.title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            LOGGER.error("Failed to create the GLFW window");
            throw new RuntimeException("Failed to create the GLFW window");
        }
        GLFW.glfwMakeContextCurrent(window);

        setWindowPositionToCenter(window);
        setWindowVerticalSync(window);
        setWindowAsVisible(window);
        setWindowGLContext(window);

        return window;
    }

    private Callback setKeyUpCallbacks(final long window, final Map<Integer, Runnable> keyUpCallbacks) {
        return GLFW.glfwSetKeyCallback(window, (windowHandle, keyHandle, scancode, action, mods) -> {
            /*
             * For each callback...
             */
            keyUpCallbacks.forEach((keyCode, callback) -> {
                /*
                 * If the mapped key is detected as being released (key-up)...
                 */
                if (keyHandle == keyCode && action == GLFW.GLFW_RELEASE) {
                    /*
                     * Run the callback!
                     */
                    callback.run();
                }
            });
        });
    }

    private GLFWFramebufferSizeCallback setResizeCallback(final long window) {
        return GLFW.glfwSetFramebufferSizeCallback(window, (windowHandle, width, height) -> {
            this.width = width;
            this.height = height;
            this.shouldResize = true;
        });
    }

    private GLFWCursorPosCallback setMousePositionCallback(final long window) {
        return GLFW.glfwSetCursorPosCallback(window, (windowHandle, x, y) -> {
            this.mousePosition.x = x;
            this.mousePosition.y = y;
        });
    }

    private GLFWMouseButtonCallback setMouseButtonCallback(final long window) {
        return GLFW.glfwSetMouseButtonCallback(window, (windowHandle, button, action, mode) -> {
            this.mouseButtonStatus.put(button, action);
        });
    }

    private void setWindowPositionToCenter(final long window) {
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer widthPointer = stack.mallocInt(1);
            final IntBuffer heightPointer = stack.mallocInt(1);

            // set the size values in the pointers
            GLFW.glfwGetWindowSize(window, widthPointer, heightPointer);

            final GLFWVidMode mode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

            // center the window
            final int posX = (mode.width() - widthPointer.get(0)) / 2;
            final int posY = (mode.height() - heightPointer.get(0)) / 2;
            GLFW.glfwSetWindowPos(window, posX, posY);
        }
    }

    private void setWindowVerticalSync(final long window) {
        if (this.vSync) {
            GLFW.glfwSwapInterval(1);
        }
    }

    private void setWindowAsVisible(final long window) {
        GLFW.glfwShowWindow(window);
    }

    private void setWindowGLContext(final long window) {
        GL.createCapabilities();
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public Camera getCamera() {
        return this.camera;
    }

    @Override
    public boolean isKeyPressed(final int keyCode) {
        return GLFW.glfwGetKey(this.windowHandle, keyCode) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean isMouseButtonPressed(final int buttonCode) {
        if (!this.mouseButtonStatus.containsKey(buttonCode)) {
            return false;
        }
        return this.mouseButtonStatus.get(buttonCode) == GLFW.GLFW_PRESS;
    }

    @Override
    public Vector2d getMousePosition() {
        return this.mousePosition;
    }

    @Override
    public void update(final Engine engine, final Scene scene, final double dt) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        if (this.shouldResize) {
            LOGGER.debug("resizing window to {} x {}", this.width, this.height);
            GL11.glViewport(0, 0, this.width, this.height);
            this.shouldResize = false;
        }

        this.renderSystem.update(engine, this, scene);
        this.inputSystem.update(engine, this, dt);

        GLFW.glfwSwapBuffers(this.windowHandle);
        GLFW.glfwPollEvents();
    }

    @Override
    public void destroy() {
        for (final Callback callback : this.callbacks) {
            if (callback == null) {
                continue;
            }
            callback.close();
        }
        this.inputSystem.destroy();
        this.renderSystem.destroy();
        GLFW.glfwDestroyWindow(this.windowHandle);
        LOGGER.debug("OpenGL window destroyed");
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isClosed() {
        return GLFW.glfwWindowShouldClose(this.windowHandle);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "OpenGlWindow["
            + "title='" + this.title + "', "
            + "width=" + this.width + ", "
            + "height=" + this.height + ", "
            + "shouldResize=" + this.shouldResize
            + ']';
    }

    public static class Builder {

        private final Map<Integer, Runnable> keyUpCallbacks;
        private int width;
        private int height;
        private String title;
        private boolean vSync;
        private InputSystem inputSystem;
        private Camera camera;
        private RenderSystem renderSystem;

        private Builder() {
            this.keyUpCallbacks = new HashMap<>();
        }

        public Builder width(final int width) {
            this.width = width;
            return this;
        }

        public Builder height(final int height) {
            this.height = height;
            return this;
        }

        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        public Builder vSync(final boolean vSync) {
            this.vSync = vSync;
            return this;
        }

        public Builder inputSystem(final InputSystem inputSystem) {
            this.inputSystem = inputSystem;
            return this;
        }

        public Builder camera(final Camera camera) {
            this.camera = camera;
            return this;
        }

        public Builder keyUpEventHandler(final int keyCode, final Runnable runnable) {
            this.keyUpCallbacks.put(keyCode, runnable);
            return this;
        }

        public Builder renderSystem(final RenderSystem renderSystem) {
            this.renderSystem = renderSystem;
            return this;
        }

        public OpenGlWindow build() {
            return new OpenGlWindow(this);
        }
    }
}
