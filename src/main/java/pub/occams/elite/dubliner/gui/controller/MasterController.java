package pub.occams.elite.dubliner.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.application.ImageApi;
import pub.occams.elite.dubliner.gui.controller.module.AreasController;
import pub.occams.elite.dubliner.gui.controller.module.HelpController;
import pub.occams.elite.dubliner.gui.controller.module.ScanController;

public class MasterController extends Controller<AnchorPane> {

    @FXML
    private Tab scanTab;
    @FXML
    private Tab areaTab;
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
        alert.setHeaderText(App.NAME + "-" + App.VERSION + "\nhttps://bitbucket.org/lorinescu/dubliner\n(lorin@occams.pub)");
        alert.getDialogPane().setContent(new ImageView("/pub/occams/elite/dubliner/gui/controller/about.jpg"));
        alert.showAndWait();
    }

    public void postConstruct(final ImageApi imageApi, SettingsController settingsController, final ScanController scanController,
                              final AreasController areasController, final HelpController helpController) {
        this.imageApi = imageApi;
        this.settingsController = settingsController;
        this.scanTab.setContent(scanController.getView());
        this.areaTab.setContent(areasController.getView());
        this.helpTab.setContent(helpController.getView());
    }
}
