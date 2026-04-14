package simulator.view;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import simulator.control.Controller;

public class MainWindow extends JFrame {
    private Controller ctrl;

    public MainWindow(Controller ctrl) {
        super("[ECOSYSTEM SIMULATOR]");
        this.ctrl = ctrl;
        initGUI();
    }

    private void initGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        //añadimos el panel de control (Arriba / PAGE_START)
        ControlPanel ctrlPanel = new ControlPanel(ctrl);
        mainPanel.add(ctrlPanel, BorderLayout.PAGE_START);

        //añadimos la barra de estado (Abajo / PAGE_END)
        StatusBar statusBar = new StatusBar(ctrl);
        mainPanel.add(statusBar, BorderLayout.PAGE_END);

        //panel central para las tablas (Usamos BoxLayout vertical)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        //añadir las tablas de especies y regiones 
         contentPanel.add(new InfoTable("Species", new SpeciesTableModel(ctrl)));
         contentPanel.add(new InfoTable("Regions", new RegionsTableModel(ctrl)));

        //comportamiento al cerrar la ventana , usando viewUtils
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ViewUtils.quit(MainWindow.this);
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack(); // Ajusta el tamaño al contenido
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        setVisible(true); // ¡Que se haga la luz!
    }

}
