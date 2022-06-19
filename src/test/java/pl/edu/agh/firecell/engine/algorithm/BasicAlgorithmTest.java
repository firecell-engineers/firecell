package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BasicAlgorithmTest {

    @Test
    void compute() {

        // given
        double deltaTime = 0.5;
        Vector3i spaceSize = new Vector3i(3, 3, 3);

        Vector3i testCase1Wood = new Vector3i(1, 1, 1);
        Vector3i testCase2Wood = new Vector3i(2, 0, 1);
        Vector3i testCase3Wood = new Vector3i(2, 2, 2);

        State initWoodenState = new State(
                new ArrayList<>(
                        List.of(
                            cell(0), cell(0), cell(0),
                            cell(0), cell(400), cell(0),
                            cell(0), cell(0), cell(0),

                            cell(0), cell(200), cell(0),
                            cell(600), cell(400), cell(400),
                            cell(0), cell(100), cell(0),

                            cell(0), cell(0), cell(0),
                            cell(0), cell(300), cell(0),
                            cell(0), cell(0), cell(0)
                        )
                ),
                spaceSize
        );

        BasicAlgorithm algorithm = new BasicAlgorithm(deltaTime);

        // when
        Cell resultCell1Wood = algorithm.compute(initWoodenState, testCase1Wood);
        Cell resultCell2Wood = algorithm.compute(initWoodenState, testCase2Wood);
        Cell resultCell3Wood = algorithm.compute(initWoodenState, testCase3Wood);

        // then
        double coe = BasicAlgorithm.GAMMA_PRIM_N1;

        Cell expected1 = cell(initWoodenState.getCell(testCase1Wood).temperature() + deltaTime * coe * 400);
        Cell expected2 = cell(initWoodenState.getCell(testCase2Wood).temperature());
        Cell expected3 = cell(initWoodenState.getCell(testCase3Wood).temperature());

        assertEquals(expected1, resultCell1Wood);
        assertEquals(expected2, resultCell2Wood);
        assertEquals(expected3, resultCell3Wood);

    }

    @Test
    void compute2() {

        // given
        double deltaTime = 0.5;
        Vector3i spaceSize = new Vector3i(3, 3, 3);

        Vector3i testCase1Air = new Vector3i(1, 1, 1);
        Vector3i testCase2Air = new Vector3i(2, 0, 1);
        Vector3i testCase3Air = new Vector3i(2, 2, 2);

        State initAirState = new State(
                new ArrayList<>(
                        List.of(
                                cell(0,   Material.AIR), cell(0,   Material.AIR), cell(0,   Material.AIR),
                                cell(0,   Material.AIR), cell(400, Material.AIR), cell(0,   Material.AIR),
                                cell(0,   Material.AIR), cell(0,   Material.AIR), cell(0,   Material.AIR),

                                cell(0,   Material.AIR), cell(200, Material.AIR), cell(0,   Material.AIR),
                                cell(600, Material.AIR), cell(400, Material.AIR), cell(400, Material.AIR),
                                cell(0,   Material.AIR), cell(100, Material.AIR), cell(0,   Material.AIR),

                                cell(0,   Material.AIR), cell(0,   Material.AIR), cell(0,   Material.AIR),
                                cell(0,   Material.AIR), cell(300, Material.AIR), cell(0,   Material.AIR),
                                cell(0,   Material.AIR), cell(0,   Material.AIR), cell(0,   Material.AIR)
                        )
                ),
                spaceSize
        );

        BasicAlgorithm algorithm = new BasicAlgorithm(deltaTime);

        Cell resultCell1Air = algorithm.compute(initAirState, testCase1Air);
        Cell resultCell2Air = algorithm.compute(initAirState, testCase2Air);
        Cell resultCell3Air = algorithm.compute(initAirState, testCase3Air);

        // then
        double coe = BasicAlgorithm.GAMMA_PRIM_N1;

        Cell expected11 = cell(initAirState.getCell(testCase1Air).temperature() + deltaTime * coe * (-300), Material.AIR);
        Cell expected22 = cell(initAirState.getCell(testCase2Air).temperature(), Material.AIR);
        Cell expected33 = cell(initAirState.getCell(testCase3Air).temperature(), Material.AIR);

        assertEquals(expected11, resultCell1Air);
        assertEquals(expected22, resultCell2Air);
        assertEquals(expected33, resultCell3Air);

    }
    private Cell cell(double temp){
        return cell(temp, Material.WOOD);
    }
    private Cell cell(double temp, Material material){
        return new Cell(temp, 0, true, material);
    }
}