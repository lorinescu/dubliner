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
import pub.occams.elite.dubliner.application.ImageApiImpl;
import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.gui.controller.Controller;
import pub.occams.elite.dubliner.gui.controller.MasterController;
import pub.occams.elite.dubliner.gui.controller.SettingsController;
import pub.occams.elite.dubliner.gui.controller.module.HelpController;
import pub.occams.elite.dubliner.gui.controller.module.ScanController;
import pub.occams.elite.dubliner.util.ImageUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

        final HelpController helpController = loadFxml(HelpController.class, "Help.fxml");

        final MasterController masterController = loadFxml(MasterController.class, "Master.fxml");
        masterController.postConstruct(imageApi, settingsController, scanController, helpController);

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

        PropertyConfigurator.configure("conf/log4j.properties");
        Logger.getLogger(LOGGER_NAME).info("Starting " + NAME + "-" + VERSION);

        try {
            imageApi = new ImageApiImpl(loadSettings());
        } catch (IOException e) {
            Logger.getLogger(LOGGER_NAME).error("Failed to load settings ", e);
        }

        if (args.length !=0 ) {
            if (args.length != 2 || !"-cli".equals(args[0])) {
                System.out.println("Usage: java -jar dubliner.jar -cli <path/to/image>");
            } else {
                final List<ControlSystem> cs = imageApi.extractControlDataFromImages(Collections.singletonList(new File(args[1])));
                if (null != cs && cs.size() == 1) {
                    System.out.println(cs);
                }
            }
        } else {
            launch(args);
        }
        System.exit(1);
    }
}
