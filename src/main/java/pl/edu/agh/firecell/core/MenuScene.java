package pl.edu.agh.firecell.core;

import imgui.ImGui;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.StateBuilder;

import java.util.function.Consumer;

import static imgui.flag.ImGuiWindowFlags.*;

public class MenuScene implements Scene {

    private final Logger logger = LoggerFactory.getLogger(MenuScene.class);

    private final Runnable startStateBuilderHandler;
    private final IOListener ioListener;
    private SimulationConfig config = createInitialSimulationConfig();
    private final Consumer<SimulationConfig> startSimulationHandler;

    public static final Vector2i MENU_SIZE = new Vector2i(150, 100);

    public MenuScene(Consumer<SimulationConfig> startSimulationHandler, Runnable startStateBuilderHandler,
                     IOListener ioListener) {
        this.startStateBuilderHandler = startStateBuilderHandler;
        this.ioListener = ioListener;
        this.startSimulationHandler = startSimulationHandler;
    }

    @Override
    public void update(double frameTime) {
        renderGUI();
    }

    private void renderGUI() {
        renderMenuBar();
        renderMenu();
    }

    private void renderMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("Simulation")) {
                if (ImGui.menuItem("Start simulation")) {
                    startSimulationHandler.accept(config);
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
                startStateBuilderHandler.run();
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
                .igniteCuboid(new Vector3i(14, 4, 14), new Vector3i(2, 2, 2)) // fire source
                .build();

        return new SimulationConfig(spaceSize, initialState, 0.5);
    }
}
