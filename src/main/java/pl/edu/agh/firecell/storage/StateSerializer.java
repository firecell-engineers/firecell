package pl.edu.agh.firecell.storage;

import pl.edu.agh.firecell.model.State;

import java.io.IOException;
import java.io.OutputStream;

public interface StateSerializer {
    byte[] serialize(State state);
    void writeTo(State state, OutputStream stream) throws IOException;
}
