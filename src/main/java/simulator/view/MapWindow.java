package simulator.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

public class MapWindow extends JFrame implements EcoSysObserver {

    private Controller ctrl;
    private AbstractMapViewer viewer;
    private Frame parent;

    //constructor
    MapWindow(Frame parent, Controller ctrl) {
        super("[MAP VIEWER]");
        this.ctrl = ctrl;
        this.parent = parent;
        initGUI();
        
        // Registrar esta ventana como observadora para recibir eventos del simulador
        this.ctrl.addObserver(this);
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        //creamos el visor 
        viewer = new MapViewer();
        mainPanel.add(viewer, BorderLayout.CENTER);

        //al cerrar la ventana dejamos de ser observadores
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ctrl.removeObserver(MapWindow.this);
            }
        });

        pack();
        setResizable(false); // impedimos redimensionar para facilitar el dibujo
        
        //posicionamiento centrado respecto a la ventana principal
        if (this.parent != null) {
            setLocation(
                this.parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2,
                this.parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2
            );
        }
        
        setVisible(true);
    }

    
    //MÉTODOS DEL OBSERVER
    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        // Usamos invokeLater para asegurar que el cambio de tamaño (pack) se haga en el hilo de Swing
        SwingUtilities.invokeLater(() -> {
            viewer.reset(time, map, animals);
            pack();
        });
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        SwingUtilities.invokeLater(() -> {
            viewer.reset(time, map, animals);
            pack();
        });
    }

    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        // En cada paso de simulación, pedimos al visor que redibuje a los animales
        SwingUtilities.invokeLater(() -> {
            viewer.update(animals, time);
        });
    }

    // Estos eventos no requieren redibujar el mapa completo necesariamente o el viewer ya los gestiona
    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) { }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) { }


}
