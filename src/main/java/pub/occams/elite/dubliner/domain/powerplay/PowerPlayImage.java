package pub.occams.elite.dubliner.domain.powerplay;

import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;

public class PowerPlayImage {

    public final OcrDataRectangle<Power> power;
    public OcrDataRectangle<String> systemNameRectangle;
    public String systemName;

    public PowerPlayImage(final OcrDataRectangle<Power> power, final OcrDataRectangle<String> systemNameRectangle,
                          final String systemName) {
        this.power = power;
        this.systemNameRectangle = systemNameRectangle;
        this.systemName = systemName;
    }

    @Override
    public String toString() {
        return power.data.name + ","
                + systemName;
    }
}
