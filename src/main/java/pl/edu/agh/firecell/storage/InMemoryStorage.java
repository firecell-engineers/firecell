package pl.edu.agh.firecell.storage;

import pl.edu.agh.firecell.model.State;

import java.util.*;

public class InMemoryStorage implements StateProvider, StateConsumer {
    private final Map<Integer, State> cache = new HashMap<>();

    @Override
    public void putState(State state, int index) {
        cache.put(index, state);
    }

    @Override
    public Optional<State> getState(int index) {
        if (!cache.containsKey(index)) {
            return Optional.empty();
        }
        return Optional.of(cache.get(index));
    }
}
