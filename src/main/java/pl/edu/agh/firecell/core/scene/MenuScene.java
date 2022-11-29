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
import pl.edu.agh.firecell.core.statebuilder.Room;
import pl.edu.agh.firecell.core.statebuilder.RoomStorage;
import pl.edu.agh.firecell.core.statebuilder.StateBuilder;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;

import java.io.IOException;
import java.util.function.Consumer;

import static imgui.flag.ImGuiWindowFlags.*;

public class MenuScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(MenuScene.class);

    private final Consumer<Room> startStateBuilderHandler;
    private final RoomStorage roomStorage;
    private final SimulationConfig defaultConfig = createInitialSimulationConfig();
    private final Consumer<SimulationConfig> startSimulationHandler;
    private Dialog currentDialog = null;

    public MenuScene(Consumer<SimulationConfig> startSimulationHandler, Consumer<Room> startStateBuilderHandler,
                     RoomStorage roomStorage) {
        this.startStateBuilderHandler = startStateBuilderHandler;
        this.startSimulationHandler = startSimulationHandler;
        this.roomStorage = roomStorage;
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
                if (ImGui.menuItem("Start simulation (OLD)")) {  // TODO: remove this item
                    startSimulationHandler.accept(defaultConfig);
                }
                if (ImGui.menuItem("Start simulation")) {
                    currentDialog = new RoomListDialog("Start simulation",
                            roomStorage.getRoomNames(), this::startSimulationHandler);
                    logger.debug("Created start simulation dialog");
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Editor")) {
                if (ImGui.menuItem("Create new room")) {
                    startStateBuilderHandler.accept(null);
                }
                if (ImGui.menuItem("Edit room")) {
                    currentDialog = new RoomListDialog("Edit room",
                            roomStorage.getRoomNames(), this::editRoomHandler);
                    logger.debug("Created edit room dialog");
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
            Room room = roomStorage.loadRoom(roomName);
            StateBuilder stateBuilder = new StateBuilder(room.spaceSize());
            for (ElementWrapper elementWrapper : room.elements()) {
                stateBuilder.addElement(elementWrapper.element());
            }
            State initialState = stateBuilder.build();
            SimulationConfig simulationConfig = new SimulationConfig(initialState, defaultConfig.stepTime());
            startSimulationHandler.accept(simulationConfig);
        } catch (IOException e) {
            // TODO: show some gui
        }
    }

    private void editRoomHandler(String roomName) {
        if (roomName == null) {
            return;
        }
        try {
            Room room = roomStorage.loadRoom(roomName);
            startStateBuilderHandler.accept(room);
        } catch (IOException e) {
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