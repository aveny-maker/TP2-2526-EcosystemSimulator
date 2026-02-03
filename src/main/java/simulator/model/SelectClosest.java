package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy{
    
    @Override 
    public Animal select(Animal a, List<Animal> as) {
        Animal closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Animal other : as) {
            double distance = a.getPosition().distanceTo(other.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                closest = other;
            }
        }
        return closest;
    }

}
