package pl.edu.agh.firecell.storage;

import pl.edu.agh.firecell.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryStorage implements Storage {
    private final List<State> cache = new ArrayList<>();

    @Override
    public void putState(State state, int index) {
        cache.set(index, state);
    }

    @Override
    public Optional<State> getState(int index) {
        if (index < 0 || index >= cache.size()) {
            return Optional.empty();
        }
        return Optional.of(cache.get(index));
    }
}
