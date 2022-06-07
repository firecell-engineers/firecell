package pl.edu.agh.firecell.engine.algorithm;

import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Index;
import pl.edu.agh.firecell.model.State;

import java.util.List;

public class BasicAlgorithm implements Algorithm {

    @Override
    public Cell compute(State oldState, Index cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);

        double temp1 = conductivity(
                oldState.getCell(cellIndex.getNorthIndex()),
                oldCell,
                oldState.getCell(cellIndex.getSouthIndex()));

        double temp2 = conductivity(
                oldState.getCell(cellIndex.getDownIndex()),
                oldCell,
                oldState.getCell(cellIndex.getUpIndex()));

        double temp3 = conductivity(
                oldState.getCell(cellIndex.getEastIndex()),
                oldCell,
                oldState.getCell(cellIndex.getWestIndex()));

        return new Cell(avg(List.of(temp1, temp2, temp3)), oldCell.coefficientConductivity());
    }

    private double conductivity(Cell left, Cell middle, Cell right){

        double dt = 0.05;
        double gammaPrimN = 0;
        double gammaPrimN1 = 0;

        return middle.temp() +
                (
                        gammaPrimN  * (middle.temp() - left.temp())   -
                        gammaPrimN1 * (right.temp()  - middle.temp())
                ) * dt;

    }

    private double avg(List<Double> arr){
        Double r = (double) 0;
        for (Double aDouble : arr) {
            r += aDouble;
        }
        return r/arr.size();
    }

}
