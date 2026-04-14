package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.Animal;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

public class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver{
    private Controller ctrl;
    private List<String> columnNames;
    private List<String> speciesList; // Lista de las especies descubiertas
    private Map<String, Map<Animal.State, Integer>> stateCounts; // Cuentas por estado


    public SpeciesTableModel (Controller ctrl) {
        this.ctrl = ctrl;
        this.columnNames = new ArrayList<>();
        this.speciesList = new ArrayList<>();
        this.stateCounts = new HashMap<>();

        //crear columnas 
        columnNames.add("Species");
        columnNames.add("Total");
        for (Animal.State s : Animal.State.values()) {
            columnNames.add(s.toString());
        }

        //suscribirse al simulador
        this.ctrl.addObserver(this);
        
    }


    // MÉTODOS DEL TABLEMODEL


    @Override
    public int getRowCount() {
        return speciesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String species = speciesList.get(rowIndex);
        Map<Animal.State, Integer> counts = stateCounts.get(species);

        //si la tabla nos pide el dato de la primera columna, le devolvemos el nombre:
        if (columnIndex == 0) return species;

        //si nos pide la segunda columna, le devolvemos el total, sumando los de todos los estados
        if (columnIndex == 1) {
            int total = 0;
            for (Integer c : counts.values()) {
                total += c;
            }
            return total;
        }

        //si es una columna de estado, restamos 2 al índice porque las 2 primeras son Species y Total
        Animal.State state = Animal.State.values()[columnIndex - 2];

        if (counts.containsKey(state)) {
            return counts.get(state); // Si hay animales en ese estado, devuelve el número
        } else {
            return 0; // Si no existe ese estado en el diccionario, es que hay 0
        }
    }

    //ACTUALIZACIÓN DE DATOS (OBSERVER)
    private void updateData(List<AnimalInfo> animals) {
        SwingUtilities.invokeLater(() -> {
            //limpiar datos antiguos
            stateCounts.clear();
            speciesList.clear();

            //rellenar con los nuevos datos
            for (AnimalInfo a : animals) {
                String gcode = a.getGeneticCode();
                Animal.State state = a.getState();

                // si es una especie nueva, la registramos
                if (!speciesList.contains(gcode)) {
                    speciesList.add(gcode);
                    stateCounts.put(gcode, new HashMap<>());
                }

                //sumamos 1 al contador de ese estado concreto
                Map<Animal.State, Integer> counts = stateCounts.get(gcode);

                if (counts.containsKey(state)) {
                    //si ya habiamos contado animales en este estado, miramos cuantos van, y le sumamos 1
                    int numeroActual = counts.get(state);
                    counts.put(state, numeroActual + 1);

                } else {
                    // si es la primera vez que vemos un animal en este estado, empezamos a contar desde 1
                    counts.put(state, 1);
                }

            }

            //aviso a la tabla para que se redibuje sola
            fireTableDataChanged();
        });
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) { 
        updateData(animals); 
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) { 
        updateData(animals); 
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) { 
        updateData(animals); 
    }

    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) { 
        updateData(animals); 
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {}  //no afecta a las especies globales 

}



