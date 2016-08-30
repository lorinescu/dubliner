package pub.occams.elite.dubliner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.application.ImageApiV1;
import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.gui.controller.Controller;
import pub.occams.elite.dubliner.gui.controller.MasterController;
import pub.occams.elite.dubliner.gui.controller.SettingsController;
import pub.occams.elite.dubliner.gui.controller.module.AreasController;
import pub.occams.elite.dubliner.gui.controller.module.HelpController;
import pub.occams.elite.dubliner.gui.controller.module.ScanController;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class App extends Application {

    public static final String NAME = "The Dubliner";
    public static final String VERSION = "0.0.1";

    //FIXME: uncouple this
    public static final String LOGGER_NAME = "App";

    private static ImageApi imageApi;
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private <T extends Controller> T loadFxml(final Class<?> klass, final String fxml) throws IOException {
        final FXMLLoader loader = new FXMLLoader(klass.getResource(fxml));
        loader.load();
        return loader.getController();
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {

        final SettingsController settingsController = loadFxml(SettingsController.class, "Settings.fxml");
        settingsController.postConstruct(imageApi);

        final ScanController scanController = loadFxml(ScanController.class, "Scan.fxml");
        scanController.postConstruct(imageApi);

        final AreasController areasController = loadFxml(AreasController.class, "Areas.fxml");
        areasController.postContruct(imageApi);

        final HelpController helpController = loadFxml(HelpController.class, "Help.fxml");

        final MasterController masterController = loadFxml(MasterController.class, "Master.fxml");
        masterController.postConstruct(imageApi, settingsController, scanController, areasController, helpController);

        final Scene scene = new Scene(masterController.getView());
        scene.getStylesheets().add(App.class.getResource("gui/style/custom.css").toExternalForm());
        primaryStage.setScene(scene);


        primaryStage.getIcons().add(new Image(MasterController.class.getResourceAsStream("edmund-fist.png")));

        primaryStage.setTitle(NAME + "-" + VERSION);
        primaryStage.show();
    }

    public static SettingsDto loadSettingsV1() throws IOException {
        JSON_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        final File configFile = new File("conf/settings_v1.json");
        return JSON_MAPPER.readValue(configFile, SettingsDto.class);
    }

    public static SettingsDto loadSettingsV2() throws IOException {
        JSON_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        final File configFile = new File("conf/settings_v2.json");
        return JSON_MAPPER.readValue(configFile, SettingsDto.class);
    }

    public static void main(String[] args) throws IOException {

        PropertyConfigurator.configure("conf/log4j.properties");
        Logger.getLogger(LOGGER_NAME).info("Starting " + NAME + "-" + VERSION);

        boolean debug = false;
        boolean cliMode = false;
        File imageFile = null;
        if (args.length != 0) {
            for (final String arg : args) {
                if ("-help".equals(arg)) {
                    System.out.println("Usage: java -jar dubliner.jar [-help] [-debug] [-cli <path/to/image>]");
                    System.exit(0);
                } else if ("-debug".equals(arg)) {
                    debug = true;
                } else if ("-cli".equals(arg)) {
                    cliMode = true;
                } else {
                    imageFile = new File(arg);
                }
            }
        }

        try {
            imageApi = new ImageApiV1(loadSettingsV1(), debug);
        } catch (IOException e) {
            Logger.getLogger(LOGGER_NAME).error("Failed to load settings ", e);
        }

        if (cliMode && null != imageFile) {
            if (!imageFile.exists()) {
                System.out.println("File: "+imageFile.getName()+" does not exist");
                System.exit(1);
            }
            final List<ControlSystem> cs = imageApi.extractDataFromImages(Collections.singletonList(imageFile));
            if (null != cs && cs.size() == 1) {
                System.out.println(cs);
            }
        } else {
            launch(args);
        }
        System.exit(0);
    }
}
