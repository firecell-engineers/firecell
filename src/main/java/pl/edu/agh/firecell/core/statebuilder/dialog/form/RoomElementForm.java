package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import imgui.ImGui;
import imgui.type.ImInt;
import pl.edu.agh.firecell.core.statebuilder.element.Element;
import pl.edu.agh.firecell.core.statebuilder.element.RoomElement;

import static pl.edu.agh.firecell.model.util.GuiUtils.createVector2i;
import static pl.edu.agh.firecell.model.util.GuiUtils.createVector3i;

public class RoomElementForm extends AbstractElementForm<RoomElement> {
    private final ImInt positionX = new ImInt(0);
    private final ImInt positionY = new ImInt(0);
    private final ImInt sizeX = new ImInt(0);
    private final ImInt sizeY = new ImInt(0);
    private final ImInt sizeZ = new ImInt(0);

    @Override
    protected void buildElementGui() {
        ImGui.text("Position");
        ImGui.inputInt("X##roomPosition", positionX);
        ImGui.inputInt("Y##roomPosition", positionY);
        ImGui.text("Size");
        ImGui.inputInt("X##roomSize", sizeX);
        ImGui.inputInt("Y##roomSize", sizeY);
        ImGui.inputInt("Z##roomSize", sizeZ);
    }

    @Override
    protected Element createBaseElement() {
        return new RoomElement(createVector2i(positionX, positionY), createVector3i(sizeX, sizeY, sizeZ));
    }

    @Override
    protected void fillFields(RoomElement element) {
        positionX.set(element.getPosition().x);
        positionY.set(element.getPosition().y);
        sizeX.set(element.getSize().x);
        sizeY.set(element.getSize().y);
        sizeZ.set(element.getSize().z);
    }

    @Override
    protected Class<RoomElement> getElementClass() {
        return RoomElement.class;
    }
}
