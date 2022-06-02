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

    private SimulationConfig config = new SimulationConfig(new State(new Cell[][][]{}));
    private final Consumer<SimulationConfig> startSimulationHandler;

    public MenuScene(Consumer<SimulationConfig> startSimulationHandler) {
        this.startSimulationHandler = startSimulationHandler;
    }

    @Override
    public void update(double deltaTime) {
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
            if (ImGui.beginMenu("Chuje muje")) {
                if (ImGui.menuItem("Siema")) {
                    logger.info("Siema");
                }
                if (ImGui.menuItem("Spierdalaj")) {
                    logger.info("Spierdalaj");
                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }

    @Override
    public void dispose() {}
}
