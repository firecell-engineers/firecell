package pl.edu.agh.firecell.core.statebuilder.dialog;

import imgui.ImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.statebuilder.ElementType;
import pl.edu.agh.firecell.core.statebuilder.ElementWrapper;
import pl.edu.agh.firecell.core.statebuilder.dialog.form.ElementForm;
import pl.edu.agh.firecell.model.util.GuiUtils;

import java.util.function.BiConsumer;

public class ManipulateElementDialog extends AbstractDialog {
    private static final Logger logger = LoggerFactory.getLogger(ManipulateElementDialog.class);

    private final BiConsumer<ElementWrapper, Mode> addElementHandler;
    private ElementType selectedElementType = null;
    private ElementForm elementForm = null;
    private ElementWrapper currentElement = null;
    private Mode mode = Mode.ADD;

    public ManipulateElementDialog(BiConsumer<ElementWrapper, Mode> addElementHandler) {
        this.addElementHandler = addElementHandler;
        setVisible(false);
    }

    @Override
    protected void buildGui() {
        createElementTypeComboBox();
        if (elementForm != null) {
            elementForm.buildGui();
            if (ImGui.button("Save")) {
                ElementWrapper element = elementForm.createElement();
                addElementHandler.accept(element, mode);
            }
        }
    }

    private void createElementTypeComboBox() {
        ElementType oldType = selectedElementType;
        selectedElementType = GuiUtils.comboBox("Element type",
                ElementType::name, selectedElementType, ElementType.values());

        if (oldType != selectedElementType) {
            createElementForm();
        }
    }

    private void createElementForm() {
        elementForm = selectedElementType.getFormFactory().get();
        if (currentElement != null && elementForm.matchesElement(currentElement)) {
            elementForm.setElement(currentElement);
        }
        logger.info("Created form for {}", selectedElementType);
    }

    public void setCurrentElement(ElementWrapper currentElement) {
        this.currentElement = currentElement;
        if (currentElement == null) {
            selectedElementType = null;
        } else {
            selectedElementType = ElementType.determineType(currentElement.element());
            createElementForm();
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        setTitle(mode.title);
    }

    public enum Mode {
        ADD("Add element"),
        EDIT("Edit element");

        private final String title;

        Mode(String title) {
            this.title = title;
        }
    }
}
