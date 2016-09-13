package pub.occams.elite.dubliner.domain.image;

import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;
import pub.occams.elite.dubliner.domain.powerplay.Power;

public class ClassifiedImage {

    public final InputImage inputImage;
    public final OcrDataRectangle<ImageType> type;
    public final OcrDataRectangle<Power> power;

    public ClassifiedImage(final InputImage inputImage,
                           final OcrDataRectangle<ImageType> type,
                           final OcrDataRectangle<Power> power) {
        this.inputImage = inputImage;
        this.type = type;
        this.power = power;
    }

}
