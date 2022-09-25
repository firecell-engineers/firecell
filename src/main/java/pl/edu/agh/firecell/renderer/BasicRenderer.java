package pl.edu.agh.firecell.renderer;

import io.reactivex.rxjava3.disposables.Disposable;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.model.*;
import pl.edu.agh.firecell.renderer.camera.Camera;
import pl.edu.agh.firecell.renderer.camera.CameraController;
import pl.edu.agh.firecell.renderer.mesh.MeshUtils;
import pl.edu.agh.firecell.renderer.mesh.Shader;
import pl.edu.agh.firecell.renderer.mesh.StateMesh;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.Comparator;

import static org.lwjgl.opengl.GL11.*;

public class BasicRenderer implements Renderer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Shader opaqueMaterialShader = new Shader("material.glsl.vert", "ambientDiffuse.glsl.frag");
    private final Shader transparentTempShader = new Shader("temperature.glsl.vert", "ambientDiffuse.glsl.frag");
    private final Shader fireShader = new Shader("burning.glsl.vert", "ambientDiffuse.glsl.frag");
    private final Camera camera;
    private final IOListener ioListener;
    private final CameraController cameraController;
    private Disposable windowSizeSubscription;
    private RenderMode renderMode = RenderMode.STANDARD;


    public BasicRenderer(float aspectRatio, IOListener ioListener, SimulationConfig config)
            throws IOException, InvalidPathException, IllegalStateException {
        camera = new Camera(aspectRatio);
        cameraController = new CameraController(camera, ioListener);
        this.ioListener = ioListener;
        initializeRendering(config);
    }

    @Override
    public void render(State state, double frameTime) {
        cameraController.update(frameTime);
        opaqueMaterialShader.bind();
        opaqueMaterialShader.setMatrix4("uView", camera.viewMatrix());
        transparentTempShader.bind();
        transparentTempShader.setMatrix4("uView", camera.viewMatrix());
        fireShader.bind();
        fireShader.setMatrix4("uView", camera.viewMatrix());

        switch (renderMode) {
            case STANDARD -> renderStateStandard(state);
            case TEMPERATURE_AIR -> renderStateTemperatureAir(state);
            case TEMPERATURE_SOLID -> renderStateTemperatureSolid(state);
        }
    }

    private void renderStateStandard(State state) {
        renderOpaqueCellsExcludingAir(state);
        renderFire(state);
    }

    private void renderStateTemperatureAir(State state) {
        renderOpaqueCellsExcludingAir(state);
        renderTemperatureTransparentAir(state);
    }

    private void renderStateTemperatureSolid(State state) {
        renderTemperatureTransparentSolids(state);
    }

    private void renderOpaqueCellsExcludingAir(State state) {
        var notAirCellsList = state.getIndexedCellsStream()
                .filter(indexedCell -> !indexedCell.cell().material().equals(Material.AIR))
                .toList();
        var mesh = new StateMesh(MeshUtils.CUBE_VERTICES, notAirCellsList);
        opaqueMaterialShader.bind();
        mesh.draw();
    }

    private void renderFire(State state) {
        glDepthMask(false);
        var burningCellsList = state.getIndexedCellsStream()
                .filter(indexedCell -> indexedCell.cell().burningTime() > 0)
                .sorted(cameraDistanceCellComparator(camera.position()))
                .toList();
        var mesh = new StateMesh(MeshUtils.CUBE_VERTICES, burningCellsList);
        fireShader.bind();
        mesh.draw();
        glDepthMask(true);
    }

    private void renderTemperatureTransparentAir(State state) {
        glDepthMask(false);
        var airCells = state.getIndexedCellsStream()
                .filter(indexedCell -> indexedCell.cell().material().equals(Material.AIR))
                .sorted(cameraDistanceCellComparator(camera.position()))
                .toList();
        var mesh = new StateMesh(MeshUtils.CUBE_VERTICES, airCells);
        transparentTempShader.bind();
        mesh.draw();
        glDepthMask(true);
    }

    private void renderTemperatureTransparentSolids(State state) {
        glDepthMask(false);
        var notAirCellsList = state.getIndexedCellsStream()
                .filter(indexedCell -> !indexedCell.cell().material().equals(Material.AIR))
                .toList();
        var mesh = new StateMesh(MeshUtils.CUBE_VERTICES, notAirCellsList);
        transparentTempShader.bind();
        mesh.draw();
        glDepthMask(true);
    }

    @Override
    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
    }

    @Override
    public void dispose() {
        windowSizeSubscription.dispose();
        cameraController.dispose();
    }

    private void initializeRendering(SimulationConfig config) {
        var spaceSize = new Vector3f(config.size().x, config.size().y, config.size().z);
        camera.setPosition(new Vector3f(spaceSize).mul(0.5f).add(new Vector3f(0.0f, 0.0f, spaceSize.z * 1.5f)));

        opaqueMaterialShader.bind();
        opaqueMaterialShader.setMatrix4("uProjection", camera.perspectiveMatrix());
        opaqueMaterialShader.setVector3("uLightDir", new Vector3f(-1.0f, -0.8f, 0.5f));
        opaqueMaterialShader.setVector3("uLightColor", new Vector3f(1.0f, 1.0f, 1.0f));
        transparentTempShader.bind();
        transparentTempShader.setMatrix4("uProjection", camera.perspectiveMatrix());
        transparentTempShader.setVector3("uLightDir", new Vector3f(-1.0f, -0.8f, 0.5f));
        transparentTempShader.setVector3("uLightColor", new Vector3f(1.0f, 1.0f, 1.0f));
        fireShader.bind();
        fireShader.setMatrix4("uProjection", camera.perspectiveMatrix());
        fireShader.setVector3("uLightDir", new Vector3f(-1.0f, -0.8f, 0.5f));
        fireShader.setVector3("uLightColor", new Vector3f(1.0f, 1.0f, 1.0f));

        windowSizeSubscription = ioListener.windowSizeObservable().subscribe(size -> {
            camera.setAspectRatio(size.x / (float) size.y);
            opaqueMaterialShader.setMatrix4("uProjection", camera.perspectiveMatrix());
            transparentTempShader.setMatrix4("uProjection", camera.perspectiveMatrix());
            fireShader.setMatrix4("uProjection", camera.perspectiveMatrix());
        });
    }


    private Comparator<IndexedCell> cameraDistanceCellComparator(Vector3f cameraPosition) {
        return Comparator.comparingDouble(indexedCell -> new Vector3f(indexedCell.index())
                .sub(cameraPosition, new Vector3f())
                .length()
        );
    }
}

