package pl.edu.agh.firecell.storage.serialization;

import pl.edu.agh.firecell.model.State;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StateSerializer {
    void writeTo(State state, OutputStream stream) throws IOException;
    State parseFrom(InputStream stream) throws IOException;
}
