package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import imgui.ImGui;
import imgui.type.ImString;
import pl.edu.agh.firecell.core.statebuilder.ElementWrapper;
import pl.edu.agh.firecell.core.statebuilder.element.Element;

// TODO: add validation
public abstract class AbstractElementForm implements ElementForm {
    private final ImString name = new ImString("", 100);

    @Override
    public void buildGui() {
        ImGui.inputText("Name##elementName", name);
        buildElementGui();
    }

    protected abstract void buildElementGui();

    @Override
    public ElementWrapper createElement() {
        return new ElementWrapper(createBaseElement(), name.get());
    }

    protected abstract Element createBaseElement();
}
