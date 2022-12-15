package pl.edu.agh.firecell.renderer.mesh;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {

    protected final float[] vertices;
    protected final int vaoID;
    protected int attributeCount;

    public Mesh(float[] vertices) {
        this.vertices = vertices;
        attributeCount = 0;

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        int vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // positions
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        attributeCount++;

        // normals
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        attributeCount++;

        glBindVertexArray(0);
    }

    protected void enableAttributes() {
        for (int i = 0; i < attributeCount; i++) {
            glEnableVertexAttribArray(i);
        }
    }

    public void draw() {
        glBindVertexArray(vaoID);
        enableAttributes();
        glDrawArrays(GL_TRIANGLES, 0, vertices.length);
        glBindVertexArray(0);
    }
}
