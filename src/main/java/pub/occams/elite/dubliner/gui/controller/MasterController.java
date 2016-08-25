package pub.occams.elite.dubliner.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.gui.controller.module.ScanController;
import pub.occams.elite.dubliner.gui.controller.module.HelpController;

public class MasterController extends Controller<AnchorPane> {

    @FXML
    private Tab scanTab;
    @FXML
    private Tab helpTab;

    private ImageApi imageApi;
    private SettingsController settingsController;

    @FXML
    private void showSettings(ActionEvent actionEvent) {
        settingsController.show();
    }

    @FXML
    private void close(ActionEvent actionEvent) {
        System.exit(0);
    }

    @FXML
    private void about(ActionEvent actionEvent) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(App.NAME + "-"+ App.VERSION + " (reddit: cmdrmacarye)");
        alert.getDialogPane().setContent(new ImageView("/pub/occams/elite/dubliner/gui/controller/about.jpg"));
        alert.showAndWait();
    }

    public void postConstruct(final ImageApi imageApi, SettingsController settingsController, final ScanController scanController, final HelpController helpController) {
        this.imageApi = imageApi;
        this.settingsController = settingsController;
        this.scanTab.setContent(scanController.getView());
        this.helpTab.setContent(helpController.getView());
    }
}
