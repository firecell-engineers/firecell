package pl.edu.agh.firecell.renderer;

public class MeshFactory {
    private MeshFactory() {
    }

    public static Mesh createTriangleMesh() {
        float[] triangleVertices = {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.0f, 0.5f, 0.0f
        };
        int[] triangleIndices = {
                0, 1, 2
        };
        return new Mesh(triangleVertices, triangleIndices);
    }
}
