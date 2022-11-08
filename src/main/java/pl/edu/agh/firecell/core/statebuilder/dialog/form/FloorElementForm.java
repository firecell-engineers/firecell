package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import imgui.ImGui;
import imgui.type.ImInt;
import org.joml.Vector2i;
import pl.edu.agh.firecell.core.statebuilder.element.Element;
import pl.edu.agh.firecell.core.statebuilder.element.FloorElement;

public class FloorElementForm extends AbstractElementForm<FloorElement> {
    private final ImInt positionX = new ImInt(0);
    private final ImInt positionY = new ImInt(0);
    private final ImInt sizeX = new ImInt(0);
    private final ImInt sizeY = new ImInt(0);

    @Override
    protected void buildElementGui() {
        ImGui.text("Position");
        ImGui.inputInt("X##floorPosition", positionX);
        ImGui.inputInt("Y##floorPosition", positionY);
        ImGui.text("Size");
        ImGui.inputInt("X##floorSize", sizeX);
        ImGui.inputInt("Y##floorSize", sizeY);
    }

    @Override
    protected Element createBaseElement() {
        return new FloorElement(createVector(positionX, positionY), createVector(sizeX, sizeY));
    }

    @Override
    protected void fillFields(FloorElement element) {
        positionX.set(element.getPosition().x);
        positionY.set(element.getPosition().y);
        sizeX.set(element.getSize().x);
        sizeY.set(element.getSize().y);
    }

    @Override
    protected Class<FloorElement> getElementClass() {
        return FloorElement.class;
    }

    private Vector2i createVector(ImInt x, ImInt y) {
        return new Vector2i(x.get(), y.get());
    }
}
