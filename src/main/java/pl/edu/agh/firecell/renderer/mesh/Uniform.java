package pl.edu.agh.firecell.renderer.mesh;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public record Uniform<T>(String name, T value) {

    public void populateShader(Shader shader) {
        if (value instanceof Integer) {
            shader.setInt(name, (Integer) value);
        } else if (value instanceof Float) {
            shader.setFloat(name, (Float) value);
        } else if (value instanceof Vector3f) {
            shader.setVector3(name, (Vector3f) value);
        } else if (value instanceof Vector4f) {
            shader.setVector4(name, (Vector4f) value);
        } else if (value instanceof Matrix4f) {
            shader.setMatrix4(name, (Matrix4f) value);
        }
    }
}
