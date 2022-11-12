package pl.edu.agh.firecell.core;

import imgui.ImGui;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.statebuilder.ElementWrapper;
import pl.edu.agh.firecell.core.statebuilder.RoomStorage;
import pl.edu.agh.firecell.core.statebuilder.StateBuilder;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.Room;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static imgui.flag.ImGuiWindowFlags.*;

public class MenuScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(MenuScene.class);

    private final Consumer<Room> startStateBuilderHandler;
    private final RoomStorage roomStorage;
    private final SimulationConfig defaultConfig = createInitialSimulationConfig();
    private final Consumer<SimulationConfig> startSimulationHandler;
    private final List<String> savedRoomNames = new ArrayList<>();
    private String selectedRoom = null;

    public static final Vector2i MENU_SIZE = new Vector2i(150, 100);

    public MenuScene(Consumer<SimulationConfig> startSimulationHandler, Consumer<Room> startStateBuilderHandler,
                     RoomStorage roomStorage) {
        this.startStateBuilderHandler = startStateBuilderHandler;
        this.startSimulationHandler = startSimulationHandler;
        this.roomStorage = roomStorage;
        savedRoomNames.addAll(roomStorage.getRoomNames());
    }

    @Override
    public void update(double frameTime) {
        renderGUI();
    }

    private void renderGUI() {
        renderMenuBar();
        renderMenu();
        renderRoomList();
    }

    private void renderMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Simulation")) {
                if (ImGui.menuItem("Start simulation")) {
                    startSimulationHandler.accept(defaultConfig);
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Some settings1")) {
                if (ImGui.menuItem("Some settings1.1")) {
                    logger.info("Some settings1.1");
                }
                if (ImGui.menuItem("Some settings1.2")) {
                    logger.info("Some settings1.2");
                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }

    private void renderMenu() {
        var viewport = ImGui.getMainViewport();
        var menuPosition = new Vector2i(viewport.getWorkPosX() + 30, viewport.getWorkPosY() + 30, RoundingMode.HALF_UP);
        ImGui.setNextWindowPos(menuPosition.x, menuPosition.y);
        ImGui.setNextWindowSize(MENU_SIZE.x, MENU_SIZE.y);
        if (ImGui.begin("Menu", NoResize | NoTitleBar | NoMove | NoDecoration)) {
            if (ImGui.button("Room builder")) {
                startStateBuilderHandler.accept(null);
            }
        }
        ImGui.end();
    }

    private void renderRoomList() {
        if (ImGui.begin("Saved rooms", NoResize | NoMove | NoTitleBar | NoDecoration)) {
            if (ImGui.beginListBox("Saved rooms##listbox")) {
                for (String roomName : savedRoomNames) {
                    if (ImGui.selectable(roomName, Objects.equals(selectedRoom, roomName))) {
                        selectedRoom = roomName;
                    }
                }
            }
            ImGui.endListBox();
            if (ImGui.button("Edit") && selectedRoom != null) {
                try {
                    Room room = roomStorage.loadRoom(selectedRoom);
                    startStateBuilderHandler.accept(room);
                } catch (IOException e) {
                    // TODO: show some gui
                }
            }
            if (ImGui.button("Run simulation") && selectedRoom != null) {
                try {
                    Room room = roomStorage.loadRoom(selectedRoom);
                    StateBuilder stateBuilder = new StateBuilder(room.spaceSize());
                    for (ElementWrapper elementWrapper : room.elements()) {
                        stateBuilder.addElement(elementWrapper.element());
                    }
                    State initialState = stateBuilder.build();
                    SimulationConfig simulationConfig = new SimulationConfig(room.spaceSize(), initialState, defaultConfig.stepTime());
                    startSimulationHandler.accept(simulationConfig);
                } catch (IOException e) {
                    // TODO: show some gui
                }
            }
        }
        ImGui.end();
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

        return new SimulationConfig(spaceSize, initialState, 0.5);
    }
}
