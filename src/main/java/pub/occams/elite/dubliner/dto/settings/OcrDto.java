package pub.occams.elite.dubliner.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrDto {

    @JsonProperty("filterRedChannelMin")
    public Integer filterRedChannelMin;

    @JsonProperty("tesseractForSystemNames")
    public TesseractSettingsDto tesseractForSystemNames;


    @JsonProperty("tesseractForNumbers")
    public TesseractSettingsDto tesseractForNumbers;

}
