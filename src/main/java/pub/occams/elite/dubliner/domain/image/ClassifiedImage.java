package pub.occams.elite.dubliner.domain.image;

import pub.occams.elite.dubliner.domain.powerplay.Power;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;

public class ClassifiedImage {

    private final InputImage inputImage;
    private final DataRectangle<ImageType> type;
    private final DataRectangle<Power> power;

    public ClassifiedImage(final InputImage inputImage,
                           final DataRectangle<ImageType> type,
                           final DataRectangle<Power> power) {
        this.inputImage = inputImage;
        this.type = type;
        this.power = power;
    }

    public InputImage getInputImage() {
        return inputImage;
    }

    public DataRectangle<ImageType> getType() {
        return type;
    }

    public DataRectangle<Power> getPower() {
        return power;
    }
}
