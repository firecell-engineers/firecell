package pl.edu.agh.firecell.storage.serialization;

import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.storage.proto.ProtoState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BinaryStateSerializer implements StateSerializer {
    @Override
    public void writeTo(State state, OutputStream stream) throws IOException {
        ProtoState protoState = createProtoState(state);
        protoState.writeTo(stream);
    }

    @Override
    public State parseFrom(InputStream stream) throws IOException {
        ProtoState protoState = ProtoState.parseFrom(stream);
        State state = new State();
        state.setIndex(protoState.getIndex());
        return state;
    }

    private static ProtoState createProtoState(State state) {
        return ProtoState.newBuilder()
                .setIndex(0)
                .build();
    }
}
