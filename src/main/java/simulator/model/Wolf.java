package simulator.model;

import simulator.misc.Vector2D;

public class Wolf extends Animal{

    // CONSTANTES 
    private static final String WOLF_GENETIC_CODE = "Wolf";
    private static final double INIT_SIGHT_WOLF = 50.0;
    private static final double INIT_SPEED_WOLF = 60.0;
    private static final double MAX_AGE_WOLF = 14.0;
    private static final double FOOD_DROP_RATE_WOLF = 18.0;
    private static final double BOOST_FACTOR_WOLF = 3.0; // Corren más que las ovejas (x3)
    private static final double HUNT_BOOST_FACTOR_WOLF = 1.2; // Gasto de energía al cazar
    private static final double DESIRE_THRESHOLD_WOLF = 65.0;
    private static final double DESIRE_INCREASE_RATE_WOLF = 30.0;
    private static final double PREGNANT_PROBABILITY_WOLF = 0.75; // Menos probabilidad que la oveja
    private static final double ENERGY_LIM_HUNGER = 50.0; // Umbral de hambre
    private static final double ENERGY_ADD_HUNT = 50.0;   // Energía ganada al comer
    private static final double ENERGY_MATING_COST = 10.0; // Cansancio post-cópula

    // ATRIBUTOS
    private Animal huntTarget;
    private SelectionStrategy huntingStrategy;

    // CONSTRUCTORAS
    public Wolf(SelectionStrategy mateStrategy, SelectionStrategy huntingStrategy, Vector2D pos) {
        super(WOLF_GENETIC_CODE, Diet.CARNIVORE, INIT_SIGHT_WOLF, INIT_SPEED_WOLF, mateStrategy, pos);
        this.huntingStrategy = huntingStrategy;
        this.huntTarget = null;
    }

    protected Wolf(Wolf p1, Animal p2) {
        super(p1, p2);
        this.huntingStrategy = p1.huntingStrategy;
        this.huntTarget = null;
    }


    // MÉTODOS DE ACCIÓN (Gestión de Estado)
    @Override
    protected void setNormalStateAction() {
        this.huntTarget = null;
        this.mateTarget = null;
    }

    @Override
    protected void setMateStateAction() {
        this.huntTarget = null;
    }

    @Override
    protected void setHungerStateAction() {
        this.mateTarget = null;
    }

    @Override
    protected void setDangerStateAction() {
        // Los lobos nunca entran en estado DANGER
    }

    @Override
    protected void setDeadStateAction() {
        this.huntTarget = null;
        this.mateTarget = null;
    }
    

    // UPDATE 
    @Override
    public void update(double dt) {
        //Si esta muerto no hace nada
        if (state == State.DEAD) {
            return;
        }

        //Lógica específica según el estado actual
        switch (state) {
            case NORMAL: updateNormal(dt); break;
            case HUNGER: updateHunger(dt); break;
            case MATE:   updateMate(dt);   break;
            case DANGER: /* Nunca entra en DANGER */ break;
            case DEAD:   break;
        }

        // Verificar límites del mapa (Mundo Toroidal)
        if (pos.getX() < 0 || pos.getX() >= regionMngr.getWidth() ||
            pos.getY() < 0 || pos.getY() >= regionMngr.getHeight()) {
            pos = fixPosition(pos.getX(), pos.getY()); // Usamos el método protected de Animal
            setState(State.NORMAL);
        }

        //Verificar condiciones de muerte (Edad o Energía)
        if (energy <= 0.0 || age > MAX_AGE_WOLF) {
            setState(State.DEAD);
        }

        //Alimentación pasiva
        if (state != State.DEAD) {
            double food = regionMngr.getFood(this, dt);
            this.energy += food;
            if (this.energy > MAX_ENERGY) {
                this.energy = MAX_ENERGY;
            }
        }
        
    }

    //MÉTODOS AUXILIARES PARA UPDATE
    private void updateNormal(double dt){
        //avanza normal
        advanceNormal(dt);

        //cambio de estado
        if (energy < ENERGY_LIM_HUNGER) {
            setState(State.HUNGER);
        }
        else if (desire > DESIRE_THRESHOLD_WOLF) {
            setState(State.MATE);
        }
    }

    private void updateHunger(double dt){
        //validar presa o buscar otra
        if (huntTarget == null || huntTarget.getState() == State.DEAD || pos.distanceTo(huntTarget.getPosition()) > sightRange) {
            //buscar herbívoros en rango
            huntTarget = huntingStrategy.select(this, regionMngr.getAnimalsInRange(this, a -> a.getDiet() == Diet.HERBIVORE));
        }

        //Comportamiento según si tiene presa
        if (huntTarget == null) {
            //no hay presa, avanza normal
            advanceNormal(dt);
        }

        else {
            //perseguir presa
            dest = huntTarget.getPosition();

            //se mueve más rápido
            double moveSpeed = BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - 100.0) * 0.007);
            move(moveSpeed);

            //Actualizamos parámetros
            age += dt;
            energy = Math.max(0.0, energy - FOOD_DROP_RATE_WOLF * HUNT_BOOST_FACTOR_WOLF * dt);
            desire = Math.min(100.0, desire + DESIRE_INCREASE_RATE_WOLF * dt);

            //Cazar si estamos cerca
            if (pos.distanceTo(huntTarget.getPosition())<8.0) {
                //matar presa
                huntTarget.setState(State.DEAD);
                huntTarget = null;

                // Comer
                energy += ENERGY_ADD_HUNT;
                if (energy > MAX_ENERGY) energy = MAX_ENERGY;
            }
        }

        //Cambio de estado 
        if (energy > ENERGY_LIM_HUNGER) {
            if (desire < DESIRE_THRESHOLD_WOLF) {
                setState(State.NORMAL);
            } 
            else {
                setState(State.MATE);
            }
        }

    }

    private void updateMate(double dt){
        //validar pareja
        if (mateTarget != null && (mateTarget.getState() == State.DEAD || pos.distanceTo(mateTarget.getPosition()) > sightRange)) {
            mateTarget = null;
        }

        //buscar pareja si no tiene
        if (mateTarget == null) {
            mateTarget = mateStrategy.select(this, regionMngr.getAnimalsInRange(this, a -> a.getGeneticCode().equals(WOLF_GENETIC_CODE)));
        }

        //Comportamiento según si tiene pareja
        if (mateTarget == null) {
            //no hay pareja, avanza normal
            advanceNormal(dt);
        }
        else {
            //ir hacia la pareja
            dest = mateTarget.getPosition();

            //se mueve mas rapido
            double moveSpeed = BOOST_FACTOR_WOLF * speed * dt * Math.exp((energy - 100.0) * 0.007);
            move(moveSpeed);

            // actualizar parámetros
            age += dt;
            energy = Math.max(0.0, energy - FOOD_DROP_RATE_WOLF * HUNT_BOOST_FACTOR_WOLF * dt);
            desire = Math.min(100.0, desire + DESIRE_INCREASE_RATE_WOLF * dt);

            //Sexo nena
            if (pos.distanceTo(mateTarget.getPosition())< 8.0) {
                //Resetear deseo
                this.desire = 0.0;
                mateTarget.desire = 0.0;

                //Coste energético
                this.energy = Math.max(0.0, this.energy - ENERGY_MATING_COST);
                mateTarget.energy = Math.max(0.0, mateTarget.energy - ENERGY_MATING_COST);

                //probabilidad de embarazo
                if (baby == null && simulator.misc.Utils.RAND.nextDouble() < PREGNANT_PROBABILITY_WOLF) {
                    baby = new Wolf(this, mateTarget);
                }

                mateTarget = null;
            }
        }

        //Cambio de estado
        if (energy < ENERGY_LIM_HUNGER) {
            setState(State.HUNGER);
        }
        else if (desire < DESIRE_THRESHOLD_WOLF) {
            setState(State.NORMAL);
        }
        
    }

    private void advanceNormal(double dt) {
        //destino aleatorio si esta cerca
        if (pos.distanceTo(dest)<8.0) {
            dest = new Vector2D(simulator.misc.Utils.RAND.nextDouble() * regionMngr.getWidth(),simulator.misc.Utils.RAND.nextDouble() * regionMngr.getHeight());
        }

        //movimiento
        double moveSpeed = speed * dt * Math.exp((energy - 100.0) * 0.007);
        move(moveSpeed);

        //actualizamos parametros
        age += dt;
        energy = Math.max(0.0, energy - FOOD_DROP_RATE_WOLF * dt);
        desire = Math.min(100.0, desire + DESIRE_INCREASE_RATE_WOLF * dt);
    }

}



