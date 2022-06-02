package pl.edu.agh.firecell.core;

import imgui.ImGui;
import pl.edu.agh.firecell.model.SimulationConfig;

public class SimulationScene implements Scene {

    private final SimulationConfig config;

    public SimulationScene(SimulationConfig config) {
        this.config = config;
    }

    @Override
    public void update(double deltaTime) {
        ImGui.begin("Menu");
        ImGui.text("Symulacja dziwko...");
        ImGui.end();
    }

}
