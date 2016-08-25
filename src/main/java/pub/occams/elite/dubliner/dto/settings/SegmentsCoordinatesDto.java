package pub.occams.elite.dubliner.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SegmentsCoordinatesDto {

    @JsonProperty("screenWidth")
    public Integer screenWidth;

    @JsonProperty("screenHeight")
    public Integer screenHeight;

    @JsonProperty("controlTab")
    public SegmentDto controlTab;

    @JsonProperty("name")
    public SegmentDto name;

    @JsonProperty("upkeepCost")
    public SegmentDto upkeepCost;

    @JsonProperty("defaultUpkeepCost")
    public SegmentDto defaultUpkeepCost;

    @JsonProperty("costIfFortified")
    public SegmentDto costIfFortified;

    @JsonProperty("costIfUndermined")
    public SegmentDto costIfUndermined;

    @JsonProperty("fortificationTotal")
    public SegmentDto fortificationTotal;

    @JsonProperty("fortificationTrigger")
    public SegmentDto fortificationTrigger;

    @JsonProperty("underminingTotal")
    public SegmentDto underminingTotal;

    @JsonProperty("underminingTrigger")
    public SegmentDto underminingTrigger;
}