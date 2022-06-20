package pl.edu.agh.firecell.storage;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.storage.serialization.StateSerializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FileSystemStorage implements StateProvider, StateConsumer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Subject<StateHolder> stateSubject = PublishSubject.create();
    private final StateSerializer serializer;
    private final Path path;

    private Disposable subscription;

    public FileSystemStorage(StateSerializer serializer, Path path) throws IOException {
        this.serializer = serializer;
        this.path = path;
        initialize();
    }


    @Override
    public void putState(State state, int index) {
        stateSubject.onNext(new StateHolder(state, index));
    }

    @Override
    public Optional<State> getState(int index) {
        File file = getFile(index);
        try (InputStream stream = new FileInputStream(file)) {
            State state = serializer.parseFrom(stream);
            return Optional.of(state);
        } catch (FileNotFoundException e) {
            return Optional.empty();
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private void initialize() throws IOException {
        Files.createDirectories(path);
        subscription = stateSubject.subscribeOn(Schedulers.io())
                .subscribe(this::saveState);
    }

    private void saveState(StateHolder stateHolder) {
        File file = getFile(stateHolder.index);
        try {
            if (file.createNewFile()) {
                logger.debug("Created file " + file.getName());
            }
            try (OutputStream stream = new FileOutputStream(file)) {
                serializer.writeTo(stateHolder.state, stream);
            }
        } catch (IOException e) {
            logger.error("Failed to create file for state " + stateHolder.index);
            e.printStackTrace();
        }
    }

    public void dispose() {
        subscription.dispose();
    }

    private File getFile(int index) {
        return path.resolve(String.valueOf(index)).toFile();
    }

    private record StateHolder(State state, int index) {
    }
}
