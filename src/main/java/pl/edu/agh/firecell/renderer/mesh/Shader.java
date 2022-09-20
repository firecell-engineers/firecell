package pl.edu.agh.firecell.renderer.mesh;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private final static String SHADERS_DIR = "src/main/resources/shaders";

    private final int shaderProgramID;
    private final Map<String, Integer> locationsCache = new HashMap<>();

    public Shader(String vertexSourceFilename, String fragmentSourceFilename)
            throws IOException, InvalidPathException, IllegalStateException {
        String vertexSource = readShaderSource(vertexSourceFilename);
        String fragmentSource = readShaderSource(fragmentSourceFilename);

        int vertexShaderID = createAndCompileShader(vertexSource, GL_VERTEX_SHADER);
        int fragmentShaderID = createAndCompileShader(fragmentSource, GL_FRAGMENT_SHADER);

        shaderProgramID = createAndLinkProgram(vertexShaderID, fragmentShaderID);
    }

    public void bind() {
        glUseProgram(shaderProgramID);
    }

    public static void unbind() {
        glUseProgram(0);
    }

    public void setMatrix4(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var buffer = matrix.get(stack.mallocFloat(16));
            glUniformMatrix4fv(getLocation(name), false, buffer);
        }
    }

    public void setVector3(String name, Vector3f vector) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var buffer = vector.get(stack.mallocFloat(3));
            glUniform3fv(getLocation(name), buffer);
        }
    }

    public void setVector4(String name, Vector4f vector) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var buffer = vector.get(stack.mallocFloat(4));
            glUniform4fv(getLocation(name), buffer);
        }
    }

    public void setInt(String name, int value) {
        glUniform1i(getLocation(name), value);
    }

    public void setFloat(String name, float value) {
        glUniform1f(getLocation(name), value);
    }

    private int getLocation(String name) {
        if (!locationsCache.containsKey(name)) {
            locationsCache.put(name, glGetUniformLocation(shaderProgramID, name));
        }
        return locationsCache.get(name);
    }

    private String readShaderSource(String filename) throws IOException, InvalidPathException {
        var path = Paths.get(SHADERS_DIR, filename);
        var allFileLines = Files.readAllLines(path);
        return String.join("\n", allFileLines);
    }

    private int createAndCompileShader(String source, int type) throws IllegalStateException {
        int resultID = glCreateShader(type);
        glShaderSource(resultID, source);
        glCompileShader(resultID);
        if (glGetShaderi(resultID, GL_COMPILE_STATUS) == GL_FALSE) {
            var infoLogLength = glGetShaderi(resultID, GL_INFO_LOG_LENGTH);
            var infoLog = glGetShaderInfoLog(resultID, infoLogLength);
            throw new IllegalStateException("Shader compilation failed. Info log: '" + infoLog + "'.");
        }
        return resultID;
    }

    private int createAndLinkProgram(int vertexID, int fragmentID) {
        int resultID = glCreateProgram();
        glAttachShader(resultID, fragmentID);
        glAttachShader(resultID, vertexID);
        glLinkProgram(resultID);
        if (glGetProgrami(resultID, GL_LINK_STATUS) == GL_FALSE) {
            var infoLogLength = glGetProgrami(resultID, GL_INFO_LOG_LENGTH);
            var infoLog = glGetProgramInfoLog(resultID, infoLogLength);
            throw new IllegalStateException("Shader program linking failed. Info log: '" + infoLog + "'.");
        }
        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);
        return resultID;
    }
}
