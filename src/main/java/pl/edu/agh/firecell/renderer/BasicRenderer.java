package pl.edu.agh.firecell.renderer;

import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.lwjgl.glfw.GLFW.*;

public class BasicRenderer implements Renderer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Shader shader = new Shader("instanced.glsl.vert", "basic.glsl.frag");
    private final Camera camera;
    private final IOListener ioListener;
    private Disposable windowSizeSubscription;

    public BasicRenderer(float aspectRatio, IOListener ioListener, SimulationConfig config)
            throws IOException, InvalidPathException, IllegalStateException {
        this.ioListener = ioListener;
        camera = new Camera(aspectRatio);
        initializeRendering(config);
    }

    @Override
    public void render(State state, double frameTime) {
        processCameraControl(frameTime);
        shader.setMatrix4("uView", camera.viewMatrix());
        renderState(state);
    }

    @Override
    public void dispose() {
        windowSizeSubscription.dispose();
    }

    private void renderState(State state) {
        var indexedCells = IntStream.range(0, state.cells().size()).boxed()
                .map(index -> new ImmutablePair<>(index, state.cells().get(index)))
                .toList();

        drawCells(indexedCells, state);
        drawFire(indexedCells, state);
    }

    private void drawCells(List<ImmutablePair<Integer, Cell>> cells, State state) {
        separateCellsIntoGroupsByMaterial(cells).forEach((material, indices) -> {
            if (material == Material.AIR) {
                return;
            }
            var transformations = createTransformations(indices, state);
            renderInstancedCube(resolveColor(material), transformations);
        });
    }

    private void drawFire(List<ImmutablePair<Integer, Cell>> cells, State state) {
        var burningIndices = cells.stream()
                .filter(indexCellPair -> indexCellPair.getRight().burningTime() > 0)
                .map(ImmutablePair::getLeft)
                .toList();
        renderInstancedCube(new Vector3f(1.0f, 0.0f, 0.0f), createTransformations(burningIndices, state));
    }

    private List<Transformation> createTransformations(List<Integer> indices, State state) {
        return indices.stream()
                .map(flatIndex -> IndexUtils.expandIndex(flatIndex, state.spaceSize()))
                .map(index -> new Transformation(
                        new Vector3f(index.x, index.y, index.z),
                        new Vector3f(0.0f), new Vector3f(1.0f)))
                .toList();
    }

    private void renderInstancedCube(Vector3f color, List<Transformation> transformations) {
        var mesh = MeshFactory.createInstancedCubeMesh(transformations);
        shader.setVector3("uObjectColor", color);
        mesh.draw();
    }

    private Vector3f resolveColor(Material material) {
        return switch (material) {
            case WOOD -> new Vector3f(0.7f, 0.5f, 0.5f);
            default -> new Vector3f(1.0f, 0.8f, 0.8f);
        };
    }

    private Map<Material, List<Integer>> separateCellsIntoGroupsByMaterial(List<ImmutablePair<Integer, Cell>> cells) {
        Map<Material, List<Integer>> cellsGroups = new HashMap<>();
        cells.forEach(indexCellPair -> {
            var material = indexCellPair.getRight().material();
            if (!cellsGroups.containsKey(material)) {
                cellsGroups.put(material, new LinkedList<>());
            }
            cellsGroups.get(material).add(indexCellPair.getLeft());
        });
        return cellsGroups;
    }

    private void initializeRendering(SimulationConfig config) {
        var spaceSize = new Vector3f(config.size().x, config.size().y, config.size().z);
        camera.setPosition(new Vector3f(spaceSize).mul(1.2f));
        camera.setDirection(new Vector3f(spaceSize).mul(0.5f).sub(camera.position()));
        shader.bind();
        shader.setMatrix4("uProjection", camera.perspectiveMatrix());
        shader.setVector3("uLightDir", new Vector3f(-1.0f, -0.8f, 0.5f));
        shader.setVector3("uLightColor", new Vector3f(1.0f, 1.0f, 1.0f));
        windowSizeSubscription = ioListener.windowSizeObservable().subscribe(size -> {
            camera.setAspectRatio(size.x / (float) size.y);
            shader.setMatrix4("uProjection", camera.perspectiveMatrix());
        });
    }

    private void processCameraControl(double deltaTime) {
        if (ioListener.isPressed(GLFW_KEY_D)) {
            camera.moveRight((float) deltaTime);
        }
        if (ioListener.isPressed(GLFW_KEY_A)) {
            camera.moveRight((float) -deltaTime);
        }
        if (ioListener.isPressed(GLFW_KEY_W)) {
            camera.moveForward((float) deltaTime);
        }
        if (ioListener.isPressed(GLFW_KEY_S)) {
            camera.moveForward((float) -deltaTime);
        }
        if (ioListener.isPressed(GLFW_KEY_SPACE)) {
            camera.moveUp((float) deltaTime);
        }
        if (ioListener.isPressed(GLFW_KEY_LEFT_SHIFT)) {
            camera.moveUp((float) -deltaTime);
        }
    }
}
