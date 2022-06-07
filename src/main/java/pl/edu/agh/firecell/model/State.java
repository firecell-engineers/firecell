package pl.edu.agh.firecell.model;

public record State(Cell[][][] cells) {

    public Cell getCell(int x, int y, int z){
        return cells[x][y][z];
    }

    public Cell getCell(Index index){
        return cells[index.x()][index.y()][index.z()];
    }

}
