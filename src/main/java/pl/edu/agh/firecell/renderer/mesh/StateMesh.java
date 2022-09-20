package pl.edu.agh.firecell.renderer.mesh;

import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class StateMesh extends Mesh {

    private final int cellCount;
    private int lastAttributeIndex = 1;

    public StateMesh(float[] vertices, State state) {
        super(vertices);
        cellCount = state.cells().size();

        var positionBuffer = MemoryUtil.memAllocFloat(cellCount * 3);
        var temperatureBuffer = MemoryUtil.memAllocFloat(cellCount);
        var materialBuffer = MemoryUtil.memAllocInt(cellCount);

        IntStream.range(0, cellCount)
                .forEach(flatIndex -> {
                    Vector3i expandedIndex = IndexUtils.expandIndex(flatIndex, state.spaceSize());
                    Cell cell = state.cells().get(flatIndex);

                    positionBuffer.put(flatIndex * 3, (float) expandedIndex.x);
                    positionBuffer.put(flatIndex * 3 + 1, (float) expandedIndex.y);
                    positionBuffer.put(flatIndex * 3 + 2, (float) expandedIndex.z);
                    temperatureBuffer.put(flatIndex, (float) cell.temperature());
                    materialBuffer.put(flatIndex, cell.material().ordinal());
                });

        glBindVertexArray(vaoID);

        addInstancedFloatAttribute(positionBuffer, 3);
        addInstancedFloatAttribute(temperatureBuffer, 1);
        addInstancedIntAttribute(materialBuffer, 1);

        glBindVertexArray(0);
    }

    private void addInstancedFloatAttribute(FloatBuffer buffer, int componentCount) {
        int instanceVboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instanceVboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = componentCount * Float.BYTES;
        int offset = 0;
        lastAttributeIndex++;
        glVertexAttribPointer(lastAttributeIndex, componentCount, GL_FLOAT, false, stride, offset);
        glVertexAttribDivisor(lastAttributeIndex, 1);
        glEnableVertexAttribArray(lastAttributeIndex);
    }

    private void addInstancedIntAttribute(IntBuffer buffer, int componentCount) {
        int instanceVboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instanceVboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = componentCount * Integer.BYTES;
        int offset = 0;
        lastAttributeIndex++;
        glVertexAttribPointer(lastAttributeIndex, componentCount, GL_INT, false, stride, offset);
        glVertexAttribDivisor(lastAttributeIndex, 1);
        glEnableVertexAttribArray(lastAttributeIndex);
    }

    @Override
    public void draw() {
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glDrawArraysInstanced(GL_TRIANGLES, 0, vertices.length, cellCount);
        glBindVertexArray(0);
    }
}
