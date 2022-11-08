package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import imgui.ImGui;
import imgui.type.ImInt;
import org.joml.Vector3i;
import pl.edu.agh.firecell.core.statebuilder.element.CuboidElement;
import pl.edu.agh.firecell.core.statebuilder.element.Element;
import pl.edu.agh.firecell.model.Material;

public class CuboidElementForm extends AbstractElementForm<CuboidElement> {
    private final ImInt positionX = new ImInt(0);
    private final ImInt positionY = new ImInt(0);
    private final ImInt positionZ = new ImInt(0);
    private final ImInt sizeX = new ImInt(0);
    private final ImInt sizeY = new ImInt(0);
    private final ImInt sizeZ = new ImInt(0);
    private Material selectedMaterial = Material.WOOD;

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
        if (ImGui.beginCombo("Material##cuboidMaterial", selectedMaterial.name())) {
            for (Material material : Material.values()) {
                boolean isSelected = selectedMaterial == material;
                if (ImGui.selectable(material.name(), isSelected)) {
                    selectedMaterial = material;
                }
                if (isSelected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
    }

    private Vector3i createSizeVector() {
        return new Vector3i(sizeX.get(), sizeY.get(), sizeZ.get());
    }

    @Override
    protected Element createBaseElement() {
        return new CuboidElement(createPositionVector(), createSizeVector(), selectedMaterial);
    }

    @Override
    protected void fillFields(CuboidElement element) {
        positionX.set(element.position().x);
        positionY.set(element.position().y);
        positionZ.set(element.position().z);
        sizeX.set(element.size().x);
        sizeY.set(element.size().y);
        sizeZ.set(element.size().z);
        selectedMaterial = element.material();
    }

    @Override
    protected Class<CuboidElement> getElementClass() {
        return CuboidElement.class;
    }

    private Vector3i createPositionVector() {
        return new Vector3i(positionX.get(), positionY.get(), positionZ.get());
    }
}
