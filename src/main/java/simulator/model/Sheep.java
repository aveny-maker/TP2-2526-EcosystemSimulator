package simulator.model;

import simulator.misc.Vector2D;

public class Sheep extends Animal {

    //Constantes
    final static String SHEEP_GENETIC_CODE = "Sheep";
    final static double INIT_SIGHT_SHEEP = 40;
    final static double INIT_SPEED_SHEEP = 35;
    final static double BOOST_FACTOR_SHEEP = 2.0;
    final static double MAX_AGE_SHEEP = 8;
    final static double FOOD_DROP_BOOST_FACTOR_SHEEP = 1.2;
    final static double FOOD_DROP_RATE_SHEEP = 20.0;
    final static double DESIRE_THRESHOLD_SHEEP = 65.0;
    final static double DESIRE_INCREASE_RATE_SHEEP = 40.0;
    final static double PREGNANT_PROBABILITY_SHEEP = 0.9;


    //ATRIBUTOS
    Animal dangerSource;
    SelectionStrategy dangerStrategy;



    //CONSTRUCTORES
    public Sheep(SelectionStrategy mateStrategy, SelectionStrategy dangerStrategy, Vector2D pos) {
        super("Sheep", Diet.HERBIVORE,INIT_SIGHT_SHEEP, INIT_SPEED_SHEEP, mateStrategy, pos);
        this.dangerStrategy = dangerStrategy;
        this.dangerSource = null;
    }

    protected Sheep(Sheep p1, Animal p2) {
        super(p1, p2);
        this.dangerStrategy = p1.dangerStrategy;
        this.dangerSource = null;
    }



    //MÉTODOS

    @Override
    protected void setNormalStateAction() {
        this.dangerSource = null;
        this.mateTarget = null;
    }

    @Override
    protected void setMateStateAction() {
        this.dangerSource = null;
    }

    @Override
    protected void setDangerStateAction() {
        this.mateTarget = null;
    }

    @Override
    protected void setDeadStateAction() {
        this.dangerSource = null;
        this.mateTarget = null;
    }

    @Override
    protected void setHungerStateAction() {
        // No se utiliza en Sheep
    }



    //METODO UPDATE PRINCIPAL
    @Override
    public void update(double dt) {
        // 1. Si está muerto, no hace nada
        if (state == State.DEAD) return;

        // 2. Lógica específica según el estado actual
        switch (state) {
        case NORMAL -> updateNormal(dt);
        case DANGER -> updateDanger(dt);
        case MATE -> updateMate(dt);
        default -> {
            }
        }

        // 3. Verificar si se ha salido del mapa
        if (pos.getX() < 0 || pos.getX() >= regionMngr.getWidth() ||
            pos.getY() < 0 || pos.getY() >= regionMngr.getHeight()) {
            // Usamos el método protected de la clase padre
            pos = fixPosition(pos.getX(), pos.getY());
            setState(State.NORMAL);
        }

        // 4. Verificar condiciones de muerte (Edad o Energía)
        if (energy <= 0.0 || age > MAX_AGE_SHEEP) {
            setState(State.DEAD);
        }

        // 5. Alimentación (si sigue vivo)
        if (state != State.DEAD) {
            double food = regionMngr.getFood(this, dt);
            this.energy += food;
            if (this.energy > MAX_ENERGY) this.energy = MAX_ENERGY;
        }
    }


    //METODOS AUXILIARES UPDATE
    private void updateNormal(double dt) {}
    private void updateDanger(double dt) {}
    private void updateMate(double dt) {}










    


}
