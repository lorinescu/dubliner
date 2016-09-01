package pub.occams.elite.dubliner.application;

import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.*;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.*;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;
import pub.occams.elite.dubliner.domain.geometry.LineSegment;
import pub.occams.elite.dubliner.domain.geometry.Range;
import pub.occams.elite.dubliner.domain.geometry.Rectangle;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static pub.occams.elite.dubliner.domain.ImageType.*;
import static pub.occams.elite.dubliner.util.ImageUtil.*;
import static pub.occams.elite.dubliner.util.ImageUtil.invert;

public class ImageApiV2 extends ImageApiBase {

    public ImageApiV2(final SettingsDto settings, final boolean debug) {
        super(settings, debug);
    }

    private List<LineSegment> detectLines(final BufferedImage image, final File file, final int pixelResolution,
                                          final int threshold, final int maxGap,
                                          final int minLength, final int maxLength) {
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


            if (distanceBetweenPoints(pt1.x(), pt1.y(), pt2.x(), pt2.y()) > maxLength) {
                continue;
            }
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
                                                    final int pixelResolution, final int threshold, final int maxGap,
                                                    final int minLength, final int maxLength) {
        return detectLines(image, file, pixelResolution, threshold, maxGap, minLength, maxLength)
                .stream()
                .sorted((s1, s2) -> s1.y0 - s2.y0)
                .collect(Collectors.toList());
    }

    private List<LineSegment> detectVerticalLines(final BufferedImage image, final File file,
                                                  final int pixelResolution, final int threshold, final int maxGap,
                                                  final int minLength, final int maxLength) {
        return detectLines(image, file, pixelResolution, threshold, maxGap, minLength, maxLength)
                .stream()
                .sorted((s1, s2) -> s1.x0 - s2.x0)
                .collect(Collectors.toList());
    }

    private Optional<ControlSystem> extractControl(final ClassifiedImage input) {
        return Optional.empty();
    }

    //TODO: merge Horiz/Vert methods can be ... merged
    private List<LineSegment> mergeHorizontalSegments(final BufferedImage img, final File file,
                                                      final List<LineSegment> segments, final int threshold) {

        final Map<Range, LineSegment> clusters = new HashMap<>();

        for (final LineSegment s : segments) {
            Range foundRange = null;
            for (final Range range : clusters.keySet()) {
                if (s.y0 >= range.low && s.y0 <= range.high) {
                    foundRange = range;
                    break;
                }
            }
            final int low = s.y0 - threshold;
            final int high = s.y0 + threshold;
            if (null != foundRange) {
                final int newLow = low < foundRange.low ? low : foundRange.low;
                final int newHigh = high > foundRange.high ? high : foundRange.high;
                final Range newRange = new Range(newLow, newHigh);
                clusters.put(newRange, clusters.remove(foundRange));
            } else {
                clusters.put(new Range(low, high), s);
            }
        }
        final List<LineSegment> out = new ArrayList<>(clusters.values());
        out.sort((o1, o2) -> o1.y0 - o2.y0);
        saveImageAtStage(ImageUtil.drawSegments(img, out), file, "classify-merge-horizontal-progress");
        return out;
    }

    private List<LineSegment> mergeVerticalSegments(final BufferedImage img, final File file,
                                                    final List<LineSegment> segments, final int threshold) {

        final Map<Range, LineSegment> clusters = new HashMap<>();

        for (final LineSegment s : segments) {
            Range foundRange = null;
            for (final Range range : clusters.keySet()) {
                if (s.x0 >= range.low && s.x0 <= range.high) {
                    foundRange = range;
                    break;
                }
            }
            final int low = s.x0 - threshold;
            final int high = s.x0 + threshold;
            if (null != foundRange) {
                final int newLow = low < foundRange.low ? low : foundRange.low;
                final int newHigh = high > foundRange.high ? high : foundRange.high;
                final Range newRange = new Range(newLow, newHigh);
                clusters.put(newRange, clusters.remove(foundRange));
            } else {
                clusters.put(new Range(low, high), s);
            }
        }
        final List<LineSegment> out = new ArrayList<>(clusters.values());
        out.sort((o1, o2) -> o1.x0 - o2.x0);
        saveImageAtStage(ImageUtil.drawSegments(img, out), file, "classify-merge-vertical-progress");
        return out;
    }

    private Optional<DataRectangle<ImageType>> detectSelectedTab(final InputImage inputImage) {

        final File file = inputImage.getFile();
        final BufferedImage originalImage = inputImage.getImage();

        final int totalPowerPlayTabs = 6;
        final int maxLength = inputImage.getImage().getWidth() / totalPowerPlayTabs;
        final int minLength = inputImage.getImage().getWidth() / (2 * totalPowerPlayTabs);
        final List<LineSegment> segments = detectHorizontalLines(originalImage, file, 1, 80, 2, minLength, maxLength);

        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(originalImage, segments), file, "detect-selected-tab-segments");
        }

        final int twoLineSegmentsOnTopAndBottomOfTheTabButton = 2;
        if (segments.size() < twoLineSegmentsOnTopAndBottomOfTheTabButton) {
            return Optional.empty();
        }

        final double scaleDownFactor = 0.8;
        final Rectangle tab = ImageUtil.scale(new Rectangle(
                segments.get(0).x0, segments.get(0).y0,
                segments.get(1).x1, segments.get(1).y1
        ), scaleDownFactor);

        final Optional<BufferedImage> maybeTabImage = ImageUtil.crop(tab, originalImage);
        if (!maybeTabImage.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage tabImage = maybeTabImage.get();
        saveImageAtStage(tabImage, file, "detect-selected-tab-selection");

        final BufferedImage ocrInputImage = invert(scale(tabImage));

        saveImageAtStage(ocrInputImage, file, "detect-selected-tab-ocr-input");

        final String str = ocrRectangle(ocrInputImage);
        if (null == str || str.isEmpty()) {
            return Optional.empty();
        }

        final String title = str.trim().replace("0", "O");
        LOGGER.info("Selected tab, raw OCR:[" + str + "], corrected:[" + title + "]");
        final ImageType type;
        if ("PREPARATION".equals(title)) {
            type = PP_PREPARATION;
        } else if ("EXPANSION".equals(title)) {
            type = PP_EXPANSION;
        } else if ("CONTROL".equals(title)) {
            type = PP_CONTROL;
        } else if ("TURMOIL".equals(title)) {
            type = PP_TURMOIL;
        } else {
            return Optional.empty();
        }
        return Optional.of(new DataRectangle<>(type, tab));
    }

    private Optional<DataRectangle<Power>> detectSelectedPower(final InputImage inputImage) {
        final File file = inputImage.getFile();
        final BufferedImage img = inputImage.getImage();

        final double longSeparatorLinesLengthFactor = 0.8;
        final int maxLength = img.getWidth();
        final int minLength = (int) (img.getWidth() * longSeparatorLinesLengthFactor);
        final List<LineSegment> segments = detectHorizontalLines(img, file, 1, 80, 10, minLength, maxLength);
        final List<LineSegment> merged = mergeHorizontalSegments(img, file, segments, 3);
        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(img, merged), file, "detect-selected-power");
        }

        if (merged.size() != 3) {
            return Optional.empty();
        }

        final Rectangle powerNameSlice = new Rectangle(
                merged.get(1).x0, merged.get(1).y0,
                merged.get(2).x1, merged.get(2).y1
        );

        final Optional<BufferedImage> maybePowerImage = ImageUtil.crop(powerNameSlice, img);
        if (!maybePowerImage.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage powerImage = maybePowerImage.get();
        saveImageAtStage(powerImage, file, "detect-selected-power");

        final BufferedImage ocrInputImage = invert(scale(powerImage));

        saveImageAtStage(ocrInputImage, file, "detect-selected-power-ocr-input");

        final String str = ocrRectangle(ocrInputImage);
        if (null == str || str.isEmpty()) {
            return Optional.empty();
        }

        Power power = null;
        for (final Power p : Power.values()) {
            final String str1 = str.trim().toUpperCase();
            final String str2 = p.getName();
            if (str1.contains(str2)) {
                power = p;
                break;
            }
        }
        LOGGER.info("Selected power, raw OCR:[" + str + "], found:[" + (null == power ? "nothing" : power.getName()) + "]");
        if (null == power) {
            return Optional.empty();
        }
        return Optional.of(new DataRectangle<>(power, powerNameSlice));
    }

    @Override
    public Optional<ClassifiedImage> classify(final InputImage inputImage) {
        final File file = inputImage.getFile();
        LOGGER.info("classification start: " + file.getAbsolutePath());

        final Optional<DataRectangle<ImageType>> maybeType = detectSelectedTab(inputImage);
        if (!maybeType.isPresent()) {
            return Optional.empty();
        }
        final ImageType type = maybeType.get().getData();

        final Optional<DataRectangle<Power>> maybePower = detectSelectedPower(inputImage);
        if (!maybePower.isPresent()) {
            return Optional.empty();
        }
        final Power power = maybePower.get().getData();

        LOGGER.info("classification end: " + power + "/" + type + ", for file: " + file.getAbsolutePath());

        return Optional.of(new ClassifiedImage(inputImage, maybeType.get(), maybePower.get()));
    }

    public Optional<ControlSystem> extract(final ClassifiedImage input) {

        final File file = input.getInputImage().getFile();
        final BufferedImage img = input.getInputImage().getImage();

        LOGGER.info("Extraction start for file: " + file.getAbsolutePath());

        if (debug) {
            saveImageAtStage(ImageUtil.drawDataRectangles(img, input.getType(), input.getPower()), file,
                    "extract-data-previous-rects");
        }

        final Rectangle reference = input.getPower().getRectangle();
        final int x0 = reference.x0;
        final int y0 = reference.y1 + 10; //a few extra pictures to remove the line below the power name
        final int x1 = reference.x1;
        final int y1 = img.getHeight();
        final Rectangle skipTabsAndPower = new Rectangle(x0, y0, x1, y1);
        final Optional<BufferedImage> maybeImg2 = ImageUtil.crop(skipTabsAndPower, img);
        if (!maybeImg2.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage img2 = maybeImg2.get();
        saveImageAtStage(img2, file, "extract-data-details-area");

        final double horizontalLineBelowSystemNameLengthFactor = 0.3;
        final int minLength = (int) (img2.getWidth() * horizontalLineBelowSystemNameLengthFactor);
        final int maxLength = img2.getWidth();
        final List<LineSegment> segments = detectHorizontalLines(img2, file, 1, 80, 1, minLength, maxLength);
        final List<LineSegment> merged = mergeHorizontalSegments(img2, file, segments, 3);
        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(img, merged), file, "extract-data");
        }

        if (merged.size() < 1) {
            return Optional.empty();
        }

        final LineSegment sysNameLine = merged.get(0);
        final Rectangle sysNameRect = new Rectangle(
                sysNameLine.x0, 0, sysNameLine.x1, sysNameLine.y1
        );
        final Optional<BufferedImage> maybeImg3 = ImageUtil.crop(sysNameRect, img2);
        if (!maybeImg3.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage img3 = maybeImg3.get();
        saveImageAtStage(img3, file, "extract-data-system-name");

        final BufferedImage ocrInputImage = invert(scale(img3));

        saveImageAtStage(ocrInputImage, file, "extract-data-system-name-ocr-input");

        final String str = ocrRectangle(ocrInputImage);
        if (null == str || str.isEmpty()) {
            return Optional.empty();
        }

        final String sysName = cleanSystemName(str);
        LOGGER.info("Selected system, raw OCR:[" + str + "], corrected:[" + sysName + "]");

        Optional<ControlSystem> maybeCs = Optional.empty();
        switch (input.getType().getData()) {
            case PP_CONTROL:
                maybeCs = extractControl(input);
                break;
            case PP_EXPANSION:
                maybeCs = Optional.empty();
                break;
            case PP_PREPARATION:
                maybeCs = Optional.empty();
                break;
            case PP_TURMOIL:
                maybeCs = Optional.empty();
                break;
        }
        LOGGER.info("Extraction end for file: " + file.getAbsolutePath());

        return maybeCs;
    }

    @Override
    public List<ControlSystem> extractDataFromImages(final List<File> files) {

        files
                .stream()
                .map(this::load)
                .filter(Optional::isPresent).map(Optional::get)
                .map(this::classify)
                .filter(Optional::isPresent).map(Optional::get)
                .map(this::extract)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
           /*

        pipeline*:

        1. pick an image from the image folder
        2. if it is not a power play image goto 1.
        3. find the coordinates** of the selected tab
        5. crop the tab area, ocr the segment and apply corrections
        6. if the selected tab is not Preparation | Control | Expansion goto 1.
        7. find the coordinates of the power name
        8. crop the power name, ocr the segment and apply corrections
        9. if the power name is not Edmund | Winters | ..... goto 1.
        ^^DONE^^
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
                    new File("data/control_images/1920x1080/mahon/control/1.bmp")
//                    new File("data/control_images/1920x1080/ald/control/1.bmp"),
//                    new File("data/control_images/1920x1200/ald/control/1.bmp"),
//                    new File("data/control_images/1920x1200/mahon/control/1.bmp"),
//                    new File("data/control_images/1920x1200/mahon/control/4.bmp"),
//                    new File("data/control_images/1920x1200/mahon/expansion/1.bmp"),
//                    new File("data/control_images/1920x1200/ald/expansion/1.bmp"),
//                    new File("data/control_images/1920x1080/antal/preparation/1.bmp"),
//                    new File("data/control_images/1920x1200/ald/preparation/1.bmp")

            );
            api.extractDataFromImages(images);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
