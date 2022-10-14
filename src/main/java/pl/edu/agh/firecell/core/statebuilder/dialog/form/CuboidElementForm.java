package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import imgui.ImGui;
import imgui.type.ImInt;
import org.joml.Vector3i;
import pl.edu.agh.firecell.core.statebuilder.StateBuilder;
import pl.edu.agh.firecell.model.Material;

public class CuboidElementForm extends AbstractElementForm {
    private final ImInt sizeX = new ImInt(0);
    private final ImInt sizeY = new ImInt(0);
    private final ImInt sizeZ = new ImInt(0);
    private Material selectedMaterial = Material.WOOD;

    @Override
    protected void buildElementGui() {
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
    protected StateBuilder.Element createBaseElement(StateBuilder stateBuilder, Vector3i position) {
        stateBuilder.addCuboid(position, createSizeVector(), selectedMaterial);
        return stateBuilder.getLastElement();
    }
}
