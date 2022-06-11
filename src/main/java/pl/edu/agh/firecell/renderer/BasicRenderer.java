package pl.edu.agh.firecell.renderer;

import org.joml.Random;
import org.joml.Vector3f;
import pl.edu.agh.firecell.core.IOListener;
import pl.edu.agh.firecell.model.State;

import java.io.IOException;
import java.nio.file.InvalidPathException;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class BasicRenderer implements Renderer {

    private final Shader shader = new Shader("basic.glsl.vert", "basic.glsl.frag");
    private final Mesh mesh = MeshFactory.createCubeMesh();
    private final Transformation transformation = new Transformation();
    private final Camera camera;
    private float rot = 0.0f;

    public BasicRenderer(float aspectRatio, IOListener ioListener) throws IOException, InvalidPathException, IllegalStateException {
        shader.bind();
        camera = new Camera(aspectRatio);
        shader.setMatrix4("projection", camera.perspectiveMatrix());
        transformation.setPosition(new Vector3f(0, 0, -8));
        ioListener.windowSizeObservable().subscribe(size -> {
            camera.setAspectRatio(size.x / (float) size.y);
            shader.setMatrix4("projection", camera.perspectiveMatrix());
        });
    }

    @Override
    public void render(State state, double frameTime) {
        var random = new Random();
        transformation.addRotation(new Vector3f((float)frameTime, (float)frameTime * 2.5f, (float)frameTime * 1.5f));
        shader.setMatrix4("model", transformation.modelMatrix());
        mesh.draw();
    }
}
