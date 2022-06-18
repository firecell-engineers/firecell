package pl.edu.agh.firecell.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

    private Vector3f position;
    private Vector3f rotation;
    private Vector3f scale;

    private Matrix4f modelMatrix;
    private boolean modelMatrixDirty = true;

    public Transformation(Vector3f position, Vector3f rotation, Vector3f scale) {
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public Transformation() {
        this(new Vector3f(0), new Vector3f(0), new Vector3f(1));
    }

    public Vector3f position() {
        return position;
    }

    public Vector3f rotation() {
        return rotation;
    }

    public Vector3f scale() {
        return scale;
    }

    public void addPosition(Vector3f positionChange) {
        this.position.add(positionChange);
        modelMatrixDirty = true;
    }

    public void addRotation(Vector3f rotationChange) {
        this.rotation.add(rotationChange);
        modelMatrixDirty = true;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
        modelMatrixDirty = true;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
        modelMatrixDirty = true;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
        modelMatrixDirty = true;
    }

    public Matrix4f modelMatrix() {
        if (modelMatrixDirty) {
            modelMatrixDirty = false;
            modelMatrix = new Matrix4f()
                    .translate(position)
                    .rotateX(rotation.x).rotateY(rotation.y).rotateZ(rotation.z)
                    .scale(scale);
        }
        return modelMatrix;
    }
}
