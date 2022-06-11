package pl.edu.agh.firecell.renderer;

import org.joml.Vector3f;
import pl.edu.agh.firecell.model.State;

import java.io.IOException;
import java.nio.file.InvalidPathException;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class BasicRenderer implements Renderer {

    private final Shader shader = new Shader("basic.glsl.vert", "basic.glsl.frag");
    private final Mesh mesh = MeshFactory.createTriangleMesh();
    private final Transformation transformation = new Transformation();
    private float rot = 0.0f;
    private float alpha = 0.0f;
    private float scale = 1.0f;

    public BasicRenderer() throws IOException, InvalidPathException, IllegalStateException {
        shader.bind();
    }

    @Override
    public void render(State state, double frameTime) {
        transformation.setRotation(new Vector3f(0, rot, 0));
        transformation.setScale(new Vector3f(scale, scale, 0));
        transformation.setPosition(new Vector3f((float) Math.sin(alpha) / 3.0f, (float) Math.cos(alpha) / 3.0f, 0));
        shader.setMatrix4("model", transformation.modelMatrix());
        rot += frameTime * 3;
        alpha += frameTime * 2;
        scale = (float) ((Math.sin(glfwGetTime()) / 3.0) + 1.0f);
        mesh.draw();
    }
}
