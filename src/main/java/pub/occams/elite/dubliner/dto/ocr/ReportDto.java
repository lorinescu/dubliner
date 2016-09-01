package pub.occams.elite.dubliner.dto.ocr;

import com.fasterxml.jackson.annotation.JsonProperty;
import pub.occams.elite.dubliner.domain.Power;

import java.util.Map;

public class ReportDto {

    @JsonProperty("powers")
    public Map<Power, PowerReportDto> powers;
}
