package pl.edu.agh.firecell.core;

import imgui.ImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.engine.BasicEngine;
import pl.edu.agh.firecell.engine.Engine;
import pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.BasicRenderer;
import pl.edu.agh.firecell.renderer.Renderer;
import pl.edu.agh.firecell.storage.BasicStorage;
import pl.edu.agh.firecell.storage.Storage;

import java.io.IOException;
import java.nio.file.InvalidPathException;

public class SimulationScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(SimulationScene.class);

    private final Engine engine;
    private final Storage storage;
    private final Renderer renderer;
    private final Runnable finishSimulationHandler;
    private State currentState;

    public SimulationScene(SimulationConfig config, Runnable finishSimulationHandler, IOListener ioListener, float aspectRatio)
            throws IOException, InvalidPathException, IllegalStateException {
        this.currentState = config.initialState();
        this.finishSimulationHandler = finishSimulationHandler;
        renderer = new BasicRenderer(aspectRatio, ioListener, config);
        storage = new BasicStorage();
        engine = new BasicEngine(config, storage, new BasicAlgorithm());
    }

    @Override
    public void update(double frameTime) {
        renderer.render(currentState, frameTime);
        renderGUI(frameTime);
    }

    private void renderGUI(double frameTime) {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Simulation")) {
                if (ImGui.menuItem("Finish simulation")) {
                    finishSimulationHandler.run();
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Diagnostics")) {
                ImGui.text("Framerate: %s".formatted(String.valueOf(Math.round(1 / frameTime))));
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }

    @Override
    public void dispose() {
        renderer.dispose();
        engine.stop();
    }
}
