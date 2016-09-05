package pub.occams.elite.dubliner.dto.ocr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;

public class ControlDto extends PowerPlayDto {

    @JsonIgnore
    public DataRectangle<String> costsRectangle;
    @JsonIgnore
    public DataRectangle<String> fortifyRectangle;
    @JsonIgnore
    public DataRectangle<String> undermineRectangle;

    @JsonProperty("upkeepFromLastCycle")
    public Integer upkeepFromLastCycle;

    @JsonProperty("defaultUpkeepCost")
    public Integer defaultUpkeepCost;

    @JsonProperty("costIfFortified")
    public Integer costIfFortified;

    @JsonProperty("costIfUndermined")
    public Integer costIfUndermined;

    @JsonProperty("baseIncome")
    public Integer baseIncome;

    @JsonProperty("fortifyTotal")
    public Integer fortifyTotal;

    @JsonProperty("fortifyTrigger")
    public Integer fortifyTrigger;

    @JsonProperty("undermineTotal")
    public Integer undermineTotal;

    @JsonProperty("undermineTrigger")
    public Integer undermineTrigger;
}
