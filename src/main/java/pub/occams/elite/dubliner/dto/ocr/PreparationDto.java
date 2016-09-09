package pub.occams.elite.dubliner.dto.ocr;

import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.Power;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;

public class PreparationDto extends PowerPlayDto {

    public final Integer ccToSpendThisCycle;
    public final Power highestContributingPower;
    public final Integer highestContributingPowerAmount;
    public final Integer cost;
    public final Integer prep;

    public PreparationDto(final ClassifiedImage classifiedImage, final DataRectangle<String> systemNameRectangle,
                          final String systemName, final Integer ccToSpendThisCycle,
                          final Power highestContributingPower, final Integer highestContributingPowerAmount,
                          final Integer cost, final Integer prep) {
        super(classifiedImage, systemNameRectangle, systemName);
        this.ccToSpendThisCycle = ccToSpendThisCycle;
        this.highestContributingPower = highestContributingPower;
        this.highestContributingPowerAmount = highestContributingPowerAmount;
        this.cost = cost;
        this.prep = prep;
    }
}
