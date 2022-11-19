package pl.edu.agh.firecell.core.statebuilder.element;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.List;

public record CuboidElement(Vector3i position, Vector3i size, Material material) implements BaseCuboidElement {

    @Override
    public void addTo(List<Cell> cells, Vector3i spaceSize) {
        IndexUtils.range(position, new Vector3i(-1).add(position).add(size))
                .filter(expandedIndex -> IndexUtils.isInSpaceSize(expandedIndex, spaceSize))
                .map(expandedIndex -> IndexUtils.flattenIndex(expandedIndex, spaceSize))
                .forEach(flatIndex -> cells.set(flatIndex, createCell()));
    }

    private Cell createCell() {
        if (material == Material.WOOD) {
            return new Cell(15, 0, true, material, 5);
        }
        return new Cell(15, 0, true, material);
    }
}
