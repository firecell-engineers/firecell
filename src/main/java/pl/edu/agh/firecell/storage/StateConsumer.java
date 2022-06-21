package pl.edu.agh.firecell.storage;

import pl.edu.agh.firecell.model.State;

public interface StateConsumer {
    void putState(State state, int index);
}
