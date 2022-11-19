package pl.edu.agh.firecell.core;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import io.reactivex.rxjava3.disposables.Disposable;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.core.io.KeyEvent;
import pl.edu.agh.firecell.core.statebuilder.Room;
import pl.edu.agh.firecell.storage.RoomStorage;
import pl.edu.agh.firecell.core.statebuilder.StateBuilderScene;
import pl.edu.agh.firecell.core.util.LoggingOutputStream;
import pl.edu.agh.firecell.core.util.StateUtils;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.storage.SimulationStorage;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Window implements StartSimulationHandler {
    private static final Path SAVED_SIMULATIONS_PATH = Path.of("simulations");
    private static final String STATES_DIRECTORY_NAME = "states";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // need to be same as in glsl shaders
    private static final String GLSL_VERSION = "#version 330";
    private final RoomStorage roomStorage = new RoomStorage();
    private final SimulationStorage simulationStorage = new SimulationStorage(SAVED_SIMULATIONS_PATH);

    private final String name;
    private Vector2i size;

    private long glfwWindow;
    private ImGuiImplGlfw imGuiGlfw;
    private ImGuiImplGl3 imGuiGl3;

    private Scene scene;
    private final IOListener ioListener;

    private final Disposable windowSizeSubscription;
    private final Disposable closeKeyEventSubscription;

    public Window(int initialWidth, int initialHeight, String appName) {
        size = new Vector2i(initialWidth, initialHeight);
        ioListener = new IOListener(size);
        name = appName;

        initializeGLFW();
        initializeOpenGL();
        initializeImGui();

        windowSizeSubscription = ioListener.windowSizeObservable().subscribe(size -> {
            this.size = size;
            glViewport(0, 0, size.x, size.y);
        });
        closeKeyEventSubscription = ioListener.keyObservable(GLFW_KEY_ESCAPE)
                .filter(KeyEvent::pressed)
                .subscribe(pressed -> glfwSetWindowShouldClose(glfwWindow, true));
    }

    public void run() {

        double startFrameTime = glfwGetTime();
        double frameTime = 0.0;

        scene = new MenuScene(this, this::startStateBuilder, roomStorage, simulationStorage);

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
        dispose();
    }

    private void dispose() {
        scene.dispose();

        closeKeyEventSubscription.dispose();
        windowSizeSubscription.dispose();

        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();

        Callbacks.glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void finishSceneHandler() {
        scene.dispose();
        logger.info("Finished scene {}. Opening menu.", scene.getClass().getName());
        scene = new MenuScene(this, this::startStateBuilder, roomStorage, simulationStorage);
    }

    private void startStateBuilder(Room room) {
        try {
            StateBuilderScene builderScene;
            if (room == null) {
                builderScene = new StateBuilderScene(roomStorage, ioListener, size.x / (float) size.y,
                        this::finishSceneHandler);
            } else {
                builderScene = new StateBuilderScene(roomStorage, ioListener, size.x / (float) size.y,
                        this::finishSceneHandler, room);
            }
            scene.dispose();
            scene = builderScene;
            logger.info("Starting state builder.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeGLFW() {
        GLFWErrorCallback.createPrint(new PrintStream(new LoggingOutputStream(logger, LoggingOutputStream.LogLevel.ERROR))).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindow = glfwCreateWindow(size.x, size.y, name, MemoryUtil.NULL, MemoryUtil.NULL);
        if (glfwWindow == MemoryUtil.NULL) {
            throw new IllegalStateException("Failed to create the GLFW window");
        }
        glfwMakeContextCurrent(glfwWindow);
        glfwSetKeyCallback(glfwWindow, ioListener::keyCallback);
        glfwSetWindowSizeCallback(glfwWindow, ioListener::windowSizeCallback);
        glfwSetFramebufferSizeCallback(glfwWindow, ioListener::windowSizeCallback);
        glfwSetCursorPosCallback(glfwWindow, ioListener::mousePositionCallback);
        glfwSetMouseButtonCallback(glfwWindow, ioListener::mouseButtonCallback);
        glfwSwapInterval(1);
        glfwShowWindow(glfwWindow);
    }

    private void initializeOpenGL() {
        logger.info(String.format("Using LWJGL %s.", Version.getVersion()));
        GL.createCapabilities();
        glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        glViewport(0, 0, size.x, size.y);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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

    @Override
    public void runNewSimulation(SimulationConfig config, String simulationName) {
        try {
            simulationStorage.initializeSimulation(simulationName, new StoredSimulationConfig(config.size(), config.stepTime()));
            Path statesStoragePath = simulationStorage.resolvePath(simulationName).resolve(STATES_DIRECTORY_NAME);
            var simulationScene = new SimulationScene(config, this::finishSceneHandler,
                    ioListener, getAspectRatio(), statesStoragePath);
            scene.dispose();
            scene = simulationScene;
            logger.info("Starting simulation.");
        } catch (IOException | InvalidPathException | IllegalStateException e) {
            logger.error("Could not start simulation.", e);
        }
    }

    @Override
    public void runSavedSimulation(String simulationName) {
        try {
            StoredSimulationConfig storedConfig = simulationStorage.readStoredConfig(simulationName);
            Vector3i spaceSize = storedConfig.spaceSize();
            SimulationConfig config = new SimulationConfig(spaceSize,
                    StateUtils.emptyState(spaceSize), storedConfig.stepTime());
            Path statesStoragePath = simulationStorage.resolvePath(simulationName).resolve(STATES_DIRECTORY_NAME);
            var simulationScene = new StoredSimulationScene(config, this::finishSceneHandler,
                    ioListener, getAspectRatio(), statesStoragePath);
            scene.dispose();
            scene = simulationScene;
            logger.info("Running stored simulation.");
        } catch (IOException | InvalidPathException | IllegalStateException e) {
            logger.error("Could not start stored simulation.", e);
        }
    }


    private float getAspectRatio() {
        return size.x / (float) size.y;
    }
}
