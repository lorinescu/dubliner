package pub.occams.elite.dubliner.playground;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage;

import javax.swing.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;

public class OpenCVLineDetection {

    private static final double RADIANS_IN_A_DEGREE = Math.PI / 180;
    private static final String IMAGE_FILE = "data/control_images/1920x1080_10.bmp";

    public static void main(String[] args) {
        testStandardHough();
    }

    private static void testStandardHough() {
        IplImage src = cvLoadImage(IMAGE_FILE, 0);

        if (src == null) {
            System.out.println("Couldn't load source image.");
            return;
        }

        final IplImage dst = cvCreateImage(cvGetSize(src), src.depth(), 1);
        final IplImage colorDst = cvCreateImage(cvGetSize(src), src.depth(), 3);

        cvCanny(src, dst, 80, 200, 3);
        cvCvtColor(dst, colorDst, CV_GRAY2BGR);

        final CvMemStorage storage = cvCreateMemStorage(0);

        final CvSeq lines = cvHoughLines2(dst, storage, CV_HOUGH_STANDARD, 1, RADIANS_IN_A_DEGREE, 300);

        for (int i = 0; i < lines.total(); i++) {
            final CvPoint2D32f point = new CvPoint2D32f(cvGetSeqElem(lines, i));

            float rho = point.x();
            float theta = point.y();

            if (!isHorizontalOrVerticalLine(theta)) {
                continue;
            }
            double a = Math.cos((double) theta), b = Math.sin((double) theta);
            double x0 = a * rho, y0 = b * rho;
            CvPoint pt1 = cvPoint((int) Math.round(x0 + 1000 * (-b)), (int) Math.round(y0 + 1000 * (a))), pt2 = cvPoint((int) Math.round(x0 - 1000 * (-b)), (int) Math.round(y0 - 1000 * (a)));
            System.out.println("Line spotted: ");
            System.out.println("\t rho= " + rho);
            System.out.println("\t theta= " + theta);
            cvLine(colorDst, pt1, pt2, CV_RGB(255, 0, 0), 3, CV_AA, 0);
        }

        showResultAndSrcImage(src, colorDst);
    }

    private static boolean isHorizontalOrVerticalLine(final float theta) {
        if (theta > 0.0001 && theta < Math.PI / 2 - 0.0001) {
            return false;
        }
        return true;
    }

    private static void showResultAndSrcImage(final IplImage src, final IplImage result) {
        final CanvasFrame source = new CanvasFrame("Source");
        source.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        source.showImage(new ToIplImage().convert(src));

        final CanvasFrame hough = new CanvasFrame("Hough");
        hough.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        hough.showImage(new ToIplImage().convert(result));
    }
}