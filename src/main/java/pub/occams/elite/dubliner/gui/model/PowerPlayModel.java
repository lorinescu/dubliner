package pub.occams.elite.dubliner.gui.model;

import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;

public class PowerPlayModel {
    public final ClassifiedImage classifiedImage;
    public final DataRectangle<String> systemNameRectangle;

    public final String systemName;

    public PowerPlayModel(ClassifiedImage classifiedImage, DataRectangle<String> systemNameRectangle,
                          final String systemName) {
        this.classifiedImage = classifiedImage;
        this.systemNameRectangle = systemNameRectangle;
        this.systemName = systemName;
    }
}
