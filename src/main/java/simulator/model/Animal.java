package simulator.model;


import simulator.misc.Vector2D;

public abstract class Animal implements Entity, AnimalInfo {

    //Atributos
    String geneticCode;
    Diet diet;
    State state;
    Vector2D pos;
    Vector2D dest;
    double energy;
    double speed;
    double age;
    double desire;
    double sightRange;
    Animal baby;
    AnimalMapView regionMngr;
    SelectionStrategy mateStrategy;


    //Constructores

    //Cuando se crea un animal desde 0
    protected Animal(String geneticCode, Diet diet, double sightRange, double initSpeed, SelectionStrategy mateStrategy, Vector2D pos) {
        this.geneticCode = geneticCode;
        this.diet = diet;
        this.sightRange = sightRange;
        this.speed = initSpeed;
        this.mateStrategy = mateStrategy;
        this.pos = pos;
        this.state = State.NORMAL;
        this.energy = 100;
        this.age = 0;
        this.desire = 0.0;
        this.dest = null;
        this.mateTarget=null;
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
        this.pos = p1.getPosition().plus(Vector2D.getRandomVector(-1,1).scale(60.0*(Utils.RAND.nextGaussian()+1)));
        this.sightRange = Utils.getRandomizedParameter((p1.getSightRange()+p2.getSightRange())/2,0.2);
        this.speed = Utils.getRandomizedParameter((p1.getSpeed()+p2.getSpeed())/2, 0.2);
        this.age = 0.0;
    }


    //Metodos





}
