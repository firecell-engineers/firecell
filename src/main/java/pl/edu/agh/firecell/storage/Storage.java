package pl.edu.agh.firecell.storage;

import pl.edu.agh.firecell.model.State;

import java.util.Optional;

public interface Storage {
    void putState(State state, int index);
    Optional<State> getState(int index);
}
