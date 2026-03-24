package simulator.model;
import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {
    // ATRIBUTOS
    private double food;
    private double factor;

    //CONSTRUCTOR
    public DynamicSupplyRegion(double initialFood, double factor) {
        super(); // Inicializa la lista de animales
        this.food = initialFood;
        this.factor = factor;
    }

    //MÉTODOS

    //UPDATE
    @Override
    public void update(double dt) {
        if (Utils.RAND.nextDouble() < 0.5) {
            food += dt * factor;
        }
    }

    //GETFOOD
    @Override
    public double getFood(AnimalInfo a, double dt) {
        //Carnívoros no comen aquí
        if (a.getDiet() == Animal.Diet.CARNIVORE) {
            return 0.0;
        }

        //Contar herbívoros (n)
        int n = 0;
        for (Animal animal : animals) {
            if (animal.getDiet() == Animal.Diet.HERBIVORE) {
                n++;
            }
        }

        //Calcular la demanda según la fórmula
        double demand = 60.0 * Math.exp(-Math.max(0, n - 5.0) * 2.0) * dt;

        //Tomar el mínimo entre lo que hay (food) y lo que pide (demand)
        double foodTaken = Math.min(food, demand);

        // Restar la comida consumida a la región
        food -= foodTaken;

        return foodTaken;
    }
    // TOSTRING
    @Override
    public String toString() {
        return "Dynamic Region";
    }

}
