package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.factories.Builder;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DynamicSupplyRegionBuilder;
import simulator.factories.Factory;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SheepBuilder;
import simulator.factories.WolfBuilder;
import simulator.misc.Utils;
import simulator.model.Animal;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.model.Simulator;

public class Main {

  private enum ExecMode {
    BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

    private String tag;
    private String desc;

    private ExecMode(String modeTag, String modeDesc) {
      tag = modeTag;
      desc = modeDesc;
    }

    public String getTag() {
      return tag;
    }

    public String getDesc() {
      return desc;
    }
  }
  
// default values for some parameters
  private final static Double DEFAULT_TIME = 10.0; // in seconds
  private final static Double DEFAULT_DELTA_TIME = 0.03; // in seconds

  // some attributes to stores values corresponding to command-line parameters
  private static Double time = null;
  public static Double deltaTime = null; //publico porque lo va a necesitar la interfaz gráfica para mostrarlo en pantalla
  private static String inFile = null;
  private static String outFile = null;
  private static boolean simpleViewer = false;
  private static ExecMode mode = ExecMode.GUI;

  //factorias (publicas porque las va a necesitar la interfaz gráfica para mostrar la descripción de los objetos en pantalla)
  public static Factory<Animal> animalsFactory;
  public static Factory<Region> regionsFactory;


  private static void parseArgs(String[] args) {

    // define the valid command line options
    Options cmdLineOptions = buildOptions();

    // parse the command line as provided in args
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(cmdLineOptions, args);
      parseHelpOption(line, cmdLineOptions);

      //leemos el modo
      parseModeOption(line);

      //leemos archivo sabiendo el modo
      parseInFileOption(line);

      parseOutFileOption(line);
      parseTimeOption(line);
      parseDeltaTimeOption(line);
      parseSimpleViewerOption(line);

  
      
      String[] remaining = line.getArgs();
      if (remaining.length > 0) {
        String error = "Illegal arguments:";
        for (String o : remaining)
          error += (" " + o);
        throw new ParseException(error);
      }

    } catch (ParseException e) {
      System.err.println(e.getLocalizedMessage());
      System.exit(1);
    }

  }

  private static Options buildOptions() {
    Options cmdLineOptions = new Options();

    // help
    cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

    // input file
    cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("A configuration file (optional in GUI mode).").build());

    // output file
    cmdLineOptions.addOption(Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());

    // steps
    cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg().desc("An real number representing the total simulation time in seconds. Default value: "+ DEFAULT_TIME + ".").build());

    // delta time
    cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg().desc("A double representing actual time, in seconds, per simulation step. Default value: " + DEFAULT_DELTA_TIME + ".").build());

    // simple viewer
    cmdLineOptions.addOption(Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());

    // mode
    cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg().desc("Execution Mode. Possible values: 'batch' (Batch mode), 'gui' (Graphical User Interface mode). Default value: 'gui'.").build());

    return cmdLineOptions;
  }

  private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
    if (line.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
      System.exit(0);
    }
  }

  private static void parseInFileOption(CommandLine line) throws ParseException {
    inFile = line.getOptionValue("i");
    if (inFile == null && mode == ExecMode.BATCH) {
      throw new ParseException("An input file is required in batch mode");
    }
  }

  private static void parseOutFileOption(CommandLine line) throws ParseException {
    outFile = line.getOptionValue("o");
  }

  private static void parseTimeOption(CommandLine line) throws ParseException {
    String t = line.getOptionValue("t", DEFAULT_TIME.toString());
    try {
      time = Double.parseDouble(t);
      assert (time >= 0);
    } catch (Exception e) {
      throw new ParseException("Invalid value for time: " + t);
    }
  }

  private static void parseDeltaTimeOption(CommandLine line) throws ParseException {
    String dt = line.getOptionValue("dt", DEFAULT_DELTA_TIME.toString());
    try {
      deltaTime = Double.parseDouble(dt);
      assert (deltaTime >= 0);
    } catch (Exception e) {
      throw new ParseException("Invalid value for delta-time: " + dt);
    }
  }

  private static void parseSimpleViewerOption(CommandLine line) {
    simpleViewer = line.hasOption("sv");
  }

  private static void parseModeOption(CommandLine line) throws ParseException {
    if (line.hasOption("m")) {
      String m = line.getOptionValue("m");
      if (m.equals("batch")) {
        mode = ExecMode.BATCH;
      } else if (m.equals("gui")) {
        mode = ExecMode.GUI;
      } else {
        throw new ParseException("Invalid execution mode: " + m);
      }
    }
    // Si no han puesto el comando -m, no hacemos nada y se queda con el valor por defecto (ExecMode.GUI) que pusimos en los atributos.
  }

  private static void initFactories() {
    //inicializar factoría de estrategias
    List<Builder<SelectionStrategy>> strategyBuilders = new ArrayList<>();
    strategyBuilders.add(new SelectFirstBuilder());
    strategyBuilders.add(new SelectClosestBuilder());
    strategyBuilders.add(new SelectYoungestBuilder());
    Factory<SelectionStrategy> strategyFactory = new BuilderBasedFactory<>(strategyBuilders);

    //inicializar factoría de regiones
    List<Builder<Region>> regionBuilders = new ArrayList<>();
    regionBuilders.add(new DefaultRegionBuilder());
    regionBuilders.add(new DynamicSupplyRegionBuilder());
    regionsFactory = new BuilderBasedFactory<>(regionBuilders);

    //inicializar factoría de animales
    List<Builder<Animal>> animalBuilders = new ArrayList<>();
    animalBuilders.add(new SheepBuilder(strategyFactory));
    animalBuilders.add(new WolfBuilder(strategyFactory));
    animalsFactory = new BuilderBasedFactory<>(animalBuilders);
  }

  private static JSONObject loadJSONFile(InputStream in) {
    return new JSONObject(new JSONTokener(in));
  }


  private static void start_batch_mode() throws Exception {
    // usamos el método auxiliar para crear el controlador
    Controller ctrl = createController();

    // configurar el archivo de salida
    OutputStream os = (outFile == null) ? System.out : new FileOutputStream(new File(outFile));

    // arrancar la simulación
    ctrl.run(time, deltaTime, simpleViewer, os);

    // cerrar flujo
    if (outFile != null) {
      os.close();
    }
  }

  private static void start_GUI_mode() throws Exception {
    // usamos el método auxiliar
    Controller ctrl = createController();

    // lanzamos la interfaz gráfica
    javax.swing.SwingUtilities.invokeAndWait(() -> new simulator.view.MainWindow(ctrl));
  }

  // MÉTODO AUXILIAR PARA NO REPETIR CÓDIGO EN GUI Y BATCH
  private static Controller createController() throws Exception {
    Simulator sim;
    Controller ctrl;

    if (inFile != null) {
      // Si hay archivo, lo leemos y cargamos los datos
      InputStream is = new FileInputStream(new File(inFile));
      JSONObject jsonInput = loadJSONFile(is);

      int cols = jsonInput.getInt("cols");
      int rows = jsonInput.getInt("rows");
      int width = jsonInput.getInt("width");
      int height = jsonInput.getInt("height");

      sim = new Simulator(cols, rows, width, height, animalsFactory, regionsFactory);
      ctrl = new Controller(sim);
      ctrl.loadData(jsonInput);
      is.close();
    } else {
      // Si no hay archivo (solo posible en GUI), usamos valores por defecto
      sim = new Simulator(15, 20, 800, 600, animalsFactory, regionsFactory);
      ctrl = new Controller(sim);
    }

    return ctrl;
  }

  private static void start(String[] args) throws Exception {
    initFactories();
    parseArgs(args);
    switch (mode) {
      case BATCH:
        start_batch_mode();
        break;
      case GUI:
        start_GUI_mode();
        break;
    }
  }

  public static void main(String[] args) {
    Utils.RAND.setSeed(2147483647l);
    try {
      start(args);
    } catch (Exception e) {
      System.err.println("Something went wrong ...");
      System.err.println();
      e.printStackTrace();
    }
  }
}
