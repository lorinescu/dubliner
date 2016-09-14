package pub.occams.elite.dubliner.domain.powerplay;

import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;
import pub.occams.elite.dubliner.domain.image.ClassifiedImage;

public class ControlSystem extends SystemBase {

    public final OcrDataRectangle<String> costsRectangle;
    public final OcrDataRectangle<String> fortifyRectangle;
    public final OcrDataRectangle<String> undermineRectangle;

    public final Integer upkeepFromLastCycle;

    public final Integer defaultUpkeepCost;

    public final Integer costIfFortified;

    public final Integer costIfUndermined;

    public final Integer baseIncome;

    public final Integer fortifyTotal;

    public final Integer fortifyTrigger;
    public final Integer undermineTotal;

    public final Integer undermineTrigger;

    public ControlSystem(final PowerPlayImage classifiedImage, final OcrDataRectangle<String> systemNameRectangle,
                         final String systemName, final OcrDataRectangle<String> costsRectangle,
                         final OcrDataRectangle<String> fortifyRectangle, final OcrDataRectangle<String> undermineRectangle,
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

    @Override
    public String toString() {
        return super.toString() + "," +
                upkeepFromLastCycle + "," +
                defaultUpkeepCost + "," +
                costIfFortified + "," +
                costIfUndermined + "," +
                baseIncome + "," +
                fortifyTotal + "," +
                fortifyTrigger + "," +
                undermineTotal + "," +
                undermineTrigger + "," +
                classifiedImage.inputImage.getFile().getAbsolutePath();
    }
}
