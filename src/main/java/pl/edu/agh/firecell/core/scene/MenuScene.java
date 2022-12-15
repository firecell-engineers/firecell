package pl.edu.agh.firecell.core.scene;

import imgui.ImGui;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.dialog.Dialog;
import pl.edu.agh.firecell.core.dialog.RoomListDialog;
import pl.edu.agh.firecell.core.statebuilder.ElementWrapper;
import pl.edu.agh.firecell.core.statebuilder.StateBlueprint;
import pl.edu.agh.firecell.core.statebuilder.StateBuilder;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.storage.SimulationStorage;
import pl.edu.agh.firecell.storage.StateBlueprintStorage;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static imgui.flag.ImGuiWindowFlags.*;

public class MenuScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(MenuScene.class);

    private final Consumer<StateBlueprint> startStateBuilderHandler;
    private final StateBlueprintStorage stateBlueprintStorage = new StateBlueprintStorage();
    private final SimulationStorage simulationStorage = new SimulationStorage();
    private final SimulationConfig defaultConfig = createInitialSimulationConfig();
    private final Consumer<String> startStoredSimulationHandler;
    private final BiConsumer<SimulationConfig, String> startSimulationHandler;
    private Dialog currentDialog = null;

    public MenuScene(BiConsumer<SimulationConfig, String> startSimulationHandler,
                     Consumer<String> startStoredSimulationHandler, Consumer<StateBlueprint> startStateBuilderHandler) {
        this.startStateBuilderHandler = startStateBuilderHandler;
        this.startStoredSimulationHandler = startStoredSimulationHandler;
        this.startSimulationHandler = startSimulationHandler;
    }

    @Override
    public void update(double frameTime) {
        renderGUI();
    }

    private void renderGUI() {
        renderMenuBar();
        renderCurrentDialog();
    }

    private void renderMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Simulation")) {
                if (ImGui.menuItem("Start simulation")) {
                    currentDialog = new RoomListDialog("Start simulation",
                            stateBlueprintStorage.getBlueprintNames(), this::startSimulationHandler);
                    logger.debug("Created start simulation dialog");
                }
                if (ImGui.menuItem("Run stored simulation")) {
                    currentDialog = new SimulationsListDialog(simulationStorage.findStoredSimulations(),
                            this::startStoredSimulationHandler);
                    logger.debug("Created start stored simulation dialog");
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("State builder")) {
                if (ImGui.menuItem("Create new state")) {
                    startStateBuilderHandler.accept(null);
                }
                if (ImGui.menuItem("Edit state")) {
                    currentDialog = new RoomListDialog("Edit state",
                            stateBlueprintStorage.getBlueprintNames(), this::editRoomHandler);
                    logger.debug("Created edit state dialog");
                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }

    private void renderCurrentDialog() {
        if (currentDialog != null) {
            var viewport = ImGui.getMainViewport();
            var menuPosition = new Vector2i(viewport.getWorkPosX() + 30, viewport.getWorkPosY() + 30, RoundingMode.HALF_UP);
            ImGui.setNextWindowPos(menuPosition.x, menuPosition.y);
            currentDialog.setFlags(AlwaysAutoResize | NoResize | NoMove | NoDecoration);
            currentDialog.render();
        }
    }

    private void startSimulationHandler(String roomName) {
        if (roomName == null) {
            return;
        }
        try {
            StateBlueprint stateBlueprint = stateBlueprintStorage.loadBlueprint(roomName);
            StateBuilder stateBuilder = new StateBuilder(stateBlueprint.spaceSize());
            for (ElementWrapper elementWrapper : stateBlueprint.elements()) {
                stateBuilder.addElement(elementWrapper.element());
            }
            State initialState = stateBuilder.build();
            SimulationConfig simulationConfig = new SimulationConfig(initialState, defaultConfig.stepTime());
            startSimulationHandler.accept(simulationConfig, roomName);
        } catch (IOException e) {
            logger.warn("Failed to load blueprint", e);
            // TODO: show some gui
        }
    }

    private void startStoredSimulationHandler(String simulationName) {
        if (simulationName == null) {
            return;
        }
        startStoredSimulationHandler.accept(simulationName);
    }

    private void editRoomHandler(String roomName) {
        if (roomName == null) {
            return;
        }
        try {
            StateBlueprint stateBlueprint = stateBlueprintStorage.loadBlueprint(roomName);
            startStateBuilderHandler.accept(stateBlueprint);
        } catch (IOException e) {
            logger.warn("Failed to load blueprint", e);
            // TODO: show some gui
        }
    }

    @Override
    public void dispose() {
    }

    private SimulationConfig createInitialSimulationConfig() {
        Vector3i spaceSize = new Vector3i(30, 20, 30);
        State initialState = new StateBuilder(spaceSize)
                .addCuboid(new Vector3i(0, 0, 0), new Vector3i(30, 1, 30), Material.WOOD) // floor
                .addCuboid(new Vector3i(0, 0, 0), new Vector3i(30, 20, 1), Material.WOOD) // wall
                .addCuboid(new Vector3i(0, 0, 0), new Vector3i(1, 20, 30), Material.WOOD) // wall
                .addCuboid(new Vector3i(12, 1, 12), new Vector3i(1, 4, 1), Material.WOOD) // table leg
                .addCuboid(new Vector3i(17, 1, 12), new Vector3i(1, 4, 1), Material.WOOD) // table leg
                .addCuboid(new Vector3i(17, 1, 17), new Vector3i(1, 4, 1), Material.WOOD) // table leg
                .addCuboid(new Vector3i(12, 1, 17), new Vector3i(1, 4, 1), Material.WOOD) // table leg
                .addCuboid(new Vector3i(12, 4, 12), new Vector3i(6, 1, 6), Material.WOOD) // table surface
                .addCuboid(new Vector3i(14, 7, 14), new Vector3i(1, 1, 1), Material.WOOD) // cube above the table
                .igniteCuboid(new Vector3i(14, 4, 14), new Vector3i(2, 2, 2)) // fire source
                .build();

        return new SimulationConfig(initialState, 0.5);
    }
}
