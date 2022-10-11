package pl.edu.agh.firecell.core.statebuilder.dialog;

import imgui.ImGui;

import static imgui.flag.ImGuiWindowFlags.AlwaysAutoResize;

public abstract class AbstractDialog implements Dialog {
    private boolean visible = true;
    private String title = "Dialog";

    @Override
    public void render() {
        if (visible) {
            if (ImGui.begin(title, AlwaysAutoResize)) {
                buildGui();
            }
            ImGui.end();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    protected abstract void buildGui();

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
}
