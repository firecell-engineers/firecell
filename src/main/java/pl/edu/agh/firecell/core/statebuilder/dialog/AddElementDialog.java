package pl.edu.agh.firecell.core.statebuilder.dialog;

import imgui.ImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.statebuilder.ElementType;
import pl.edu.agh.firecell.core.statebuilder.ElementWrapper;
import pl.edu.agh.firecell.core.statebuilder.StateBuilder;
import pl.edu.agh.firecell.core.statebuilder.dialog.form.ElementForm;

import java.util.function.Consumer;

public class AddElementDialog extends AbstractDialog {
    private static final Logger logger = LoggerFactory.getLogger(AddElementDialog.class);

    private final Consumer<ElementWrapper> addElementHandler;
    private final StateBuilder stateBuilder;
    private ElementType selectedElementType = null;
    private ElementForm elementForm = null;

    public AddElementDialog(Consumer<ElementWrapper> addElementHandler, StateBuilder stateBuilder) {
        this.addElementHandler = addElementHandler;
        this.stateBuilder = stateBuilder;
        setTitle("Add element");
        setVisible(false);
    }

    @Override
    protected void buildGui() {
        ElementType oldType = selectedElementType;
        if (ImGui.beginCombo("Element type", selectedElementType != null ? selectedElementType.name() : "-- select --")) {
            for (ElementType elementType : ElementType.values()) { // TODO: make some utils class for these combo boxes to avoid duplicates
                boolean isSelected = selectedElementType == elementType;
                if (ImGui.selectable(elementType.name(), isSelected)) {
                    selectedElementType = elementType;
                    logger.info("Selected {}", selectedElementType);
                }
                if (isSelected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        if (oldType != selectedElementType) {
            elementForm = selectedElementType.getFormFactory().get();
            logger.info("Created form for {}", selectedElementType);
        }
        if (elementForm != null) {
            elementForm.buildGui();
            if (ImGui.button("Create")) {
                ElementWrapper element = elementForm.createElement(stateBuilder);
                addElementHandler.accept(element);
            }
        }
    }
}
