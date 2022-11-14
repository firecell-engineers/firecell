package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import pl.edu.agh.firecell.core.statebuilder.element.Element;
import pl.edu.agh.firecell.core.statebuilder.element.IgnitionElement;

import static pl.edu.agh.firecell.model.util.GuiUtils.createVector3i;

public class IgnitionElementForm extends AbstractCuboidElementForm<IgnitionElement> {
    @Override
    protected Element createBaseElement() {
        return new IgnitionElement(createVector3i(positionX, positionY, positionZ), createVector3i(sizeX, sizeY, sizeZ));
    }

    @Override
    protected Class<IgnitionElement> getElementClass() {
        return IgnitionElement.class;
    }
}
