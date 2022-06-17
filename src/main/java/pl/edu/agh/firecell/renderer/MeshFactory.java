package pl.edu.agh.firecell.renderer;

import java.util.List;

public class MeshFactory {
    private MeshFactory() {
    }

    public static Mesh createCubeMesh() {
        return new Mesh(cubeVertices());
    }

    public static InstancedMesh createInstancedCubeMesh(List<Transformation> transformations) {
        return new InstancedMesh(cubeVertices(), transformations);
    }

    private static float[] cubeVertices() {
        return new float[]{
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,
                1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,
                1.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f,
                1.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,

                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,

                0.0f, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f,

                1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f,

                0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,
                1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,
                1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f,
                1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,

                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f

        };
    }
}
