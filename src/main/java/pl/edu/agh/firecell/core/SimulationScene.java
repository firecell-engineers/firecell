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
import pl.edu.agh.firecell.renderer.rendermode.RenderMode;
import pl.edu.agh.firecell.renderer.Renderer;
import pl.edu.agh.firecell.storage.FileSystemStorage;
import pl.edu.agh.firecell.storage.serialization.BinaryStateSerializer;

import java.io.IOException;
import java.nio.file.InvalidPathException;

public class SimulationScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(SimulationScene.class);

    private final Engine engine;
    private final FileSystemStorage storage;
    private final Renderer renderer;
    private final Runnable finishSimulationHandler;
    private State currentState;
    private final double stepTime;
    private int indexStep = 0;
    private long lastFrameTime = now();

    public SimulationScene(SimulationConfig config, Runnable finishSimulationHandler,
                           IOListener ioListener, float aspectRatio)
            throws IOException, InvalidPathException, IllegalStateException {
        this.currentState = config.initialState();
        this.finishSimulationHandler = finishSimulationHandler;
        this.stepTime = config.stepTime();
        renderer = new BasicRenderer(aspectRatio, ioListener, config);
        storage = new FileSystemStorage(new BinaryStateSerializer());
        engine = new BasicEngine(config, storage, new BasicAlgorithm(stepTime));
        engine.run();
    }

    @Override
    public void update(double frameTime) {
        if (now() - lastFrameTime > stepTime * 1000) {
            lastFrameTime = now();
            storage.getState(indexStep).ifPresent(state -> {
                currentState = state;
                indexStep++;
            });
            logger.info("Getting next state with index: " + indexStep);
        }
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

    @Override
    public void dispose() {
        renderer.dispose();
        engine.stop();
    }

    private long now() {
        return System.currentTimeMillis();
    }
}
