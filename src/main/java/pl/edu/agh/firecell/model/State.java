package pl.edu.agh.firecell.model;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector3i;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record State(List<Cell> cells, Vector3i spaceSize) {

    public Cell getCell(Vector3i index) {
        return cells.get(IndexUtils.flattenIndex(index, spaceSize));
    }

    public double getTemp(Vector3i index) {
        return cells.get(IndexUtils.flattenIndex(index, spaceSize)).temperature();
    }

    public Stream<Pair<Vector3i, Cell>> getIndexedCellsStream() {
        return IntStream.range(0, cells.size())
                .mapToObj(flatIndex -> IndexUtils.expandIndex(flatIndex, spaceSize))
                .map(expandedIndex -> new ImmutablePair<>(expandedIndex, getCell(expandedIndex)));
    }
}
