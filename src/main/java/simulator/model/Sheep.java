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

    @Override
    protected double getMaxAge() {
        return MAX_AGE_SHEEP; // Le devolvemos la constante de edad de la oveja
    }



    //METODO UPDATE PRINCIPAL
    @Override
    public void updateAnimal(double dt) {
        // Lógica específica según el estado actual
        switch (state) {
        case NORMAL -> updateNormal(dt);
        case DANGER -> updateDanger(dt);
        case MATE -> updateMate(dt);
        default -> {
            }
        }
    }


    //METODOS AUXILIARES UPDATE
    private void updateNormal(double dt) {
        //Avanzar 
        advanceNormal(dt);

        //Cambio estado
        //Si no hay peligroso buscar a uno que lo sea
        if (dangerSource == null) {
            dangerSource = dangerStrategy.select(this, regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.CARNIVORE));
        }

        //Cambio estado a danger o mate
        if (dangerSource != null) {
            setState(State.DANGER);
        } else if (desire > DESIRE_THRESHOLD_SHEEP) {
            setState(State.MATE);
        }

    }




    private void updateDanger(double dt) {
        //Verificar si el peligro ha muerto
        if (dangerSource != null && dangerSource.getState()== State.DEAD) {
            dangerSource = null;
        }

        //Movimiento
        if (dangerSource == null) {
            //si no hay peligro, avanza como en el estado normal
            advanceNormal(dt);
        }
        else {
            //Comportamiento de huida

            //huir en dirección contraria al peligro
            dest = pos.plus(pos.minus(dangerSource.getPosition()).direction());

            //se mueve mas rapido
            double moveSpeed = BOOST_FACTOR_SHEEP * speed * dt * Math.exp((energy - 100.0) * 0.007); 
            move(moveSpeed);

            //actualizamos parametros
            age += dt;
            energy = Math.max(0.0, energy - FOOD_DROP_RATE_SHEEP * FOOD_DROP_BOOST_FACTOR_SHEEP * dt);
            desire = Math.min(100.0, desire + DESIRE_INCREASE_RATE_SHEEP * dt);
        }

        //Cambio de estado

        if (dangerSource == null || pos.distanceTo(dangerSource.getPosition()) > sightRange) {
            //busca nuevo peligro
            dangerSource = dangerStrategy.select(this,regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.CARNIVORE));

            //si tras buscar seguimos sin peligro
            if (dangerSource == null) {
                if (desire < DESIRE_THRESHOLD_SHEEP) {
                    setState(State.NORMAL);
                } else {
                    setState(State.MATE);
                }
            }
        }
    }

    private void updateMate(double dt) {
        //Validar si la pareja sigue siendo válida (viva y visible)
        if (mateTarget != null && (mateTarget.getState() == State.DEAD || pos.distanceTo(mateTarget.getPosition()) > sightRange)) {
            mateTarget = null;
        }

        //Si no hay pareja, buscar una nueva
        if (mateTarget == null) {
            //buscamos animales con el mismo código genético
            mateTarget = mateStrategy.select(this,regionMngr.getAnimalsInRange(this, a -> a.getGeneticCode().equals(SHEEP_GENETIC_CODE)));
        }

        //Comportamiento según si hay objetivo
        if (mateTarget == null) {
            //se comporta como en estado normal
            advanceNormal(dt);
        }
        else {
            //ir hacia la pareja
            dest = mateTarget.getPosition();

            //se mueve mas rapido
            double moveSpeed = BOOST_FACTOR_SHEEP * speed * dt * Math.exp((energy - 100.0) * 0.007);
            move(moveSpeed);

            // actualizar parámetros
            age += dt;
            energy = Math.max(0.0, energy - FOOD_DROP_RATE_SHEEP * FOOD_DROP_BOOST_FACTOR_SHEEP * dt);
            desire = Math.min(100.0, desire + DESIRE_INCREASE_RATE_SHEEP * dt);

            // Interacción de apareamiento (si están cerca)
            if (pos.distanceTo(mateTarget.getPosition())<8.0) {
                //reseteo deseo ambos
                this.desire = 0.0;
                mateTarget.desire = 0.0;

                //probabilidad embarazo
                if (baby == null && simulator.misc.Utils.RAND.nextDouble() < PREGNANT_PROBABILITY_SHEEP) {
                    baby = new Sheep(this, mateTarget);
                }

                //fin encuentro
                mateTarget = null;
            }
        }

        //Cambio de estado
        if (dangerSource == null) {
            dangerSource = dangerStrategy.select(this, regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.CARNIVORE));
        }

        if (dangerSource != null) {
            setState(State.DANGER);
        } else if (desire < DESIRE_THRESHOLD_SHEEP) {
            setState(State.NORMAL);
        }
    }

    // Método común para avanzar normalmente (usado cuando no hay amenaza)
    private void advanceNormal(double dt) {
        // Destino
        if (pos.distanceTo(dest) < 8.0) {
            dest = new Vector2D(simulator.misc.Utils.RAND.nextDouble() * regionMngr.getWidth(),simulator.misc.Utils.RAND.nextDouble() * regionMngr.getHeight());
        }

        // Movimiento
        double moveSpeed = speed * dt * Math.exp((energy - 100.0) * 0.007);
        move(moveSpeed);

        //ctualizamos parámetros
        age += dt;
        energy = Math.max(0.0, energy - FOOD_DROP_RATE_SHEEP * dt);
        desire = Math.min(100.0, desire + DESIRE_INCREASE_RATE_SHEEP * dt);
    }


}
