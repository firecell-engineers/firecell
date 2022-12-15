package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import imgui.ImGui;
import imgui.type.ImInt;
import pl.edu.agh.firecell.core.statebuilder.element.BaseCuboidElement;

public abstract class AbstractCuboidElementForm<T extends BaseCuboidElement> extends AbstractElementForm<T> {
    protected final ImInt positionX = new ImInt(0);
    protected final ImInt positionY = new ImInt(0);
    protected final ImInt positionZ = new ImInt(0);
    protected final ImInt sizeX = new ImInt(0);
    protected final ImInt sizeY = new ImInt(0);
    protected final ImInt sizeZ = new ImInt(0);

    @Override
    protected void buildElementGui() {
        ImGui.text("Position");
        ImGui.inputInt("X##elementPosition", positionX);
        ImGui.inputInt("Y##elementPosition", positionY);
        ImGui.inputInt("Z##elementPosition", positionZ);
        ImGui.text("Size");
        ImGui.inputInt("X##cuboidSize", sizeX);
        ImGui.inputInt("Y##cuboidSize", sizeY);
        ImGui.inputInt("Z##cuboidSize", sizeZ);
    }

    @Override
    protected void fillFields(T element) {
        positionX.set(element.position().x);
        positionY.set(element.position().y);
        positionZ.set(element.position().z);
        sizeX.set(element.size().x);
        sizeY.set(element.size().y);
        sizeZ.set(element.size().z);
    }
}
