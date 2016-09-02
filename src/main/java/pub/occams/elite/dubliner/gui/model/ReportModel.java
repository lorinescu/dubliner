package pub.occams.elite.dubliner.gui.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import pub.occams.elite.dubliner.domain.Power;
import pub.occams.elite.dubliner.dto.ocr.ReportDto;

public class ReportModel {
    public final ObservableMap<Power, PowerReportModel> powers = FXCollections.observableHashMap();

    public static ReportModel fromDto(final ReportDto dto) {

        final ReportModel m = new ReportModel();
        dto.powers.forEach(
                (power, powerReportDto) ->
                        m.powers.put(power, PowerReportModel.fromDto(powerReportDto))
        );

        return m;
    }
}
