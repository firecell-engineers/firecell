package pl.edu.agh.core;

import imgui.ImGui;

public class MenuScene implements IScene {
    @Override
    public void update(double deltaTime) {
        ImGui.begin("Menu");
        ImGui.text("Menu scene in dev...");
        ImGui.end();
    }

}
