package pub.occams.elite.dubliner.dto.ocr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;

public class PowerPlayDto {
    @JsonIgnore
    public ClassifiedImage classifiedImage;
    @JsonIgnore
    public DataRectangle<String> systemNameRectangle;

    @JsonProperty("systemName")
    public String systemName;

    public PowerPlayDto(final ClassifiedImage classifiedImage, final DataRectangle<String> systemNameRectangle,
                        final String systemName) {
        this.classifiedImage = classifiedImage;
        this.systemNameRectangle = systemNameRectangle;
        this.systemName = systemName;
    }
}
