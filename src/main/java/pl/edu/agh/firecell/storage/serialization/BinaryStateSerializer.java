package pl.edu.agh.firecell.storage.serialization;

import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.storage.proto.ProtoState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static pl.edu.agh.firecell.storage.serialization.ConversionUtils.convertFromProto;
import static pl.edu.agh.firecell.storage.serialization.ConversionUtils.convertToProto;

public class BinaryStateSerializer implements StateSerializer {
    @Override
    public void writeTo(State state, OutputStream stream) throws IOException {
        ProtoState protoState = convertToProto(state);
        protoState.writeTo(stream);
    }

    @Override
    public State parseFrom(InputStream stream) throws IOException {
        ProtoState protoState = ProtoState.parseFrom(stream);
        return convertFromProto(protoState);
    }
}
