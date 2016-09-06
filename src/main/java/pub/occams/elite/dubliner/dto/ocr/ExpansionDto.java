package pub.occams.elite.dubliner.dto.ocr;

import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;

public class ExpansionDto extends PowerPlayDto {

    public ExpansionDto(ClassifiedImage classifiedImage, DataRectangle<String> systemNameRectangle, String systemName) {
        super(classifiedImage, systemNameRectangle, systemName);
    }
}
