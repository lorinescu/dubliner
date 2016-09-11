package pub.occams.elite.dubliner.domain.powerplay;

import pub.occams.elite.dubliner.domain.geometry.DataRectangle;
import pub.occams.elite.dubliner.domain.image.ClassifiedImage;

public class PreparationSystem extends SystemBase {

    public final DataRectangle<String> ccToSpendThisCycleRectangle;
    public final DataRectangle<String> prepRectangle;
    public final DataRectangle<String> costRectangle;
    public final DataRectangle<String> preparationHighestContributionRectangle;

    public final Integer ccToSpendThisCycle;
    public final Power highestContributingPower;
    public final Integer highestContributingPowerAmount;
    public final Integer cost;
    public final Integer prep;

    public PreparationSystem(final ClassifiedImage classifiedImage, final DataRectangle<String> systemNameRectangle,
                             final String systemName, final DataRectangle<String> ccToSpendThisCycleRectangle,
                             final DataRectangle<String> prepRectangle, final DataRectangle<String> costRectangle,
                             final DataRectangle<String> preparationHighestContributionRectangle,
                             final Integer ccToSpendThisCycle, final Power highestContributingPower,
                             final Integer highestContributingPowerAmount, final Integer cost, final Integer prep) {
        super(classifiedImage, systemNameRectangle, systemName);
        this.ccToSpendThisCycleRectangle = ccToSpendThisCycleRectangle;
        this.prepRectangle = prepRectangle;
        this.costRectangle = costRectangle;
        this.preparationHighestContributionRectangle = preparationHighestContributionRectangle;
        this.ccToSpendThisCycle = ccToSpendThisCycle;
        this.highestContributingPower = highestContributingPower;
        this.highestContributingPowerAmount = highestContributingPowerAmount;
        this.cost = cost;
        this.prep = prep;
    }

    @Override
    public String toString() {
        return super.toString() + ","
                + ccToSpendThisCycle + ","
                + highestContributingPower + ","
                + highestContributingPowerAmount + ","
                + cost + ","
                + prep;
    }
}
