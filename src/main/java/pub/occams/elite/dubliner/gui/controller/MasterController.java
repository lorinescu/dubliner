package pub.occams.elite.dubliner.gui.controller;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.domain.powerplay.OcrResult;
import pub.occams.elite.dubliner.gui.controller.module.PowerPlayController;
import pub.occams.elite.dubliner.gui.controller.module.SystemReportsController;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.prefs.Preferences;

public class MasterController extends Controller<AnchorPane> {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.LOGGER_NAME);

    private static final Preferences prefs = Preferences.userRoot().node("pub/occams/elite/dubliner/prefs");
    private static final String LAST_DIR = "LAST_DIR";
    private static final MediaPlayer MEDIA_PLAYER = new MediaPlayer(new Media(new File("conf/work-complete.wav").toURI().toString()));

    @FXML
    private TextField screenshotDirectoryField;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label currentFileLabel;
    @FXML
    private Tab powerPlayTab;
    @FXML
    private Tab systemReportsTab;

    private PowerPlayController powerPlayController;
    private SystemReportsController systemReportsController;

    private ImageApi imageApi;

    private Task<OcrResult> ocrTask;

    private final ObjectProperty<File> imageDir = new SimpleObjectProperty<>(null);

    @FXML
    private void initialize() {
        imageDir.addListener(
                (observable, oldValue, newDir) -> {
                    if (null == newDir) {
                        return;
                    }
                    screenshotDirectoryField.setText(newDir.getAbsolutePath());
                    prefs.put(LAST_DIR, newDir.getAbsolutePath());
                }
        );
        imageDir.setValue(new File(prefs.get(LAST_DIR, "./")));

        progressIndicator.setProgress(0);
    }

    @FXML
    private void selectScreenshotDirectory(ActionEvent actionEvent) {
        final DirectoryChooser chooser = new DirectoryChooser();
        final File lastDir = new File(prefs.get(LAST_DIR, "."));
        if (lastDir.exists() && lastDir.isDirectory()) {
            chooser.setInitialDirectory(lastDir);
        }

        imageDir.set(chooser.showDialog(getView().getScene().getWindow()));
    }

    @FXML
    private void startScan(ActionEvent actionEvent) {
        if (null == imageDir.get()) {
            return;
        }

        if (null != ocrTask && ocrTask.isRunning()) {
            new Alert(Alert.AlertType.ERROR, "A scan task is already running").show();
            return;
        }

        ocrTask = new Task<OcrResult>() {
            @Override
            protected OcrResult call() throws Exception {
                Platform.runLater(
                        () -> {
                            powerPlayController.resetData();
                            systemReportsController.resetData();
                        }
                );
                final BiConsumer<Double, String> progressCallback =
                        (progress, fileName) ->
                                Platform.runLater(
                                        () -> {
                                            progressIndicator.setProgress(progress);
                                            currentFileLabel.setText(fileName);
                                        }
                                );
                return imageApi.generateReport(
                        getUnprocessedFilesFromDir(imageDir.get()),
                        progressCallback
                );
            }
        };
        ocrTask.setOnSucceeded(
                event -> {
                    final OcrResult report = ocrTask.getValue();
                    powerPlayController.setData(report);
                    systemReportsController.setData(report);
                    progressIndicator.setProgress(1.0);
                    currentFileLabel.setText("---");
                    MEDIA_PLAYER.play();
                }
        );
        ocrTask.exceptionProperty().addListener(
                (observable, oldValue, ex) -> {
                    MEDIA_PLAYER.play();
                    LOGGER.error("Error in ocrTask", ex);
                }
        );
        ocrTask.setOnFailed(
                event -> {
                    MEDIA_PLAYER.play();
                    LOGGER.error("OcrTask failed");
                }
        );

        new Thread(this.ocrTask).start();
    }


    @FXML
    private void close(ActionEvent actionEvent) {
        System.exit(0);
    }

    @FXML
    private void showHelp(final ActionEvent actionEvent) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(null);

        final TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);

        area.setText(
                "conf/ directory can be found in the application's installation directory" +
                        "\n" +
                        "Modify conf/settings.json according to your needs then use http://http://jsonlint.com/ to validate it." +
                        "\n" +
                        "You are probably not interested in the \"ocr\" section unless you want to tune the OCR engine" +
                        "\n" +
                        "In the \"corrections\" section you can add ... corrections. A correction is  used to transform common " +
                        "OCR output errors strings into valid ones. Currently this exquisite software supports " +
                        "power and system names corrections. " +
                        "\n" +
                        "For example, if the OCR extracts system name AL1OTH instead of ALIOTH you can add " +
                        "\n" +
                        "\"AL1OTH\" : \"ALIOTH\" " +
                        "\n" +
                        "The next time AL1OTH is found it will be replaced with ALIOTH. " +
                        "\n" +
                        "Power name corrections work the same way as system names."
        );
        area.setMaxWidth(Double.MAX_VALUE);
        area.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(area);
        alert.getDialogPane().setExpanded(true);

        alert.showAndWait();
    }

    @FXML
    private void about(ActionEvent actionEvent) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(App.NAME + "-" + App.VERSION + "\nhttps://bitbucket.org/lorinescu/dubliner\n(lorin@occams.pub)");
        alert.getDialogPane().setContent(new ImageView("/pub/occams/elite/dubliner/gui/controller/about.jpg"));
        alert.showAndWait();
    }

    private List<File> getUnprocessedFilesFromDir(final File dataDir) {

        if (null != dataDir && dataDir.isDirectory()) {
            final String[] extensions = new String[1];
            extensions[0] = "bmp";
            final Collection<File> files = FileUtils.listFiles(dataDir, extensions, true);
            return new ArrayList<>(files);
        }

        return new ArrayList<>();
    }

    public void postConstruct(final ImageApi imageApi, final PowerPlayController powerPlayController,
                              final SystemReportsController systemReportsController) {
        this.imageApi = imageApi;
        this.powerPlayController = powerPlayController;
        this.systemReportsController = systemReportsController;
        this.powerPlayTab.setContent(powerPlayController.getView());
        this.systemReportsTab.setContent(systemReportsController.getView());
    }

}
