package pl.edu.agh.firecell.model;

public record Cell(
        double temperature,
        int burningTime,
        boolean flammable,
        Material material,
        int smokeIndicator
        ) {

        public Cell(double temperature,
                    int burningTime,
                    boolean flammable,
                    Material material){
                this(temperature, burningTime, flammable, material, 0);
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
                smokeIndicator == c.smokeIndicator;
        }
}
