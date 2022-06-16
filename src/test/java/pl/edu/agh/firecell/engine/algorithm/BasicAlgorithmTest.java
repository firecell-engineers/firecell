package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.MatterState;
import pl.edu.agh.firecell.model.State;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BasicAlgorithmTest {

    @Test
    void compute() {

        // given
        Vector3i testCase1 = new Vector3i(1, 1, 1);
        Vector3i spaceSize = new Vector3i(3, 3, 3);

        State initState = new State(
                new ArrayList<>(
                        List.of(
                            getCell(0), getCell(0), getCell(0),
                            getCell(0), getCell(400), getCell(0),
                            getCell(0), getCell(0), getCell(0),

                            getCell(0), getCell(200), getCell(0),
                            getCell(600), getCell(400), getCell(400),
                            getCell(0), getCell(100), getCell(0),

                            getCell(0), getCell(0), getCell(0),
                            getCell(0), getCell(300), getCell(0),
                            getCell(0), getCell(0), getCell(0)
                        )
                ),
                spaceSize
        );
        BasicAlgorithm algorithm = new BasicAlgorithm();

        // when
        Cell resultCell = algorithm.compute(initState, testCase1);

        // then
        double coe = BasicAlgorithm.GAMMA_PRIM_N1;
        double dt = algorithm.getDeltaTime();

        Cell expected = getCell(initState.getCell(testCase1).temperature() + dt * coe * 400);
        assertEquals(expected, resultCell);
    }

    private Cell getCell(double temp){
        return new Cell(
                temp,
                0,
                true,
                MatterState.SOLID,
                Material.WOOD
        );
    }
}