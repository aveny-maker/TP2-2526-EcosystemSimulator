package simulator.view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class InfoTable extends JPanel {

    //atributos
    private String title;
    private TableModel tableModel;

    //constructor
    public InfoTable(String title, TableModel tableModel) {
        this.title = title;
        this.tableModel = tableModel;
        initGUI();
    }

    private void initGUI() {
        //cambiar el layout del panel a BorderLayout
        this.setLayout(new BorderLayout());

        //añadir un borde con título
        this.setBorder(BorderFactory.createTitledBorder(title));

        //crear la tabla pasándole el modelo de datos
        JTable table = new JTable(tableModel);

        //meter la tabla en un JScrollPane (para que tenga barra de scroll si hay muchos datos) y añadirlo al centro del panel
        this.add(new JScrollPane(table), BorderLayout.CENTER);
    }



}
