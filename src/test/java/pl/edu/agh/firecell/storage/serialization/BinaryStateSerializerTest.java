package pl.edu.agh.firecell.storage.serialization;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BinaryStateSerializerTest {
    @Test
    void testSerializeDeserialize() throws IOException {
        BinaryStateSerializer serializer = new BinaryStateSerializer();
        State state = new State(List.of(
                new Cell(20.1, 10, true, Material.AIR),
                new Cell(21.2, 8, true, Material.WOOD),
                new Cell(22.1, 4, false, Material.WOOD)
        ), new Vector3i(3, 0, 0));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        serializer.writeTo(state, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        State result = serializer.parseFrom(inputStream);

        assertEquals(state, result);
    }
}