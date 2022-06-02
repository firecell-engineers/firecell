package pl.edu.agh.firecell.storage;

import pl.edu.agh.firecell.model.State;

import java.util.Optional;

public class BasicStorage implements Storage {

    @Override
    public void putState(State state, int index) {

    }

    @Override
    public Optional<State> getState(int index) {
        return Optional.empty();
    }
}
