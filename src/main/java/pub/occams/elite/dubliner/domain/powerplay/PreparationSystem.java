package pub.occams.elite.dubliner.domain.powerplay;

import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;
import pub.occams.elite.dubliner.domain.image.ClassifiedImage;

public class PreparationSystem extends SystemBase {

    public final OcrDataRectangle<Integer> ccToSpendThisCycleRectangle;
    public final OcrDataRectangle<Integer> prepRectangle;
    public final OcrDataRectangle<Integer> costRectangle;
    public final OcrDataRectangle<String> preparationHighestContributionRectangle;

    public final Integer ccToSpendThisCycle;
    public final Power highestContributingPower;
    public final Integer highestContributingPowerAmount;
    public final Integer cost;
    public final Integer prep;

    public PreparationSystem(final ClassifiedImage classifiedImage,
                             final OcrDataRectangle<String> systemNameRectangle,
                             final String systemName,
                             final OcrDataRectangle<Integer> ccToSpendThisCycleRectangle,
                             final OcrDataRectangle<Integer> prepRectangle,
                             final OcrDataRectangle<Integer> costRectangle,
                             final OcrDataRectangle<String> preparationHighestContributionRectangle,
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
                + prep + "," +
                classifiedImage.inputImage.getFile().getAbsolutePath();
    }
}
