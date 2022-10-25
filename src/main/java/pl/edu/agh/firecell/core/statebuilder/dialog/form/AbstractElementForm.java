package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.Vector3i;
import pl.edu.agh.firecell.core.statebuilder.ElementWrapper;
import pl.edu.agh.firecell.core.statebuilder.element.Element;

// TODO: add validation
public abstract class AbstractElementForm implements ElementForm {
    private final ImString name = new ImString("", 100);
    private final ImInt positionX = new ImInt(0);
    private final ImInt positionY = new ImInt(0);
    private final ImInt positionZ = new ImInt(0);

    @Override
    public void buildGui() {
        ImGui.inputText("Name##elementName", name);
        ImGui.text("Position");
        ImGui.inputInt("X##elementPosition", positionX);
        ImGui.inputInt("Y##elementPosition", positionY);
        ImGui.inputInt("Z##elementPosition", positionZ);
        buildElementGui();
    }

    protected abstract void buildElementGui();

    @Override
    public ElementWrapper createElement() {
        return new ElementWrapper(createBaseElement(getPositionVector()), name.get());
    }

    private Vector3i getPositionVector() {
        return new Vector3i(positionX.get(), positionY.get(), positionZ.get());
    }

    protected abstract Element createBaseElement(Vector3i position);
}
