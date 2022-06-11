package pl.edu.agh.firecell.renderer;

import pl.edu.agh.firecell.model.State;

import java.io.IOException;
import java.nio.file.InvalidPathException;

public class BasicRenderer implements Renderer {

    private final Shader basicShader;
    private final Mesh triangleMesh;

    public BasicRenderer() throws IOException, InvalidPathException, IllegalStateException {
        basicShader = new Shader("basic.glsl.vert", "basic.glsl.frag");
        basicShader.bind();
        triangleMesh = MeshFactory.createTriangleMesh();
    }

    @Override
    public void render(State state) {
        triangleMesh.draw();
    }
}
