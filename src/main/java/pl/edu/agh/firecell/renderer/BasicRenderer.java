package pl.edu.agh.firecell.renderer;

import org.joml.Vector3f;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.model.State;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.lwjgl.glfw.GLFW.*;

public class BasicRenderer implements Renderer {

    private final Shader shader = new Shader("instanced.glsl.vert", "basic.glsl.frag");

    private final InstancedMesh instancedCubeMesh;

    private final Camera camera;
    private final IOListener ioListener;

    public BasicRenderer(float aspectRatio, IOListener ioListener) throws IOException, InvalidPathException, IllegalStateException {
        this.ioListener = ioListener;
        shader.bind();
        camera = new Camera(aspectRatio);
        shader.setMatrix4("uProjection", camera.perspectiveMatrix());

        shader.setMatrix4("uModel", new Transformation().modelMatrix());
        instancedCubeMesh = MeshFactory.createInstancedCubeMesh(generateInstanceTransformations(5));

        shader.setVector3("uObjectColor", new Vector3f(1.0f, 0.5f, 0.2f).mul(0.8f));
        shader.setVector3("uLightDir", new Vector3f(0.2f, -0.2f, 1.0f));
        shader.setVector3("uLightColor", new Vector3f(1.0f, 1.0f, 1.0f));

        ioListener.windowSizeObservable().subscribe(size -> {
            camera.setAspectRatio(size.x / (float) size.y);
            shader.setMatrix4("uProjection", camera.perspectiveMatrix());
        });
    }

    @Override
    public void render(State state, double frameTime) {
        processCameraControl(frameTime);
        shader.setMatrix4("uView", camera.viewMatrix());
        instancedCubeMesh.draw();
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

    private List<Transformation> generateInstanceTransformations(int instancesCount) {
        Random random = new Random((long) (glfwGetTime() * 1e5));
        return IntStream.range(0, instancesCount)
                .mapToObj(index -> new Transformation(
                        new Vector3f(random.nextInt(10) - 5, 0, random.nextInt(10) - 15),
                        new Vector3f(
                                (float)(random.nextFloat() * 2 * Math.PI),
                                (float)(random.nextFloat() * 2 * Math.PI),
                                (float)(random.nextFloat() * 2 * Math.PI)
                        ),
                        new Vector3f(1)
                ))
                .toList();
    }
}
