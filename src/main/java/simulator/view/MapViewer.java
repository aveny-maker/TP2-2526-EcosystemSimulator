package simulator.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import simulator.model.Animal;
import simulator.model.AnimalInfo;
import simulator.model.MapInfo;



@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {

	// Anchura/altura de la simulación -- se supone que siempre van a ser
	// iguales al tamaño del componente
	private int width;
	private int height;

	// Número de filas/columnas de la simulación (regiones)
	private int rows;
	private int cols;

	// Anchura/altura de una región
	int rWidth;
	int rHeight;

	// Mostramos sólo animales con este estado. Los posibles valores de currState
	// son null, y los valores de Animal.State.values(). Si es null mostramos todo.
	Animal.State currentState;

	// En estos atributos guardamos la lista de animales y el tiempo que hemos
	// recibido la última vez para dibujarlos.
	volatile private Collection<AnimalInfo> objs;
	volatile private Double time;

	// Una clase auxiliar para almacenar información sobre una especie.
	private static class SpeciesInfo {
		private Integer count;
		private Color color;

		SpeciesInfo(Color color) {
			count = 0;
			this.color = color;
		}
	}

	// Un mapa para la información sobre las especies.
	Map<String, SpeciesInfo> kindsInfo = new HashMap<>();

	// El font que usamos para dibujar texto.
	private Font textFont = new Font("Arial", Font.BOLD, 12);

	// Indica si mostramos el texto la ayuda o no.
	private boolean showHelp;

	public MapViewer() {
		initGUI();
	}

	private void initGUI() {

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyChar()) {
				case 'h':
					showHelp = !showHelp;
					repaint();
					break;
				case 's':
					//cambiar currState de manera circular
					Animal.State[] allStates = Animal.State.values();
					if (currentState == null) {
						currentState = allStates[0];
					} else {
						int nextIndex = currentState.ordinal() + 1;
						if (nextIndex < allStates.length) {
							currentState = allStates[nextIndex];
						} else {
							currentState = null;
						}
					}
					repaint();
					break;
				default:
				}
			}

		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				// Esto es necesario para capturar las teclas cuando el ratón está sobre este componente.
				requestFocus();
			}
		});

		// Por defecto mostramos todos los animales.
		currentState = null;

		// Por defecto mostramos el texto de ayuda.
		showHelp = true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D gr = (Graphics2D) g;
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Cambiar el font para dibujar texto.
		g.setFont(textFont);

		// Dibujar fondo blanco.
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, width, height);

		// Dibujar los animales, el tiempo, información sobre las especies, etc.
		if (objs != null) drawObjects(gr, objs, time);

		if (showHelp) {
			gr.setColor(Color.RED);
			gr.drawString("h: toggle help", 10, 20);
			gr.drawString("s: show animals of a specific state", 10, 35);
		}
	}

	private boolean visible(AnimalInfo a) {
	//Devolver true si es visible según el estado
		return currentState == null || a.getState() == currentState;
	}

	private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {
		//dibujar el grid de regiones
		g.setColor(Color.LIGHT_GRAY);

		//lineas verticales
		for (int i = 0; i <= cols; i++) {
			g.drawLine(i * rWidth, 0, i * rWidth, height);
		}
		//lineas horizontales
		for (int i = 0; i <= rows; i++) {
			g.drawLine(0, i * rHeight, width, i * rHeight);
		}

		//dibujar los animales.
		for (AnimalInfo a : animals) {

			// Si no es visible saltamos la iteración.
			if (!visible(a)) continue;

			// La información sobre la especie de 'a'.
			SpeciesInfo speciesInfo = kindsInfo.get(a.getGeneticCode());

			//crear entrada si es null
			if (speciesInfo == null) {
				speciesInfo = new SpeciesInfo(ViewUtils.getColor(a.getGeneticCode()));
				kindsInfo.put(a.getGeneticCode(), speciesInfo);
			}

			//incrementar contador
			speciesInfo.count++;

			//dibujar el animal según su edad y color
			g.setColor(speciesInfo.color);
			int size = (int) (a.getAge() / 2.0) + 2;
			int x = (int) a.getPosition().getX() - size / 2;
			int y = (int) a.getPosition().getY() - size / 2;
			g.fillOval(x, y, size, size);
		}

		// Establecemos el color para las etiquetas de texto (estado y tiempo)
		g.setColor(Color.MAGENTA);

		//etiqueta del estado visible
		int yPos = showHelp ? 60 : 20; // bajamos el texto si la ayuda esta visible
		if (currentState != null) {
			drawStringWithRect(g, 10, yPos, "State: " + currentState.toString());
			yPos += 20;
		}


		//etiqueta del tiempo
		drawStringWithRect(g, 10, yPos, "Time: " + String.format("%.3f", time));
		yPos += 30; // Dejamos hueco para empezar a pintar las especies


		//dibujar info de especies y resetear contadores
		for (Entry<String, SpeciesInfo> e : kindsInfo.entrySet()) {
			SpeciesInfo info = e.getValue();
			g.setColor(info.color);
			drawStringWithRect(g, 10, yPos, e.getKey() + ": " + info.count);
			yPos += 20;
			
			//resetear contador para el próximo frame
			info.count = 0; 
		}
	}

	// Un método que dibujar un texto con un rectángulo.

	void drawStringWithRect(Graphics2D g, int x, int y, String s) {
		Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
		g.drawString(s, x, y);
		g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
	}

	@Override
	public void update(List<AnimalInfo> objs, Double time) {
		//guardar atributos y repintar
		this.objs = objs;
		this.time = time;
		repaint();
	}

	@Override
	public void reset(double time, MapInfo map, List<AnimalInfo> animals) {// RESUELTO TODO 12: Actualizar atributos de dimensiones
		this.width = map.getWidth();
		this.height = map.getHeight();
		this.cols = map.getCols();
		this.rows = map.getRows();
		this.rWidth = map.getRegionWidth();
		this.rHeight = map.getRegionHeight();

		setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));
		update(animals, time);
	}

}