package pub.occams.elite.dubliner.dto.ocr;

import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;

public class PreparationDto extends PowerPlayDto {

    public PreparationDto(ClassifiedImage classifiedImage, DataRectangle<String> systemNameRectangle, String systemName) {
        super(classifiedImage, systemNameRectangle, systemName);
    }
}
