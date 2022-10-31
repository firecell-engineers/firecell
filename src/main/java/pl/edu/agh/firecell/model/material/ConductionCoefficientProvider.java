package pl.edu.agh.firecell.model.material;

import java.util.HashMap;
import java.util.Optional;

public class ConductionCoefficientProvider {

    private final HashMap<Pair, Double> coeTable;

    public ConductionCoefficientProvider() {
        coeTable = new HashMap<>();
    //==================================================================================================================
        coeTable.put(new Pair(Material.AIR.ordinal(), Material.AIR.ordinal()),                              /**/ 0.008);
        coeTable.put(new Pair(Material.AIR.ordinal(), Material.WOOD.ordinal()),                             /**/ 0.02);
        coeTable.put(new Pair(Material.AIR.ordinal(), Material.CELLULAR_CONCRETE.ordinal()),                /**/ 0.015);
    //==================================================================================================================
        coeTable.put(new Pair(Material.WOOD.ordinal(), Material.WOOD.ordinal()),                            /**/ 0.2);
        coeTable.put(new Pair(Material.WOOD.ordinal(), Material.CELLULAR_CONCRETE.ordinal()),               /**/ 0.15);
    //==================================================================================================================
        coeTable.put(new Pair(Material.CELLULAR_CONCRETE.ordinal(), Material.CELLULAR_CONCRETE.ordinal()),  /**/ 0.1);
    //==================================================================================================================
    }

    public Optional<Double> getConductionCoe(Material m1, Material m2) {
        return getConductionCoe(m1.ordinal(), m2.ordinal());
    }

    public Optional<Double> getConductionCoe(int m1, int m2) {
        Pair p = m1< m2?new Pair(m1, m2):new Pair(m2, m1);
        return coeTable.containsKey(p)?Optional.of(coeTable.get(p)):Optional.empty();
    }

    public int getNumOfCoefficients(){
        return coeTable.size();
    }
}

record Pair(int left, int right){}
