package pl.edu.agh.firecell.core;

import imgui.ImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.diagnostics.DiagnosticsManager;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.engine.BasicEngine;
import pl.edu.agh.firecell.engine.Engine;
import pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.BasicRenderer;
import pl.edu.agh.firecell.renderer.Renderer;
import pl.edu.agh.firecell.renderer.rendermode.RenderMode;
import pl.edu.agh.firecell.storage.FileSystemStorage;
import pl.edu.agh.firecell.storage.serialization.BinaryStateSerializer;

import java.io.IOException;
import java.nio.file.InvalidPathException;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class SimulationScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(SimulationScene.class);

    private final Engine engine;
    private final FileSystemStorage storage;
    private final Renderer renderer;
    private final Runnable finishSimulationHandler;
    private DiagnosticsManager diagnosticsManager;
    private State currentState;
    private final double stepTime;
    private int indexStep = 0;
    private double lastStateUpdateTime = glfwGetTime();

    public SimulationScene(SimulationConfig config, Runnable finishSimulationHandler,
                           IOListener ioListener, float aspectRatio)
            throws IOException, InvalidPathException, IllegalStateException {
        this.currentState = config.initialState();
        this.finishSimulationHandler = finishSimulationHandler;
        this.stepTime = config.stepTime();
        this.diagnosticsManager = new DiagnosticsManager(this.currentState, 0);
        this.renderer = new BasicRenderer(aspectRatio, ioListener, config);
        this.storage = new FileSystemStorage(new BinaryStateSerializer());
        this.engine = new BasicEngine(config, this.storage, new BasicAlgorithm(this.stepTime));
        this.engine.run();
    }

    @Override
    public void update(double frameTime) {
        updateCurrentState(frameTime);
        renderer.render(currentState, frameTime);
        renderGUI();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        engine.stop();
    }

    private void updateCurrentState(double frameTime) {
        double currentStepTime = glfwGetTime() - lastStateUpdateTime;
        if (currentStepTime > stepTime) {
            lastStateUpdateTime = glfwGetTime();
            storage.getState(indexStep).ifPresent(state -> {
                currentState = state;
                diagnosticsManager = new DiagnosticsManager(state, frameTime);
                indexStep++;
            });
            logger.info("Getting next state with index: " + indexStep);
        }
    }

    private void renderGUI() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Simulation")) {
                if (ImGui.menuItem("Finish simulation")) {
                    finishSimulationHandler.run();
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Diagnostics")) {
                ImGui.text("Framerate: %s".formatted(String.valueOf(Math.round(diagnosticsManager.framerate()))));

                ImGui.text("Total burning cells: %s".formatted(String.valueOf(Math.round(diagnosticsManager.burningCellsCount()))));

                ImGui.text("Average temperature:        %s C".formatted(String.valueOf(Math.round(diagnosticsManager.averageTemperature()))));
                ImGui.text("Average solids temperature: %s C".formatted(String.valueOf(Math.round(diagnosticsManager.averageSolidsTemperature()))));
                ImGui.text("Average air temperature:    %s C".formatted(String.valueOf(Math.round(diagnosticsManager.averageAirTemperature()))));

                ImGui.text("Total temperature:  %s C".formatted(String.valueOf(Math.round(diagnosticsManager.totalTemperature()))));
                ImGui.text("Solids temperature: %s C".formatted(String.valueOf(Math.round(diagnosticsManager.solidsTemperature()))));
                ImGui.text("Air temperature:    %s C".formatted(String.valueOf(Math.round(diagnosticsManager.airTemperature()))));

                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Render mode")) {
                if (ImGui.menuItem("Normal")) {
                    renderer.setRenderMode(RenderMode.STANDARD);
                    logger.info("Render mode set to Normal");
                }
                if (ImGui.menuItem("Air temperature")) {
                    renderer.setRenderMode(RenderMode.TEMPERATURE_AIR);
                    logger.info("Render mode set to Air temperature");
                }
                if (ImGui.menuItem("Solid temperature")) {
                    renderer.setRenderMode(RenderMode.TEMPERATURE_SOLID);
                    logger.info("Render mode set to Solid");
                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }
}
