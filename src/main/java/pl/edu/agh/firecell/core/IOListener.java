package pl.edu.agh.firecell.core;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.joml.Vector2i;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class IOListener {

    public static final int KEYS_TOTAL = 350;
    private static final boolean[] pressedKeys = new boolean[KEYS_TOTAL];

    private static Vector2i windowSize;
    private static final Subject<Vector2i> windowSizeObservable = PublishSubject.create();

    public IOListener() {

    }

    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        pressedKeys[key] = action == GLFW_PRESS;
    }

    public static void windowSizeCallback(long window, int width, int height) {
        windowSizeObservable.onNext(new Vector2i(width, height));
    }

    public static boolean isPressed(int key) {
        return pressedKeys[key];
    }

    public Observable<Vector2i> windowSizeObservable() {
        return windowSizeObservable;
    }
}