package pl.edu.agh.firecell.renderer;

import io.reactivex.rxjava3.disposables.Disposable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.camera.Camera;
import pl.edu.agh.firecell.renderer.camera.CameraController;
import pl.edu.agh.firecell.renderer.mesh.Shader;
import pl.edu.agh.firecell.renderer.mesh.Uniform;
import pl.edu.agh.firecell.renderer.rendermode.*;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.stream.Stream;

public class BasicRenderer implements Renderer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Shader opaqueMaterialShader = new Shader("material.glsl.vert", "ambientDiffuse.glsl.frag");
    private final Shader transparentTempShader = new Shader("temperature.glsl.vert", "ambientDiffuse.glsl.frag");
    private final Shader fireShader = new Shader("burning.glsl.vert", "ambientDiffuse.glsl.frag");
    private final Shader smokeShader = new Shader("smoke.glsl.vert", "ambientDiffuse.glsl.frag");

    private final Camera camera;
    private final CameraController cameraController;
    private final IOListener ioListener;
    private Disposable windowSizeSubscription;
    private RenderStrategy renderStrategy;
    private RenderMode renderMode;

    private Uniform<Matrix4f> projection;
    private final Uniform<Vector3f> lightDirection;
    private final Uniform<Vector3f> lightColor;

    public BasicRenderer(float aspectRatio, IOListener ioListener, SimulationConfig config)
            throws IOException, InvalidPathException, IllegalStateException {
        this.ioListener = ioListener;
        this.renderMode = RenderMode.STANDARD;
        this.camera = createCamera(config.initialState().spaceSize(), aspectRatio);
        this.cameraController = new CameraController(camera, ioListener);
        this.renderStrategy = new StandardRenderStrategy(camera, opaqueMaterialShader, fireShader, smokeShader);
        this.projection = new Uniform<>("uProjection", camera.perspectiveMatrix());
        this.lightDirection = new Uniform<>("uLightDir", new Vector3f(-1.0f, -0.8f, 0.5f));
        this.lightColor = new Uniform<>("uLightColor", new Vector3f(1.0f, 1.0f, 1.0f));
        initializeRendering();
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
        smokeShader.bind();
        smokeShader.setMatrix4("uView", camera.viewMatrix());

        renderStrategy.renderState(state);
    }

    @Override
    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
        switch (renderMode) {
            case STANDARD -> renderStrategy = new StandardRenderStrategy(camera, opaqueMaterialShader, fireShader, smokeShader);
            case TEMPERATURE_AIR ->
                    renderStrategy = new TemperatureAirRenderStrategy(camera, opaqueMaterialShader, transparentTempShader);
            case TEMPERATURE_SOLID ->
                    renderStrategy = new TemperatureSolidRenderStrategy(camera, transparentTempShader);
        }
    }

    @Override
    public void dispose() {
        windowSizeSubscription.dispose();
        cameraController.dispose();
    }

    private Camera createCamera(Vector3i spaceSize, float aspectRatio) {
        // camera position is slightly behind upper space corner
        var cameraPosition = new Vector3f(spaceSize).mul(new Vector3f(1.2f), new Vector3f());
        // camera orientation is pointing to the space center
        var cameraEulerAngles = new Vector3f((float) (-Math.PI / 6), (float) (-Math.PI * 0.75), 0.f);
        return new Camera(aspectRatio, cameraPosition, cameraEulerAngles);
    }

    private void initializeRendering() {
        Stream.of(opaqueMaterialShader, transparentTempShader, fireShader, smokeShader)
                .forEach(shader -> {
                    shader.bind();
                    projection.populateShader(shader);
                    lightDirection.populateShader(shader);
                    lightColor.populateShader(shader);
                });

        windowSizeSubscription = ioListener.windowSizeObservable().subscribe(size -> {
            camera.setAspectRatio(size.x / (float) size.y);
            projection = new Uniform<>("uProjection", camera.perspectiveMatrix());
            Stream.of(opaqueMaterialShader, transparentTempShader, fireShader, smokeShader)
                    .forEach(projection::populateShader);
        });
    }
}

