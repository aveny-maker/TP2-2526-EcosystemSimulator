package simulator.model;


import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {

    //CONSTANTES
    final static double INIT_ENERGY = 100.0;
    final static double MUTATION_TOLERANCE = 0.2;
    final static double NEARBY_FACTOR = 60.0;
    final static double COLLISION_RANGE = 8;
    final static double HUNGER_DECAY_EXP_FACTOR = 0.007;
    final static double MAX_ENERGY = 100.0;
    final static double MAX_DESIRE = 100.0;

    //ATRIBUTOS 
    protected String geneticCode;
    protected Diet diet;
    protected State state;
    protected Vector2D pos;
    protected Vector2D dest;
    protected double energy;
    protected double speed;
    protected double age;
    protected double desire;
    protected double sightRange;
    protected Animal baby;
    protected AnimalMapView regionMngr;
    protected SelectionStrategy mateStrategy;
    protected Animal mateTarget;


    //CONSTRUCTORES

    //Cuando se crea un animal desde 0
    protected Animal(String geneticCode, Diet diet, double sightRange, double initSpeed, SelectionStrategy mateStrategy, Vector2D pos) {
        // Validaciones
        if (geneticCode == null || geneticCode.isBlank()) 
            throw new IllegalArgumentException("Código genético no válido.");
        if (sightRange <= 0) 
            throw new IllegalArgumentException("El rango de visión debe ser positivo.");
        if (initSpeed <= 0) 
            throw new IllegalArgumentException("La velocidad inicial debe ser positiva.");
        if (mateStrategy == null) 
            throw new IllegalArgumentException("La estrategia de apareamiento es obligatoria.");

        //  Asignación de parámetros recibidos
        this.geneticCode = geneticCode;
        this.diet = diet;
        this.sightRange = sightRange;
        this.mateStrategy = mateStrategy;
        this.pos = pos; 

        // Inicialización según fórmulas del enunciado
        this.speed = Utils.getRandomizedParameter(initSpeed, 0.1); 
        this.state = State.NORMAL;
        this.energy = 100.0; 
        this.desire = 0.0;
        this.age = 0.0;
        
        // Inicialización a null de referencias de control
        this.dest = null;
        this.mateTarget = null;
        this.baby = null;
        this.regionMngr = null;
    }

    //Cuando nazca un animal a partir de otros 2
    protected Animal(Animal p1, Animal p2) {
        this.dest= null;
        this.baby = null;
        this.mateTarget = null;
        this.regionMngr = null;
        this.state = State.NORMAL;
        this.desire = 0.0;
        this.geneticCode = p1.geneticCode;
        this.diet = p1.diet;
        this.mateStrategy =  p2.mateStrategy;
        this.energy = (p1.energy + p2.energy)/2;
        this.pos = p1.getPosition().plus(Vector2D.get_random_vector(-1,1).scale(60.0*(Utils.RAND.nextGaussian()+1)));
        this.sightRange = Utils.getRandomizedParameter((p1.getSightRange()+p2.getSightRange())/2,0.2);
        this.speed = Utils.getRandomizedParameter((p1.getSpeed()+p2.getSpeed())/2, 0.2);
        this.age = 0.0;
    }


    //METODOS
    public void init(AnimalMapView regMngr){

        //inicializar el gestor de regiones
        this.regionMngr = regMngr;
        
        //Gestión de la posicion inicial
        if (this.pos == null) {
            // Si es null, elegir posición aleatoria dentro del rango del mapa
            double x = Utils.RAND.nextDouble() * (regMngr.getWidth() - 1);
            double y = Utils.RAND.nextDouble() * (regMngr.getHeight() - 1);
            this.pos = new Vector2D(x, y);
        }
        else {
            // Si no es null, ajustarla para que esté dentro del mapa (mundo toroidal)
            this.pos = fixPosition(this.pos.getX(), this.pos.getY());

        }

        //Elegir un destino aleatorio inicial dentro del rango del mapa
        double destX = Utils.RAND.nextDouble() * (regMngr.getWidth() - 1);
        double destY = Utils.RAND.nextDouble() * (regMngr.getHeight() - 1);
        this.dest = new Vector2D(destX, destY);
    }

    protected Vector2D fixPosition(double x, double y) {
        int width = regionMngr.getWidth();   
        int height = regionMngr.getHeight(); 
        
        // Lógica toroidal proporcionada en el  enunciado
        while (x >= width) x = (x - width);
        while (x < 0) x = (x + width);
        while (y >= height) y = (y - height);
        while (y < 0) y = (y + height);
        
        return new Vector2D(x, y);
    }

    public Animal deliverBaby() {
        Animal b = this.baby;
        this.baby= null;
        return b;
    }

    protected void move(double speed) {
        pos = pos.plus(dest.minus(pos).direction().scale(speed));
    }

    protected void setState(State state) {
        this.state = state;
        switch (state) {
            case NORMAL -> setNormalStateAction();
            case MATE -> setMateStateAction();
            case HUNGER -> setHungerStateAction();
            case DANGER -> setDangerStateAction();
            case DEAD -> setDeadStateAction();
        }
    }

    // Métodos abstractos que deben implementar Sheep y Wolf
    protected abstract void setNormalStateAction();
    protected abstract void setMateStateAction();
    protected abstract void setHungerStateAction();
    protected abstract void setDangerStateAction();
    protected abstract void setDeadStateAction();

    //GETTERS
    @Override
    public State getState() {
        return state;
    }

    @Override
    public Vector2D getPosition() {
        return pos;
    }

    @Override
    public String getGeneticCode() {
        return geneticCode;
    }

    @Override
    public Diet getDiet() {
        return diet;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public double getSightRange() {
        return sightRange;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public double getAge() {
        return age;
    }

    @Override
    public Vector2D getDestination() {
        return dest;
    }

    @Override
    public boolean isPregnant() {
        return baby != null;
    }

    @Override
    public JSONObject asJSON() {
        JSONObject jo = new JSONObject();
        jo.put("pos", pos.asJSONArray()); // Usamos el método de Vector2D
        jo.put("gcode", geneticCode);
        jo.put("diet", diet.toString());
        jo.put("state", state.toString());
        return jo;
    }

}
