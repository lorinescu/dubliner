package pub.occams.elite.dubliner.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.dto.settings.SegmentDto;
import pub.occams.elite.dubliner.dto.settings.SegmentsCoordinatesDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.gui.model.CorrectionModel;
import pub.occams.elite.dubliner.gui.model.ResolutionsModel;
import pub.occams.elite.dubliner.gui.model.SegmentModel;

import java.util.Optional;

public class SettingsController extends Controller<Pane> {

    //resolutions tabs
    @FXML
    private ComboBox<ResolutionsModel> resolutionCombo;
    @FXML
    private TableView<SegmentModel> segmentsTable;
    @FXML
    private TableColumn<SegmentModel, String> segmentColumn;
    @FXML
    private TableColumn<SegmentModel, Number> xCoordColumn;
    @FXML
    private TableColumn<SegmentModel, Number> yColumn;
    @FXML
    private TableColumn<SegmentModel, Number> widthColumn;
    @FXML
    private TableColumn<SegmentModel, Number> heightColumn;

    //corrections tab
    @FXML
    private TableView<CorrectionModel> correctionsTable;
    @FXML
    private TableColumn<CorrectionModel, String> fromColumn;
    @FXML
    private TableColumn<CorrectionModel, String> toColumn;

    private final ObservableList<ResolutionsModel> resolutionData = FXCollections.observableArrayList();
    private final ObservableList<SegmentModel> segmentsData = FXCollections.observableArrayList();
    private final ObservableList<CorrectionModel> correctionsData = FXCollections.observableArrayList();

    private final Stage stage = new Stage();
    private ImageApi imageApi;

    @FXML
    private void initialize() {
        stage.setScene(new Scene(getView()));

        resolutionCombo.setItems(resolutionData);

        segmentsTable.setItems(segmentsData);

        segmentColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        xCoordColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        xCoordColumn.setCellValueFactory(data -> data.getValue().xProperty());
        yColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        yColumn.setCellValueFactory(data -> data.getValue().yProperty());
        widthColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        widthColumn.setCellValueFactory(data -> data.getValue().widthProperty());
        heightColumn.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
        heightColumn.setCellValueFactory(data -> data.getValue().heightProperty());

        correctionsTable.setItems(correctionsData);
        fromColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        fromColumn.setCellValueFactory(data -> data.getValue().fromProperty());
        toColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        toColumn.setCellValueFactory(data -> data.getValue().toProperty());
    }

    @FXML
    private void applyResolutions(ActionEvent actionEvent) {
        new Alert(Alert.AlertType.ERROR, "NOT IMPLEMENTED").showAndWait();
    }

    @FXML
    private void applyCorrections(ActionEvent actionEvent) {
        new Alert(Alert.AlertType.ERROR, "NOT IMPLEMENTED").showAndWait();
    }

    private SegmentModel segmentToModel(final String name, final SegmentDto s) {
        return new SegmentModel(name, s.x, s.y, s.width, s.height);
    }

    private void reload() {
        final SettingsDto settings = imageApi.getSettings();

        resolutionData.clear();
        settings.segmentsCoordinates.forEach(
                coord -> resolutionData.add(new ResolutionsModel(coord.screenWidth, coord.screenHeight))
        );
        resolutionCombo.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final Optional<SegmentsCoordinatesDto> maybeCoord = settings.segmentsCoordinates
                            .stream()
                            .filter(coord -> coord.screenWidth.equals(newValue.getWidth()) &&
                                    coord.screenHeight.equals(newValue.getHeight()))
                            .findFirst();
                    if (!maybeCoord.isPresent()) {
                        new Alert(Alert.AlertType.ERROR, "NOT FOUND").showAndWait();
                        return;
                    }

                    final SegmentsCoordinatesDto c = maybeCoord.get();
                    segmentsData.add(segmentToModel("controlTab", c.controlTab));
                    segmentsData.add(segmentToModel("costIfFortified", c.costIfFortified));
                    segmentsData.add(segmentToModel("costIfUndermined", c.costIfUndermined));
                    segmentsData.add(segmentToModel("defaultUpkeepCost", c.defaultUpkeepCost));
                    segmentsData.add(segmentToModel("fortificationTotal", c.fortificationTotal));
                    segmentsData.add(segmentToModel("fortificationTrigger", c.fortificationTrigger));
                    segmentsData.add(segmentToModel("underminingTotal", c.underminingTotal));
                    segmentsData.add(segmentToModel("underminingTrigger", c.underminingTrigger));
                }
        );
        segmentsData.clear();

        correctionsData.clear();
        settings.corrections.systemName.forEach(
                (from, to) -> correctionsData.add(new CorrectionModel(from, to))
        );
    }

    public void postConstruct(final ImageApi imageApi) {
        this.imageApi = imageApi;
    }

    public void show() {
        reload();
        stage.show();
    }
}
