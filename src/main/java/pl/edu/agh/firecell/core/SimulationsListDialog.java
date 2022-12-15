package pl.edu.agh.firecell.core;

import imgui.ImGui;
import pl.edu.agh.firecell.core.dialog.AbstractDialog;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class SimulationsListDialog extends AbstractDialog {

    private final List<String> simulationNames;
    private final Consumer<String> selectHandler;
    private String selectedSimulation = null;

    public SimulationsListDialog(List<String> simulationNames, Consumer<String> selectHandler) {
        super("Run stored simulation");
        this.simulationNames = simulationNames;
        this.selectHandler = selectHandler;
    }

    @Override
    protected void buildGui() {
        if (ImGui.beginListBox("Stored simulations##listbox")) {
            for (String roomName : simulationNames) {
                if (ImGui.selectable(roomName, Objects.equals(selectedSimulation, roomName))) {
                    selectedSimulation = roomName;
                }
            }
        }
        ImGui.endListBox();
        if (ImGui.button("Select") && selectedSimulation != null) {
            selectHandler.accept(selectedSimulation);
        }
        if (ImGui.button("Cancel")) {
            selectHandler.accept(null);
        }
    }
}
