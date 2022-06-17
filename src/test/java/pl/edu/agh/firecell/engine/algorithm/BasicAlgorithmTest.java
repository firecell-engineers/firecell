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
        Vector3i spaceSize = new Vector3i(3, 3, 3);

        Vector3i testCase1Wood = new Vector3i(1, 1, 1);
        Vector3i testCase2Wood = new Vector3i(2, 0, 1);
        Vector3i testCase3Wood = new Vector3i(2, 2, 2);

        State initWoodenState = new State(
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
        Cell resultCell1Wood = algorithm.compute(initWoodenState, testCase1Wood);
        Cell resultCell2Wood = algorithm.compute(initWoodenState, testCase2Wood);
        Cell resultCell3Wood = algorithm.compute(initWoodenState, testCase3Wood);

        // then
        double coe = BasicAlgorithm.GAMMA_PRIM_N1;
        double dt = algorithm.getDeltaTime();

        Cell expected1 = getCell(initWoodenState.getCell(testCase1Wood).temperature() + dt * coe * 400);
        Cell expected2 = getCell(initWoodenState.getCell(testCase2Wood).temperature());
        Cell expected3 = getCell(initWoodenState.getCell(testCase3Wood).temperature());

        assertEquals(expected1, resultCell1Wood);
        assertEquals(expected2, resultCell2Wood);
        assertEquals(expected3, resultCell3Wood);

    }

    @Test
    void compute2() {

        // given
        Vector3i spaceSize = new Vector3i(3, 3, 3);

        Vector3i testCase1Air = new Vector3i(1, 1, 1);
        Vector3i testCase2Air = new Vector3i(2, 0, 1);
        Vector3i testCase3Air = new Vector3i(2, 2, 2);

        State initAirState = new State(
                new ArrayList<>(
                        List.of(
                                getCell(0,   Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID),
                                getCell(0,   Material.AIR, MatterState.FLUID), getCell(400, Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID),
                                getCell(0,   Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID),

                                getCell(0,   Material.AIR, MatterState.FLUID), getCell(200, Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID),
                                getCell(600, Material.AIR, MatterState.FLUID), getCell(400, Material.AIR, MatterState.FLUID), getCell(400, Material.AIR, MatterState.FLUID),
                                getCell(0,   Material.AIR, MatterState.FLUID), getCell(100, Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID),

                                getCell(0,   Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID),
                                getCell(0,   Material.AIR, MatterState.FLUID), getCell(300, Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID),
                                getCell(0,   Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID), getCell(0,   Material.AIR, MatterState.FLUID)
                        )
                ),
                spaceSize
        );

        BasicAlgorithm algorithm = new BasicAlgorithm();

        Cell resultCell1Air = algorithm.compute(initAirState, testCase1Air);
        Cell resultCell2Air = algorithm.compute(initAirState, testCase2Air);
        Cell resultCell3Air = algorithm.compute(initAirState, testCase3Air);

        // then
        double coe = BasicAlgorithm.GAMMA_PRIM_N1;
        double dt = algorithm.getDeltaTime();

        Cell expected11 = getCell(initAirState.getCell(testCase1Air).temperature() + dt * coe * (-300), Material.AIR, MatterState.FLUID);
        Cell expected22 = getCell(initAirState.getCell(testCase2Air).temperature(), Material.AIR, MatterState.FLUID);
        Cell expected33 = getCell(initAirState.getCell(testCase3Air).temperature(), Material.AIR, MatterState.FLUID);

        assertEquals(expected11, resultCell1Air);
        assertEquals(expected22, resultCell2Air);
        assertEquals(expected33, resultCell3Air);

    }
    private Cell getCell(double temp){
        return getCell(temp, Material.WOOD, MatterState.SOLID);
    }
    private Cell getCell(double temp, Material material, MatterState matterState){
        return new Cell(
                temp,
                0,
                true,
                matterState,
                material
        );
    }
}