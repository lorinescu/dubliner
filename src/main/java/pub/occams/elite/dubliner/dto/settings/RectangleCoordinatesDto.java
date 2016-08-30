package pub.occams.elite.dubliner.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RectangleCoordinatesDto {

    @JsonProperty("screenWidth")
    public Integer screenWidth;

    @JsonProperty("screenHeight")
    public Integer screenHeight;

    @JsonProperty("overviewTab")
    public RectangleDto overviewTab;

    @JsonProperty("preparationTab")
    public RectangleDto preparationTab;

    @JsonProperty("expansionTab")
    public RectangleDto expansionTab;

    @JsonProperty("controlTab")
    public RectangleDto controlTab;

    @JsonProperty("name")
    public RectangleDto name;

    @JsonProperty("upkeepCost")
    public RectangleDto upkeepCost;

    @JsonProperty("defaultUpkeepCost")
    public RectangleDto defaultUpkeepCost;

    @JsonProperty("costIfFortified")
    public RectangleDto costIfFortified;

    @JsonProperty("costIfUndermined")
    public RectangleDto costIfUndermined;

    @JsonProperty("fortificationTotal")
    public RectangleDto fortificationTotal;

    @JsonProperty("fortificationTrigger")
    public RectangleDto fortificationTrigger;

    @JsonProperty("underminingTotal")
    public RectangleDto underminingTotal;

    @JsonProperty("underminingTrigger")
    public RectangleDto underminingTrigger;
}