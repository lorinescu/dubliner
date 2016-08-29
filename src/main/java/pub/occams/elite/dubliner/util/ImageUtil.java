package pub.occams.elite.dubliner.util;

import org.apache.log4j.Logger;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.dto.settings.SegmentDto;
import pub.occams.elite.dubliner.dto.settings.SegmentsCoordinatesDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class ImageUtil {

    private static final Logger LOGGER = Logger.getLogger(App.LOGGER_NAME);

    private static final short[] invertTable;

    static {
        invertTable = new short[256];
        for (int i = 0; i < 256; i++) {
            invertTable[i] = (short) (255 - i);
        }
    }

    public static Optional<BufferedImage> readImageFromFile(final File file) {
        try {
            return Optional.of(ImageIO.read(file));
        } catch (IOException e) {
            LOGGER.error("Could not read image from file: " + file.getName(), e);
        }
        return Optional.empty();
    }

    public static Optional<SegmentsCoordinatesDto> getCoordinatesForImage(final BufferedImage image,
                                                                          final SettingsDto settings) {
        return settings.segmentsCoordinates
                .stream()
                .filter(coord -> coord.screenWidth.equals(image.getWidth()) && coord.screenHeight.equals(image.getHeight()))
                .findFirst();
    }

    public static Optional<BufferedImage> crop(final SegmentDto coord, final BufferedImage image) {
        try {
            return Optional.of(image.getSubimage(coord.x, coord.y, coord.width, coord.height));
        } catch (RasterFormatException e) {
            LOGGER.error("Failed to crop image, incorrect bounds");
        }
        return Optional.empty();
    }

    public static BufferedImage filterRedAndBinarize(final BufferedImage image, final int minRed) {
        final BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
//                int alpha = rgb >> 24 & 0xff;
                int red = rgb >> 16 & 0xff;
//                int green = rgb >> 8 & 0xff;
//                int blue = rgb & 0xff;

                int color = 2147483647;
                if (red > minRed) {
                    color = 0;
                }
                output.setRGB(x, y, color);
            }
        }
        return output;
    }

    public static BufferedImage invert(final BufferedImage image) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        final BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final BufferedImageOp invertOp = new LookupOp(new ShortLookupTable(0, invertTable), null);
        return invertOp.filter(image, dst);
    }


    public static BufferedImage scale(final BufferedImage image) {
        int newW = image.getWidth() * 4;
        int newH = image.getHeight() * 4;
        final BufferedImage scaledImage = new BufferedImage(newW, newH, image.getType());
        final Graphics2D g = scaledImage.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(image, 0, 0, newW, newH, null);

        return scaledImage;
    }

    //returns the % of black pixels - histogram for a black/white image
    public static double percentBlack(final BufferedImage image) {
        final long totalPixels = image.getHeight() * image.getWidth();
        long blackPixels = 0;
        for (int x = 0; x<image.getWidth(); x++) {
            for (int y = 0; y <image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);
                if ( 0 == (pixel & 0x00FFFFFF)) {
                    blackPixels++;
                }
            }
        }
        return (double) blackPixels * 100 / totalPixels;
    }
}
