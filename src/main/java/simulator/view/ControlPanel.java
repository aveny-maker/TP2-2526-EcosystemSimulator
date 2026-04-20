package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.launcher.Main;

public class ControlPanel extends JPanel {
    private Controller ctrl;

    private JToolBar toolBar;
    private JFileChooser fc;
    private boolean stopped = true;

    //botones
    private JButton openButton;
    private JButton viewerButton;
    private JButton regionsButton;
    private JButton runButton;
    private JButton stopButton;
    private JButton quitButton;
    private ChangeRegionsDialog changeRegionsDialog;

    //inputs
    private JSpinner stepsSpinner;
    private JTextField dtTextField;

    //constructor
    public ControlPanel(Controller ctrl) {
        this.ctrl = ctrl;
        this.changeRegionsDialog = new ChangeRegionsDialog(ctrl);
        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        toolBar = new JToolBar();
        add(toolBar, BorderLayout.PAGE_START);

        // selector de archivos (apunta a la carpeta examples por defecto)
        fc = new JFileChooser();
        fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/src/main/resources/examples"));

        // BOTÓN ABRIR ARCHIVO
        openButton = new JButton();
        openButton.setToolTipText("Load an input file");
        openButton.setIcon(new ImageIcon(getClass().getResource("/icons/open.png")));
        openButton.addActionListener(e -> {
            int ret = fc.showOpenDialog(ViewUtils.getWindow(this));
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    InputStream is = new FileInputStream(file);
                    JSONObject jsonInput = new JSONObject(new JSONTokener(is));
                    
                    int cols = jsonInput.getInt("cols");
                    int rows = jsonInput.getInt("rows");
                    int width = jsonInput.getInt("width");
                    int height = jsonInput.getInt("height");
                    
                    // Reseteamos y cargamos datos usando el controlador
                    ctrl.reset(cols, rows, width, height);
                    ctrl.loadData(jsonInput);
                    is.close();
                } catch (Exception ex) {
                    ViewUtils.showErrorMsg(this, "Error loading file: " + ex.getMessage());
                }
            }
        });
        toolBar.add(openButton);
        toolBar.addSeparator();

        //BOTÓN VISOR (MapWindow)
        viewerButton = new JButton();
        viewerButton.setToolTipText("Open Map Viewer");
        viewerButton.setIcon(new ImageIcon(getClass().getResource("/icons/viewer.png")));

        viewerButton.addActionListener(e -> {
            new MapWindow(ViewUtils.getWindow(this), ctrl); 
        });

        toolBar.add(viewerButton);

        // BOTÓN REGIONES
        regionsButton = new JButton();
        regionsButton.setToolTipText("Change Regions");
        regionsButton.setIcon(new ImageIcon(getClass().getResource("/icons/regions.png")));
        regionsButton.addActionListener(e -> {
            changeRegionsDialog.open(ViewUtils.getWindow(this));
        });
        toolBar.add(regionsButton);
        toolBar.addSeparator();

        //BOTÓN RUN 
        runButton = new JButton();
        runButton.setToolTipText("Run Simulation");
        runButton.setIcon(new ImageIcon(getClass().getResource("/icons/run.png")));
        runButton.addActionListener(e -> {
            // Bloqueamos los botones mientras se ejecuta
            stopped = false;
            setButtonsEnabled(false);
            
            try {
                double dt = Double.parseDouble(dtTextField.getText());
                int steps = (Integer) stepsSpinner.getValue();
                runSim(steps, dt);
            } catch (Exception ex) {
                ViewUtils.showErrorMsg(this, "Invalid delta-time format");
                setButtonsEnabled(true);
            }
        });
        toolBar.add(runButton);

        //BOTÓN STOP
        stopButton = new JButton();
        stopButton.setToolTipText("Stop Simulation");
        stopButton.setIcon(new ImageIcon(getClass().getResource("/icons/stop.png")));
        stopButton.addActionListener(e -> stopped = true);
        toolBar.add(stopButton);
        toolBar.addSeparator();

        //SPINNER DE PASOS
        toolBar.add(new JLabel(" Steps: "));
        stepsSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, 100000, 100));
        stepsSpinner.setMaximumSize(new Dimension(80, 40));
        toolBar.add(stepsSpinner);

        //TEXTFIELD DEL DELTA-TIME
        toolBar.add(new JLabel(" Delta-Time: "));
        // gracias a que antes pusimos Main.deltaTime en public, lo podemos leer aquí:
        dtTextField = new JTextField(Main.deltaTime.toString());
        dtTextField.setMaximumSize(new Dimension(80, 40));
        toolBar.add(dtTextField);

        //BOTÓN SALIR (Alineado a la derecha con Box.createGlue)
        toolBar.add(Box.createGlue()); 
        toolBar.addSeparator();
        quitButton = new JButton();
        quitButton.setToolTipText("Quit");
        quitButton.setIcon(new ImageIcon(getClass().getResource("/icons/exit.png")));
        quitButton.addActionListener((e) -> ViewUtils.quit(this));
        toolBar.add(quitButton);
    }

    //BUCLE ASÍNCRONO DE LA SIMULACIÓN
    private void runSim(int n, double dt) {
        if (n > 0 && !this.stopped) {
            try {
                this.ctrl.advance(dt);
                //se llama a sí mismo en la cola de eventos
                SwingUtilities.invokeLater(() -> runSim(n - 1, dt));
            } catch (Exception e) {
                ViewUtils.showErrorMsg(this, "Simulation error: " + e.getMessage());
                setButtonsEnabled(true);
                this.stopped = true;
            }
        } else {
            //cuando termina o lo paramos, volvemos a activar los botones
            setButtonsEnabled(true);
            this.stopped = true;
        }
    }


    // Método auxiliar para activar/desactivar todos los botones a la vez
    private void setButtonsEnabled(boolean state) {
        openButton.setEnabled(state);
        viewerButton.setEnabled(state);
        regionsButton.setEnabled(state);
        runButton.setEnabled(state);
        quitButton.setEnabled(state);
    }


}
