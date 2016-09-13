package pub.occams.elite.dubliner.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrDto {

    @JsonProperty("filterRedChannelMin")
    public Integer filterRedChannelMin;

    @JsonProperty("tesseractForNumbersAndLetters")
    public TesseractSettingsDto tesseractForNumbersAndLetters;


    @JsonProperty("tesseractForNumbers")
    public TesseractSettingsDto tesseractForNumbers;

}
