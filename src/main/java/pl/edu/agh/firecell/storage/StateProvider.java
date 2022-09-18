package pl.edu.agh.firecell.storage;

import pl.edu.agh.firecell.model.State;

import java.util.Optional;

public interface StateProvider {
    Optional<State> getState(int index);
}
