package pl.edu.agh.firecell.core;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.joml.Vector2i;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.util.LoggingOutputStream;
import pl.edu.agh.firecell.model.SimulationConfig;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Window {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // need to be same as in glsl shaders
    private static final String GLSL_VERSION = "#version 330";

    private final String name;
    private Vector2i size;

    private long glfwWindow;
    private ImGuiImplGlfw imGuiGlfw;
    private ImGuiImplGl3 imGuiGl3;

    private Scene scene;
    private final IOListener ioListener = new IOListener();

    public Window(int initialWidth, int initialHeight, String name) {
        size = new Vector2i(initialWidth, initialHeight);
        this.name = name;

        initializeGLFW();
        initializeOpenGL();
        initializeImGui();

        ioListener.windowSizeObservable().subscribe(size -> {
            this.size = size;
            glViewport(0, 0, size.x, size.y);
        });
    }

    public void run() {

        double startFrameTime = glfwGetTime();
        double frameTime = 0.0;

        scene = new MenuScene(this::startSimulation);

        while (!glfwWindowShouldClose(glfwWindow)) {
            glfwPollEvents();
            imGuiGlfw.newFrame();
            ImGui.newFrame();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            scene.update(frameTime);

            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());
            if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                final long backupWindowPtr = glfwGetCurrentContext();
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                glfwMakeContextCurrent(backupWindowPtr);
            }
            glfwSwapBuffers(glfwWindow);

            if (IOListener.isPressed(GLFW_KEY_ESCAPE)) {
                glfwSetWindowShouldClose(glfwWindow, true);
            }

            frameTime = glfwGetTime() - startFrameTime;
            startFrameTime = glfwGetTime();
        }
        scene.dispose();
        dispose();
    }

    private void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();

        Callbacks.glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void startSimulation(SimulationConfig config) {
        try {
            var simulationScene = new SimulationScene(config, this::finishSimulation, ioListener, size.x / (float) size.y);
            scene.dispose();
            scene = simulationScene;
            logger.info("Starting simulation.");
        } catch (IOException | InvalidPathException | IllegalStateException e) {
            logger.error("Could not create Simulation Scene.", e);
        }
    }

    private void finishSimulation() {
        scene.dispose();
        scene = new MenuScene(this::startSimulation);
        logger.info("Finished simulation.");
    }

    private void initializeGLFW() {
        GLFWErrorCallback.createPrint(new PrintStream(new LoggingOutputStream(logger, LoggingOutputStream.LogLevel.ERROR))).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindow = glfwCreateWindow(size.x, size.y, name, MemoryUtil.NULL, MemoryUtil.NULL);
        if (glfwWindow == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }
        glfwMakeContextCurrent(glfwWindow);
        glfwSetKeyCallback(glfwWindow, IOListener::keyCallback);
        glfwSetWindowSizeCallback(glfwWindow, IOListener::windowSizeCallback);
        glfwSetFramebufferSizeCallback(glfwWindow, IOListener::windowSizeCallback);
        glfwSwapInterval(1);
        glfwShowWindow(glfwWindow);
    }

    private void initializeOpenGL() {
        logger.info(String.format("Using LWJGL %s.", Version.getVersion()));
        GL.createCapabilities();
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glViewport(0, 0, size.x, size.y);
    }

    private void initializeImGui() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable | ImGuiConfigFlags.DockingEnable);
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
        imGuiGlfw.init(glfwWindow, true);
        imGuiGl3.init(GLSL_VERSION);
    }
}
