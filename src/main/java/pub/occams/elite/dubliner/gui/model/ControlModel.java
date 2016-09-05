package pub.occams.elite.dubliner.gui.model;

import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.Power;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;
import pub.occams.elite.dubliner.dto.ocr.ControlDto;

public class ControlModel extends PowerPlayModel {

    public DataRectangle<String> costsRectangle;
    public DataRectangle<String> fortifyRectangle;
    public DataRectangle<String> undermineRectangle;

    public final Power power;

    public final Integer upkeepFromLastCycle;

    public final Integer defaultUpkeepCost;

    public final Integer costIfFortified;

    public final Integer costIfUndermined;

    public final Integer baseIncome;

    public final Integer fortifyTotal;

    public final Integer fortifyTrigger;

    public final Integer undermineTotal;

    public final Integer undermineTrigger;

    public ControlModel(final ClassifiedImage classifiedImage, final DataRectangle<String> systemNameRectangle,
                        final String systemName, final DataRectangle<String> costsRectangle,
                        final Power power,
                        final DataRectangle<String> fortifyRectangle, final DataRectangle<String> undermineRectangle,
                        final Integer upkeepFromLastCycle, final Integer defaultUpkeepCost,
                        final Integer costIfFortified, final Integer costIfUndermined,
                        final Integer baseIncome, final Integer fortifyTotal, final Integer fortifyTrigger,
                        final Integer undermineTotal, final Integer undermineTrigger) {
        super(classifiedImage, systemNameRectangle, systemName);
        this.costsRectangle = costsRectangle;
        this.fortifyRectangle = fortifyRectangle;
        this.undermineRectangle = undermineRectangle;
        this.power = power;
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

    @Override
    public String toString() {
        return systemName +
                "," + upkeepFromLastCycle +
                "," + defaultUpkeepCost +
                "," + costIfFortified +
                "," + costIfUndermined +
                "," + baseIncome +
                "," + fortifyTotal +
                "," + fortifyTrigger +
                "," + undermineTotal +
                "," + undermineTrigger;
    }

    public static ControlModel fromDto(final Power power, final ControlDto dto) {
        return new ControlModel(
                dto.classifiedImage, dto.systemNameRectangle, dto.systemName, dto.costsRectangle, power,
                dto.fortifyRectangle, dto.undermineRectangle,
                dto.upkeepFromLastCycle, dto.defaultUpkeepCost, dto.costIfFortified, dto.costIfUndermined,
                dto.baseIncome, dto.fortifyTotal, dto.fortifyTrigger, dto.undermineTotal, dto.undermineTrigger
        );
    }
}
