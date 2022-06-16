package pl.edu.agh.firecell.model;

public record Cell(
        double temperature,
        int burningTime,
        boolean flammable,
        MatterState type,
        Material material
        ) {

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
                type == c.type &&
                material == c.material;
        }
}
