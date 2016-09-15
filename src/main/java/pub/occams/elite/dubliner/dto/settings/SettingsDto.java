package pub.occams.elite.dubliner.dto.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsDto {


    @JsonProperty("csvSeparator")
    public String csvSeparator;

    @JsonProperty("ocr")
    public OcrDto ocr;

    @JsonProperty("corrections")
    public CorrectionsDto corrections;

}