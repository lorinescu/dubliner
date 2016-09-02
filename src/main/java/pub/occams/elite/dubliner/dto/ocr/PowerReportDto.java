package pub.occams.elite.dubliner.dto.ocr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class PowerReportDto {

    @JsonProperty("control")
    public List<ControlDto> control = new ArrayList<>();

    @JsonProperty("expansion")
    public List<ExpansionDto> expansion = new ArrayList<>();

    @JsonProperty("preparation")
    public List<PreparationDto> preparation = new ArrayList<>();

}
