package com.drewmalin.snickerdoodle.engine.window;

import org.joml.Vector2d;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class OpenGlWindow implements Window {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGlWindow.class);

    private final long windowHandle;
    private final String title;
    private final Vector2d mousePosition;
    private final boolean vSync;
    private final Map<Integer, Integer> mouseButtonStatus;

    private final GLFWKeyCallback closeWindowHotkeyCallback;
    private final GLFWFramebufferSizeCallback resizeWindowCallback;
    private final GLFWCursorPosCallback mousePositionCallback;
    private final GLFWMouseButtonCallback mouseButtonCallback;

    private int width;
    private int height;
    private boolean shouldResize;

    public OpenGlWindow(final int width, final int height, final String title, final boolean vSync) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.vSync = vSync;
        this.mouseButtonStatus = new HashMap<>();
        this.mousePosition = new Vector2d();

        this.windowHandle = initializeGLFWWindow();
        this.closeWindowHotkeyCallback = setWindowCloseHotkey(this.windowHandle, GLFW.GLFW_KEY_ESCAPE);
        this.resizeWindowCallback = setResizeCallback(this.windowHandle);
        this.mousePositionCallback = setMousePositionCallback(this.windowHandle);
        this.mouseButtonCallback = setMouseButtonCallback(this.windowHandle);
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
        setWindowPositionToCenter(window);
        setWindowVerticalSync(window);
        setWindowAsVisible(window);
        setWindowGLContext(window);

        return window;
    }


    private GLFWKeyCallback setWindowCloseHotkey(final long window, final long key) {
        return GLFW.glfwSetKeyCallback(window, (windowHandle, keyHandle, scancode, action, mods) -> {
            // event detected on escape key
            if (keyHandle == key) {
                // event is of type "release" (key-up)
                if (action == GLFW.GLFW_RELEASE) {
                    GLFW.glfwSetWindowShouldClose(windowHandle, true);
                }
            }
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
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwShowWindow(window);
    }

    private void setWindowGLContext(final long window) {
        GL.createCapabilities();
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public boolean isClosed() {
        return GLFW.glfwWindowShouldClose(this.windowHandle);
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
    public void update(final Runnable onUpdate) {
        preUpdate();
        onUpdate.run();
        postUpdate();
    }

    private void preUpdate() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        if (this.shouldResize) {
            LOGGER.debug("resizing window to {} x {}", this.width, this.height);
            GL11.glViewport(0, 0, this.width, this.height);
            this.shouldResize = false;
        }
    }

    private void postUpdate() {
        GLFW.glfwSwapBuffers(this.windowHandle);
        GLFW.glfwPollEvents();
    }

    @Override
    public void destroy() {
        if (this.closeWindowHotkeyCallback != null) {
            this.closeWindowHotkeyCallback.close();
        }
        if (this.resizeWindowCallback != null) {
            this.resizeWindowCallback.close();
        }
        if (this.mousePositionCallback != null) {
            this.mousePositionCallback.close();
        }
        if (this.mouseButtonCallback != null) {
            this.mouseButtonCallback.close();
        }
        GLFW.glfwDestroyWindow(this.windowHandle);
        LOGGER.debug("OpenGL window destroyed");
    }

    @Override
    public boolean isVerticalSyncEnabled() {
        return this.vSync;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
