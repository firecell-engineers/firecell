package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import pl.edu.agh.firecell.core.statebuilder.element.CuboidElement;
import pl.edu.agh.firecell.core.statebuilder.element.Element;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.util.GuiUtils;

import static pl.edu.agh.firecell.model.util.GuiUtils.createVector3i;

public class CuboidElementForm extends AbstractCuboidElementForm<CuboidElement> {
    private Material selectedMaterial = Material.WOOD;

    @Override
    protected void buildElementGui() {
        super.buildElementGui();
        selectedMaterial = GuiUtils.comboBox("Material##cuboidMaterial", Material::name, selectedMaterial, Material.values());
    }

    @Override
    protected Element createBaseElement() {
        return new CuboidElement(createVector3i(positionX, positionY, positionZ), createVector3i(sizeX, sizeY, sizeZ), selectedMaterial);
    }

    @Override
    protected void fillFields(CuboidElement element) {
        super.fillFields(element);
        selectedMaterial = element.material();
    }

    @Override
    protected Class<CuboidElement> getElementClass() {
        return CuboidElement.class;
    }
}
