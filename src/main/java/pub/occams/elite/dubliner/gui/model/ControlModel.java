package pub.occams.elite.dubliner.gui.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;
import pub.occams.elite.dubliner.dto.ocr.ControlDto;

public class ControlModel extends PowerPlayModel {

    public DataRectangle<String> costsRectangle;
    public DataRectangle<String> fortifyRectangle;
    public DataRectangle<String> undermineRectangle;

    public final IntegerProperty upkeepFromLastCycle = new SimpleIntegerProperty();

    public final IntegerProperty costIfFortified = new SimpleIntegerProperty();

    public final IntegerProperty costIfUndermined = new SimpleIntegerProperty();

    public final IntegerProperty baseIncome = new SimpleIntegerProperty();

    public final IntegerProperty fortifyTotal = new SimpleIntegerProperty();

    public final IntegerProperty fortifyTrigger = new SimpleIntegerProperty();

    public final IntegerProperty undermineTotal = new SimpleIntegerProperty();

    public final IntegerProperty undermineTrigger = new SimpleIntegerProperty();

    public ControlModel(final ClassifiedImage classifiedImage, final DataRectangle<String> systemNameRectangle,
                        final String systemName, final DataRectangle<String> costsRectangle,
                        final DataRectangle<String> fortifyRectangle, final DataRectangle<String> undermineRectangle,
                        final Integer upkeepFromLastCycle, final Integer costIfFortified, final Integer costIfUndermined,
                        final Integer baseIncome, final Integer fortifyTotal, final Integer fortifyTrigger,
                        final Integer undermineTotal, final Integer undermineTrigger) {
        super(classifiedImage, systemNameRectangle, systemName);
        this.costsRectangle = costsRectangle;
        this.fortifyRectangle = fortifyRectangle;
        this.undermineRectangle = undermineRectangle;
        this.upkeepFromLastCycle.set(upkeepFromLastCycle);
        this.costIfFortified.set(costIfFortified);
        this.costIfUndermined.set(costIfUndermined);
        this.baseIncome.set(baseIncome);
        this.fortifyTotal.set(fortifyTotal);
        this.fortifyTrigger.set(fortifyTrigger);
        this.undermineTotal.set(undermineTotal);
        this.undermineTrigger.set(undermineTrigger);
    }

    @Override
    public String toString() {
        return systemName.get() +
                "," + upkeepFromLastCycle.get() +
                "," + costIfFortified.get() +
                "," + costIfUndermined.get() +
                "," + baseIncome.get() +
                "," + fortifyTotal.get() +
                "," + fortifyTrigger.get() +
                "," + undermineTotal.get() +
                "," + undermineTrigger.get();
    }

    public static ControlModel fromDto(final ControlDto dto) {
        return new ControlModel(
                dto.classifiedImage, dto.systemNameRectangle, dto.systemName, dto.costsRectangle, dto.fortifyRectangle,
                dto.undermineRectangle, dto.upkeepFromLastCycle, dto.costIfFortified, dto.costIfUndermined, dto.baseIncome,
                dto.fortifyTotal, dto.fortifyTrigger, dto.undermineTotal, dto.undermineTrigger
        );
    }
}
