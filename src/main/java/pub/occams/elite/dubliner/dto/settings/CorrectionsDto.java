package pub.occams.elite.dubliner.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class CorrectionsDto {

    @JsonProperty("systemName")
    public Map<String, String> systemName;
}
