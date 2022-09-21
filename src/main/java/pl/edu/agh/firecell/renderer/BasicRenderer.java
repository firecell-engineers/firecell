package pl.edu.agh.firecell.renderer;

import io.reactivex.rxjava3.disposables.Disposable;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.camera.Camera;
import pl.edu.agh.firecell.renderer.camera.CameraController;
import pl.edu.agh.firecell.renderer.mesh.MeshUtils;
import pl.edu.agh.firecell.renderer.mesh.Shader;
import pl.edu.agh.firecell.renderer.mesh.StateMesh;

import java.io.IOException;
import java.nio.file.InvalidPathException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;

public class BasicRenderer implements Renderer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Shader opaqueMaterialShader = new Shader("opaqueMaterial.glsl.vert", "basic.glsl.frag");
    private final Shader transparentTempShader = new Shader("transparentTemperature.glsl.vert", "basic.glsl.frag");
    private final Camera camera;
    private final IOListener ioListener;
    private final CameraController cameraController;
    private Disposable windowSizeSubscription;
    private RenderMode renderMode = RenderMode.TEMPERATURE_AIR;

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

        switch (renderMode) {
            case STANDARD -> renderStateStandard(state);
            case TEMPERATURE_AIR -> renderStateTemperatureAir(state);
            case TEMPERATURE_SOLID -> renderStateTemperatureSolid(state);
        }
    }

    private void renderStateStandard(State state) {
        var solidStateMesh = new StateMesh(
                MeshUtils.CUBE_VERTICES, state,
                cell -> !cell.material().equals(Material.AIR)
        );
        opaqueMaterialShader.bind();
        solidStateMesh.draw();
    }

    private void renderStateTemperatureAir(State state) {
        glDisable(GL_BLEND);
        var solidStateMesh = new StateMesh(
                MeshUtils.CUBE_VERTICES, state,
                cell -> !cell.material().equals(Material.AIR)
        );
        opaqueMaterialShader.bind();
        solidStateMesh.draw();

        glEnable(GL_BLEND);
        var airStateMesh = new StateMesh(
                MeshUtils.CUBE_VERTICES, state,
                cell -> cell.material().equals(Material.AIR) &&
                        cell.temperature() > 25
        );
        transparentTempShader.bind();
        airStateMesh.draw();
    }

    private void renderStateTemperatureSolid(State state) {
        glEnable(GL_BLEND);
        var solidStateMesh = new StateMesh(
                MeshUtils.CUBE_VERTICES, state,
                cell -> cell.material().equals(Material.AIR)
        );
        transparentTempShader.bind();
        solidStateMesh.draw();
    }

    @Override
    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
        opaqueMaterialShader.setInt("uRenderMode", renderMode.ordinal());
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

        windowSizeSubscription = ioListener.windowSizeObservable().subscribe(size -> {
            camera.setAspectRatio(size.x / (float) size.y);
            opaqueMaterialShader.setMatrix4("uProjection", camera.perspectiveMatrix());
        });
    }
}
