package pub.occams.elite.dubliner.dto.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsDto {


    @JsonProperty("ocr")
    public OcrDto ocr;

    @JsonProperty("corrections")
    public CorrectionsDto corrections;

    @JsonProperty("segmentsCoordinates")
    public List<SegmentsCoordinatesDto> segmentsCoordinates;

}