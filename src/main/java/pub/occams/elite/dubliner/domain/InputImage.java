package pub.occams.elite.dubliner.domain;

import java.awt.image.BufferedImage;
import java.io.File;

public class InputImage {

    private final File file;
    private final BufferedImage image;

    public InputImage(File file, BufferedImage image) {
        this.file = file;
        this.image = image;
    }

    public File getFile() {
        return file;
    }

    public BufferedImage getImage() {
        return image;
    }
}
