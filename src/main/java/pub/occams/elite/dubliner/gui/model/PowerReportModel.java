package pub.occams.elite.dubliner.gui.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import pub.occams.elite.dubliner.dto.ocr.PowerReportDto;

import java.util.stream.Collectors;

public class PowerReportModel {

    public final ObservableList<ControlModel> control = FXCollections.observableArrayList();

    public static PowerReportModel fromDto(final PowerReportDto dto) {
        final PowerReportModel m = new PowerReportModel();
        m.control.setAll(
                dto.control
                        .stream()
                        .map(ControlModel::fromDto)
                        .collect(Collectors.toList())
        );
        return m;
    }
}
