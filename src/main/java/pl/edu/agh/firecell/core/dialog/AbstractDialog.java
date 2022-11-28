package pl.edu.agh.firecell.core.dialog;

import imgui.ImGui;

import static imgui.flag.ImGuiWindowFlags.AlwaysAutoResize;

public abstract class AbstractDialog implements Dialog {
    private boolean visible = true;
    private String title = "Dialog";
    private int flags = AlwaysAutoResize;

    public AbstractDialog() {

    }

    public AbstractDialog(String title) {
        this.title = title;
    }

    @Override
    public void render() {
        if (visible) {
            if (ImGui.begin(title, flags)) {
                buildGui();
            }
            ImGui.end();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    protected abstract void buildGui();

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setFlags(int flags) {
        this.flags = flags;
    }
}
