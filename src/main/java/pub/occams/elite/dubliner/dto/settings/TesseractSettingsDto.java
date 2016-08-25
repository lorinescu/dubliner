package pub.occams.elite.dubliner.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TesseractSettingsDto {

    @JsonProperty("dataPath")
    public String dataPath;

    @JsonProperty("language")
    public String language;

    @JsonProperty("variables")
    public Map<String, String> variables;

}
