package pl.edu.agh.firecell.renderer;

import org.joml.Vector3f;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransformationTest {

    @ParameterizedTest(name = "Vector {3} should be transformed to {4}")
    @MethodSource("transformPointTestCases")
    public void transformPointTest(Vector3f posChange, Vector3f rotChange, Vector3f scale, Vector3f givenPoint, Vector3f expectedPoint) {
        // given
        var tr = transformation();

        // when
        tr.addPosition(posChange);
        tr.addRotation(rotChange);
        tr.setScale(scale);
        var modelMatrix = tr.modelMatrix();
        modelMatrix.transformPosition(givenPoint); // changing givenPoint in place

        // then
        assertEquals(expectedPoint, givenPoint);
    }

    private Transformation transformation() {
        return new Transformation(new Vector3f(0), new Vector3f(0), new Vector3f(1));
    }

    private static Vector3f vec3(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    public static Stream<Arguments> transformPointTestCases() {
        return Stream.of(
                Arguments.of(vec3(1, 0, 1), vec3(0, 0, (float)(-Math.PI / 2.0)), vec3(2, 2, 2), vec3(1, 1, 0), vec3(3, -2, 1))
        );
    }
}
