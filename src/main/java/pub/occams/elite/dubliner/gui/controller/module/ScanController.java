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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.domain.powerplay.ControlSystem;
import pub.occams.elite.dubliner.domain.powerplay.Power;
import pub.occams.elite.dubliner.domain.powerplay.PowerPlayReport;
import pub.occams.elite.dubliner.domain.powerplay.PreparationSystem;
import pub.occams.elite.dubliner.gui.control.ValidTextField;
import pub.occams.elite.dubliner.gui.controller.Controller;
import pub.occams.elite.dubliner.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import static javafx.embed.swing.SwingFXUtils.toFXImage;
import static pub.occams.elite.dubliner.util.ImageUtil.matToBufferedImage;

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

    /* Control tab */
    @FXML
    private ListView<ControlSystem> controlSystemList;

    @FXML
    private ValidTextField controlPowerText;
    @FXML
    private ImageView controlPowerImage;

    @FXML
    private TextField controlNameText;
    @FXML
    private ImageView controlNameImage;

    @FXML
    private TextField controlUpkeepFromLastCycleText;
    @FXML
    private TextField controlDefaultUpkeepCostText;
    @FXML
    private TextField controlFortifiedCostText;
    @FXML
    private TextField controlUnderminedCostText;
    @FXML
    private TextField controlBaseIncomeText;
    @FXML
    private ImageView controlCostsImage;

    @FXML
    private TextField controlFortifyTotalText;
    @FXML
    private TextField controlFortifyTriggerText;
    @FXML
    private ImageView controlFortifyImage;

    @FXML
    private TextField controlUndermineTotalText;
    @FXML
    private TextField controlUndermineTriggerText;
    @FXML
    private ImageView controlUndermineImage;

    /* Preparation tab */
    @FXML
    private ListView<PreparationSystem> preparationSystemList;

    @FXML
    private ValidTextField preparationPowerText;
    @FXML
    private ImageView preparationPowerImage;
    @FXML
    private ValidTextField preparationNameText;
    @FXML
    private ImageView preparationNameImage;
    @FXML
    private ValidTextField preparationCcToSpendThisCycleText;
    @FXML
    private ImageView preparationCcToSpendThisCycleImage;
    @FXML
    private ValidTextField preparationPrepText;
    @FXML
    private ImageView preparationPrepImage;
    @FXML
    private ValidTextField preparationCostText;
    @FXML
    private ImageView preparationCostImage;
    @FXML
    private ValidTextField preparationHighestContributionText;
    @FXML
    private ValidTextField preparationHighestContributingPowerText;
    @FXML
    private ImageView preparationContributionImage;


    private final ObservableList<Power> powerData = FXCollections.observableArrayList();
    private final ObservableList<ControlSystem> controlData = FXCollections.observableArrayList();
    private final ObservableList<PreparationSystem> preparationData = FXCollections.observableArrayList();

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

        resetControlDetails();
        resetPreparationDetails();


        powerData.clear();
        preparationData.clear();
        controlData.clear();

        progressIndicator.setVisible(true);

        startScan();
    }

    @FXML
    private void copyPreparationCSV(ActionEvent actionEvent) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Power,Name,CCtoSpendThisCycle,HighestContributingPower,HighestContributingPowerAmount,Cost,Prep");
        preparationData.forEach(
                s -> sb
                        .append("\"").append(s.classifiedImage.getPower().getData()).append("\"")
                        .append("\"").append(s.systemName).append("\",")
                        .append("\"").append(s.ccToSpendThisCycle).append("\",")
                        .append("\"").append(s.highestContributingPower).append("\",")
                        .append("\"").append(s.highestContributingPowerAmount).append("\",")
                        .append("\"").append(s.cost).append("\",")
                        .append("\"").append(s.prep).append("\",")
                        .append("\"").append(s.classifiedImage.getInputImage().getFile().getAbsolutePath()).append("\"\n")
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
                        .append("\"").append(s.classifiedImage.getPower().getData()).append("\"")
                        .append("\"").append(s.systemName).append("\",")
                        .append("\"").append(s.upkeepFromLastCycle).append("\",")
                        .append("\"").append(s.defaultUpkeepCost).append("\",")
                        .append("\"").append(s.costIfFortified).append("\",")
                        .append("\"").append(s.costIfUndermined).append("\",")
                        .append("\"").append(s.baseIncome).append("\",")
                        .append("\"").append(s.fortifyTotal).append("\",")
                        .append("\"").append(s.fortifyTrigger).append("\",")
                        .append("\"").append(s.undermineTotal).append("\",")
                        .append("\"").append(s.undermineTrigger).append(",")
                        .append("\"").append(s.classifiedImage.getInputImage().getFile().getAbsolutePath()).append("\"\n")
        );
        final ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
    }

    @FXML
    private void showOriginalControlImage(ActionEvent actionEvent) {
        final ControlSystem system = controlSystemList.getSelectionModel().getSelectedItem();
        if (null == system) {
            return;
        }

        final BufferedImage originalImage = matToBufferedImage(system.classifiedImage.getInputImage().getOnDemandImage());
        if (null == originalImage) {
            new Alert(Alert.AlertType.ERROR, "Could not find image").showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(new ImageView(toFXImage(originalImage, null)));
        alert.setHeaderText(system.classifiedImage.getInputImage().getFile().getAbsolutePath());
        alert.show();
    }

    @FXML
    private void showOriginalPreparationImage(ActionEvent actionEvent) {
        final PreparationSystem system = preparationSystemList.getSelectionModel().getSelectedItem();
        if (null == system) {
            return;
        }

        final BufferedImage originalImage = matToBufferedImage(system.classifiedImage.getInputImage().getOnDemandImage());
        if (null == originalImage) {
            new Alert(Alert.AlertType.ERROR, "Could not find image").showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.getDialogPane().setContent(new ImageView(toFXImage(originalImage, null)));
        alert.setHeaderText(system.classifiedImage.getInputImage().getFile().getAbsolutePath());
        alert.show();
    }


    private void startScan() {
        ocrTask = new Task<PowerPlayReport>() {
            @Override
            protected PowerPlayReport call() throws Exception {
                return imageApi.generateReport(
                        getUnprocessedFilesFromDir(imageDir.get())
                );
            }
        };
        ocrTask.setOnSucceeded(
                event -> {
                    final PowerPlayReport report = ocrTask.getValue();
                    controlData.setAll(report.control);
                    preparationData.setAll(report.preparation);

                    String musicFile = "conf/work-complete.wav";

                    Media sound = new Media(new File(musicFile).toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(sound);
                    mediaPlayer.play();

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

    private void resetPreparationDetails() {

        preparationPowerText.setText(null);
        preparationPowerImage.setImage(null);

        preparationNameText.setText(null);
        preparationNameImage.setImage(null);

        preparationCcToSpendThisCycleText.setText(null);
        preparationCcToSpendThisCycleImage.setImage(null);


        preparationPrepText.setText(null);
        preparationPrepImage.setImage(null);

        preparationCostText.setText(null);
        preparationCostImage.setImage(null);

        preparationHighestContributingPowerText.setText(null);
        preparationHighestContributionText.setText(null);

        preparationCostText.setText(null);
        preparationContributionImage.setImage(null);
    }

    private void setPreparationDetails(final PreparationSystem s) {

        preparationPowerText.setText(s.classifiedImage.getPower().getData().getName());
        preparationNameText.setText(s.systemName);
        preparationCcToSpendThisCycleText.setText(String.valueOf(s.ccToSpendThisCycle));
        preparationPrepText.setText(String.valueOf(s.prep));
        preparationCostText.setText(String.valueOf(s.cost));
        preparationHighestContributingPowerText.setText(s.highestContributingPower.getName());
        preparationHighestContributionText.setText(String.valueOf(s.highestContributingPowerAmount));
        preparationCostText.setText(String.valueOf(s.cost));


        final Mat originalImage = s.classifiedImage.getInputImage().getOnDemandImage();

        if (null == originalImage) {
            return;
        }

        if (null != s.classifiedImage.getPower()) {
            preparationPowerImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.classifiedImage.getPower().getRectangle(), originalImage).get()), null));
        } else {
            preparationPowerImage.setImage(null);
        }

        if (null != s.systemNameRectangle) {
            preparationNameImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.systemNameRectangle.getRectangle(), originalImage).get()), null));
        } else {
            preparationNameImage.setImage(null);
        }

        if (null != s.ccToSpendThisCycleRectangle) {
            preparationCcToSpendThisCycleImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.ccToSpendThisCycleRectangle.getRectangle(), originalImage).get()), null));
        } else {
            preparationCcToSpendThisCycleImage.setImage(null);
        }

        if (null != s.prepRectangle) {
            preparationPrepImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.prepRectangle.getRectangle(), originalImage).get()), null));
        } else {
            preparationPrepImage.setImage(null);
        }
        if (null != s.costRectangle) {
            preparationCostImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.costRectangle.getRectangle(), originalImage).get()), null));
        } else {
            preparationCostImage.setImage(null);
        }
        if (null != s.preparationHighestContributionRectangle) {
            preparationContributionImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.preparationHighestContributionRectangle.getRectangle(), originalImage).get()), null));
        } else {
            preparationContributionImage.setImage(null);
        }

    }


    private void setControlDetails(final ControlSystem s) {

        controlPowerText.setText(s.classifiedImage.getPower().getData().getName());
        controlNameText.setText(s.systemName);
        controlUpkeepFromLastCycleText.setText(String.valueOf(s.upkeepFromLastCycle));
        controlDefaultUpkeepCostText.setText(String.valueOf(s.defaultUpkeepCost));
        controlFortifiedCostText.setText(String.valueOf(s.costIfFortified));
        controlUnderminedCostText.setText(String.valueOf(s.costIfUndermined));
        controlBaseIncomeText.setText(String.valueOf(s.baseIncome));

        controlFortifyTotalText.setText(String.valueOf(s.fortifyTotal));
        controlFortifyTriggerText.setText(String.valueOf(s.fortifyTrigger));

        controlUndermineTotalText.setText(String.valueOf(s.undermineTotal));
        controlUndermineTriggerText.setText(String.valueOf(s.undermineTrigger));


        final Mat originalImage = s.classifiedImage.getInputImage().getOnDemandImage();

        if (null == originalImage) {
            return;
        }

        if (null != s.classifiedImage.getPower()) {
            controlPowerImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.classifiedImage.getPower().getRectangle(), originalImage).get()), null));
        } else {
            controlPowerImage.setImage(null);
        }

        if (null != s.systemNameRectangle) {
            controlNameImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.systemNameRectangle.getRectangle(), originalImage).get()), null));
        } else {
            controlNameImage.setImage(null);
        }


        if (null != s.costsRectangle) {
            controlCostsImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.costsRectangle.getRectangle(), originalImage).get()), null));
        } else {
            controlCostsImage.setImage(null);
        }

        if (null != s.fortifyRectangle) {
            controlFortifyImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.fortifyRectangle.getRectangle(), originalImage).get()), null));
        } else {
            controlFortifyImage.setImage(null);
        }

        if (null != s.undermineRectangle) {
            controlUndermineImage.setImage(toFXImage(matToBufferedImage(ImageUtil.crop(s.undermineRectangle.getRectangle(), originalImage).get()), null));
        } else {
            controlUndermineImage.setImage(null);
        }

    }

    private void resetControlDetails() {

        controlPowerText.setText(null);
        controlPowerImage.setImage(null);

        controlNameText.setText(null);
        controlNameImage.setImage(null);

        controlUpkeepFromLastCycleText.setText(null);
        controlDefaultUpkeepCostText.setText(null);
        controlBaseIncomeText.setText(null);
        controlFortifiedCostText.setText(null);
        controlUnderminedCostText.setText(null);
        controlCostsImage.setImage(null);

        controlFortifyTotalText.setText(null);
        controlFortifyTriggerText.setText(null);
        controlFortifyImage.setImage(null);

        controlUndermineTotalText.setText(null);
        controlUndermineTriggerText.setText(null);
        controlUndermineImage.setImage(null);
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
