package pub.occams.elite.dubliner.domain.powerplay;

import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;

public class SystemBase {
    public PowerPlayImage classifiedImage;
    public OcrDataRectangle<String> systemNameRectangle;
    public String systemName;

    public SystemBase(final PowerPlayImage classifiedImage, final OcrDataRectangle<String> systemNameRectangle,
                      final String systemName) {
        this.classifiedImage = classifiedImage;
        this.systemNameRectangle = systemNameRectangle;
        this.systemName = systemName;
    }

    @Override
    public String toString() {
        return classifiedImage.power.data.name + ","
                + systemName;
    }
}
