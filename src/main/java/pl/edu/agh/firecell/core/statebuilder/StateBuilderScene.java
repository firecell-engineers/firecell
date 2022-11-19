package pl.edu.agh.firecell.core.statebuilder;

import imgui.ImGui;
import imgui.type.ImString;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3i;
import pl.edu.agh.firecell.core.Scene;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.core.statebuilder.dialog.ManipulateElementDialog;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.renderer.BasicRenderer;
import pl.edu.agh.firecell.renderer.Renderer;
import pl.edu.agh.firecell.storage.RoomStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static imgui.flag.ImGuiWindowFlags.*;
import static pl.edu.agh.firecell.core.util.StateUtils.emptyState;

// TODO: make abstract state-rendering scene
public class StateBuilderScene implements Scene {
    private final ImString nameBuffer;
    private final StateBuilderService stateBuilderService;
    private final RoomStorage roomStorage;
    private final List<ElementWrapper> elements = new ArrayList<>();
    private final Renderer renderer;
    private ElementWrapper selectedElement = null;
    private int editedElementIndex = -1;
    private final ManipulateElementDialog manipulateElementDialog;
    private final Runnable finishedHandler;
    private final Vector3i spaceSize = new Vector3i(30, 20, 30); // TODO: let user define space size
    public static final String DEFAULT_ROOM_NAME = "New room";

    public StateBuilderScene(RoomStorage roomStorage, IOListener ioListener, float aspectRatio,
                             Runnable finishedHandler, Room room) throws IOException {
        this.finishedHandler = finishedHandler;
        this.roomStorage = roomStorage;
        if (room != null) {
            elements.addAll(room.elements());
            nameBuffer = new ImString(room.name(), 100);
        } else {
            nameBuffer = new ImString(determineDefaultRoomName(), 100);
        }

        stateBuilderService = new StateBuilderService(spaceSize);
        renderer = new BasicRenderer(aspectRatio, ioListener, createInitialConfig(spaceSize));
        manipulateElementDialog = new ManipulateElementDialog(this::manipulateElementHandler);

        roomStorage.createBaseDirectory();
        stateBuilderService.scheduleStateCalculation(elements);
    }

    public StateBuilderScene(RoomStorage roomStorage, IOListener ioListener, float aspectRatio,
                             Runnable finishedHandler) throws IOException {
        this(roomStorage, ioListener, aspectRatio, finishedHandler, null);
    }

    @Override
    public void update(double frameTime) {
        renderer.render(stateBuilderService.getCurrentState(), frameTime);
        renderGUI();
    }

    private void renderGUI() {
        renderMenuBar();
        renderEditMenu();
        manipulateElementDialog.render();
    }

    private void renderMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Save")) {
                    saveElements();
                    finishedHandler.run();
                }
                if (ImGui.menuItem("Close")) {
                    finishedHandler.run();
                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }

    private void renderEditMenu() {
        var viewport = ImGui.getMainViewport();
        var menuPosition = new Vector2i(viewport.getWorkPosX() + 30, viewport.getWorkPosY() + 30, RoundingMode.HALF_UP);
        ImGui.setNextWindowPos(menuPosition.x, menuPosition.y);
        if (ImGui.begin("Edit##fdsdfsfds", AlwaysAutoResize | NoResize | NoMove)) {
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
                selectedElement = null;
                stateBuilderService.scheduleStateCalculation(elements);
            }
            if (ImGui.button("Edit") && selectedElement != null) {
                manipulateElementDialog.setVisible(true);
                manipulateElementDialog.setMode(ManipulateElementDialog.Mode.EDIT);
                manipulateElementDialog.setCurrentElement(selectedElement);
                editedElementIndex = elements.indexOf(selectedElement);
            }
            if (ImGui.button("Add element")) {
                manipulateElementDialog.setMode(ManipulateElementDialog.Mode.ADD);
                manipulateElementDialog.setVisible(true);
                manipulateElementDialog.setCurrentElement(null);
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

    private void manipulateElementHandler(ElementWrapper element, ManipulateElementDialog.Mode mode) {
        if (element == null) {
            manipulateElementDialog.setVisible(false);
            return;
        }

        switch (mode) {
            case ADD -> elements.add(element);
            case EDIT -> elements.set(editedElementIndex, element);
        }

        stateBuilderService.scheduleStateCalculation(elements);
        manipulateElementDialog.setVisible(false);
    }

    @Override
    public void dispose() {
        stateBuilderService.dispose();
    }

    private SimulationConfig createInitialConfig(Vector3i spaceSize) {
        return new SimulationConfig(spaceSize, emptyState(spaceSize), 0.5);
    }

    private String determineDefaultRoomName() {
        Set<String> roomNames = new HashSet<>(roomStorage.getRoomNames());
        int number = 0;
        String newRoomName = DEFAULT_ROOM_NAME;
        while (roomNames.contains(newRoomName)) {
            number++;
            newRoomName = "%s (%d)".formatted(DEFAULT_ROOM_NAME, number);
        }
        return newRoomName;
    }
}
