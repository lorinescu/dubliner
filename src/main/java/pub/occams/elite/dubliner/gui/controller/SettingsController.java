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
import pub.occams.elite.dubliner.dto.settings.RectangleDto;
import pub.occams.elite.dubliner.dto.settings.RectangleCoordinatesDto;
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
    private TableView<SegmentModel> rectanglesTable;
    @FXML
    private TableColumn<SegmentModel, String> rectangleColumn;
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
    private final ObservableList<SegmentModel> rectanglesData = FXCollections.observableArrayList();
    private final ObservableList<CorrectionModel> correctionsData = FXCollections.observableArrayList();

    private final Stage stage = new Stage();
    private ImageApi imageApi;

    @FXML
    private void initialize() {
        stage.setScene(new Scene(getView()));

        resolutionCombo.setItems(resolutionData);

        rectanglesTable.setItems(rectanglesData);

        rectangleColumn.setCellValueFactory(data -> data.getValue().nameProperty());
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

    private SegmentModel rectangleToModel(final String name, final RectangleDto s) {
        return new SegmentModel(name, s.x, s.y, s.width, s.height);
    }

    private void reload() {
        final SettingsDto settings = imageApi.getSettings();

        resolutionData.clear();
        settings.rectangleCoordinates.forEach(
                coord -> resolutionData.add(new ResolutionsModel(coord.screenWidth, coord.screenHeight))
        );
        resolutionCombo.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final Optional<RectangleCoordinatesDto> maybeCoord = settings.rectangleCoordinates
                            .stream()
                            .filter(coord -> coord.screenWidth.equals(newValue.getWidth()) &&
                                    coord.screenHeight.equals(newValue.getHeight()))
                            .findFirst();
                    if (!maybeCoord.isPresent()) {
                        new Alert(Alert.AlertType.ERROR, "NOT FOUND").showAndWait();
                        return;
                    }

                    final RectangleCoordinatesDto c = maybeCoord.get();
                    rectanglesData.add(rectangleToModel("controlTab", c.controlTab));
                    rectanglesData.add(rectangleToModel("costIfFortified", c.costIfFortified));
                    rectanglesData.add(rectangleToModel("costIfUndermined", c.costIfUndermined));
                    rectanglesData.add(rectangleToModel("defaultUpkeepCost", c.defaultUpkeepCost));
                    rectanglesData.add(rectangleToModel("fortificationTotal", c.fortificationTotal));
                    rectanglesData.add(rectangleToModel("fortificationTrigger", c.fortificationTrigger));
                    rectanglesData.add(rectangleToModel("undermineTotal", c.underminingTotal));
                    rectanglesData.add(rectangleToModel("undermineTrigger", c.underminingTrigger));
                }
        );
        rectanglesData.clear();

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
