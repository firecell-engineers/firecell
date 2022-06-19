package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BasicAlgorithmTest {

    @ParameterizedTest(name = "Compute")
    @MethodSource("engineComputeTestCases")
    public void flattenIndexTest(State state, Vector3i cellIndex, Cell resultCell, BasicAlgorithm algorithm) {
        // given when then
        assertEquals(algorithm.compute(state, cellIndex), resultCell);
    }

    public static Stream<Arguments> engineComputeTestCases() {

        double deltaTime = 0.5;
        Vector3i spaceSize = new Vector3i(3, 3, 3);

        State initWoodenState = getInitState(spaceSize, Material.WOOD);
        State initAirState = getInitState(spaceSize, Material.AIR);

        Vector3i testCase1Wood = new Vector3i(1, 1, 1);
        Vector3i testCase2Wood = new Vector3i(2, 0, 1);
        Vector3i testCase3Wood = new Vector3i(2, 2, 2);

        Vector3i testCase1Air = new Vector3i(1, 1, 1);
        Vector3i testCase2Air = new Vector3i(2, 0, 1);
        Vector3i testCase3Air = new Vector3i(2, 2, 2);

        BasicAlgorithm algorithm = new BasicAlgorithm(deltaTime);
        double coe = BasicAlgorithm.GAMMA_PRIM_N1;

        return Stream.of(
                Arguments.of(initWoodenState, testCase1Wood, cell(initWoodenState.getCell(testCase1Wood).temperature() + deltaTime * coe * 400), algorithm),
                Arguments.of(initWoodenState, testCase2Wood, cell(initWoodenState.getCell(testCase2Wood).temperature()), algorithm),
                Arguments.of(initWoodenState, testCase3Wood, cell(initWoodenState.getCell(testCase3Wood).temperature()), algorithm),
                Arguments.of(initAirState, testCase1Air, cell(initAirState.getCell(testCase1Air).temperature() + deltaTime * coe * (-300), Material.AIR), algorithm),
                Arguments.of(initAirState, testCase2Air, cell(initAirState.getCell(testCase2Air).temperature(), Material.AIR), algorithm),
                Arguments.of(initAirState, testCase3Air, cell(initAirState.getCell(testCase3Air).temperature(), Material.AIR), algorithm)
        );
    }

    private static State getInitState(Vector3i spaceSize, Material material) {
        return new State(
                new ArrayList<>(
                        List.of(
                                cell(0, material), cell(0, material), cell(0, material),
                                cell(0, material), cell(400, material), cell(0, material),
                                cell(0, material), cell(0, material), cell(0, material),

                                cell(0, material), cell(200, material), cell(0, material),
                                cell(600, material), cell(400, material), cell(400, material),
                                cell(0, material), cell(100, material), cell(0, material),

                                cell(0, material), cell(0, material), cell(0, material),
                                cell(0, material), cell(300, material), cell(0, material),
                                cell(0, material), cell(0, material), cell(0, material)
                        )
                ),
                spaceSize
        );
    }

    private static Cell cell(double temp){
        return cell(temp, Material.WOOD);
    }
    private static Cell cell(double temp, Material material){
        return new Cell(temp, 0, true, material);
    }
}