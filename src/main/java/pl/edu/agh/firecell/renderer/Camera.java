package pl.edu.agh.firecell.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Camera {

    private static final float FOV = 60.0f;
    private static final float Z_NEAR = 0.05f;
    private static final float Z_FAR = 500.0f;
    private static final float MOVING_SPEED = 10.0f;

    private Logger logger = LoggerFactory.getLogger(Camera.class);

    private float aspectRatio;
    private Vector3f position = new Vector3f(0);
    private Vector3f direction = new Vector3f(0, 0, -1);
    private Vector3f up = new Vector3f(0, 1, 0);

    private Matrix4f viewMatrix;
    private Matrix4f perspectiveMatrix;

    public Camera(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        updateProjectionMatrix();
        updateViewMatrix();
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        updateProjectionMatrix();
    }

    public void setPosition(Vector3f position) {
        this.position = position;
        updateViewMatrix();
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
        updateViewMatrix();
    }

    public Matrix4f perspectiveMatrix() {
        return perspectiveMatrix;
    }

    public Matrix4f viewMatrix() {
        return viewMatrix;
    }

    public void moveForward(float deltaTime) {
        var directionXZ = new Vector3f(direction.x, 0, direction.z).normalize();
        position.add(directionXZ.mul(deltaTime * MOVING_SPEED));
        updateViewMatrix();
    }

    public void moveRight(float deltaTime) {
        var directionXZ = new Vector3f(direction.x, 0, direction.z);
        var rightXZ = directionXZ.cross(up, new Vector3f()).normalize();
        position.add(rightXZ.mul(deltaTime * MOVING_SPEED));
        updateViewMatrix();
    }

    public void moveUp(float deltaTime) {
        position.add(up.mul(deltaTime * MOVING_SPEED, new Vector3f()));
        updateViewMatrix();
    }

    public Vector3f position() {
        return position;
    }

    public Vector3f direction() {
        return direction;
    }

    public Vector3f up() {
        return up;
    }

    private void updateViewMatrix() {
        viewMatrix = new Matrix4f().lookAt(position, position.add(direction, new Vector3f()), up);
    }

    private void updateProjectionMatrix() {
        perspectiveMatrix = new Matrix4f().perspective((float) Math.toRadians(FOV), aspectRatio, Z_NEAR, Z_FAR);
    }
}
