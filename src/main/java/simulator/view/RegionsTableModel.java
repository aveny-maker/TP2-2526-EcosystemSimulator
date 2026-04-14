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

public class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {
    private Controller ctrl;
    private List<String> columnNames;
    private List<RegionRowData> tableData;

    //record privado para almacenar los datos de cada fila
    private record RegionRowData(int row, int col, String desc, Map<Animal.Diet, Integer> dietCounts) {}

    //constructor
    public RegionsTableModel(Controller ctrl) {
        this.ctrl = ctrl;
        this.columnNames = new ArrayList<>();
        this.tableData = new ArrayList<>();

        //crear las columnas 
        columnNames.add("Row");
        columnNames.add("Col");
        columnNames.add("Desc.");

        //añadimos una columna por cada dieta que exista (HERBIVORE, CARNIVORE...)
        for (Animal.Diet d : Animal.Diet.values()) {
            columnNames.add(d.toString());
        }

        // suscribirse al simulador
        this.ctrl.addObserver(this);
    }



    //MÉTODOS OBLIGATORIOS DEL TABLEMODEL

    @Override
    public int getRowCount() {
        return tableData.size();
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
        RegionRowData data = tableData.get(rowIndex);
        
        //Las tres primeras columnas son fijas
        if (columnIndex == 0) return data.row();
        if (columnIndex == 1) return data.col();
        if (columnIndex == 2) return data.desc();

        //si nos piden una columna de dieta, restamos 3 (porque las columnas 0, 1 y 2 ya están ocupadas)
        Animal.Diet diet = Animal.Diet.values()[columnIndex - 3];
        
        //comprobamos si hay animales con esa dieta
        if (data.dietCounts().containsKey(diet)) {
            return data.dietCounts().get(diet);
        } else {
            return 0; //si no hay, devolvemos 0
        }
    }


    //ACTUALIZACIÓN DE DATOS (OBSERVER)

    private void updateData(MapInfo map) {
        SwingUtilities.invokeLater(() -> {
            //borramos los datos de la tabla anterior
            tableData.clear();

            //aquí usamos el iterador, recorremos región a región de forma segura
            for (MapInfo.RegionData rData : map) {
                Map<Animal.Diet, Integer> counts = new HashMap<>();
                
                //contar las dietas de los animales que hay dentro de esta región concreta
                for (AnimalInfo a : rData.r().getAnimalsInfo()) {
                    Animal.Diet diet = a.getDiet();
                    
                    // sumar al contador
                    if (counts.containsKey(diet)) {
                        int numeroActual = counts.get(diet);
                        counts.put(diet, numeroActual + 1);
                    } else {
                        counts.put(diet, 1);
                    }
                }
                
                // guardar la fila completa en nuestra lista
                tableData.add(new RegionRowData(rData.row(), rData.col(), rData.r().toString(), counts));
            }

            // avisamos a la interfaz de que borre la pantalla y vuelva a preguntar los datos
            fireTableDataChanged();
        });
    }



    //EVENTOS DEL SIMULADOR
    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) { 
        updateData(map);
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) { 
        updateData(map); 
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) { 
        updateData(map); 
    }

    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) { 
        updateData(map); 
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
        updateData(map); 
    }
}

