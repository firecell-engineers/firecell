package pl.edu.agh.firecell.core.statebuilder;

import imgui.ImGui;
import imgui.type.ImString;
import org.joml.Vector3i;
import pl.edu.agh.firecell.core.Scene;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.core.statebuilder.dialog.AddElementDialog;
import pl.edu.agh.firecell.model.Room;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.BasicRenderer;
import pl.edu.agh.firecell.renderer.Renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static imgui.flag.ImGuiWindowFlags.AlwaysAutoResize;

// TODO: make abstract state-rendering scene
public class StateBuilderScene implements Scene {
    private final ImString nameBuffer;
    private final StateBuilder stateBuilder;
    private final RoomStorage roomStorage;
    private final List<ElementWrapper> elements = new ArrayList<>();
    private final Renderer renderer;
    private ElementWrapper selectedElement = null;
    private final AddElementDialog addElementDialog;
    private final Runnable finishedHandler;
    private State currentState;
    private final Vector3i spaceSize = new Vector3i(30, 20, 30); // TODO: let user define space size

    private StateBuilderScene(RoomStorage roomStorage, IOListener ioListener, float aspectRatio,
                              Runnable finishedHandler, List<ElementWrapper> elements, String name) throws IOException {
        this.finishedHandler = finishedHandler;
        this.roomStorage = roomStorage;
        this.elements.addAll(elements);
        nameBuffer = new ImString(name, 100);
        roomStorage.createBaseDirectory();
        stateBuilder = createStateBuilder(spaceSize, elements);
        currentState = stateBuilder.build();
        renderer = new BasicRenderer(aspectRatio, ioListener, createInitialConfig(currentState, spaceSize));
        addElementDialog = new AddElementDialog(this::addElementHandler, stateBuilder);
    }

    public StateBuilderScene(RoomStorage roomStorage, IOListener ioListener, float aspectRatio,
                             Runnable finishedHandler) throws IOException {
        this(roomStorage, ioListener, aspectRatio, finishedHandler, Collections.emptyList(), "");
    }

    public StateBuilderScene(RoomStorage roomStorage, IOListener ioListener, float aspectRatio,
                             Runnable finishedHandler, Room initialRoom) throws IOException {
        this(roomStorage, ioListener, aspectRatio, finishedHandler, initialRoom.elements(), initialRoom.name());
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
                if (ImGui.menuItem("Save")) {
                    saveElements();
                    finishedHandler.run();
                }
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
                elements.remove(selectedElement);
                stateBuilder.removeElement(selectedElement.element());
                currentState = stateBuilder.build();
            }
            if (ImGui.button("Add element")) {
                addElementDialog.setVisible(!addElementDialog.isVisible());
            }
        }
        ImGui.end();
    }

    private void saveElements() {
        Room room = new Room(spaceSize, nameBuffer.get(), elements);
        try {
            roomStorage.saveRoom(room);
        } catch (IOException e) {
            // TODO: show some error message
        }
    }

    private void addElementHandler(ElementWrapper element) {
        elements.add(element);
        currentState = stateBuilder.build();
        addElementDialog.setVisible(false);
    }

    @Override
    public void dispose() {

    }

    private static StateBuilder createStateBuilder(Vector3i spaceSize, List<ElementWrapper> elements) {
        StateBuilder stateBuilder = new StateBuilder(spaceSize);
        for (ElementWrapper element : elements) {
            stateBuilder.addElement(element.element());
        }
        return stateBuilder;
    }

    private SimulationConfig createInitialConfig(State state, Vector3i spaceSize) {
        return new SimulationConfig(spaceSize, state, 0.5);
    }
}
