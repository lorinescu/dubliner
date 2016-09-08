package pub.occams.elite.dubliner.domain;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;

public class InputImage {

    private final File file;
    private final Mat image;

    public InputImage(final File file, final Mat image) {
        this.file = file;
        this.image = image;
    }

    public File getFile() {
        return file;
    }

    public Mat getImage() {
        return image;
    }
}
