package pl.edu.agh.firecell.model.util;

import org.joml.Vector3i;

public class IndexUtils {

    private IndexUtils() {
    }

    public static int flattenIndex(Vector3i index, Vector3i spaceSize) {
        if (index.x < 0 || index.y < 0 || index.z < 0) {
            throw new IndexOutOfBoundsException("Index %s has negative components.".formatted(index));
        }

        int flattenedIndex = index.x + index.y * spaceSize.x + index.z * spaceSize.y;

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

    public static Vector3i up(Vector3i index) {
        return new Vector3i(index.x, index.y, index.z + 1);
    }

    public static Vector3i down(Vector3i index) {
        return new Vector3i(index.x, index.y, index.z - 1);
    }

    public static Vector3i east(Vector3i index) {
        return new Vector3i(index.x + 1, index.y, index.z);
    }

    public static Vector3i west(Vector3i index) {
        return new Vector3i(index.x - 1, index.y, index.z);
    }

    public static Vector3i north(Vector3i index) {
        return new Vector3i(index.x, index.y + 1, index.z);
    }

    public static Vector3i south(Vector3i index) {
        return new Vector3i(index.x, index.y - 1, index.z);
    }
}
