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
import pl.edu.agh.firecell.storage.InMemoryStorage;

import java.io.IOException;
import java.nio.file.InvalidPathException;

public class SimulationScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(SimulationScene.class);

    private final Engine engine;
    private final InMemoryStorage storage;
    private final Renderer renderer;
    private final Runnable finishSimulationHandler;
    private State currentState;
    private final double stepTime = 0.5;

    public SimulationScene(SimulationConfig config, Runnable finishSimulationHandler, IOListener ioListener, float aspectRatio)
            throws IOException, InvalidPathException, IllegalStateException {
        this.currentState = config.initialState();
        this.finishSimulationHandler = finishSimulationHandler;
        renderer = new BasicRenderer(aspectRatio, ioListener, config);
        storage = new InMemoryStorage();
        engine = new BasicEngine(config, storage, new BasicAlgorithm(stepTime));
    }

    @Override
    public void update(double frameTime) {
        renderer.render(currentState, frameTime);
        renderGUI();
    }

    private void renderGUI() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Simulation")) {
                if (ImGui.menuItem("Finish simulation")) {
                    finishSimulationHandler.run();
                }
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
