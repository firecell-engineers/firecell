package pl.edu.agh.firecell.model;

public record Cell(
        double temperature,
        int burningTime,
        boolean flammable,
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
                material == c.material;
        }
}
