package pl.edu.agh.firecell.renderer;

import io.reactivex.rxjava3.disposables.Disposable;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.State;

import java.io.IOException;
import java.nio.file.InvalidPathException;

import static org.lwjgl.glfw.GLFW.*;

public class BasicRenderer implements Renderer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Shader shader = new Shader("instancedState.glsl.vert", "instancedState.glsl.frag");
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
        shader.bind();
        shader.setMatrix4("uView", camera.viewMatrix());
        renderState(state);
    }

    @Override
    public void dispose() {
        windowSizeSubscription.dispose();
    }

    private void renderState(State state) {
        var stateMesh = new StateMesh(MeshFactory.cubeVertices(), state);
        shader.bind();
        stateMesh.draw();
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
