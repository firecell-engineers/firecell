package pl.edu.agh.firecell.model;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record State(List<Cell> cells, Vector3i spaceSize) {

    public Cell getCell(Vector3i index) {
        return cells.get(IndexUtils.flattenIndex(index, spaceSize));
    }

    public boolean hasCell(Vector3i index) {
        return 0 <= index.x && index.x < spaceSize.x &&
                0 <= index.y && index.y < spaceSize.y &&
                0 <= index.z && index.z < spaceSize.z;

    }

    public double getTemp(Vector3i index) {
        return cells.get(IndexUtils.flattenIndex(index, spaceSize)).temperature();
    }

    public Stream<IndexedCell> getIndexedCellsStream() {
        return IntStream.range(0, cells.size())
                .mapToObj(flatIndex -> IndexUtils.expandIndex(flatIndex, spaceSize))
                .map(expandedIndex -> new IndexedCell(expandedIndex, getCell(expandedIndex)));
    }
}
