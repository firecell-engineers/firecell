package pl.edu.agh.firecell.core.io;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;


// TODO: Refactor this class: it should be proper singleton (probably IoC container is the only way, maybe spring)
public class IOListener {

    private final Logger logger = LoggerFactory.getLogger(IOListener.class);

    private final int KEYS_COUNT = 350;
    private final List<Boolean> keys = new ArrayList<>(KEYS_COUNT);
    private final Subject<KeyEvent> keysObservable = PublishSubject.create();

    private Vector2i windowSize;
    private final Subject<Vector2i> windowSizeObservable = PublishSubject.create();

    public IOListener(Vector2i windowSize) {
        this.windowSize = windowSize;
        IntStream.range(0, KEYS_COUNT).forEach(idx -> keys.add(false));
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        switch (action) {
            case GLFW_PRESS -> {
                keys.set(key, true);
                keysObservable.onNext(new KeyEvent(key, true));
            }
            case GLFW_RELEASE -> {
                keys.set(key, false);
                keysObservable.onNext(new KeyEvent(key, false));
            }
        }
    }

    public Observable<KeyEvent> keyObservable(int keyCode) {
        return keysObservable.filter(keyEvent -> keyEvent.keyCode() == keyCode);
    }

    public boolean isPressed(int key) {
        return keys.get(key);
    }

    public void windowSizeCallback(long window, int width, int height) {
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