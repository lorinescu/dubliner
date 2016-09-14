package pub.occams.elite.dubliner.domain.powerplay;

import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;
import pub.occams.elite.dubliner.domain.image.ClassifiedImage;
import pub.occams.elite.dubliner.domain.image.ImageType;
import pub.occams.elite.dubliner.domain.image.InputImage;

public class PowerPlayImage extends ClassifiedImage {
    public final OcrDataRectangle<Power> power;

    public PowerPlayImage(final InputImage inputImage,
                          final OcrDataRectangle<ImageType> type,
                          final OcrDataRectangle<Power> power) {
        super(inputImage, type);
        this.power = power;
    }
}
