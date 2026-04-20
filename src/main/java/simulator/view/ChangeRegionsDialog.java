package simulator.view;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

public class ChangeRegionsDialog extends JDialog implements EcoSysObserver {

    //modelos de datos para los despegables
    private DefaultComboBoxModel<String> regionsModel;
    private DefaultComboBoxModel<String> fromRowModel;
    private DefaultComboBoxModel<String> toRowModel;
    private DefaultComboBoxModel<String> fromColModel;
    private DefaultComboBoxModel<String> toColModel;

    //modelo de datos para la tabla
    private DefaultTableModel dataTableModel;
    private Controller ctrl;

    //lista con inforamcion de las regiones extraida de la factoria
    private List<JSONObject> regionsInfo;

    private String[] headers = { "Key", "Value", "Description" };
    private int status; // 1 si pulsan OK, 0 si Cancel

    //constructor
    public ChangeRegionsDialog(Controller ctrl) {

        //inicializamos el dialog como modal para que bloquee la ventana principal mientras está abierto
        super((Frame) null, true);
        this.ctrl = ctrl;
        initGUI();

        //suscribirse al simulador para reaccionar a cambios en dimensiones del mapa
        this.ctrl.addObserver(this);
    }

    private void initGUI() {
        setTitle("Change Regions");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        setContentPane(mainPanel);

        //TEXTO DE AYUDA
        JLabel helpLabel = new JLabel("<html><p style='margin: 10px 10px 5px 10px;'>Select a region type, the rows/cols interval, and provide values for the parameters in the <b>Value</b> column (default values are used for parameters with no value).</p></html>");
        helpLabel.setAlignmentX(CENTER_ALIGNMENT);
        mainPanel.add(helpLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        //cargamos la informacion de las regiones desde la factoria
        this.regionsInfo = Main.regionsFactory.getInfo();

        //TABLA DE PARÁMETROS

        //clase anonima para que no se puedan editar claves y descripciones
        this.dataTableModel = new DefaultTableModel() {
            @Override
            //por defecto se puede borrar el texto de las celdas, pero solo queremos que se pueda escribir en la columna Value
            public boolean isCellEditable(int row, int column) {
                // hacer editable solo la columna 1
                return column == 1;
            }
        };


        //asignar titulos a las columnas
        this.dataTableModel.setColumnIdentifiers(this.headers);

        //crear la tabla con el modelo y meterla en un scrollpane
        JTable table = new JTable(this.dataTableModel);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(600, 150));

        //añadir la tabla al panel principal con un espacio debajo
        mainPanel.add(tableScroll);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));


        //PANEL DE COMBOBOXES

        //creamos un subpanel para agrupar los menus desplegables
        JPanel combosPanel = new JPanel();
        combosPanel.setAlignmentX(CENTER_ALIGNMENT);

        //selección del tipo de región
        combosPanel.add(new JLabel("Region type: "));
        this.regionsModel = new DefaultComboBoxModel<>();
        for (JSONObject info : regionsInfo) {
            regionsModel.addElement(info.getString("desc"));
        }
        JComboBox<String> regionsComboBox = new JComboBox<>(regionsModel);
        
        // conectamos la selección
        regionsComboBox.addActionListener(e -> updateTableModel(regionsComboBox.getSelectedIndex()));
        combosPanel.add(regionsComboBox);

        //espaciado
        combosPanel.add(new JLabel(" "));

        //selección del rango de filas
        combosPanel.add(new JLabel("Row from/to: "));
        fromRowModel = new DefaultComboBoxModel<>();
        combosPanel.add(new JComboBox<>(fromRowModel));
        toRowModel = new DefaultComboBoxModel<>();
        combosPanel.add(new JComboBox<>(toRowModel));

        //espacio
        combosPanel.add(new JLabel(" "));

        //selección del rango de columnas 
        combosPanel.add(new JLabel("Column from/to: "));
        fromColModel = new DefaultComboBoxModel<>();
        combosPanel.add(new JComboBox<>(fromColModel));
        toColModel = new DefaultComboBoxModel<>();
        combosPanel.add(new JComboBox<>(toColModel));

        //añadimos el subpanel de combos al panel principal y dejamos un espacio debajo
        mainPanel.add(combosPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        //BOTONES OK Y CANCEL

        //creamos un subpanel para agrupar los botones
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setAlignmentX(CENTER_ALIGNMENT);

        //boton cancelar, cierra el dialog sin hacer nada
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            status = 0;
            setVisible(false);
        });
        buttonsPanel.add(cancelButton);

        //boton OK, cierra el dialog y guarda el estado para que el metodo open() sepa que se han guardado cambios
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            status = 1; //estado 1 significa que se han guardado cambios
            setVisible(false); //ocultamos la ventana y devolemos el control para que procese los cambios
        });
        buttonsPanel.add(okButton);


        //añadimos el subpanel de botones al panel principal
        mainPanel.add(buttonsPanel);


        //configuracion de ventana
        setPreferredSize(new Dimension(850, 400)); //tamaño base
        pack(); //ajusta componentes internos para que quepan bien
        setResizable(false); //bloqueamos el redimensionado para evitar problemas de diseño

        setVisible(false); //la ventana se muestra al llamar al metodo open(), no antes
    }




    //MÉTODOS AUXILIARES

    //metodo auxiliar para rellenar la tabla segun la region elegida
    private void updateTableModel(int selectedIndex) {

        dataTableModel.setRowCount(0); // borramos la tabla

        if (selectedIndex >= 0) {
            //obtener json de la region seleccionada
            JSONObject info = regionsInfo.get(selectedIndex);
            JSONObject data = info.getJSONObject("data"); // cogemos los parámetros que necesita
            
            //iteramos sobre las claves 
            for (String key : data.keySet()) {
                //obtenemos la descricion asociada a esa clave
                String desc = data.getString(key);

                //añadimos una fila a la tabla con la clave, un campo vacio para el valor, y la descripcion
                dataTableModel.addRow(new Object[]{key, "", desc}); 
            }
        }
    }


    //metodo para abrir el dialogo de forma modal, bloqueando la ventana principal hasta que se cierre
    //si usuario pulsa OK, recopilamos datos, consruimos el JSON y enviamos orden al controlador para cambiar las regiones
    //si pulsa Cancel, no hacemos nada

    public void open(Frame parent) {

        //centramos el dialogo respecto a la ventana principal
        setLocation( parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
        
        // reseteamos el dialogo para que aparezca limpio
        if (regionsModel.getSize() > 0) {
            regionsModel.setSelectedItem(regionsModel.getElementAt(0));
            updateTableModel(0);
        }
        status = 0;
        
        pack();

        //la ejecucion se quedara bloqueada en esta linea hasta que el usuario pulse OK o Cancel
        // después se va a ocultar el dialog y se haran los cambios si es necesario
        setVisible(true);

        // si han pulsado OK 
        if (status == 1) {
            try {
                //leer la tabla para crear region_data
                JSONObject region_data = new JSONObject();
                for (int i = 0; i < dataTableModel.getRowCount(); i++) {
                    String key = dataTableModel.getValueAt(i, 0).toString();
                    String value = dataTableModel.getValueAt(i, 1).toString();
                    
                    if (!value.trim().isEmpty()) {
                        // si han escrito algo, lo añadimos
                        region_data.put(key, Double.parseDouble(value));
                    }
                }

                //sacar el tipo (region_type)
                JSONObject info = regionsInfo.get(regionsModel.getIndexOf(regionsModel.getSelectedItem()));
                String region_type = info.getString("type");

                // sacar coordenadas
                int row_from = Integer.parseInt(fromRowModel.getSelectedItem().toString());
                int row_to = Integer.parseInt(toRowModel.getSelectedItem().toString());
                int col_from = Integer.parseInt(fromColModel.getSelectedItem().toString());
                int col_to = Integer.parseInt(toColModel.getSelectedItem().toString());

                //crear el JSON final
                JSONObject spec = new JSONObject();
                spec.put("type", region_type);
                spec.put("data", region_data);

                JSONObject regionObj = new JSONObject();
                regionObj.put("row", new JSONArray().put(row_from).put(row_to));
                regionObj.put("col", new JSONArray().put(col_from).put(col_to));
                regionObj.put("spec", spec);

                JSONObject finalJson = new JSONObject();
                finalJson.put("regions", new JSONArray().put(regionObj));

                //mandar al controlador
                ctrl.setRegions(finalJson);

            } catch (Exception e) {
                //si hay algun error al leer la tabla o construir el JSON, mostramos un mensaje de error
                ViewUtils.showErrorMsg(this, "Error: " + e.getMessage());
            }
        }
    }

    //MÉTODOS DEL OBSERVER
    private void updateDimensions(MapInfo map) {

        //quitamos datos anteriores de los modelos de los despegables
        fromRowModel.removeAllElements();
        toRowModel.removeAllElements();
        fromColModel.removeAllElements();
        toColModel.removeAllElements();

        //rellenamos los modelos de los despegables con las nuevas dimensiones del mapa
        for (int i = 0; i < map.getRows(); i++) {
            fromRowModel.addElement(String.valueOf(i));
            toRowModel.addElement(String.valueOf(i));
        }

        //lo mismo para las columnas
        for (int i = 0; i < map.getCols(); i++) {
            fromColModel.addElement(String.valueOf(i));
            toColModel.addElement(String.valueOf(i));
        }
    }


    //el tamaño del mapa solo cambia al registrarse por primera vez o al resetear con un archivo nuevo
    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) { updateDimensions(map); }
    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) { updateDimensions(map); }
    @Override

    //no afecta a las dimensiones del mapa, no hacemos nada
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) { }
    @Override
    public void onAdvance(double time, MapInfo map, List<AnimalInfo> animals, double dt) { }
    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) { }


    
}
