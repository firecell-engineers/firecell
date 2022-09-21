package pl.edu.agh.firecell.renderer.mesh;

import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.function.Predicate;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class StateMesh extends Mesh {
    private final int cellCount;

    public StateMesh(float[] vertices, State state, Predicate<Cell> cellPredicate) {
        super(vertices);

        List<Pair<Vector3i, Cell>> filteredIndexedCellsList = state.getIndexedCellsStream()
                .filter(indexedCell -> cellPredicate.test(indexedCell.getRight()))
                .toList();

        cellCount = filteredIndexedCellsList.size();
        var positionBuffer = MemoryUtil.memAllocFloat(cellCount * 3);
        var temperatureBuffer = MemoryUtil.memAllocFloat(cellCount);
        var materialBuffer = MemoryUtil.memAllocInt(cellCount);
        var burningTimeBuffer = MemoryUtil.memAllocInt(cellCount);

        filteredIndexedCellsList.forEach(indexedCell -> {
            Vector3i expandedIndex = indexedCell.getLeft();
            Cell cell = indexedCell.getRight();
            int flatIndex = IndexUtils.flattenIndex(indexedCell.getLeft(), state.spaceSize());

            positionBuffer.put(flatIndex * 3, (float) expandedIndex.x);
            positionBuffer.put(flatIndex * 3 + 1, (float) expandedIndex.y);
            positionBuffer.put(flatIndex * 3 + 2, (float) expandedIndex.z);
            temperatureBuffer.put(flatIndex, (float) cell.temperature());
            materialBuffer.put(flatIndex, cell.material().ordinal());
            burningTimeBuffer.put(flatIndex, cell.burningTime());
        });

        glBindVertexArray(vaoID);

        addInstancedFloatAttribute(positionBuffer, 3);
        addInstancedFloatAttribute(temperatureBuffer, 1);
        addInstancedIntAttribute(materialBuffer, 1);
        addInstancedIntAttribute(burningTimeBuffer, 1);

        glBindVertexArray(0);
    }

    public StateMesh(float[] vertices, State state) {
        this(vertices, state, cell -> true);
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
        attributeCount ++;
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
        attributeCount ++;
    }

    @Override
    public void draw() {
        glBindVertexArray(vaoID);
        enableAttributes();
        glDrawArraysInstanced(GL_TRIANGLES, 0, vertices.length, cellCount);
        glBindVertexArray(0);
    }
}
