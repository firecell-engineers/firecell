package pl.edu.agh.firecell.renderer;

import org.joml.Matrix4f;

public class Camera {

    private static final float FOV = 60.0f;
    private static final float Z_NEAR = 1e-4f;
    private static final float Z_FAR = 1e6f;

    private Matrix4f perspectiveMatrix;

    public Camera(float aspectRatio) {
        perspectiveMatrix = new Matrix4f().perspective((float) Math.toRadians(FOV), aspectRatio, Z_NEAR, Z_FAR);
    }

    public void setAspectRatio(float aspectRatio) {
        perspectiveMatrix = new Matrix4f().perspective((float) Math.toRadians(FOV), aspectRatio, Z_NEAR, Z_FAR);
    }

    public Matrix4f perspectiveMatrix() {
        return perspectiveMatrix;
    }
}
