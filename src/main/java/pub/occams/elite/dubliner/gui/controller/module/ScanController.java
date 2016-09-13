package pub.occams.elite.dubliner.gui.controller.module;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.domain.geometry.Rectangle;
import pub.occams.elite.dubliner.domain.powerplay.*;
import pub.occams.elite.dubliner.gui.control.OcrDataTextField;
import pub.occams.elite.dubliner.gui.controller.Controller;
import pub.occams.elite.dubliner.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import static javafx.embed.swing.SwingFXUtils.toFXImage;
import static pub.occams.elite.dubliner.util.ImageUtil.matToBufferedImage;

public class ScanController extends Controller<AnchorPane> {

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

    /* Preparation tab */
    @FXML
    private ListView<PreparationSystem> preparationSystemList;

    @FXML
    private OcrDataTextField preparationPowerText;
    @FXML
    private ImageView preparationPowerImage;
    @FXML
    private OcrDataTextField preparationNameText;
    @FXML
    private ImageView preparationNameImage;
    @FXML
    private OcrDataTextField preparationCcToSpendThisCycleText;
    @FXML
    private ImageView preparationCcToSpendThisCycleImage;
    @FXML
    private OcrDataTextField preparationPrepText;
    @FXML
    private ImageView preparationPrepImage;
    @FXML
    private OcrDataTextField preparationCostText;
    @FXML
    private ImageView preparationCostImage;
    @FXML
    private OcrDataTextField preparationHighestContributionText;
    @FXML
    private OcrDataTextField preparationHighestContributingPowerText;
    @FXML
    private ImageView preparationContributionImage;

    /* Expansion tab */
    @FXML
    private ListView<ExpansionSystem> expansionSystemList;
    @FXML
    private OcrDataTextField expansionPowerText;
    @FXML
    private ImageView expansionPowerImage;

    @FXML
    private OcrDataTextField expansionNameText;
    @FXML
    private ImageView expansionNameImage;
    @FXML
    private OcrDataTextField expansionPotentialValueText;

    @FXML
    private ImageView expansionPotentialValueImage;
    @FXML
    private OcrDataTextField expansionTotalText;

    @FXML
    private OcrDataTextField expansionTriggerText;
    @FXML
    private ImageView expansionImage;
    @FXML
    private OcrDataTextField oppositionTotalText;
    @FXML
    private OcrDataTextField oppositionTriggerText;
    @FXML
    private ImageView oppositionImage;

    /* Control tab */
    @FXML
    private ListView<ControlSystem> controlSystemList;

    @FXML
    private OcrDataTextField controlPowerText;
    @FXML
    private ImageView controlPowerImage;

    @FXML
    private OcrDataTextField controlNameText;
    @FXML
    private ImageView controlNameImage;

    @FXML
    private OcrDataTextField controlUpkeepFromLastCycleText;
    @FXML
    private OcrDataTextField controlDefaultUpkeepCostText;
    @FXML
    private OcrDataTextField controlFortifiedCostText;
    @FXML
    private OcrDataTextField controlUnderminedCostText;
    @FXML
    private OcrDataTextField controlBaseIncomeText;
    @FXML
    private ImageView controlCostsImage;

    @FXML
    private OcrDataTextField controlFortifyTotalText;
    @FXML
    private OcrDataTextField controlFortifyTriggerText;
    @FXML
    private ImageView controlFortifyImage;

    @FXML
    private OcrDataTextField controlUndermineTotalText;
    @FXML
    private OcrDataTextField controlUndermineTriggerText;
    @FXML
    private ImageView controlUndermineImage;

    private final ObservableList<Power> powerData = FXCollections.observableArrayList();
    private final ObservableList<PreparationSystem> preparationData = FXCollections.observableArrayList();
    private final ObservableList<ExpansionSystem> expansionData = FXCollections.observableArrayList();
    private final ObservableList<ControlSystem> controlData = FXCollections.observableArrayList();

    private final ObjectProperty<File> imageDir = new SimpleObjectProperty<>(null);

    private ImageApi imageApi;

    private Task<PowerPlayReport> ocrTask;

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

        preparationSystemList.setItems(preparationData);
        preparationSystemList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, preparationSystem) -> {
                    if (null == preparationSystem) {
                        resetPreparationDetails();
                    } else {
                        setPreparationDetails(preparationSystem);
                    }
                }
        );

        expansionSystemList.setItems(expansionData);
        expansionSystemList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, expansionSystem) -> {
                    if (null == expansionSystem) {
                        resetExpansionDetails();
                    } else {
                        setExpansionDetails(expansionSystem);
                    }
                }
        );

        controlSystemList.setItems(controlData);
        controlSystemList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, controlSystem) -> {
                    if (null == controlSystem) {
                        resetControlDetails();
                    } else {
                        setControlDetails(controlSystem);
                    }
                }
        );

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
    private void start(ActionEvent actionEvent) {
        if (null == imageDir.get()) {
            return;
        }

        if (null != ocrTask && ocrTask.isRunning()) {
            new Alert(Alert.AlertType.ERROR, "A scan task is already running").show();
            return;
        }

        resetPreparationDetails();
        resetExpansionDetails();
        resetControlDetails();


        powerData.clear();
        preparationData.clear();
        expansionData.clear();
        controlData.clear();

        startScan();
    }

    @FXML
    private void copyPreparationCSV(ActionEvent actionEvent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Power,Name,CCtoSpendThisCycle,HighestContributingPower,HighestContributingPowerAmount,Cost,Prep," +
                "InputFile");
        preparationData.forEach(
                s -> sb
                        .append("\"").append(s.classifiedImage.power.data).append("\"")
                        .append("\"").append(s.systemName).append("\",")
                        .append("\"").append(s.ccToSpendThisCycle).append("\",")
                        .append("\"").append(s.highestContributingPower).append("\",")
                        .append("\"").append(s.highestContributingPowerAmount).append("\",")
                        .append("\"").append(s.cost).append("\",")
                        .append("\"").append(s.prep).append("\",")
                        .append("\"").append(s.classifiedImage.inputImage.getFile().getAbsolutePath()).append("\"\n")
        );
        final ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    private void copyExpansionCSV(ActionEvent actionEvent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Power,Name,PotentialValue,ExpansionTotal,ExpansionTrigger,OppositionTotal,OppositionTrigger, " +
                "InputFile");
        expansionData.forEach(
                s -> sb
                        .append("\"").append(s.classifiedImage.power.data).append("\"")
                        .append("\"").append(s.systemName).append("\",")
                        .append("\"").append(s.potentialValue).append("\",")
                        .append("\"").append(s.expansionTotal).append("\",")
                        .append("\"").append(s.expansionTrigger).append("\",")
                        .append("\"").append(s.oppositionTotal).append("\",")
                        .append("\"").append(s.oppositionTrigger).append("\",")
                        .append("\"").append(s.classifiedImage.inputImage.getFile().getAbsolutePath()).append("\"\n")
        );
        final ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    private void copyControlCSV(final ActionEvent actionEvent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Power,Name,UpkeepFromLastCycle,DefaultUpkeepCost, CostIfFortified,CostIfUndermined,BaseIncome," +
                "FortifyTotal,FortifyTrigger," +
                "UndermineTotal,UndermineTrigger,InputFile\n");
        controlData.forEach(
                s -> sb
                        .append("\"").append(s.classifiedImage.power.data).append("\"")
                        .append("\"").append(s.systemName).append("\",")
                        .append("\"").append(s.upkeepFromLastCycle).append("\",")
                        .append("\"").append(s.defaultUpkeepCost).append("\",")
                        .append("\"").append(s.costIfFortified).append("\",")
                        .append("\"").append(s.costIfUndermined).append("\",")
                        .append("\"").append(s.baseIncome).append("\",")
                        .append("\"").append(s.fortifyTotal).append("\",")
                        .append("\"").append(s.fortifyTrigger).append("\",")
                        .append("\"").append(s.undermineTotal).append("\",")
                        .append("\"").append(s.undermineTrigger).append("\",")
                        .append("\"").append(s.classifiedImage.inputImage.getFile().getAbsolutePath()).append("\"\n")
        );
        final ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void showOriginalImage(final SystemBase system) {
        if (null == system) {
            return;
        }

        final BufferedImage originalImage = matToBufferedImage(system.classifiedImage.inputImage.getOnDemandImage());
        if (null == originalImage) {
            new Alert(Alert.AlertType.ERROR, "Could not find image").showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(new ImageView(toFXImage(originalImage, null)));
        alert.setHeaderText(system.classifiedImage.inputImage.getFile().getAbsolutePath());
        alert.show();
    }

    @FXML
    private void showOriginalPreparationImage(ActionEvent actionEvent) {
        showOriginalImage(preparationSystemList.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void showOriginalExpansionImage(ActionEvent actionEvent) {
        showOriginalImage(expansionSystemList.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void showOriginalControlImage(ActionEvent actionEvent) {
        showOriginalImage(controlSystemList.getSelectionModel().getSelectedItem());
    }

    private void startScan() {
        ocrTask = new Task<PowerPlayReport>() {
            @Override
            protected PowerPlayReport call() throws Exception {
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
                    final PowerPlayReport report = ocrTask.getValue();
                    preparationData.setAll(report.preparation);
                    expansionData.setAll(report.expansion);
                    controlData.setAll(report.control);
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


    private String getOrEmpty(final Supplier<String> func) {
        try {
            return func.get();
        } catch (final NullPointerException e) {
        }
        return "";
    }

    private WritableImage cropOrNull(final Supplier<Rectangle> func, final Mat image) {
        final Rectangle rect;
        try {
            rect = func.get();
        } catch (final NullPointerException e) {
            return null;
        }

        final Optional<Mat> maybeCropped = ImageUtil.crop(rect, image);
        if (!maybeCropped.isPresent()) {
            return null;
        }

        return toFXImage(matToBufferedImage(maybeCropped.get()), null);
    }

    private void resetPreparationDetails() {

        preparationPowerText.setText(null, null);
        preparationPowerImage.setImage(null);

        preparationNameText.setText(null, null);
        preparationNameImage.setImage(null);

        preparationCcToSpendThisCycleText.setText(null, null);
        preparationCcToSpendThisCycleImage.setImage(null);


        preparationPrepText.setText(null, null);
        preparationPrepImage.setImage(null);

        preparationCostText.setText(null, null);
        preparationCostImage.setImage(null);

        preparationHighestContributingPowerText.setText(null, null);
        preparationHighestContributionText.setText(null);

        preparationCostText.setText(null, null);
        preparationContributionImage.setImage(null);
    }

    private void setPreparationDetails(final PreparationSystem s) {

        preparationPowerText.setText(
                getOrEmpty(() -> s.classifiedImage.power.data.name),
                getOrEmpty(() -> s.classifiedImage.power.rawData)
        );

        preparationNameText.setText(
                s.systemName,
                getOrEmpty(() -> s.systemNameRectangle.rawData)
        );

        preparationCcToSpendThisCycleText.setText(
                String.valueOf(s.ccToSpendThisCycle),
                getOrEmpty(() -> s.ccToSpendThisCycleRectangle.rawData)
        );

        preparationPrepText.setText(
                String.valueOf(s.prep),
                getOrEmpty(() -> s.prepRectangle.rawData)
        );

        preparationCostText.setText(
                String.valueOf(s.cost),
                getOrEmpty(() -> s.costRectangle.rawData)
        );

        preparationHighestContributingPowerText.setText(
                s.highestContributingPower.name,
                getOrEmpty(() -> s.preparationHighestContributionRectangle.rawData)
        );

        preparationHighestContributionText.setText(
                String.valueOf(s.highestContributingPowerAmount),
                getOrEmpty(() -> s.preparationHighestContributionRectangle.rawData)
        );

        final Mat originalImage = s.classifiedImage.inputImage.getOnDemandImage();

        if (null == originalImage) {
            return;
        }

        preparationPowerImage.setImage(
                cropOrNull(() -> s.classifiedImage.power.rectangle, originalImage)
        );

        preparationNameImage.setImage(
                cropOrNull(() -> s.systemNameRectangle.rectangle, originalImage)
        );

        preparationCcToSpendThisCycleImage.setImage(
                cropOrNull(() -> s.ccToSpendThisCycleRectangle.rectangle, originalImage)
        );

        preparationPrepImage.setImage(
                cropOrNull(() -> s.prepRectangle.rectangle, originalImage)
        );

        preparationCostImage.setImage(
                cropOrNull(() -> s.costRectangle.rectangle, originalImage)
        );

        preparationContributionImage.setImage(
                cropOrNull(() -> s.preparationHighestContributionRectangle.rectangle, originalImage)
        );
    }

    private void resetExpansionDetails() {
        expansionPowerText.setText(null, null);
        expansionPowerImage.setImage(null);

        expansionNameText.setText(null, null);
        expansionNameImage.setImage(null);

        expansionPotentialValueText.setText(null, null);
        expansionPotentialValueImage.setImage(null);

        expansionTotalText.setText(null, null);
        expansionTriggerText.setText(null, null);
        expansionImage.setImage(null);

        oppositionTotalText.setText(null, null);
        oppositionTriggerText.setText(null, null);
        oppositionImage.setImage(null);
    }

    private void setExpansionDetails(final ExpansionSystem s) {
        expansionPowerText.setText(
                getOrEmpty(() -> s.classifiedImage.power.data.name),
                getOrEmpty(() -> s.classifiedImage.power.rawData)
        );

        expansionNameText.setText(
                s.systemName,
                getOrEmpty(() -> s.systemNameRectangle.rawData)
        );

        expansionPotentialValueText.setText(
                String.valueOf(s.potentialValue),
                getOrEmpty(() -> s.potentialValueRectangle.rawData)
        );

        expansionTotalText.setText(
                String.valueOf(s.expansionTotal),
                getOrEmpty(() -> s.expansionRectangle.rawData)
        );
        expansionTriggerText.setText(
                String.valueOf(s.expansionTrigger),
                getOrEmpty(() -> s.expansionRectangle.rawData)
        );

        oppositionTotalText.setText(
                String.valueOf(s.oppositionTotal),
                getOrEmpty(() -> s.oppositionRectangle.rawData)
        );
        oppositionTriggerText.setText(
                String.valueOf(s.oppositionTrigger),
                getOrEmpty(() -> s.oppositionRectangle.rawData)
        );

        final Mat originalImage = s.classifiedImage.inputImage.getOnDemandImage();

        if (null == originalImage) {
            return;
        }

        expansionPowerImage.setImage(cropOrNull(() -> s.classifiedImage.power.rectangle, originalImage));
        expansionNameImage.setImage(cropOrNull(() -> s.systemNameRectangle.rectangle, originalImage));
        expansionPotentialValueImage.setImage(cropOrNull(() -> s.potentialValueRectangle.rectangle, originalImage));
        expansionImage.setImage(cropOrNull(() -> s.expansionRectangle.rectangle, originalImage));
        oppositionImage.setImage(cropOrNull(() -> s.oppositionRectangle.rectangle, originalImage));
    }

    private void resetControlDetails() {

        controlPowerText.setText(null, null);
        controlPowerImage.setImage(null);

        controlNameText.setText(null, null);
        controlNameImage.setImage(null);

        controlUpkeepFromLastCycleText.setText(null, null);
        controlDefaultUpkeepCostText.setText(null, null);
        controlBaseIncomeText.setText(null, null);
        controlFortifiedCostText.setText(null, null);
        controlUnderminedCostText.setText(null, null);
        controlCostsImage.setImage(null);

        controlFortifyTotalText.setText(null, null);
        controlFortifyTriggerText.setText(null, null);
        controlFortifyImage.setImage(null);

        controlUndermineTotalText.setText(null, null);
        controlUndermineTriggerText.setText(null, null);
        controlUndermineImage.setImage(null);
    }

    private void setControlDetails(final ControlSystem s) {

        controlPowerText.setText(
                getOrEmpty(() -> s.classifiedImage.power.data.name),
                getOrEmpty(() -> s.classifiedImage.power.rawData)
        );

        controlNameText.setText(
                s.systemName,
                getOrEmpty(() -> s.systemNameRectangle.rawData)
        );

        controlUpkeepFromLastCycleText.setText(
                String.valueOf(s.upkeepFromLastCycle),
                getOrEmpty(() -> s.costsRectangle.rawData)
        );
        controlDefaultUpkeepCostText.setText(
                String.valueOf(s.defaultUpkeepCost),
                getOrEmpty(() -> s.costsRectangle.rawData)
        );
        controlFortifiedCostText.setText(
                String.valueOf(s.costIfFortified),
                getOrEmpty(() -> s.costsRectangle.rawData)
        );
        controlUnderminedCostText.setText(
                String.valueOf(s.costIfUndermined),
                getOrEmpty(() -> s.costsRectangle.rawData)
        );
        controlBaseIncomeText.setText(
                String.valueOf(s.baseIncome),
                getOrEmpty(() -> s.costsRectangle.rawData)
        );

        controlFortifyTotalText.setText(
                String.valueOf(s.fortifyTotal),
                getOrEmpty(() -> s.fortifyRectangle.rawData)
        );
        controlFortifyTriggerText.setText(
                String.valueOf(s.fortifyTrigger),
                getOrEmpty(() -> s.fortifyRectangle.rawData)
        );

        controlUndermineTotalText.setText(
                String.valueOf(s.undermineTotal),
                getOrEmpty(() -> s.undermineRectangle.rawData)
        );
        controlUndermineTriggerText.setText(
                String.valueOf(s.undermineTrigger),
                getOrEmpty(() -> s.undermineRectangle.rawData)
        );

        final Mat originalImage = s.classifiedImage.inputImage.getOnDemandImage();

        if (null == originalImage) {
            return;
        }

        controlPowerImage.setImage(cropOrNull(() -> s.classifiedImage.power.rectangle, originalImage));
        controlNameImage.setImage(cropOrNull(() -> s.systemNameRectangle.rectangle, originalImage));
        controlCostsImage.setImage(cropOrNull(() -> s.costsRectangle.rectangle, originalImage));
        controlFortifyImage.setImage(cropOrNull(() -> s.fortifyRectangle.rectangle, originalImage));
        controlUndermineImage.setImage(cropOrNull(() -> s.undermineRectangle.rectangle, originalImage));
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
