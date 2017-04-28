package pub.occams.elite.dubliner.domain.image;

import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;

public class ProcessedImage<T> extends ClassifiedImage {

    public final T data;

    public ProcessedImage(final InputImage inputImage, final OcrDataRectangle<ImageType> type, final T data) {
        super(inputImage, type);
        this.data = data;
    }
}
