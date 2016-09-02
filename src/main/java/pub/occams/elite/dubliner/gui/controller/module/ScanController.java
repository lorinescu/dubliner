package pub.occams.elite.dubliner.gui.controller.module;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.domain.Power;
import pub.occams.elite.dubliner.dto.ocr.ReportDto;
import pub.occams.elite.dubliner.gui.controller.Controller;
import pub.occams.elite.dubliner.gui.model.ControlModel;
import pub.occams.elite.dubliner.gui.model.ReportModel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

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
    private ComboBox<Power> powersCombo;
    @FXML
    private ListView<ControlModel> controlSystemList;
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

    private final ObjectProperty<ReportModel> report = new SimpleObjectProperty<>();

    private final ObjectProperty<File> imageDir = new SimpleObjectProperty<>(null);
    private ImageApi imageApi;

    private Task<ReportDto> ocrTask;

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

        report.addListener(
                (observable, oldValue, newValue) -> {
                    powersCombo.getItems().setAll(newValue.powers.keySet().stream().collect(Collectors.toList()));
                }
        );

        powersCombo.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (null == newValue) {
                        return;
                    }
                    controlSystemList.setItems(report.get().powers.get(newValue).control);
                }
        );

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
        if (null != report.get()) {
            report.set(null);
        }

        powersCombo.getItems().clear();

        controlSystemList.getItems().clear();

        progressIndicator.setVisible(true);

        startScan();
    }


    @FXML
    private void copyCSV(final ActionEvent actionEvent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Name,UpkeepFromLastCycle,CostIfFortified,CostIfUndermined,BaseIncome,FortifyTotal,FortifyTrigger," +
                "UndermineTotal,UndermineTrigger\n");
//        controlSystems.forEach(
//                cs -> sb
//                        .append("\"").append(cs.systemName).append("\",")
//                        .append("\"").append(cs.upkeepFromLastCycle).append("\",")
//                        .append("\"").append(cs.costIfFortified).append("\",")
//                        .append("\"").append(cs.costIfUndermined).append("\",")
//                        .append("\"").append(cs.baseIncome).append("\",")
//                        .append("\"").append(cs.fortifyTotal).append("\",")
//                        .append("\"").append(cs.fortifyTrigger).append("\",")
//                        .append("\"").append(cs.undermineTotal).append("\",")
//                        .append("\"").append(cs.undermineTrigger).append("\"\n")
//        );
        final ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);

    }

    @FXML
    private void copyJSON(ActionEvent actionEvent) {
    }

    @FXML
    private void showOriginalImage(ActionEvent actionEvent) {
        final ControlModel system = controlSystemList.getSelectionModel().getSelectedItem();
        if (null == system) {
            return;
        }

        final BufferedImage originalImage = system.classifiedImage.getInputImage().getImage();
        if (null == originalImage) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(new ImageView(toFXImage(originalImage, null)));
        alert.setHeaderText(system.classifiedImage.getInputImage().getFile().getAbsolutePath());
        alert.show();
    }

    private void startScan() {
        ocrTask = new Task<ReportDto>() {
            @Override
            protected ReportDto call() throws Exception {
                return imageApi.extractDataFromImages(
                        getUnprocessedFilesFromDir(imageDir.get())
                );
            }
        };
        ocrTask.setOnSucceeded(
                event -> {
                    final ReportDto dto = ocrTask.getValue();
                    final ReportModel report = ReportModel.fromDto(dto);
                    this.report.set(report);
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

    private void setDetails(final ControlModel m) {
        nameText.setText(m.systemName.getValue());
//        nameImage.setImage(toFXImage(m.getControlSystemRectangles().getSystemName(), null));

        upkeepFromLastCycleText.setText(String.valueOf(m.upkeepFromLastCycle));
//        upkeepFromLastCycleImage.setImage(toFXImage(m.getControlSystemRectangles().getUpkeepFromLastCycle(), null));

        fortifiedCostText.setText(String.valueOf(m.costIfFortified));
//        fortifiedCostImage.setImage(toFXImage(m.getControlSystemRectangles().getCostIfFortified(), null));

        underminedCostText.setText(String.valueOf(m.costIfUndermined));
//        underminedCostImage.setImage(toFXImage(m.getControlSystemRectangles().getCostIfUndermined(), null));

        baseIncomeText.setText(String.valueOf(m.baseIncome));
//        baseIncomeImage.setImage(toFXImage(m.getControlSystemRectangles().getBaseIncome(), null));

        fortifyTotalText.setText(String.valueOf(m.fortifyTotal));
//        fortifyTotalImage.setImage(toFXImage(m.getControlSystemRectangles().getFortificationTotal(), null));

        fortifyTriggerText.setText(String.valueOf(m.fortifyTrigger));
//        fortifyTriggerImage.setImage(toFXImage(m.getControlSystemRectangles().getFortificationTrigger(), null));

        undermineTotalText.setText(String.valueOf(m.undermineTotal));
//        undermineTotalImage.setImage(toFXImage(m.getControlSystemRectangles().getUnderminingTotal(), null));

        undermineTriggerText.setText(String.valueOf(m.undermineTrigger));
//        undermineTriggerImage.setImage(toFXImage(m.getControlSystemRectangles().getUnderminingTrigger(), null));
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
