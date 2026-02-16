package simulator.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegionManager implements AnimalMapView{

    //ATRIBUTOS

    // Dimensiones del mapa (globales)
    private int width;
    private int height;

    // Dimensiones de la cuadrícula (filas x columnas)
    private int cols;
    private int rows;

    // Dimensiones de cada celda/región individual
    private int regionWidth;
    private int regionHeight;

    // La matriz de regiones
    private Region[][] regions;

    // Mapa auxiliar para saber rápidamente en qué región está cada animal
    private Map<Animal, Region> animalRegion;

    //CONSTRUCTOR
    public RegionManager(int colsP, int rowsP, int widthP, int heightP) {
        this.cols = colsP;
        this.rows = rowsP;
        this.width = widthP;
        this.height = heightP;
        
        // Calculamos cuánto mide cada celda
        this.regionWidth = width / cols;
        this.regionHeight = height / rows;

        // Inicializamos la matriz
        this.regions = new Region[rows][cols];
        
        // Por defecto, llenamos todo con nulos (se configurarán después con setRegion)
        // Ojo: en algunas versiones se inicializan aquí con DefaultRegion. 
        // Nosotros dejaremos un método setRegion para hacerlo desde fuera.
        
        this.animalRegion = new HashMap<>();
    }


    //MÉTODOS

    //DE CONFIGURACIÓN
    
    public void setRegion(int row, int col, Region r) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            regions[row][col] = r;
        }
    }











}
