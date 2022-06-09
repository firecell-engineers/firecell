package pl.edu.agh.firecell.core;

import imgui.ImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;

import java.util.function.Consumer;

public class MenuScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(MenuScene.class);

    private SimulationConfig config = new SimulationConfig(new State(new Cell[][][]{}), 1.0);
    private final Consumer<SimulationConfig> startSimulationHandler;

    public MenuScene(Consumer<SimulationConfig> startSimulationHandler) {
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
}
