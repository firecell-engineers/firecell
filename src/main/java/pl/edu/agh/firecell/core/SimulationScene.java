package pl.edu.agh.firecell.core;

import imgui.ImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.engine.BasicEngine;
import pl.edu.agh.firecell.engine.Engine;
import pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.renderer.BasicRenderer;
import pl.edu.agh.firecell.renderer.Renderer;
import pl.edu.agh.firecell.storage.BasicStorage;
import pl.edu.agh.firecell.storage.Storage;

public class SimulationScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(SimulationScene.class);

    private final SimulationConfig config;
    private final Engine engine;
    private final Storage storage;
    private final Renderer renderer;

    private final Runnable finishSimulationHandler;

    private int currentStateIndex = 0;

    public SimulationScene(SimulationConfig config, Runnable finishSimulationHandler) {
        this.config = config;
        this.finishSimulationHandler = finishSimulationHandler;
        renderer = new BasicRenderer();
        storage = new BasicStorage();
        engine = new BasicEngine(config, storage, new BasicAlgorithm());

        engine.run();
    }

    @Override
    public void update(double deltaTime) {
        storage.getState(currentStateIndex).ifPresent(renderer::render);
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
        engine.stop();
    }
}
