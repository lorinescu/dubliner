package pub.occams.elite.dubliner.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.gui.controller.module.ScanController;

public class MasterController extends Controller<AnchorPane> {

    @FXML
    private Tab scanTab;

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

    public void postConstruct(final ScanController scanController) {
        this.scanTab.setContent(scanController.getView());
    }
}
