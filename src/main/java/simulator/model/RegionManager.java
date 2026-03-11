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
        
       // Rellenamos todo el mapa con DefaultRegion inicialmente
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.regions[i][j] = new DefaultRegion();
            }
        }
        
        this.animalRegion = new HashMap<>();
    }


    //MÉTODOS

    //DE CONFIGURACIÓN

    public void setRegion(int row, int col, Region r) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            regions[row][col] = r;
        }
    }

    //metodo auxiliar que nos dice en que region cae una coordenada
    private Region getRegionForAnimal(Animal a) {
        double x = a.getPosition().getX();
        double y = a.getPosition().getY();

        // Fórmula: Coordenada / Tamaño_Celda = Índice
        int col = (int) (x / regionWidth);
        int row = (int) (y / regionHeight);

        // Si el animal está justo en el borde,
        // el índice podría salirse del array. Lo ajustamos al último índice válido.
        if (col >= cols) col = cols - 1;
        if (row >= rows) row = rows - 1;
        if (col < 0) col = 0;
        if (row < 0) row = 0;

        return regions[row][col];
    }

    //DE GESTIÓN DE ANIMALES

    //Añade un animal a una region
    public void registerAnimal(Animal a) {
        a.init(this); //Pasamos this para que el animal conozca el mapa
        Region region = getRegionForAnimal(a);
        region.addAnimal(a);
        animalRegion.put(a, region);
    }

    //Quita un animal de una region
    public void unregisterAnimal(Animal a) {
        Region region = animalRegion.get(a);
        if (region != null) {
            region.removeAnimal(a);
        }
        animalRegion.remove(a);
    }

    //Comprueba si el animal sigue en la region que estaba, si ha pasado a otra la actualiza
    public void updateAnimalRegion(Animal a) {
        Region currentRegion = animalRegion.get(a);
        Region newRegion = getRegionForAnimal(a);

        if (currentRegion != newRegion) {
            if (currentRegion != null) {
                currentRegion.removeAnimal(a);
            }
            if (newRegion != null) {
                newRegion.addAnimal(a);
            }
            animalRegion.put(a, newRegion);
        }
    }

    //llama a update de todas las regiones de la matriz de regiones
    public void updateAllRegions(double dt) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (regions[i][j] != null) {
                    regions[i][j].update(dt);
                }
            }
        }
    }

    @Override
    public List<Animal> getAnimalsInRange(Animal e, Predicate<Animal> filter) {

        List<Animal> animalsInRange = new ArrayList<>();

        //Sacamos la posicion del animal 
        double x = e.getPosition().getX();
        double y = e.getPosition().getY();
        double range = e.getSightRange();

        //Sacamos la casilla en la que está el animal
        int currentCol = (int) (x / regionWidth);
        int currentRow = (int) (y / regionHeight);

        //Casillas a las que alcanza a ver el animal
        int extentCols = (int) (range / regionWidth) + 1;
        int extentRows = (int) (range / regionHeight) + 1;

        //limites del recuadro cortando lo que se salga del mapa

        //fila
        int startRow = Math.max(0, currentRow - extentRows);
        int endRow = Math.min(rows - 1, currentRow + extentRows);

        //columna
        int startCol = Math.max(0, currentCol - extentCols);
        int endCol = Math.min(cols - 1, currentCol + extentCols);
    

        
        for (int r = startRow ; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                Region region = regions[r][c];
                if (region != null) {
                    for (Animal a : region.getAnimals()) {
                        // No incluirse a sí mismo
                        if (a != e) {
                            //Comprobar filtro (si es carnívoro y la distancia visual)
                            if (filter.test(a) && e.getPosition().distanceTo(a.getPosition()) <= range) {
                                animalsInRange.add(a);
                            }
                        }
                    }
                }
            }
        }
        return animalsInRange;
    }


    //IMPLEMENTACIÓN DE ANIMALMAPVIEW

    //implementación MapInfo
    @Override
    public int getCols() { return cols; }

    @Override
    public int getRows() { return rows; }

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    @Override
    public int getRegionWidth() { return regionWidth; }

    @Override
    public int getRegionHeight() { return regionHeight; }


    //implementación FoodSupplier
    @Override
    public double getFood(AnimalInfo a, double dt) {
        // Buscamos en el mapa auxiliar dónde está el animal
        Region r = animalRegion.get(a);
        return (r != null) ? r.getFood(a, dt) : 0.0;
    } 



    //SALIDA JSON

    @Override
    public JSONObject asJSON() {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();

        // El bucle queda mucho más limpio, solo recorre y delega
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                ja.put(regionAsJSON(i, j));
            }
        }

        jo.put("regions", ja);
        return jo;
    }

    //función auxiliar privada para formatear una region
    private JSONObject regionAsJSON(int r, int c) {
        JSONObject rObj = new JSONObject();
        rObj.put("row", r);
        rObj.put("col", c);

        // Si hay región configurada, pedimos sus datos
        if (regions[r][c] != null) {
            rObj.put("data", regions[r][c].asJSON());
        }
        
        return rObj;
    }
}
