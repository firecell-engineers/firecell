package pl.edu.agh.firecell.model.material;

public record UnorderedMaterialPair(Material m1,
                                    Material m2) {

    public UnorderedMaterialPair(Material m1, Material m2) {
        this.m1 = m1.ordinal() < m2.ordinal() ? m1 : m2;
        this.m2 = m1.ordinal() < m2.ordinal() ? m2 : m1;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof UnorderedMaterialPair p))
            return false;
        return m1.equals(p.m1) &&
                m2.equals(p.m2);
    }

}
