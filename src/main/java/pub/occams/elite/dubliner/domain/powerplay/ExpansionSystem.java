package pub.occams.elite.dubliner.domain.powerplay;

import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;
import pub.occams.elite.dubliner.domain.image.ClassifiedImage;

public class ExpansionSystem extends SystemBase {

    public OcrDataRectangle<Integer> potentialValueRectangle;
    public OcrDataRectangle<String> expansionRectangle;
    public OcrDataRectangle<String> oppositionRectangle;

    public final Integer potentialValue;
    public final Integer expansionTotal;
    public final Integer expansionTrigger;
    public final Integer oppositionTotal;
    public final Integer oppositionTrigger;

    public ExpansionSystem(final ClassifiedImage classifiedImage,
                           final OcrDataRectangle<String> systemNameRectangle, final String systemName,
                           final OcrDataRectangle<Integer> potentialValueRectangle,
                           final OcrDataRectangle<String> expansionRectangle,
                           final OcrDataRectangle<String> oppositionRectangle,
                           final Integer potentialValue, final Integer expansionTotal,
                           final Integer expansionTrigger, final Integer oppositionTotal, final Integer oppositionTrigger) {
        super(classifiedImage, systemNameRectangle, systemName);
        this.potentialValueRectangle = potentialValueRectangle;
        this.expansionRectangle = expansionRectangle;
        this.oppositionRectangle = oppositionRectangle;
        this.potentialValue = potentialValue;
        this.expansionTotal = expansionTotal;
        this.expansionTrigger = expansionTrigger;
        this.oppositionTotal = oppositionTotal;
        this.oppositionTrigger = oppositionTrigger;
    }

    @Override
    public String toString() {
        return super.toString() + "," +
                potentialValue + "," +
                expansionTotal + "," +
                expansionTrigger + "," +
                oppositionTotal + "," +
                oppositionTrigger;
    }
}
