package pl.edu.agh.firecell.renderer;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh {

    private final float[] vertices;
    private final int[] indices;

    private final int eboID;
    private final int vboID;
    private final int vaoID;

    public Mesh(float[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(this.indices.length);
        elementBuffer.put(this.indices).flip();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        int positionCount = 3;
        int vertexSize = (positionCount) * Float.BYTES;
        glVertexAttribPointer(0, positionCount, GL_FLOAT, false, vertexSize, 0);
        glEnableVertexAttribArray(0);
    }

    public void draw() {
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
    }
}
