package simulator.model;

import java.util.List;

import simulator.misc.Vector2D;

public class SelectClosest implements SelectionStrategy{
    
    @Override 
    public Animal select(Animal a, List<Animal> as) {
        if (as.isEmpty()) {
            return null;
        }

        Animal closest = null;
        double minDistance = Double.MAX_VALUE;
        Vector2D currentPos= a.getPosition();
        for (Animal other : as) {
            double distance = currentPos.distanceTo(other.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                closest = other;
            }
        }
        return closest;
    }

}
