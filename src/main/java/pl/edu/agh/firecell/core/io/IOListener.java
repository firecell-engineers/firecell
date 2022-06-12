package pl.edu.agh.firecell.core.io;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.joml.Vector2i;

import java.util.*;
import java.util.stream.IntStream;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class IOListener {

    public static final int KEYS_COUNT = 350;
    private static final List<Boolean> keys = new ArrayList<>(KEYS_COUNT);
    private static final List<Subject<Boolean>> keysObservables = new ArrayList<>(KEYS_COUNT);

    private static Vector2i windowSize;
    private static final Subject<Vector2i> windowSizeObservable = PublishSubject.create();

    static {
        IntStream.range(0, KEYS_COUNT).forEach(keyIndex -> {
            keys.add(keyIndex, false);
            keysObservables.add(keyIndex, PublishSubject.create());
        });
    }

    // TODO: Refactor this class: it's objects have thread-unsafe access to it's static members.
    // TODO: Also it should be proper singleton (probably IoC container is the only way, maybe spring)
    public IOListener() {
    }

    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        keys.set(key, action == GLFW_PRESS);
        keysObservables.get(key).onNext(action == GLFW_PRESS);
    }

    public Observable<Boolean> keyObservable(int key) {
        return keysObservables.get(key);
    }

    public boolean isPressed(int key) {
        return keys.get(key);
    }

    public static void windowSizeCallback(long window, int width, int height) {
        windowSize = new Vector2i(width, height);
        windowSizeObservable.onNext(new Vector2i(width, height));
    }

    public Observable<Vector2i> windowSizeObservable() {
        return windowSizeObservable;
    }

    public Vector2i windowSize() {
        return windowSize;
    }
}