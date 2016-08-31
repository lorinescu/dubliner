package pub.occams.elite.dubliner.domain;

import pub.occams.elite.dubliner.domain.geometry.LineSegment;

public class ClassifiedImage {
    private final InputImage inputImage;
    private final ImageType type;
    private final LineSegment lineBelowTabs;

    public ClassifiedImage(final InputImage inputImage, final ImageType type, final LineSegment lineBelowTabs) {
        this.inputImage = inputImage;
        this.type = type;
        this.lineBelowTabs = lineBelowTabs;
    }

    public InputImage getInputImage() {
        return inputImage;
    }

    public ImageType getType() {
        return type;
    }

    public LineSegment getLineBelowTabs() {
        return lineBelowTabs;
    }
}
