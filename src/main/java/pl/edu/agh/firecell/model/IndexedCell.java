package pl.edu.agh.firecell.model;

import org.joml.Vector3i;

public record IndexedCell(Vector3i index, Cell cell) {
}
