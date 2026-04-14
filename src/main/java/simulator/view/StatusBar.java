package simulator.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

public class StatusBar extends JPanel implements EcoSysObserver {
    private Controller ctrl;

    //las etiquetas de texto que vamos a ir actualizando
    private JLabel timeLabel;
    private JLabel animalsLabel;
    private JLabel dimensionLabel;

    public StatusBar(Controller ctrl) {
        this.ctrl = ctrl;
        initGUI();
        
        //le decimos al controlador que nos añada a la lista de suscriptores
        this.ctrl.addObserver(this); 
    }


    private void initGUI() {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.setBorder(BorderFactory.createBevelBorder(1));

        //etiqueta del tiempo
        timeLabel = new JLabel("Time: 0.000");
        this.add(timeLabel);
        
        //separador vertical
        JSeparator s1 = new JSeparator(JSeparator.VERTICAL);
        s1.setPreferredSize(new Dimension(10, 20));
        this.add(s1);

        //etiqueta de animales
        animalsLabel = new JLabel("Total Animals: 0");
        this.add(animalsLabel);
        
        //separador vertical
        JSeparator s2 = new JSeparator(JSeparator.VERTICAL);
        s2.setPreferredSize(new Dimension(10, 20));
        this.add(s2);

        //etiqueta de dimensiones
        dimensionLabel = new JLabel("Dimension: N/A");
        this.add(dimensionLabel);
    }


    //MÉTODOS DEL ECOSYSOBSERVER
    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        updateInfo(time, map, animals);
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        updateInfo(time, map, animals);
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
        updateInfo(time, map, animals);
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
        // Al cambiar una región no cambian las dimensiones ni los animales, no hacemos nada.
    }

    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        updateInfo(time, map, animals);
    }

    //método auxiliar privado para actualizar los textos de forma segura 
    private void updateInfo(double time, MapInfo map, List<AnimalInfo> animals) {
        SwingUtilities.invokeLater(() -> {
            timeLabel.setText(String.format("Time: %.3f", time));
            animalsLabel.setText("Total Animals: " + animals.size());
            dimensionLabel.setText(String.format("Dimension: %dx%d %dx%d", 
                map.getWidth(), map.getHeight(), map.getCols(), map.getRows()));
        });
    }

}