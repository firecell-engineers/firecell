package pl.edu.agh.core;

import imgui.ImGui;

public class SimulationScene implements IScene {

    @Override
    public void update(double deltaTime) {
        ImGui.begin("Menu");
        ImGui.text("Simulation scene in dev...");
        ImGui.end();
    }

}
