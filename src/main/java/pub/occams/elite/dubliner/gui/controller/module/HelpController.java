package pub.occams.elite.dubliner.gui.controller.module;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import pub.occams.elite.dubliner.gui.controller.Controller;


public class HelpController extends Controller<AnchorPane> {

    @FXML
    private TextArea info;

    @FXML
    private void initialize() {
        info.appendText("conf/ directory can be found in the application's installation directory\n"
                + "Modify conf/settings.json according to your needs then use http://http://jsonlint.com/ to validate it.\n");
        info.appendText("\n");
        info.appendText("You are probably not interested in the \"ocr\" section unless you want to tune the OCR engine\n");
        info.appendText("\n");
        info.appendText("In the \"corrections\" section you can add ... corrections. A correction is \n" +
                "used to transform common OCR output errors strings into valid ones. Currently this exquisite software supports only \n" +
                "system names corrections. For example, if the OCR extracts system name AL1OTH instead of ALIOTH you can add \n" +
                " the following correction: \n"
                + " \"AL1OTH\" : \"ALIOTH\" \n"
                + "The next time AL1OTH is found it will be replaced with ALIOTH\n");
        info.appendText("\n");
        info.appendText("In \"rectangleCoordinates\" add coordinates for resolution specific rectangle extraction. \n"
                + "A rectangle is a rectangular area cropped from a screenshot which contains interesting information to be \n"
                + "feed to OCR. In case your resolution (screenWidth by screenHeight) is not in there you can take a screenshot, fire \n"
                + "up Gimp/Photoshop/Paint and start counting pixels. Or you can send me the images and some palladium and \n"
                + "I'll add the coordinates.\n");
    }
}
