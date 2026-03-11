package simulator.model;

public class DefaultRegion extends Region {

    // No necesitamos definir constructor,se llama al de la clase padre Region

    @Override
    public void update(double dt) {
        //No hace nada.
    }

     @Override
    public double getFood(AnimalInfo a, double dt) {
        //Si es carnívoro, devuelve 0.0
        if (a.getDiet() == Animal.Diet.CARNIVORE) {
            return 0.0;
        }
        // Si es herbívoro, calculamos la comida según la fórmula
        // Primero contamos cuántos herbívoros (n) hay en esta región
        int n = 0;
        for (Animal animal : animals) {
            if (animal.getDiet() == Animal.Diet.HERBIVORE) {
                n++;
            }
        }
        // Aplicamos la fórmula dada:
        return 60.0 * Math.exp(-Math.max(0, n - 5.0) * 2.0) * dt;
    }

}
