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
    private static final double deltaTime = 0.5;

    @ParameterizedTest(name = "Compute for index {1} failed.")
    @MethodSource("computeTestMethodSource")
    public void computeTestMethod(State state, Vector3i cellIndex, Cell expectedCell) {
        // given
        Algorithm algorithm = new BasicAlgorithm(deltaTime);
        // when then
        assertEquals(expectedCell, algorithm.compute(state, cellIndex));
    }

    public static Stream<Arguments> computeTestMethodSource() {

        final Vector3i spaceSize = new Vector3i(3, 3, 3);

        final State initWoodState = getInitState(spaceSize, Material.WOOD);
        final State initAirState  = getInitState(spaceSize, Material.AIR);

        final Vector3i index111 = new Vector3i(1, 1, 1);
        final Vector3i index201 = new Vector3i(2, 0, 1);
        final Vector3i index222 = new Vector3i(2, 2, 2);

        final double COND_COEFF = BasicAlgorithm.CONDUCTIVITY_COEFFICIENT;

        return Stream.of(
                Arguments.of(initWoodState, index111, new Cell(initWoodState.getTemp(index111) + deltaTime * COND_COEFF * 400.0, 1, true, Material.WOOD),
                Arguments.of(initWoodState, index201, new Cell(initWoodState.getTemp(index201), 0, true, Material.WOOD)),
                Arguments.of(initWoodState, index222, new Cell(initWoodState.getTemp(index222), 0, true, Material.WOOD)),
                Arguments.of(initAirState,  index111, new Cell(initAirState.getTemp(index111) + deltaTime * COND_COEFF * -300, 1, true, Material.AIR)),
                Arguments.of(initAirState,  index201, new Cell(initAirState.getTemp(index201), 0, true, Material.AIR)),
                Arguments.of(initAirState,  index222, new Cell(initAirState.getTemp(index222), 0, true, Material.AIR))
        ));
    }

    private static State getInitState(Vector3i spaceSize, Material material) {
        return new State(
                new ArrayList<>(
                        List.of(
                                freshCell(0, material), freshCell(0, material), freshCell(0, material),
                                freshCell(0, material), freshCell(400, material), freshCell(0, material),
                                freshCell(0, material), freshCell(0, material), freshCell(0, material),

                                freshCell(0, material), freshCell(200, material), freshCell(0, material),
                                freshCell(600, material), freshCell(400, material), freshCell(400, material),
                                freshCell(0, material), freshCell(100, material), freshCell(0, material),

                                freshCell(0, material), freshCell(0, material), freshCell(0, material),
                                freshCell(0, material), freshCell(300, material), freshCell(0, material),
                                freshCell(0, material), freshCell(0, material), freshCell(0, material)
                        )
                ),
                spaceSize
        );
    }

    private static Cell freshCell(double temp, Material material) {
        return new Cell(temp, 0, true, material);
    }
}