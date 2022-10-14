package pl.edu.agh.firecell.core.statebuilder;

import imgui.ImGui;
import imgui.type.ImString;
import org.joml.Vector3i;
import pl.edu.agh.firecell.core.Scene;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.core.statebuilder.dialog.AddElementDialog;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.BasicRenderer;
import pl.edu.agh.firecell.renderer.Renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static imgui.flag.ImGuiWindowFlags.AlwaysAutoResize;

// TODO: make abstract state-rendering scene
public class StateBuilderScene implements Scene {
    private final ImString nameBuffer = new ImString("", 100);
    private final StateBuilder stateBuilder;
    private final List<ElementWrapper> elements = new ArrayList<>();
    private final Renderer renderer;
    private ElementWrapper selectedElement = null;
    private final AddElementDialog addElementDialog;
    private State currentState;

    public StateBuilderScene(IOListener ioListener, float aspectRatio) throws IOException {
        // TODO: let user define space size
        Vector3i spaceSize = new Vector3i(30, 20, 30);
        renderer = new BasicRenderer(aspectRatio, ioListener, createInitialConfig(spaceSize));
        stateBuilder = new StateBuilder(spaceSize);
        addElementDialog = new AddElementDialog(this::addElementHandler, stateBuilder);
        currentState = stateBuilder.build();
    }

    @Override
    public void update(double frameTime) {
        renderer.render(currentState, frameTime);
        renderGUI();
    }

    private void renderGUI() {
        renderMenuBar();
        renderEditMenu();
        addElementDialog.render();
    }

    private void renderMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                // todo: saving
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }

    private void renderEditMenu() {
        if (ImGui.begin("Edit##fdsdfsfds", AlwaysAutoResize)) {
            ImGui.inputText("Name##stateName", nameBuffer);
            ImGui.separator();
            if (ImGui.beginListBox("Elements")) {
                for (ElementWrapper element : elements) {
                    if (ImGui.selectable(element.name(), selectedElement == element)) {
                        selectedElement = element;
                    }
                }
                ImGui.endListBox();
            }
            if (ImGui.button("Remove") && selectedElement != null) {
                stateBuilder.removeElement(selectedElement.element());
                elements.remove(selectedElement);
            }
            if (ImGui.button("Add element")) {
                addElementDialog.setVisible(!addElementDialog.isVisible());
            }
        }
        ImGui.end();
    }

    private void addElementHandler(ElementWrapper element) {
        elements.add(element);
        currentState = stateBuilder.build();
        addElementDialog.setVisible(false);
    }

    @Override
    public void dispose() {

    }

    private static SimulationConfig createInitialConfig(Vector3i spaceSize) {
        State initialState = new StateBuilder(spaceSize).build();
        return new SimulationConfig(spaceSize, initialState, 0.5);
    }
}
