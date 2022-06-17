package pl.edu.agh.firecell.core;

import imgui.ImGui;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.model.*;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class MenuScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(MenuScene.class);

    private final IOListener ioListener;
    private SimulationConfig config = createInitialSimulationConfig();
    private final Consumer<SimulationConfig> startSimulationHandler;

    public MenuScene(Consumer<SimulationConfig> startSimulationHandler, IOListener ioListener) {
        this.ioListener = ioListener;
        this.startSimulationHandler = startSimulationHandler;
    }

    @Override
    public void update(double frameTime) {
        renderGUI();
    }

    private void renderGUI() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Simulation")) {
                if (ImGui.menuItem("Start simulation")) {
                    startSimulationHandler.accept(config);
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Some settings1")) {
                if (ImGui.menuItem("Some settings1.1")) {
                    logger.info("Some settings1.1");
                }
                if (ImGui.menuItem("Some settings1.2")) {
                    logger.info("Some settings1.2");
                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }

    @Override
    public void dispose() {}

    private SimulationConfig createInitialSimulationConfig() {
        Vector3i spaceSize = new Vector3i(3, 3, 3);
        State initialState = new StateBuilder(spaceSize)
                .addCuboid(new Vector3i(0, 0, 0), new Vector3i(3, 3, 0), Material.WOOD)
                .getResult();
        return new SimulationConfig(initialState, 0.001);
    }
}
