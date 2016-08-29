package pub.occams.elite.dubliner.application;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.domain.ImageType;
import pub.occams.elite.dubliner.domain.InputImage;
import pub.occams.elite.dubliner.dto.settings.SegmentDto;
import pub.occams.elite.dubliner.dto.settings.SegmentsCoordinatesDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.util.ImageUtil;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static pub.occams.elite.dubliner.util.ImageUtil.*;
import static pub.occams.elite.dubliner.util.ImageUtil.invert;

public class ImageApiV2 extends ImageApiBase {

    public ImageApiV2(final SettingsDto settings, final boolean debug) {
        super(settings, debug);
    }

    private String ocrTab(final BufferedImage image, final SegmentDto coordinates,
                          final String fileName, final String step) {

        final Optional<BufferedImage> croppedImage = crop(coordinates, image);
        if (!croppedImage.isPresent()) {
            return "";
        }

        final BufferedImage ocrInputImage = scale(
                filterRedAndBinarize(invert(croppedImage.get()), settings.ocr.filterRedChannelMin)
        );

        saveImageAtStage(ocrInputImage, fileName, step);
        final String title = ocrSegment(ocrInputImage);
        if (null == title) {
            return "";
        }
        return title;
    }

    // - the tab title should be tabName
    // - the majority of pixels (depends on the cropping area) should be white if the tab is selected
    private boolean isTabSelected(final BufferedImage image, final SegmentDto coordinates, final String tabName,
                                  final String fileName, final String step) {

        final Optional<BufferedImage> croppedImage = crop(coordinates, image);
        if (!croppedImage.isPresent()) {
            return false;
        }

        final BufferedImage ocrInputImage = scale(
                filterRedAndBinarize(invert(croppedImage.get()), settings.ocr.filterRedChannelMin)
        );

        saveImageAtStage(ocrInputImage, fileName, step);
        final String title = ocrSegment(ocrInputImage);
        if (null == title) {
            return false;
        }

        if (!tabName.equals(title.trim().replace("0", "O"))) {
            return false;
        }

        final double blacknessPct = ImageUtil.percentBlack(ocrInputImage);
        if (blacknessPct < 50) {
            return true;
        }
        return false;
    }

    @Override
    public InputImage classifyImage(final File file) {

        LOGGER.info("verifying if file " + file.getName() + " is a power play screenshot");

        final Optional<BufferedImage> maybeImage = ImageUtil.readImageFromFile(file);
        if (!maybeImage.isPresent()) {
            return new InputImage(file, null, ImageType.UNKNOWN);
        }

        final BufferedImage image = maybeImage.get();

        final Optional<SegmentsCoordinatesDto> maybeCoord = ImageUtil.getCoordinatesForImage(image, settings);
        if (!maybeCoord.isPresent()) {
            LOGGER.info("Could not find coordinates settings for image " + file.getName() +
                    " at " + image.getWidth() + "x" + image.getHeight());
            return new InputImage(file, image, ImageType.UNKNOWN);
        }
        final SegmentsCoordinatesDto coord = maybeCoord.get();

        //1. if this is a screenshot from Power Play we should have an Overview tab in left upper corner
        LOGGER.info("ocr-ing selected tab title");
        final String overviewTabText = ocrTab(image, coord.overviewTab, file.getName(), "classify-image-isPowerPlay");

        LOGGER.info("image " + file.getName() + " has [" + overviewTabText + "] in the Overview tab position");

        if (!"OVERVIEW".equals(overviewTabText.trim().replace("0", "O"))) {
            return new InputImage(file, image, ImageType.UNKNOWN);
        }

        //2. is there an interesting selected ?
        final ImageType type;
        if (isTabSelected(image, coord.preparationTab, "PREPARATION", file.getName(), "classify-image-preparation")) {
            type = ImageType.PREPARATION;
        } else if (isTabSelected(image, coord.expansionTab, "EXPANSION", file.getName(), "classify-image-expansion")) {
            type = ImageType.EXPANSION;
        } else if (isTabSelected(image, coord.controlTab, "CONTROL", file.getName(), "classify-image-control")) {
            type = ImageType.CONTROL;
        } else {
            type = ImageType.UNKNOWN;
        }

        LOGGER.info("Image:" + file.getName() + " type is: " + type);

        return new InputImage(file, image, type);
    }

    public InputImage detectLines(final InputImage image) {
        final IplImage img = ImageUtil.bufferedImageToIplImage(image.getImage());

        final IplImage dst = cvCreateImage(cvGetSize(img), img.depth(), 1);
        final IplImage colorDst = cvCreateImage(cvGetSize(img), img.depth(), 3);

        cvCanny(img, dst, 80, 200, 3);
        cvCvtColor(dst, colorDst, CV_GRAY2BGR);

        final CvMemStorage storage = cvCreateMemStorage(0);

        final double angleResolution = CV_PI / 180;
        final int pixelResolution = 1;
        final int threshold = 200;
        final int minLength = 200;
        final int maxGap = 1;
        final CvSeq lines = cvHoughLines2(dst, storage, CV_HOUGH_PROBABILISTIC, pixelResolution, angleResolution,
                threshold, minLength, maxGap, 0, CV_PI);
        for (int i = 0; i < lines.total(); i++) {
            final Pointer line = new CvPoint2D32f(cvGetSeqElem(lines, i));

            final CvPoint pt1 = new CvPoint(line.position(0));
            final CvPoint pt2 = new CvPoint(line.position(1));

//            System.out.println("Line spotted: ");
//            System.out.println("\t rho= " + rho);
//            System.out.println("\t theta= " + theta);
            System.out.println("coords: from ("+pt1.x()+","+pt1.y()+") to ("+pt2.x()+","+pt2.y()+")");
            cvLine(colorDst, pt1, pt2, CV_RGB(255, 0, 0), 2, CV_AA, 0);
            CvFont font = new CvFont();
            cvInitFont(font, CV_FONT_HERSHEY_PLAIN, 2, 2);
            cvPutText(colorDst,"L="+i,pt1,font,CvScalar.BLUE);
        }

        saveImageAtStage(ImageUtil.iplImageToBufferedImage(colorDst), image.getFile().getName(), "line-detection");
        return image;
    }

    @Override
    public List<ControlSystem> extractDataFromImages(final List<File> files) {

        files
                .stream()
                .map(this::classifyImage)
                .filter(img -> ImageType.UNKNOWN != img.getType())
                .map(this::detectLines)
                .collect(Collectors.toList());
           /*

        pipeline*:

        1. pick an image from the image folder
        2. if it is not a power play image goto 1.
        3. find the coordinates** of the selected tab
        5. crop the tab area, ocr the segment and apply corrections
        6. if the selected tab is not Preparation | Control | Expansion goto 1.
        ^^DONE^^
        7. find the coordinates of the power name
        8. crop the power name, ocr the segment and apply corrections
        9. if the power name is not Edmund | Winters | ..... goto 1.
        10. find the coordinates for:
            - system name
            - preparation tab:
            - control tab:
                - costs area
                - fortification area
                - undermining area
            - expansion tab:
        11. ocr all segments and apply corrections to system names/numbers
        12. produce a result object like:
        {
            maybe_debug: { misc data structures with detected coordinates, raw ocr results, refs to original file etc }
            power: xyz
            preparation: [..]
            control: [..]
            expansion: [..]
        }
        13. show the result
        14. optionally clean-up the processed files from the image folder

        ---
        * pipeline - single thread if the tesseract instance is shared
        ** find the coordinates - find a rectangle (x0,y0,x1,y1) that surrounds the area to be fed to ocr

        */
        return null;
    }

    public static void main(String[] args) {
        try {
            final ImageApiV2 api = new ImageApiV2(App.loadSettingsV2(), true);
            final List<File> images = Arrays.asList(
//                    new File("../dubliner-data-tmp/2560x1440_1.bmp"),
                    new File("../dubliner-data-tmp/1920x1080_26.bmp")
                    //                    new File("../dubliner-data-tmp/1600x900_1.bmp")

            );
            api.extractDataFromImages(images);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
