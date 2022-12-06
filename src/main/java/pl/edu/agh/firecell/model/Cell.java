package pl.edu.agh.firecell.model;

import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.material.MatterState;

public record Cell (
        double temperature,
        int burningTime,
        boolean flammable,
        Material material,
        int remainingFirePillar,
        double smokeIndicator,
        double oxygenLevel
        ) {

        private static final double DEFAULT_OXYGEN_LEVEL = 21;

        public Cell(double temperature, int burningTime, boolean flammable, Material material){
                this(temperature, burningTime, flammable, material, 0, 0, DEFAULT_OXYGEN_LEVEL);
        }

        public Cell(double temperature, int burningTime, boolean flammable, Material material, int remainingFirePillar){
            this(temperature, burningTime, flammable, material, remainingFirePillar, 0, DEFAULT_OXYGEN_LEVEL);
        }

        public Cell(Cell other){
            this(other.temperature, other.burningTime, other.flammable, other.material,
                    other.remainingFirePillar, other.smokeIndicator, other.oxygenLevel);
        }

    public boolean isSolid() {
        return material.getMatterState().equals(MatterState.SOLID);
    }

    public boolean isFluid() {
        return material.getMatterState().equals(MatterState.FLUID);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Cell c)) {
            return false;
        }
        return Double.compare(temperature, c.temperature) == 0 &&
                burningTime == c.burningTime &&
                flammable == c.flammable &&
                material == c.material &&
                remainingFirePillar == c.remainingFirePillar &&
                smokeIndicator == c.smokeIndicator &&
                oxygenLevel == c.oxygenLevel;
    }
}
