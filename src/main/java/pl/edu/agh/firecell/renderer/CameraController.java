package pl.edu.agh.firecell.renderer;

import io.reactivex.rxjava3.disposables.Disposable;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Camera camera;
    private final IOListener ioListener;
    private float mouseSensitivity = 0.005f;
    private final Disposable mouseDragSubscription;
    private double frameTime = 0;
    private final Map<Integer, Runnable> keyMapping = new HashMap<>();

    public CameraController(Camera camera, IOListener ioListener) {
        this.camera = camera;
        this.ioListener = ioListener;
        configureKeyMapping();
        this.mouseDragSubscription = ioListener.mouseDragObservable().subscribe(this::processMouseDrag);
    }

    public void update(double frameTime) {
        this.frameTime = frameTime;
        processKeys();
    }

    private void processKeys() {
        keyMapping.forEach((key, handler) -> {
            if (ioListener.isPressed(key)) {
                handler.run();
            }
        });
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public void dispose() {
        mouseDragSubscription.dispose();
    }

    private void configureKeyMapping() {
        keyMapping.put(GLFW_KEY_W, () -> camera.moveForward((float) frameTime));
        keyMapping.put(GLFW_KEY_S, () -> camera.moveForward((float) -frameTime));
        keyMapping.put(GLFW_KEY_D, () -> camera.moveRight((float) frameTime));
        keyMapping.put(GLFW_KEY_A, () -> camera.moveRight((float) -frameTime));
        keyMapping.put(GLFW_KEY_SPACE, () -> camera.moveUp((float) frameTime));
        keyMapping.put(GLFW_KEY_LEFT_SHIFT, () -> camera.moveUp((float) -frameTime));
    }

    private void processMouseDrag(Vector2f drag) {
        camera.addYaw(mouseSensitivity * drag.x());
        camera.addPitch(mouseSensitivity * -drag.y());
    }
}
