package pub.occams.elite.dubliner.gui.controller.module;

import javafx.scene.layout.AnchorPane;
import pub.occams.elite.dubliner.domain.powerplay.OcrResult;
import pub.occams.elite.dubliner.gui.controller.Controller;

public class SystemReportsController extends Controller<AnchorPane> {

    private String csvSeparator = ",";

    public void setData(final OcrResult data) {
    }

    public void resetData() {

    }

    public void setCsvSeparator(String csvSeparator) {
        this.csvSeparator = csvSeparator;
    }
}
