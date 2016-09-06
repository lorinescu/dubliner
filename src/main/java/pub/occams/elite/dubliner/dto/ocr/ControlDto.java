package pub.occams.elite.dubliner.dto.ocr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;

public class ControlDto extends PowerPlayDto {

    @JsonIgnore
    public final DataRectangle<String> costsRectangle;
    @JsonIgnore
    public final DataRectangle<String> fortifyRectangle;
    @JsonIgnore
    public final DataRectangle<String> undermineRectangle;

    @JsonProperty("upkeepFromLastCycle")
    public final Integer upkeepFromLastCycle;

    @JsonProperty("defaultUpkeepCost")
    public final Integer defaultUpkeepCost;

    @JsonProperty("costIfFortified")
    public final Integer costIfFortified;

    @JsonProperty("costIfUndermined")
    public final Integer costIfUndermined;

    @JsonProperty("baseIncome")
    public final Integer baseIncome;

    @JsonProperty("fortifyTotal")
    public final Integer fortifyTotal;

    @JsonProperty("fortifyTrigger")
    public final Integer fortifyTrigger;

    @JsonProperty("undermineTotal")
    public final Integer undermineTotal;

    @JsonProperty("undermineTrigger")
    public final Integer undermineTrigger;

    public ControlDto(final ClassifiedImage classifiedImage, final DataRectangle<String> systemNameRectangle,
                      final String systemName, final DataRectangle<String> costsRectangle,
                      final DataRectangle<String> fortifyRectangle, final DataRectangle<String> undermineRectangle,
                      final Integer upkeepFromLastCycle, final Integer defaultUpkeepCost, final Integer costIfFortified,
                      final Integer costIfUndermined, final Integer baseIncome, final Integer fortifyTotal,
                      final Integer fortifyTrigger, final Integer undermineTotal, final Integer undermineTrigger) {
        super(classifiedImage, systemNameRectangle, systemName);
        this.costsRectangle = costsRectangle;
        this.fortifyRectangle = fortifyRectangle;
        this.undermineRectangle = undermineRectangle;
        this.upkeepFromLastCycle = upkeepFromLastCycle;
        this.defaultUpkeepCost = defaultUpkeepCost;
        this.costIfFortified = costIfFortified;
        this.costIfUndermined = costIfUndermined;
        this.baseIncome = baseIncome;
        this.fortifyTotal = fortifyTotal;
        this.fortifyTrigger = fortifyTrigger;
        this.undermineTotal = undermineTotal;
        this.undermineTrigger = undermineTrigger;
    }
}
