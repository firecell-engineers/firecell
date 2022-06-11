package pl.edu.agh.firecell.core;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.util.FirecellUncaughtExceptionHandler;
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

    private long glfwWindow;
    private ImGuiImplGlfw imGuiGlfw;
    private ImGuiImplGl3 imGuiGl3;

    private Scene scene;

    public Window(int width, int height, String name) {
        initialize(width, height, name);
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

            frameTime = glfwGetTime() - startFrameTime;
            startFrameTime = glfwGetTime();
        }
        scene.dispose();
        dispose();
    }

    private void initialize(int width, int height, String name) {
        logger.info(String.format("Using LWJGL %s.", Version.getVersion()));

        Thread.setDefaultUncaughtExceptionHandler(new FirecellUncaughtExceptionHandler());


        GLFWErrorCallback.createPrint(createGLFWErrorPrintStream(logger)).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindow = glfwCreateWindow(width, height, name, MemoryUtil.NULL, MemoryUtil.NULL);
        if (glfwWindow == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }

        glfwSetWindowSizeCallback(glfwWindow, (window, newWidth, newHeight) -> glViewport(0, 0, newWidth, newHeight));
        glfwSetKeyCallback(glfwWindow, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(1);
        glfwShowWindow(glfwWindow);

        GL.createCapabilities();
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable | ImGuiConfigFlags.DockingEnable);

        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();

        imGuiGlfw.init(glfwWindow, true);
        imGuiGl3.init(GLSL_VERSION);

        logger.info("Firecell initialized.");
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

    private PrintStream createGLFWErrorPrintStream(Logger logger) {
        return new PrintStream(
                new LoggingOutputStream(logger, LoggingOutputStream.LogLevel.ERROR)
        );
    }

    private void startSimulation(SimulationConfig config) {
        try {
            var simulationScene = new SimulationScene(config, this::finishSimulation);
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
}
