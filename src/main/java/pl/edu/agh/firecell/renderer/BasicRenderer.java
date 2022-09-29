package pl.edu.agh.firecell.renderer;

import io.reactivex.rxjava3.disposables.Disposable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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

    private final Camera camera;
    private final IOListener ioListener;
    private final CameraController cameraController;
    private Disposable windowSizeSubscription;
    private RenderStrategy renderStrategy;
    private RenderMode renderMode;

    private Uniform<Matrix4f> projection;
    private final Uniform<Vector3f> lightDirection;
    private final Uniform<Vector3f> lightColor;

    public BasicRenderer(float aspectRatio, IOListener ioListener, SimulationConfig config)
            throws IOException, InvalidPathException, IllegalStateException {
        this.ioListener = ioListener;
        this.camera = new Camera(aspectRatio);
        this.cameraController = new CameraController(camera, ioListener);
        this.renderMode = RenderMode.STANDARD;
        this.renderStrategy = new StandardRenderStrategy(camera, opaqueMaterialShader, fireShader);
        this.projection = new Uniform<>("uProjection", camera.perspectiveMatrix());
        this.lightDirection = new Uniform<>("uLightDir", new Vector3f(-1.0f, -0.8f, 0.5f));
        this.lightColor = new Uniform<>("uLightColor", new Vector3f(1.0f, 1.0f, 1.0f));
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

        renderStrategy.renderState(state);
    }

    @Override
    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
        switch (renderMode) {
            case STANDARD -> renderStrategy = new StandardRenderStrategy(camera, opaqueMaterialShader, fireShader);
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

    private void initializeRendering(SimulationConfig config) {
        var spaceSize = new Vector3f(config.size().x, config.size().y, config.size().z);
        camera.setPosition(new Vector3f(spaceSize).mul(0.5f).add(new Vector3f(0.0f, 0.0f, spaceSize.z * 1.5f)));

        Stream.of(opaqueMaterialShader, transparentTempShader, fireShader)
                .forEach(shader -> {
                    shader.bind();
                    projection.populateShader(shader);
                    lightDirection.populateShader(shader);
                    lightColor.populateShader(shader);
                });

        windowSizeSubscription = ioListener.windowSizeObservable().subscribe(size -> {
            camera.setAspectRatio(size.x / (float) size.y);
            projection = new Uniform<>("uProjection", camera.perspectiveMatrix());
            Stream.of(opaqueMaterialShader, transparentTempShader, fireShader)
                    .forEach(projection::populateShader);
        });
    }
}

