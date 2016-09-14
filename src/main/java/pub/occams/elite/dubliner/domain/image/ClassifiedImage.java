package pub.occams.elite.dubliner.domain.image;

import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;

public class ClassifiedImage {

    public final InputImage inputImage;
    public final OcrDataRectangle<ImageType> type;

    public ClassifiedImage(final InputImage inputImage,
                           final OcrDataRectangle<ImageType> type) {
        this.inputImage = inputImage;
        this.type = type;
    }

}
