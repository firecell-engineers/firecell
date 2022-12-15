package pl.edu.agh.firecell.core.statebuilder;

import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.scene.Scene;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.core.statebuilder.dialog.ManipulateElementDialog;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.util.GuiUtils;
import pl.edu.agh.firecell.renderer.BasicRenderer;
import pl.edu.agh.firecell.renderer.Renderer;
import pl.edu.agh.firecell.storage.StateBlueprintStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static imgui.flag.ImGuiWindowFlags.*;
import static pl.edu.agh.firecell.core.util.StateUtils.emptyState;

// TODO: make abstract state-rendering scene
public class StateBuilderScene implements Scene {
    private static final Logger logger = LoggerFactory.getLogger(StateBuilderScene.class);
    private final ImString nameBuffer;
    private final StateBuilderService stateBuilderService;
    private final StateBlueprintStorage stateBlueprintStorage = new StateBlueprintStorage();
    private final List<ElementWrapper> elements = new ArrayList<>();
    private final Renderer renderer;
    private ElementWrapper selectedElement = null;
    private int editedElementIndex = -1;
    private final ManipulateElementDialog manipulateElementDialog;
    private final Runnable finishedHandler;
    private final ImInt spaceSizeX = new ImInt(30);
    private final ImInt spaceSizeY = new ImInt(10);
    private final ImInt spaceSizeZ = new ImInt(30);
    public static final String DEFAULT_ROOM_NAME = "New state";

    public StateBuilderScene(IOListener ioListener, float aspectRatio, Runnable finishedHandler,
                             StateBlueprint stateBlueprint) throws IOException {
        this.finishedHandler = finishedHandler;
        if (stateBlueprint != null) {
            elements.addAll(stateBlueprint.elements());
            nameBuffer = new ImString(stateBlueprint.name(), 100);
        } else {
            nameBuffer = new ImString(determineDefaultRoomName(), 100);
        }

        stateBuilderService = new StateBuilderService(composeSpaceSize());
        renderer = new BasicRenderer(aspectRatio, ioListener, createInitialConfig(composeSpaceSize()));
        manipulateElementDialog = new ManipulateElementDialog(this::manipulateElementHandler);

        stateBlueprintStorage.createBaseDirectory();
        stateBuilderService.scheduleStateCalculation(elements);
    }

    public StateBuilderScene(IOListener ioListener, float aspectRatio,
                             Runnable finishedHandler) throws IOException {
        this(ioListener, aspectRatio, finishedHandler, null);
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

            ImGui.text("Space size");
            boolean modifiedX = ImGui.inputInt("X##spaceSize", spaceSizeX);
            boolean modifiedY = ImGui.inputInt("Y##spaceSize", spaceSizeY);
            boolean modifiedZ = ImGui.inputInt("Z##spaceSize", spaceSizeZ);
            if (modifiedX || modifiedY || modifiedZ) {
                stateBuilderService.setSpaceSize(GuiUtils.createVector3i(spaceSizeX, spaceSizeY, spaceSizeZ));
                stateBuilderService.scheduleStateCalculation(elements);
            }
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
        StateBlueprint stateBlueprint = new StateBlueprint(composeSpaceSize(), nameBuffer.get(), elements);
        try {
            stateBlueprintStorage.saveBlueprint(stateBlueprint);
        } catch (IOException e) {
            logger.warn("Failed to save blueprint", e);
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
        return new SimulationConfig(emptyState(spaceSize), 0.5);
    }

    private String determineDefaultRoomName() {
        Set<String> roomNames = new HashSet<>(stateBlueprintStorage.getBlueprintNames());
        int number = 0;
        String newRoomName = DEFAULT_ROOM_NAME;
        while (roomNames.contains(newRoomName)) {
            number++;
            newRoomName = "%s (%d)".formatted(DEFAULT_ROOM_NAME, number);
        }
        return newRoomName;
    }

    private Vector3i composeSpaceSize() {
        return GuiUtils.createVector3i(spaceSizeX, spaceSizeY, spaceSizeZ);
    }
}
