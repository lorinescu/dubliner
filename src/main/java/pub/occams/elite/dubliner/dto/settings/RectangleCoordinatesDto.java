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

    @JsonProperty("upkeepFromLastCycle")
    public RectangleDto upkeepFromLastCycle;

    @JsonProperty("defaultUpkeepCost")
    public RectangleDto defaultUpkeepCost;

    @JsonProperty("costIfFortified")
    public RectangleDto costIfFortified;

    @JsonProperty("costIfUndermined")
    public RectangleDto costIfUndermined;

    @JsonProperty("baseIncome")
    public RectangleDto baseIncome;

    @JsonProperty("fortificationTotal")
    public RectangleDto fortificationTotal;

    @JsonProperty("fortificationTrigger")
    public RectangleDto fortificationTrigger;

    @JsonProperty("undermineTotal")
    public RectangleDto underminingTotal;

    @JsonProperty("undermineTrigger")
    public RectangleDto underminingTrigger;
}