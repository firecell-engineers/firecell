package pl.edu.agh.firecell.model;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.List;

public record State(List<Cell> cells, Vector3i spaceSize) {
    public Cell getCell(Vector3i index){
        return cells.get(IndexUtils.flattenIndex(index, spaceSize));
    }
}
