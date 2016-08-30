package pub.occams.elite.dubliner.gui.controller.module;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.gui.controller.Controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import static javafx.embed.swing.SwingFXUtils.toFXImage;

public class ScanController extends Controller<AnchorPane> {

    private static final Logger LOGGER = Logger.getLogger(App.LOGGER_NAME);

    //FIXME: on windows the app has to run as admin for this key to be created
    private static final Preferences prefs = Preferences.userRoot().node("pub/occams/elite/dubliner/prefs");
    private static final String LAST_DIR = "LAST_DIR";

    @FXML
    private TextField screenshotDirectoryField;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private ListView<ControlSystem> controlSystemList;
    @FXML
    private TextField nameText;
    @FXML
    private TextField upkeepFromLastCycleText;
    @FXML
    private TextField fortifiedCostText;
    @FXML
    private TextField underminedCostText;
    @FXML
    private TextField baseIncomeText;
    @FXML
    private TextField fortifyTotalText;
    @FXML
    private TextField fortifyTriggerText;
    @FXML
    private TextField undermineTotalText;
    @FXML
    private TextField undermineTriggerText;
    @FXML
    private ImageView nameImage;
    @FXML
    private ImageView upkeepFromLastCycleImage;
    @FXML
    private ImageView fortifiedCostImage;
    @FXML
    private ImageView underminedCostImage;
    @FXML
    private ImageView baseIncomeImage;
    @FXML
    private ImageView fortifyTotalImage;
    @FXML
    private ImageView fortifyTriggerImage;
    @FXML
    private ImageView undermineTotalImage;
    @FXML
    private ImageView undermineTriggerImage;

    private final ObservableList<ControlSystem> controlSystems = FXCollections.observableArrayList();

    private final ObjectProperty<File> imageDir = new SimpleObjectProperty<>(null);
    private ImageApi imageApi;

    private Task<List<ControlSystem>> ocrTask;

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

        controlSystemList.setItems(controlSystems);
        controlSystemList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, controlSystem) -> {
                    if (null == controlSystem) {
                        resetDetails();
                    } else {
                        setDetails(controlSystem);
                    }
                }
        );
        progressIndicator.setVisible(false);
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
    private void start(ActionEvent actionEvent) {
        if (null == imageDir.get()) {
            return;
        }

        if (null != ocrTask && ocrTask.isRunning()) {
            new Alert(Alert.AlertType.ERROR, "A scan task is already running").show();
            return;
        }

        resetDetails();
        controlSystems.clear();
        progressIndicator.setVisible(true);

        startScan();
    }


    @FXML
    private void copyCSV(ActionEvent actionEvent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Name,UpkeepFromLastCycle,CostIfFortified,CostIfUndermined,BaseIncome,FortifyTotal,FortifyTrigger," +
                "UndermineTotal,UndermineTrigger\n");
        controlSystems
                .stream()
                .forEach(
                        cs -> sb
                                .append("\"").append(cs.getSystemName()).append("\",")
                                .append("\"").append(cs.getUpkeepFromLastCycle()).append("\",")
                                .append("\"").append(cs.getCostIfFortified()).append("\",")
                                .append("\"").append(cs.getCostIfUndermined()).append("\",")
                                .append("\"").append(cs.getBaseIncome()).append("\",")
                                .append("\"").append(cs.getFortifyTotal()).append("\",")
                                .append("\"").append(cs.getFortifyTrigger()).append("\",")
                                .append("\"").append(cs.getUnderminingTotal()).append("\",")
                                .append("\"").append(cs.getUnderminingTrigger()).append("\"\n")
                );
        final ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);

    }

    @FXML
    private void copyJSON(ActionEvent actionEvent) {
    }

    @FXML
    private void showOriginalImage(ActionEvent actionEvent) {
        final ControlSystem system = controlSystemList.getSelectionModel().getSelectedItem();
        if (null == system) {
            return;
        }

        final BufferedImage originalImage = system.getControlSystemRectangles().getInput().getInputImage().getImage();
        if (null == originalImage) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(new ImageView(toFXImage(originalImage, null)));
        alert.setHeaderText(system.getControlSystemRectangles().getInput().getInputImage().getFile().getAbsolutePath());
        alert.show();
    }

    private void startScan() {
        ocrTask = new Task<List<ControlSystem>>() {
            @Override
            protected List<ControlSystem> call() throws Exception {
                return imageApi.extractDataFromImages(
                        getUnprocessedFilesFromDir(imageDir.get())
                );
            }
        };
        ocrTask.setOnSucceeded(
                event -> {
                    controlSystems.setAll(this.ocrTask.getValue());
                    FXCollections.sort(
                            controlSystems,
                            (o1, o2) -> o1.getSystemName().compareTo(o2.getSystemName())
                    );
                    progressIndicator.setVisible(false);
                }
        );
        ocrTask.exceptionProperty().addListener(
                (observable, oldValue, ex) -> {
                    progressIndicator.setVisible(false);
                    LOGGER.error("Error in ocrTask", ex);
                }
        );
        ocrTask.setOnFailed(
                event -> {
                    progressIndicator.setVisible(false);
                    LOGGER.error("OcrTask failed");
                }
        );

        new Thread(this.ocrTask).start();
    }
    private void setDetails(final ControlSystem cs) {
        nameText.setText(cs.getSystemName());
        nameImage.setImage(toFXImage(cs.getControlSystemRectangles().getSystemName(), null));

        upkeepFromLastCycleText.setText(String.valueOf(cs.getUpkeepFromLastCycle()));
        upkeepFromLastCycleImage.setImage(toFXImage(cs.getControlSystemRectangles().getUpkeepFromLastCycle(), null));

        fortifiedCostText.setText(String.valueOf(cs.getCostIfFortified()));
        fortifiedCostImage.setImage(toFXImage(cs.getControlSystemRectangles().getCostIfFortified(), null));

        underminedCostText.setText(String.valueOf(cs.getCostIfUndermined()));
        underminedCostImage.setImage(toFXImage(cs.getControlSystemRectangles().getCostIfUndermined(), null));

        baseIncomeText.setText(String.valueOf(cs.getBaseIncome()));
        baseIncomeImage.setImage(toFXImage(cs.getControlSystemRectangles().getBaseIncome(), null));

        fortifyTotalText.setText(String.valueOf(cs.getFortifyTotal()));
        fortifyTotalImage.setImage(toFXImage(cs.getControlSystemRectangles().getFortificationTotal(), null));

        fortifyTriggerText.setText(String.valueOf(cs.getFortifyTrigger()));
        fortifyTriggerImage.setImage(toFXImage(cs.getControlSystemRectangles().getFortificationTrigger(), null));

        undermineTotalText.setText(String.valueOf(cs.getUnderminingTotal()));
        undermineTotalImage.setImage(toFXImage(cs.getControlSystemRectangles().getUnderminingTotal(), null));

        undermineTriggerText.setText(String.valueOf(cs.getUnderminingTrigger()));
        undermineTriggerImage.setImage(toFXImage(cs.getControlSystemRectangles().getUnderminingTrigger(), null));
    }

    private void resetDetails() {
        nameText.setText(null);
        nameImage.setImage(null);

        upkeepFromLastCycleText.setText(null);
        upkeepFromLastCycleImage.setImage(null);

        baseIncomeText.setText(null);
        baseIncomeImage.setImage(null);

        fortifiedCostText.setText(null);
        fortifiedCostImage.setImage(null);

        underminedCostText.setText(null);
        underminedCostImage.setImage(null);

        fortifyTotalText.setText(null);
        fortifyTotalImage.setImage(null);

        fortifyTriggerText.setText(null);
        fortifyTriggerImage.setImage(null);

        undermineTotalText.setText(null);
        undermineTotalImage.setImage(null);

        undermineTriggerText.setText(null);
        undermineTriggerImage.setImage(null);
    }

    private List<File> getUnprocessedFilesFromDir(final File dataDir) {

        if (null != dataDir && dataDir.isDirectory()) {
            final File[] files = dataDir.listFiles((FileFilter) new SuffixFileFilter("bmp"));
            if (null != files) {
                return Arrays.asList(files);
            }
        }

        return new ArrayList<>();
    }

    public void postConstruct(final ImageApi imageApi) {
        this.imageApi = imageApi;
    }
}
