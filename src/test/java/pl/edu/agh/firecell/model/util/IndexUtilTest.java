package pl.edu.agh.firecell.model.util;

import org.joml.Vector3i;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
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
                Arguments.of(vec(0, 0, 0), vec(3, 3, 3), 0),
                Arguments.of(vec(0, 0, 1), vec(3, 3, 3), 9),
                Arguments.of(vec(0, 1, 2), vec(3, 3, 3), 21)
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
                Arguments.of(0, vec(3, 3, 3), vec(0, 0, 0)),
                Arguments.of(9, vec(3, 3, 3), vec(0, 0, 1)),
                Arguments.of(21, vec(3, 3, 3), vec(0, 1, 2))
        );
    }

    @ParameterizedTest(name = "Range [{0}, {1}] incorrect.")
    @MethodSource("rangeTestCases")
    public void rangeTest(Vector3i startIndex, Vector3i endIndex, Stream<Vector3i> expectedIndices) {
        // when
        var actualRange = IndexUtils.range(startIndex, endIndex);

        // then
        assertEquals(expectedIndices.collect(Collectors.toSet()), actualRange.collect(Collectors.toSet()));
    }

    public static Stream<Arguments> rangeTestCases() {
        return Stream.of(
                Arguments.of(vec(0, 0, 0), vec(2, 2, 2), createRangeManually(vec(0, 0, 0), vec(2, 2, 2)))
        );
    }

    private static Vector3i vec(int x, int y, int z) {
        return new Vector3i(x, y, z);
    }

    private static Stream<Vector3i> createRangeManually(Vector3i start, Vector3i end) {
        List<Vector3i> range = new LinkedList<>();
        for (int x = start.x; x <= end.x; x ++) {
            for (int y = start.y; y <= end.y; y ++) {
                for (int z = start.z; z <= end.z; z ++) {
                    range.add(vec(x, y, z));
                }
            }
        }
        return range.stream();
    }
}
