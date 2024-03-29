package pl.edu.agh.firecell.renderer.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Camera {

    private static final float FOV = 60.0f;
    private static final float Z_NEAR = 0.05f;
    private static final float Z_FAR = 500.0f;
    private static final float MOVING_SPEED = 15.0f;

    private final Logger logger = LoggerFactory.getLogger(Camera.class);

    private float aspectRatio;
    private Vector3f position;
    private Vector3f direction = new Vector3f(0, 0, -1);
    private Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f eulerAngles;

    private Matrix4f viewMatrix;
    private Matrix4f perspectiveMatrix;

    public Camera(float aspectRatio, Vector3f position, Vector3f eulerAngles) {
        this.position = position;
        this.eulerAngles = eulerAngles;
        this.aspectRatio = aspectRatio;
        updateDirection();
        updateProjectionMatrix();
        updateViewMatrix();
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        updateProjectionMatrix();
    }

    public void addYaw(float yawChange) {
        eulerAngles.y += yawChange;
        updateDirection();
        updateViewMatrix();
    }

    public void addPitch(float pitchChange) {
        eulerAngles.x += pitchChange;
        updateDirection();
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

    private void updateViewMatrix() {
        viewMatrix = new Matrix4f().lookAt(position, position.add(direction, new Vector3f()), up);
    }

    private void updateProjectionMatrix() {
        perspectiveMatrix = new Matrix4f().perspective((float) Math.toRadians(FOV), aspectRatio, Z_NEAR, Z_FAR);
    }

    private void updateDirection() {
        direction = new Vector3f(
                (float) (Math.cos(eulerAngles.y) * Math.cos(eulerAngles.x)),
                (float) (Math.sin(eulerAngles.x)),
                (float) (Math.sin(eulerAngles.y) * Math.cos(eulerAngles.x))
        ).normalize();
    }
}
