package pl.edu.agh.firecell.model;

import pl.edu.agh.firecell.model.exception.ConductionCoefficientException;
import pl.edu.agh.firecell.model.material.ConductionCoefficientProvider;
import pl.edu.agh.firecell.model.material.Material;

public class ModelChecker {

    private void checkConductionCoefficientProvider() throws ConductionCoefficientException{
        ConductionCoefficientProvider conductionCoefficientProvider = new ConductionCoefficientProvider();
        int numOfMaterials = Material.values().length;
        for (int i=0; i<numOfMaterials;i++){
            for(int j=i;j<numOfMaterials;j++){
                if(conductionCoefficientProvider.getConductionCoe(i, j).isEmpty())
                    throw new ConductionCoefficientException(
                            "[checkConductionCoefficientProvider] There is lack of "
                                    + i + ", " + j + " material conduction coefficient");
            }
        }
        if(conductionCoefficientProvider.getNumOfCoefficients() != (numOfMaterials*(numOfMaterials+1))/2)
            throw new ConductionCoefficientException(
                    "[checkConductionCoefficientProvider] There is incorrect number " +
                            "of relations between materials, expected value: " +
                            (numOfMaterials*(numOfMaterials+1))/2 + ", but got: " +
                            conductionCoefficientProvider.getNumOfCoefficients());
    }

    public void checkAll() throws Exception {
        checkConductionCoefficientProvider();
    }

}
