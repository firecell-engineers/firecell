package pl.edu.agh.firecell.model.util;

import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IndexUtils {

    private IndexUtils() {
    }

    public static boolean isInSpaceSize(Vector3i index, Vector3i spaceSize) {
        return 0 <= index.x && index.x < spaceSize.x
                && 0 <= index.y && index.y < spaceSize.y
                && 0 <= index.z && index.z < spaceSize.z;
    }

    public static int flattenIndex(Vector3i index, Vector3i spaceSize) {
        if (index.x < 0 || index.y < 0 || index.z < 0) {
            throw new IndexOutOfBoundsException("Index %s has negative components.".formatted(index));
        }

        int flattenedIndex = index.x + index.y * spaceSize.x + index.z * spaceSize.y * spaceSize.x;

        if (flattenedIndex >= spaceSize.x * spaceSize.y * spaceSize.z) {
            throw new IndexOutOfBoundsException("Index %s out of space %s.".formatted(index, spaceSize));
        }
        return flattenedIndex;
    }

    public static Vector3i expandIndex(int index, Vector3i spaceSize) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index %s is negative.".formatted(index));
        }
        int spaceSizeXY = spaceSize.x * spaceSize.y;
        int z = index / spaceSizeXY;
        int y = index % spaceSizeXY / spaceSize.x;
        int x = index - y * spaceSize.x - z * spaceSizeXY;
        if (index >= spaceSize.x * spaceSize.y * spaceSize.z) {
            throw new IndexOutOfBoundsException("Index %s out of space %s.".formatted(index, spaceSize));
        }

        return new Vector3i(x, y, z);
    }

    public static Stream<Vector3i> range(Vector3i startInclusive, Vector3i endInclusive) {
        return IntStream.range(startInclusive.x, endInclusive.x + 1).boxed()
                .flatMap(x -> IntStream.range(startInclusive.y, endInclusive.y + 1).mapToObj(y -> new Vector2i(x, y)))
                .flatMap(xy -> IntStream.range(startInclusive.z, endInclusive.z + 1).mapToObj(z -> new Vector3i(xy.x, xy.y, z)));
    }
}
