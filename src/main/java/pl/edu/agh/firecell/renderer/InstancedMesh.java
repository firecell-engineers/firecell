package pl.edu.agh.firecell.renderer;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstancedMesh extends Mesh {

    List<Transformation> transformations;

    public InstancedMesh(float[] vertices, List<Transformation> transformations) {
        super(vertices);
        this.transformations = transformations;

        glBindVertexArray(vaoID);

        int instanceVboID = glGenBuffers();
        var instanceBuffer = MemoryUtil.memAllocFloat(transformations.size() * 16);
        glBindBuffer(GL_ARRAY_BUFFER, instanceVboID);
        IntStream.range(0, transformations.size()).forEach(index ->  transformations.get(index).modelMatrix().get(index * 16, instanceBuffer));
        glBufferData(GL_ARRAY_BUFFER, instanceBuffer, GL_STATIC_DRAW);

        for (int i = 0; i < 4; i++) {
            int attributeOffset = i + 2;
            glVertexAttribPointer(attributeOffset, 4, GL_FLOAT, false, 16 * Float.BYTES, i * 4 * Float.BYTES);
            glVertexAttribDivisor(attributeOffset, 1);
            glEnableVertexAttribArray(attributeOffset);
        }

        glBindVertexArray(0);
    }

    @Override
    public void draw() {
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glEnableVertexAttribArray(5);
        glDrawArraysInstanced(GL_TRIANGLES, 0, vertices.length, transformations.size());
        glBindVertexArray(0);
    }
}
