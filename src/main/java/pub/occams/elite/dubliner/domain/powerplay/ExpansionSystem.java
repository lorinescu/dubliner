package pub.occams.elite.dubliner.domain.powerplay;

import pub.occams.elite.dubliner.domain.geometry.DataRectangle;
import pub.occams.elite.dubliner.domain.image.ClassifiedImage;

public class ExpansionSystem extends SystemBase {

    public ExpansionSystem(ClassifiedImage classifiedImage, DataRectangle<String> systemNameRectangle, String systemName) {
        super(classifiedImage, systemNameRectangle, systemName);
    }
}
