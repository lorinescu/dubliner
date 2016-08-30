package pub.occams.elite.dubliner.gui.controller.module;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.NumberStringConverter;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.domain.DataType;
import pub.occams.elite.dubliner.domain.ImageType;
import pub.occams.elite.dubliner.domain.Power;
import pub.occams.elite.dubliner.domain.Resolution;
import pub.occams.elite.dubliner.gui.controller.Controller;
import pub.occams.elite.dubliner.gui.model.AreaModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AreasController extends Controller<AnchorPane> {
    @FXML
    private ComboBox<Resolution> resolutionCombo;
    @FXML
    private ComboBox<ImageType> screenCombo;
    @FXML
    private ComboBox<Power> powerCombo;
    @FXML
    private Label classifierLabel;
    @FXML
    private TableView<AreaModel> areasTable;
    @FXML
    private TableColumn<AreaModel, String> nameColumn;
    @FXML
    private TableColumn<AreaModel, DataType> typeColumn;
    @FXML
    private TableColumn<AreaModel, Number> x0Column;
    @FXML
    private TableColumn<AreaModel, Number> y0Column;
    @FXML
    private TableColumn<AreaModel, Number> x1Column;
    @FXML
    private TableColumn<AreaModel, Number> y1Column;


    private final ObservableList<Resolution> resolutionData = FXCollections.observableArrayList(Resolution.values());
    private final ObservableList<ImageType> imageTypeData = FXCollections.observableArrayList(ImageType.values());
    private final ObservableList<Power> powerData = FXCollections.observableArrayList(Power.values());

    private final ObservableList<AreaModel> areaData = FXCollections.observableArrayList();

    private final ObjectProperty<Resolution> resolution = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageType> imageType = new SimpleObjectProperty<>();
    private final ObjectProperty<Power> power = new SimpleObjectProperty<>();

    private ImageApi imageApi;

    private void setupTable() {
        areasTable.setItems(areaData);

        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));

        typeColumn.setCellValueFactory(param -> param.getValue().typeProperty());
        typeColumn.setCellFactory(
                ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(DataType.values()))
        );

        x0Column.setCellValueFactory(param -> param.getValue().x0Property());
        x0Column.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));

        y0Column.setCellValueFactory(param -> param.getValue().y0Property());
        y0Column.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));

        x1Column.setCellValueFactory(param -> param.getValue().x1Property());
        x1Column.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));

        y1Column.setCellValueFactory(param -> param.getValue().y1Property());
        y1Column.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));
    }

    @FXML
    private void initialize() {
        resolutionCombo.setItems(resolutionData);
        screenCombo.setItems(imageTypeData);
        powerCombo.setItems(powerData);

        setupTable();

        screenCombo.disableProperty().bind(resolution.isNotNull().not());
        powerCombo.disableProperty().bind(imageType.isNotNull().not());
    }

    @FXML
    private void addResolution(final ActionEvent actionEvent) {
        resolution.set(resolutionCombo.getValue());
        refreshClassifierLabel();
    }

    @FXML
    private void deleteResolution(final ActionEvent actionEvent) {
        resolution.set(null);
        refreshClassifierLabel();
    }

    @FXML
    private void addPower(final ActionEvent actionEvent) {
        power.set(powerCombo.getValue());
        refreshClassifierLabel();
    }

    @FXML
    private void deletePower(final ActionEvent actionEvent) {
        power.set(null);
        refreshClassifierLabel();
    }

    @FXML
    private void addScreen(final ActionEvent actionEvent) {
        imageType.set(screenCombo.getValue());
        refreshClassifierLabel();
    }

    @FXML
    private void deleteScreen(final ActionEvent actionEvent) {
        imageType.set(null);
        refreshClassifierLabel();
    }

    @FXML
    private void addArea(final ActionEvent actionEvent) {
        areaData.add(new AreaModel("baseIncome", DataType.NUMERIC, 0, 0, 10, 10));
    }

    @FXML
    private void deleteArea(final ActionEvent actionEvent) {
        areaData.remove(areasTable.getSelectionModel().getSelectedIndex());
    }

    @FXML
    private void save(final ActionEvent actionEvent) {

    }

    private void refreshClassifierLabel() {
        final List<String> classifiers = new ArrayList<>();
        if (null != resolution.get()) {
            classifiers.add(resolution.get().toString());
        }
        if (null != imageType.get()) {
            classifiers.add(imageType.get().toString());
        }
        if (null != power.get()) {
            classifiers.add(power.get().toString());
        }
        final String txt;
        if (classifiers.size() > 0) {
            txt = classifiers.stream().collect(Collectors.joining(","));
        } else {
            txt = "---";
        }
        classifierLabel.setText(txt);
    }

    public void postContruct(final ImageApi imageApi) {
        this.imageApi = imageApi;
    }
}
