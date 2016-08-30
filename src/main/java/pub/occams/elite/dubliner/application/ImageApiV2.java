package pub.occams.elite.dubliner.application;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.*;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.*;
import pub.occams.elite.dubliner.dto.settings.RectangleDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static pub.occams.elite.dubliner.domain.ImageType.PP_CONTROL;
import static pub.occams.elite.dubliner.domain.ImageType.UNKNOWN;
import static pub.occams.elite.dubliner.util.ImageUtil.*;
import static pub.occams.elite.dubliner.util.ImageUtil.invert;

public class ImageApiV2 extends ImageApiBase {

    public ImageApiV2(final SettingsDto settings, final boolean debug) {
        super(settings, debug);
    }

    private List<LineSegment> detectLines(final BufferedImage image, final File file, final int pixelResolution,
                                          final int threshold, final int minLength, final int maxGap) {
        final IplImage img = ImageUtil.bufferedImageToIplImage(image);
        final IplImage dst = cvCreateImage(cvGetSize(img), img.depth(), 1);

        cvCanny(img, dst, 400, 500, 3);
        saveImageAtStage(ImageUtil.iplImageToBufferedImage(dst), file, "line-detection-begin");

        final CvMemStorage storage = cvCreateMemStorage(0);

        final double angleResolution = CV_PI / 2;
        final CvSeq lines = cvHoughLines2(dst, storage, CV_HOUGH_PROBABILISTIC, pixelResolution, angleResolution,
                threshold, minLength, maxGap, 0, CV_PI);

        final List<LineSegment> segments = new ArrayList<>();
        for (int i = 0; i < lines.total(); i++) {
            final Pointer line = new CvPoint2D32f(cvGetSeqElem(lines, i));

            final CvPoint pt1 = new CvPoint(line.position(0));
            final CvPoint pt2 = new CvPoint(line.position(1));
            segments.add(new LineSegment(pt1.x(), pt1.y(), pt2.x(), pt2.y()));
        }

        if (debug) {
            CvFont font = new CvFont();
            cvInitFont(font, CV_FONT_HERSHEY_PLAIN, 2, 2);
            for (int i = 0; i < segments.size(); i++) {
                final LineSegment s = segments.get(i);
                final CvPoint pt1 = new CvPoint(s.x0, s.y0);
                final CvPoint pt2 = new CvPoint(s.x1, s.y1);
                cvLine(img, pt1, pt2, CV_RGB(0, 0, 255), 2, CV_AA, 0);
                cvPutText(img, "L=" + i, pt1, font, CvScalar.BLUE);
            }
            saveImageAtStage(ImageUtil.iplImageToBufferedImage(img), file, "line-detection-end");
        }
        return segments;
    }

    private List<LineSegment> detectHorizontalLines(final BufferedImage image, final File file,
                                                    final int pixelResolution, final int threshold, final int minLength,
                                                    final int maxGap, final int maxLength) {
        return detectLines(image, file, pixelResolution, threshold, minLength, maxGap)
                .stream()
                .filter(s -> s.y0 == s.y1 && s.x1 - s.x0 <= maxLength)
                .sorted((s1, s2) -> s1.y0 - s2.y0)
                .collect(Collectors.toList());
    }

    private List<LineSegment> detectVerticalLines(final BufferedImage image, final File file,
                                                  final int pixelResolution, final int threshold, final int minLength,
                                                  final int maxGap, final int maxLength) {
        return detectLines(image, file, pixelResolution, threshold, minLength, maxGap)
                .stream()
                .filter(s -> s.x0 == s.x1 && s.y1 - s.y0 <= maxLength)
                .sorted((s1, s2) -> s1.x0 - s2.x0)
                .collect(Collectors.toList());
    }

    private Optional<ControlSystem> extractControl(final ClassifiedImage input) {
        return Optional.empty();
    }

    @Override
    public Optional<ClassifiedImage> classify(final InputImage inputImage) {

        final File file = inputImage.getFile();
        final BufferedImage originalImage = inputImage.getImage();
        LOGGER.info("classification start: " + file.getAbsolutePath());

        //crop aprox a band from the top of the image which should contain the power play tabs
        final RectangleDto rect1 = new RectangleDto();
        rect1.x = 0;
        rect1.y = 0;
        rect1.height = (int) (originalImage.getHeight() * 0.20);
        rect1.width = originalImage.getWidth();
        final Optional<BufferedImage> maybeImage2 = ImageUtil.crop(rect1, originalImage);
        if (!maybeImage2.isPresent()) {
            return Optional.empty();
        }

        final BufferedImage image2 = maybeImage2.get();
        saveImageAtStage(image2, file, "classify-aprox-cropped");

        //find the 2 horizontal lines that are above and on top of the tab menu
        final int minLength1 = (int) (image2.getWidth() * 0.9);
        final int maxLength1 = image2.getWidth();
        final List<LineSegment> segments1 = detectHorizontalLines(image2, file, 5, 900, minLength1, 0, maxLength1);
        if (segments1.size() < 2) {
            return Optional.empty();
        }

        //do an exact cropping of the power play tabs
        final RectangleDto rect2 = new RectangleDto();
        rect2.x = segments1.get(0).x0;
        rect2.y = segments1.get(0).y0;
        //there are two lines very close to each other, sometimes they are detected separately
        rect2.width = segments1.get(segments1.size() - 1).x1 - rect2.x;
        rect2.height = segments1.get(segments1.size() - 1).y1 - rect2.y;
        final Optional<BufferedImage> maybeImage3 = ImageUtil.crop(rect2, image2);
        if (!maybeImage3.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage image3 = maybeImage3.get();
        saveImageAtStage(image3, file, "classify-precise-cropped");

        //find the horizontal lines that are above and below the selected tab (part of the rectangle)
        final int minLength2 = rect2.width / 8; //aprox. width of a tab
        final int maxLength2 = (int) (rect2.width * 0.7);
        final List<LineSegment> segments2 = detectHorizontalLines(image3, file, 1, 200, minLength2, 2, maxLength2);
        if (segments2.size() != 2) {
            return Optional.empty();
        }
        final RectangleDto rect3 = new RectangleDto();
        rect3.x = segments2.get(0).x0 + 5; //shaving the vertical band from the beggining of the tab rectangle
        rect3.y = segments2.get(0).y0;
        rect3.width = segments2.get(1).x1 - rect3.x;
        rect3.height = segments2.get(1).y1 - rect3.y;

        final Optional<BufferedImage> maybeImage4 = ImageUtil.crop(rect3, image3);
        if (!maybeImage4.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage image4 = maybeImage4.get();
        saveImageAtStage(image4, file, "classify-precise-tab-cropped");

        final BufferedImage ocrInputImage = invert(scale(image4));

        saveImageAtStage(ocrInputImage, file, "classify-ocr-tab-input");

        final String str = ocrRectangle(ocrInputImage);
        if (null == str || str.isEmpty()) {
            return Optional.empty();
        }

        final String title = str.trim().replace("0", "O");
        LOGGER.info("Ocr raw:[" + str + "], corrected:[" + title + "]");
        final ImageType type;
        if ("PREPARATION".equals(title)) {
            type = ImageType.PP_PREPARATION;
        } else if ("EXPANSION".equals(title)) {
            type = ImageType.PP_EXPANSION;
        } else if ("CONTROL".equals(title)) {
            type = PP_CONTROL;
        } else if ("TURMOIL".equals(title)) {
            type = ImageType.PP_TURMOIL;
        } else {
            type = ImageType.UNKNOWN;
        }

        //crop aprox a band from the top of the image which should contain the power play tabs
        final RectangleDto rect4 = new RectangleDto();
        rect4.x = 0;
        rect4.y = segments1.get(segments1.size() - 1).y0;
        rect4.height = 100;
        rect4.width = originalImage.getWidth();
        final Optional<BufferedImage> maybeImage5 = ImageUtil.crop(rect4, originalImage);
        if (!maybeImage5.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage image5 = maybeImage5.get();
        saveImageAtStage(image5, file, "classify-power-name");

        LOGGER.info("classification end:" + file.getAbsolutePath() + ", type is: " + type);

        return Optional.of(new ClassifiedImage(inputImage, type, segments1.get(segments1.size() - 1)));
    }

    private List<LineSegment> mergeSegments(final BufferedImage img, final File file,final List<LineSegment> segments,
                                            final boolean
            isVertical,
                                            final int threshold) {
        final List<LineSegment> out = new ArrayList<>();
        boolean mergeOccured = false;
        for (int i = 1; i < segments.size(); i++) {
            final LineSegment prev = segments.get(i - 1);
            final LineSegment curr = segments.get(i);
            int delta = 0;
            if (isVertical) {
                delta = curr.x0 - prev.x0;
            } else {
                delta = curr.y0 - prev.y0;
            }
            if (delta < threshold) {
                out.add(prev);
                mergeOccured = true;
            } else {
                out.add(curr);
            }
        }
        saveImageAtStage(ImageUtil.drawSegments(img, out), file, "classify2-merged-progress");
        if (mergeOccured) {
            return mergeSegments(img, file, out, isVertical, threshold);
        }
        return out;
    }

    public Optional<ClassifiedImage> classify2(final InputImage inputImage) {
        final File file = inputImage.getFile();
        final BufferedImage originalImage = inputImage.getImage();
        LOGGER.info("classification start: " + file.getAbsolutePath());

        final BufferedImage img = filterRedAndBinarize(originalImage, 85);
//        final BufferedImage img = originalImage;
        saveImageAtStage(img, file, "classify2-filterRedAndBinarize");

        final List<LineSegment> segments = detectLines(img, file, 1, 80, 200, 0);
        final List<LineSegment> horizontals = segments
                .stream()
                .filter(s -> s.y0 == s.y1)
                .sorted((s1, s2) -> s1.y0 - s2.y0)
                .collect(Collectors.toList());
        final List<LineSegment> verticals = segments
                .stream()
                .filter(s -> s.x0 == s.x1)
                .sorted((s1, s2) -> s1.x0 - s2.x0)
                .collect(Collectors.toList());
        final int joinThreshold = 3; //if two lines are less than n px apart they are considered a single line
        final List<LineSegment> mergedHorizontals = mergeSegments(img, file, horizontals, false, joinThreshold);
        final List<LineSegment> mergedVerticals = mergeSegments(img, file, verticals, true, joinThreshold);


        final List<LineSegment> merged = new ArrayList<>(mergedHorizontals);
        merged.addAll(mergedVerticals);
        final BufferedImage img2 = ImageUtil.drawSegments(img, merged);

        saveImageAtStage(img2, file, "classify2-merged");

        final ImageType type = UNKNOWN;
        LOGGER.info("classification end:" + file.getAbsolutePath() + ", type is: " + type);

        return Optional.empty();
//        return Optional.of(new ClassifiedImage(inputImage, type, segments1.get(segments1.size() - 1)));
    }


    public Optional<ControlSystem> extract(final ClassifiedImage input) {
        switch (input.getType()) {
            case PP_CONTROL:
                return extractControl(input);
            case PP_EXPANSION:
                return Optional.empty();
            case PP_PREPARATION:
                return Optional.empty();
            case PP_TURMOIL:
                return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public List<ControlSystem> extractDataFromImages(final List<File> files) {

        files
                .stream()
                .map(this::load)
                .filter(Optional::isPresent)
                .map(Optional::get)
//                .map(this::classify)
                .map(this::classify2)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(img -> ImageType.UNKNOWN != img.getType())
                .collect(Collectors.toList());
//        files
//                .stream()
//                .map(this::prepareAndClassifyImage)
//                .filter(img -> ImageType.UNKNOWN != img.getType())
//                .map(this::detectLines)
//                .collect(Collectors.toList());
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
                    new File("data/control_images/1920x1080/mahon/control/1.bmp"),
                    new File("data/control_images/1920x1080/ald/control/1.bmp"),
                    new File("data/control_images/1920x1200/ald/control/1.bmp"),
                    new File("data/control_images/1920x1200/mahon/control/1.bmp"),
                    new File("data/control_images/1920x1200/mahon/control/4.bmp"),
                    new File("data/control_images/1920x1200/mahon/expansion/1.bmp"),
                    new File("data/control_images/1920x1200/ald/expansion/1.bmp"),
                    new File("data/control_images/1920x1080/antal/preparation/1.bmp"),
                    new File("data/control_images/1920x1200/ald/preparation/1.bmp")

            );
            api.extractDataFromImages(images);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
