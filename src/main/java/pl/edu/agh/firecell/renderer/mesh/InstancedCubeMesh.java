package pl.edu.agh.firecell.renderer.mesh;

import org.lwjgl.system.MemoryUtil;
import pl.edu.agh.firecell.model.IndexedCell;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstancedCubeMesh extends Mesh {
    private final int cellCount;

    public InstancedCubeMesh(float[] vertices, List<IndexedCell> indexedCells) {
        super(vertices);
        cellCount = indexedCells.size();

        var positionBuffer = MemoryUtil.memAllocFloat(cellCount * 3);
        var temperatureBuffer = MemoryUtil.memAllocFloat(cellCount);
        var materialBuffer = MemoryUtil.memAllocInt(cellCount);
        var burningTimeBuffer = MemoryUtil.memAllocInt(cellCount);

        int bufferIndex = 0;
        for (IndexedCell indexedCell : indexedCells) {
            positionBuffer.put(bufferIndex * 3, (float) indexedCell.index().x);
            positionBuffer.put(bufferIndex * 3 + 1, (float) indexedCell.index().y);
            positionBuffer.put(bufferIndex * 3 + 2, (float) indexedCell.index().z);
            temperatureBuffer.put(bufferIndex, (float) indexedCell.cell().temperature());
            materialBuffer.put(bufferIndex, indexedCell.cell().material().ordinal());
            burningTimeBuffer.put(bufferIndex, indexedCell.cell().burningTime());
            bufferIndex++;
        }

        glBindVertexArray(vaoID);

        addInstancedFloatAttribute(positionBuffer, 3);
        addInstancedFloatAttribute(temperatureBuffer, 1);
        addInstancedIntAttribute(materialBuffer, 1);
        addInstancedIntAttribute(burningTimeBuffer, 1);

        glBindVertexArray(0);
    }

    private void addInstancedFloatAttribute(FloatBuffer buffer, int componentCount) {
        int instanceVboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instanceVboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = componentCount * Float.BYTES;
        int offset = 0;
        glVertexAttribPointer(attributeCount, componentCount, GL_FLOAT, false, stride, offset);
        glVertexAttribDivisor(attributeCount, 1);
        glEnableVertexAttribArray(attributeCount);
        attributeCount++;
    }

    private void addInstancedIntAttribute(IntBuffer buffer, int componentCount) {
        int instanceVboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instanceVboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = componentCount * Integer.BYTES;
        int offset = 0;
        glVertexAttribPointer(attributeCount, componentCount, GL_INT, false, stride, offset);
        glVertexAttribDivisor(attributeCount, 1);
        glEnableVertexAttribArray(attributeCount);
        attributeCount++;
    }

    @Override
    public void draw() {
        glBindVertexArray(vaoID);
        enableAttributes();
        glDrawArraysInstanced(GL_TRIANGLES, 0, vertices.length, cellCount);
        glBindVertexArray(0);
    }
}
