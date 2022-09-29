package pl.edu.agh.firecell.model.util;

import org.joml.Vector3i;

import java.util.stream.Stream;

public class NeighbourUtils {

    public enum Axis {
        X, Y, Z
    }

    // [koszar] Graphics assumes that y is up, z is front. I made it here the same to avoid unnecessary computing
    public static Vector3i up(Vector3i index) {
        return new Vector3i(index.x, index.y + 1, index.z);
    }

    public static Vector3i down(Vector3i index) {
        return new Vector3i(index.x, index.y - 1, index.z);
    }

    public static Vector3i east(Vector3i index) {
        return new Vector3i(index.x + 1, index.y, index.z);
    }

    public static Vector3i west(Vector3i index) {
        return new Vector3i(index.x - 1, index.y, index.z);
    }

    public static Vector3i north(Vector3i index) {
        return new Vector3i(index.x, index.y, index.z + 1);
    }

    public static Vector3i south(Vector3i index) {
        return new Vector3i(index.x, index.y, index.z - 1);
    }

    public static Stream<Vector3i> neighboursStream(Vector3i index) {
        return Stream.of(up(index), down(index), east(index), west(index), north(index), south(index));
    }

    public static Stream<Vector3i> neighboursStream(Vector3i index, Axis axis) {
        return switch (axis) {
            case X -> Stream.of(east(index),  west(index));
            case Y -> Stream.of(up(index),    down(index));
            case Z -> Stream.of(north(index), south(index));
        };
    }
}
