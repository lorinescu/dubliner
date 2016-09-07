package pub.occams.elite.dubliner.gui.controller.module;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.domain.Power;
import pub.occams.elite.dubliner.dto.ocr.ReportDto;
import pub.occams.elite.dubliner.gui.controller.Controller;
import pub.occams.elite.dubliner.gui.model.ControlModel;
import pub.occams.elite.dubliner.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static javafx.embed.swing.SwingFXUtils.toFXImage;

public class ScanController extends Controller<AnchorPane> {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.LOGGER_NAME);

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
    private ImageView nameImage;

    @FXML
    private TextField upkeepFromLastCycleText;
    @FXML
    private TextField defaultUpkeepCost;
    @FXML
    private TextField fortifiedCostText;
    @FXML
    private TextField underminedCostText;
    @FXML
    private TextField baseIncomeText;
    @FXML
    private ImageView costsImage;

    @FXML
    private TextField fortifyTotalText;
    @FXML
    private TextField fortifyTriggerText;
    @FXML
    private ImageView fortifyImage;

    @FXML
    private TextField undermineTotalText;
    @FXML
    private TextField undermineTriggerText;
    @FXML
    private ImageView undermineImage;

    private final Map<Power, List<ControlModel>> report = new HashMap<>();

    private final ObservableList<Power> powerData = FXCollections.observableArrayList();
    private final ObservableList<ControlModel> controlData = FXCollections.observableArrayList();

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

        powersCombo.setItems(powerData);
        powersCombo.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (null == newValue) {
                        return;
                    }
                    controlData.setAll(report.get(newValue));
                }
        );

        controlSystemList.setItems(controlData);
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

        report.clear();
        powerData.clear();
        powersCombo.setPromptText(null);
        controlData.clear();

        progressIndicator.setVisible(true);

        startScan();
    }


    @FXML
    private void copyCSV(final ActionEvent actionEvent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Name,UpkeepFromLastCycle,DefaultUpkeepCost, CostIfFortified,CostIfUndermined,BaseIncome," +
                "FortifyTotal,FortifyTrigger," +
                "UndermineTotal,UndermineTrigger,InputFile\n");
        controlData.forEach(
                cs -> sb
                        .append("\"").append(cs.systemName).append("\",")
                        .append("\"").append(cs.upkeepFromLastCycle).append("\",")
                        .append("\"").append(cs.defaultUpkeepCost).append("\",")
                        .append("\"").append(cs.costIfFortified).append("\",")
                        .append("\"").append(cs.costIfUndermined).append("\",")
                        .append("\"").append(cs.baseIncome).append("\",")
                        .append("\"").append(cs.fortifyTotal).append("\",")
                        .append("\"").append(cs.fortifyTrigger).append("\",")
                        .append("\"").append(cs.undermineTotal).append("\",")
                        .append("\"").append(cs.undermineTrigger).append(",")
                        .append("\"").append(cs.classifiedImage.getInputImage().getFile().getAbsolutePath()).append("\"\n")
        );
        final ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);

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
                    dto.powers.forEach(
                            (power, powerReportDto) -> {
                                report.put(
                                        power,
                                        powerReportDto.control
                                                .stream()
                                                .map(x -> ControlModel.fromDto(power, x))
                                                .collect(Collectors.toList())
                                );
                                powerData.add(power);
                            }
                    );
                    if (dto.powers.size() > 0) {
                        powersCombo.setPromptText("Powers found");
                    }
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
        nameText.setText(m.systemName);
        if (null != m.systemNameRectangle) {
            nameImage.setImage(toFXImage(ImageUtil.crop(m.systemNameRectangle.getRectangle(), m.classifiedImage
                    .getInputImage().getImage()).get(), null));
        } else {
            nameImage.setImage(null);
        }
        upkeepFromLastCycleText.setText(String.valueOf(m.upkeepFromLastCycle));
        defaultUpkeepCost.setText(String.valueOf(m.defaultUpkeepCost));
        fortifiedCostText.setText(String.valueOf(m.costIfFortified));
        underminedCostText.setText(String.valueOf(m.costIfUndermined));
        baseIncomeText.setText(String.valueOf(m.baseIncome));

        if (null != m.costsRectangle) {
            costsImage.setImage(toFXImage(ImageUtil.crop(m.costsRectangle.getRectangle(), m.classifiedImage
                    .getInputImage().getImage()).get(), null));
        } else {
            costsImage.setImage(null);
        }

        fortifyTotalText.setText(String.valueOf(m.fortifyTotal));
        fortifyTriggerText.setText(String.valueOf(m.fortifyTrigger));

        if (null != m.fortifyRectangle) {
            fortifyImage.setImage(toFXImage(ImageUtil.crop(m.fortifyRectangle.getRectangle(), m.classifiedImage
                    .getInputImage().getImage()).get(), null));
        } else {
            fortifyImage.setImage(null);
        }

        undermineTotalText.setText(String.valueOf(m.undermineTotal));
        undermineTriggerText.setText(String.valueOf(m.undermineTrigger));
        if (null != m.undermineRectangle) {
            undermineImage.setImage(toFXImage(ImageUtil.crop(m.undermineRectangle.getRectangle(), m.classifiedImage
                    .getInputImage().getImage()).get(), null));
        } else {
            undermineImage.setImage(null);
        }
    }

    private void resetDetails() {

        nameText.setText(null);
        nameImage.setImage(null);

        upkeepFromLastCycleText.setText(null);
        defaultUpkeepCost.setText(null);
        baseIncomeText.setText(null);
        fortifiedCostText.setText(null);
        underminedCostText.setText(null);
        costsImage.setImage(null);

        fortifyTotalText.setText(null);
        fortifyTriggerText.setText(null);
        fortifyImage.setImage(null);

        undermineTotalText.setText(null);
        undermineTriggerText.setText(null);
        undermineImage.setImage(null);
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

    public void postConstruct(final ImageApi imageApi) {
        this.imageApi = imageApi;
    }

}
