package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy{

    @Override 
    public Animal select(Animal a, List<Animal> as) {
        if (as.isEmpty()) {
            return null;
        }
        
        Animal youngest = null;
        double minAge = Double.MAX_VALUE;
        for (Animal other : as) {
            double age = other.getAge();
            if (age < minAge) {
                minAge = age;
                youngest = other;
            }
        }
        return youngest;
    }

}
