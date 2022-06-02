package pl.edu.agh.firecell.storage;

import pl.edu.agh.firecell.storage.proto.State;

import java.io.IOException;
import java.io.OutputStream;

public class BinaryStateSerializer implements StateSerializer{
    @Override
    public byte[] serialize(pl.edu.agh.firecell.model.State state) {
        State protoState = createProtoState(state);
        return protoState.toByteArray();
    }

    @Override
    public void writeTo(pl.edu.agh.firecell.model.State state, OutputStream stream) throws IOException {
        State protoState = createProtoState(state);
        protoState.writeTo(stream);
    }

    private static State createProtoState(pl.edu.agh.firecell.model.State state) {
        return State.newBuilder()
                .setIndex(0)
                .build();
    }
}
