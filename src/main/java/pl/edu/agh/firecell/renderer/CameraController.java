package pl.edu.agh.firecell.renderer;

import io.reactivex.rxjava3.disposables.Disposable;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.core.io.KeyEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;

public class CameraController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Camera camera;
    private final Disposable keysSubscription;
    private final Disposable mouseDragSubscription;
    private double frameTime = 0;
    private final Map<Integer, Runnable> keyHandlers = new HashMap<>();

    public CameraController(Camera camera, IOListener ioListener) {
        this.camera = camera;
        configureKeyHandlers();
        this.keysSubscription = ioListener.keysObservable().subscribe(this::processKey);
        this.mouseDragSubscription = ioListener.mouseDragObservable().subscribe(this::processMouseDrag);
    }

    public void update(double frameTime) {
        this.frameTime = frameTime;
    }

    public void dispose() {
        keysSubscription.dispose();
        mouseDragSubscription.dispose();
    }

    private void configureKeyHandlers() {
        keyHandlers.put(GLFW_KEY_W, () -> camera.moveForward((float) frameTime));
        keyHandlers.put(GLFW_KEY_S, () -> camera.moveForward((float) -frameTime));
        keyHandlers.put(GLFW_KEY_D, () -> camera.moveRight((float) frameTime));
        keyHandlers.put(GLFW_KEY_A, () -> camera.moveRight((float) -frameTime));
        keyHandlers.put(GLFW_KEY_SPACE, () -> camera.moveUp((float) frameTime));
        keyHandlers.put(GLFW_KEY_LEFT_SHIFT, () -> camera.moveUp((float) -frameTime));
    }

    private void processKey(KeyEvent keyEvent) {
        Optional.ofNullable(keyHandlers.get(keyEvent.keyCode()))
                .ifPresent(Runnable::run);
    }

    private void processMouseDrag(Vector2f drag) {
        logger.debug("Mouse drag: (%s, %s)".formatted(drag.x, drag.y));
    }
}
