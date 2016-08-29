package pub.occams.elite.dubliner.domain;

import java.awt.image.BufferedImage;
import java.io.File;

public class InputImage {

    private final File file;
    private final BufferedImage image;
    private final ImageType type;

    public InputImage(final File file, final BufferedImage image, final ImageType type) {
        this.file = file;
        this.image = image;
        this.type = type;
    }

    public File getFile() {
        return file;
    }

    public BufferedImage getImage() {
        return image;
    }

    public ImageType getType() {
        return type;
    }
}
