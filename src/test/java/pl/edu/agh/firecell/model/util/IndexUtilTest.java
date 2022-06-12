package pl.edu.agh.firecell.model.util;

import org.joml.Vector3i;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexUtilTest {

    @ParameterizedTest(name = "Index {0} should be flattened to {2} in {1} space.")
    @MethodSource("flattenIndexTestCases")
    public void flattenIndexTest(Vector3i givenIndex, Vector3i givenSpaceSize, int expectedIndex) {
        // given when then
        assertEquals(expectedIndex, IndexUtils.flattenIndex(givenIndex, givenSpaceSize));
    }

    public static Stream<Arguments> flattenIndexTestCases() {
        return Stream.of(
                Arguments.of(new Vector3i(0, 0, 0), new Vector3i(1, 2, 3), 0),
                Arguments.of(new Vector3i(0, 0, 1), new Vector3i(1, 2, 3), 2),
                Arguments.of(new Vector3i(0, 1, 2), new Vector3i(1, 2, 3), 5)
        );
    }

    @ParameterizedTest(name = "Index {0} should be expanded to {2} in {1} space.")
    @MethodSource("expandIndexTestCases")
    public void expandIndexTest(int givenIndex, Vector3i givenSpaceSize, Vector3i expectedIndex) {
        // given when then
        assertEquals(expectedIndex, IndexUtils.expandIndex(givenIndex, givenSpaceSize));
    }

    public static Stream<Arguments> expandIndexTestCases() {
        return Stream.of(
                Arguments.of(0, new Vector3i(1, 2, 3), new Vector3i(0, 0, 0)),
                Arguments.of(2, new Vector3i(1, 2, 3), new Vector3i(0, 0, 1)),
                Arguments.of(5, new Vector3i(1, 2, 3), new Vector3i(0, 1, 2))
        );
    }
}
