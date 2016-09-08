package pub.occams.elite.dubliner.util;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;
import pub.occams.elite.dubliner.domain.geometry.LineSegment;
import pub.occams.elite.dubliner.domain.geometry.Rectangle;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_SIMPLEX;
import static org.bytedeco.javacpp.opencv_core.bitwise_not;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class ImageUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.LOGGER_NAME);

    private static final OpenCVFrameConverter.ToIplImage CONVERTER1 = new OpenCVFrameConverter.ToIplImage();
    private static final OpenCVFrameConverter.ToMat CONVERTER2 = new OpenCVFrameConverter.ToMat();
    private static final Java2DFrameConverter CONVERTER3 = new Java2DFrameConverter();

    private static final short[] invertTable;

    static {
        invertTable = new short[256];
        for (int i = 0; i < 256; i++) {
            invertTable[i] = (short) (255 - i);
        }
    }

    private static final Scalar BLUE = new Scalar(255, 0, 0, 0);

    public static Optional<Mat> readImageFromFile(final File file) {
        final Mat img = imread(file.getAbsolutePath());
        if (null != img.data()) {
            return Optional.of(img);
        }
        return Optional.empty();
    }

    public static Optional<Mat> crop(final Rectangle r, final Mat in) {

        final Rect roi = new Rect(r.x0, r.y0, r.x1 - r.x0, r.y1 - r.y0);
        final Mat out = new Mat(in, roi);
        if (null == out.data()) {
            return Optional.empty();
        }
        return Optional.of(out);
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

    //dummy impl
    public static Mat filterRedAndBinarize(final Mat image, final int minRed) {
        return image;
    }

    public static Mat drawSegments(final Mat in, final List<LineSegment> segments) {

        if (null == in || null == in.data()) {
            return new Mat();
        }

        final Mat out = new Mat();
        in.copyTo(out);
        for (final LineSegment s : segments) {
            final opencv_core.Point b = new opencv_core.Point(s.x0, s.y0);
            final opencv_core.Point e = new opencv_core.Point(s.x1, s.y1);
            line(out, b, e, BLUE);
            if (null != s.name && !s.name.isEmpty()) {
                putText(out, s.name, b, FONT_HERSHEY_SIMPLEX, 0.5, BLUE);
            }
        }
        return out;
    }

    public static Mat drawDataRectangles(final Mat in, final DataRectangle... rects) {
        if (null == in || null == in.data()) {
            return new Mat();
        }

        final Mat out = new Mat();
        in.copyTo(out);
        for (final DataRectangle dr : rects) {
            final Rectangle r = dr.getRectangle();
            final opencv_core.Point b = new opencv_core.Point(r.x0, r.y0);
            final opencv_core.Point e = new opencv_core.Point(r.x1, r.y1);
            rectangle(out, b, e, BLUE);
            if (null != dr.getData()) {
                putText(out, dr.getData().toString(), b, FONT_HERSHEY_SIMPLEX, 1, BLUE);
            }
        }
        return out;
    }

    public static Mat invert(final Mat in) {
        final int w = in.cols();
        final int h = in.rows();

        final Mat out = new Mat(new Size(w, h));

        bitwise_not(in, out);

        return out;
    }

    public static Mat scale(final Mat in) {

        int newW = in.cols() * 4;
        int newH = in.rows() * 4;
        final Size size = new Size(newW, newH);

        final Mat out = new Mat(size);

        resize(in, out, size, 0, 0, INTER_CUBIC);

        return out;
    }

    public static BufferedImage matToBufferedImage(Mat m) {
        return CONVERTER3.convert(CONVERTER2.convert(m));
    }

    public static int distanceBetweenPoints(final int x0, final int y0, final int x1, final int y1) {
        double xd = x0 - x1;
        double yd = y0 - y1;
        return (int) Math.sqrt(xd * xd + yd * yd);
    }
}
