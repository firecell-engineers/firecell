package pl.edu.agh.firecell.core;

import imgui.ImGui;
import pl.edu.agh.firecell.model.SimulationConfig;

import java.util.function.Consumer;

public class MenuScene implements Scene {

    private SimulationConfig config;
    private final Consumer<SimulationConfig> startSimulationHandler;

    public MenuScene(Consumer<SimulationConfig> startSimulationHandler) {
        this.startSimulationHandler = startSimulationHandler;
    }

    @Override
    public void update(double deltaTime) {
        ImGui.begin("Menu");
        ImGui.text("Menu scene in dev...");
        if (ImGui.button("Start simulation")) {
            startSimulationHandler.accept(config);
        }
        ImGui.end();
    }
}
