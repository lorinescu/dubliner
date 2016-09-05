package pub.occams.elite.dubliner.util;

import org.apache.log4j.Logger;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;
import pub.occams.elite.dubliner.domain.geometry.LineSegment;
import pub.occams.elite.dubliner.domain.geometry.Rectangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ImageUtil {

    private static final Logger LOGGER = Logger.getLogger(App.LOGGER_NAME);

    private static final OpenCVFrameConverter.ToIplImage CONVERTER1 = new OpenCVFrameConverter.ToIplImage();
    private static final Java2DFrameConverter CONVERTER2 = new Java2DFrameConverter();

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

    public static Optional<BufferedImage> crop(final Rectangle r, final BufferedImage image) {
        try {
            return Optional.of(image.getSubimage(r.x0, r.y0, r.x1 - r.x0, r.y1 - r.y0));
        } catch (RasterFormatException e) {
            LOGGER.error("Failed to crop image" + image.getWidth() + "x" + image.getHeight() +
                    ", incorrect bounds for rectangle:" + r.toString());
        }
        return Optional.empty();
    }

    public static Rectangle scale(final Rectangle r, final double factor) {
        final int width = r.x1 - r.x0;
        final double newWidth = width * factor;
        final int widthDelta = (int) (width - newWidth) / 2;

        final int height = r.y1 - r.y0;
        final double newHeight = height * factor;
        final int heightDelta = (int) (height - newHeight) / 2;

        return new Rectangle(r.x0 + widthDelta, r.y0 + heightDelta, r.x1 - widthDelta, r.y1 - heightDelta);
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

    public static BufferedImage drawSegments(final BufferedImage image, final List<LineSegment> segments) {
        final BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics g = output.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        Graphics2D g2 = output.createGraphics();
        int i = 0;
        for (final LineSegment s : segments) {
            g2.setColor(Color.BLUE);
            BasicStroke stroke = new BasicStroke(3);
            g2.setStroke(stroke);
            g2.drawLine(s.x0, s.y0, s.x1, s.y1);
            g2.drawString("L=" + i, s.x0, s.y0);
            i++;
        }
        g2.dispose();
        return output;
    }

    public static BufferedImage drawDataRectangles(final BufferedImage image, final DataRectangle... rects) {
        final BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics g = output.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        Graphics2D g2 = output.createGraphics();
        for (final DataRectangle dr : rects) {
            g2.setColor(Color.BLUE);
            BasicStroke stroke = new BasicStroke(3);
            g2.setStroke(stroke);
            g2.setFont(new Font("Serif", Font.BOLD, 14));
            final Rectangle r = dr.getRectangle();
            g2.drawRect(r.x0, r.y0, r.x1 - r.x0, r.y1 - r.y0);
            g2.drawString(dr.getData().toString(), r.x0 + 10, r.y0 + 10);
        }
        g2.dispose();
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
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int pixel = image.getRGB(x, y);
                if (0 == (pixel & 0x00FFFFFF)) {
                    blackPixels++;
                }
            }
        }
        return (double) blackPixels * 100 / totalPixels;
    }

    public static BufferedImage iplImageToBufferedImage(final IplImage image) {
        return CONVERTER2.getBufferedImage(CONVERTER1.convert(image));
    }

    public static IplImage bufferedImageToIplImage(final BufferedImage image) {
        return CONVERTER1.convert(CONVERTER2.convert(image));
    }

    public static int distanceBetweenPoints(final int x0, final int y0, final int x1, final int y1) {
        double xd = x0 - x1;
        double yd = y0 - y1;
        return (int) Math.sqrt(xd * xd + yd * yd);
    }
}
