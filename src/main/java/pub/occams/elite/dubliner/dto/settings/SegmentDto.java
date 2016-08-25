package pub.occams.elite.dubliner.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SegmentDto {

    @JsonProperty("x")
    public Integer x;

    @JsonProperty("y")
    public Integer y;

    @JsonProperty("width")
    public Integer width;

    @JsonProperty("height")
    public Integer height;
}