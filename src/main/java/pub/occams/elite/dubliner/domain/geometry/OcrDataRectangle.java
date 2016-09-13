package pub.occams.elite.dubliner.domain.geometry;

public class OcrDataRectangle<CORRECTED> {

    public final String rawData;
    public final CORRECTED data;
    public final Rectangle rectangle;


    public OcrDataRectangle(final String rawData, final CORRECTED data, final Rectangle rectangle) {
        this.rawData = rawData;
        this.data = data;
        this.rectangle = rectangle;
    }
}
