package pub.occams.elite.dubliner.domain.image;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;

public class InputImage {

    private final File file;
    private Mat image;

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

    public void nullImage() {
        image = null;
    }
}
