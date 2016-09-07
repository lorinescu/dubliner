package pub.occams.elite.dubliner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.application.ImageApiImpl;
import pub.occams.elite.dubliner.dto.ocr.ReportDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.gui.controller.Controller;
import pub.occams.elite.dubliner.gui.controller.MasterController;
import pub.occams.elite.dubliner.gui.controller.module.HelpController;
import pub.occams.elite.dubliner.gui.controller.module.ScanController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class App extends Application {

    public static final String NAME = "The Dubliner";
    public static final String VERSION = "0.0.2";

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    static {
        System.setProperty("logback.configurationFile", "conf/logback.xml");
    }

    public static final String LOGGER_NAME = "App";

    private static ImageApi imageApi;

    private <T extends Controller> T loadFxml(final Class<?> klass, final String fxml) throws IOException {
        final FXMLLoader loader = new FXMLLoader(klass.getResource(fxml));
        loader.load();
        return loader.getController();
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {

        final ScanController scanController = loadFxml(ScanController.class, "Scan.fxml");
        scanController.postConstruct(imageApi);

        final HelpController helpController = loadFxml(HelpController.class, "Help.fxml");

        final MasterController masterController = loadFxml(MasterController.class, "Master.fxml");
        masterController.postConstruct(scanController, helpController);

        final Scene scene = new Scene(masterController.getView());
        scene.getStylesheets().add(App.class.getResource("gui/style/custom.css").toExternalForm());
        primaryStage.setScene(scene);


        primaryStage.getIcons().add(new Image(MasterController.class.getResourceAsStream("edmund-fist.png")));

        primaryStage.setTitle(NAME + "-" + VERSION);
        primaryStage.show();
    }

    public static SettingsDto loadSettings() throws IOException {
        JSON_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        final File configFile = new File("conf/settings.json");
        return JSON_MAPPER.readValue(configFile, SettingsDto.class);
    }

    public static void main(String[] args) throws IOException {

        LoggerFactory.getLogger(LOGGER_NAME).info("Starting " + NAME + "-" + VERSION);

        boolean debug = false;
        boolean cliMode = false;
        File imageFileOrDir = null;
        if (args.length != 0) {
            for (final String arg : args) {
                if ("-help".equals(arg)) {
                    System.out.println("Usage: java -jar dubliner.jar [-help] [-debug] [-cli <path/to/image or dir>]");
                    System.exit(0);
                } else if ("-debug".equals(arg)) {
                    debug = true;
                } else if ("-cli".equals(arg)) {
                    cliMode = true;
                } else {
                    imageFileOrDir = new File(arg);
                }
            }
        }

        try {
            imageApi = new ImageApiImpl(loadSettings(), debug);
        } catch (IOException e) {
            LoggerFactory.getLogger(LOGGER_NAME).error("Failed to load settings ", e);
        }

        if (cliMode && null != imageFileOrDir) {
            if (!imageFileOrDir.exists()) {
                System.out.println(imageFileOrDir.getName() + " does not exist");
                System.exit(1);
            }
            final List<File> input = new ArrayList<>();
            if (imageFileOrDir.isDirectory()) {
                final String[] extensions = new String[1];
                extensions[0] = "bmp";
                final Collection<File> files = FileUtils.listFiles(imageFileOrDir, extensions, true);
                if (files.size() < 1) {
                    System.out.println("No bmp files found");
                    System.exit(1);
                }
                input.addAll(files);
            } else {
                input.add(imageFileOrDir);
            }
            final ReportDto dto = imageApi.extractDataFromImages(input);
            App.JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValue(System.out, dto);
        } else {
            launch(args);
        }
        System.exit(0);
    }
}
