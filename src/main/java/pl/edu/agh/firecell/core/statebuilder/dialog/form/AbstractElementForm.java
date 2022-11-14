package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import imgui.ImGui;
import imgui.type.ImString;
import org.apache.commons.lang3.StringUtils;
import pl.edu.agh.firecell.core.statebuilder.ElementType;
import pl.edu.agh.firecell.core.statebuilder.ElementWrapper;
import pl.edu.agh.firecell.core.statebuilder.element.Element;

// TODO: add validation
public abstract class AbstractElementForm<T extends Element> implements ElementForm {
    private final ImString name;

    protected AbstractElementForm() {
        name = new ImString(determineDefaultName(), 100);
    }

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

    @Override
    public boolean matchesElement(ElementWrapper element) {
        return getElementClass() == element.element().getClass();
    }

    @Override
    public void setElement(ElementWrapper element) {
        T innerElement = getElementClass().cast(element.element());
        name.set(element.name());
        fillFields(innerElement);
    }

    private String determineDefaultName() {
        return StringUtils.capitalize(ElementType.determineType(getElementClass()).name().toLowerCase());
    }

    protected abstract void fillFields(T element);

    protected abstract Class<T> getElementClass();
}
