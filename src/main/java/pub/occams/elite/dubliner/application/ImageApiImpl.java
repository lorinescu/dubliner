package pub.occams.elite.dubliner.application;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.bytedeco.javacpp.indexer.FloatRawIndexer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.correct.Corrector;
import pub.occams.elite.dubliner.domain.geometry.LineSegment;
import pub.occams.elite.dubliner.domain.geometry.OcrDataRectangle;
import pub.occams.elite.dubliner.domain.geometry.Range;
import pub.occams.elite.dubliner.domain.geometry.Rectangle;
import pub.occams.elite.dubliner.domain.image.ClassifiedImage;
import pub.occams.elite.dubliner.domain.image.ImageType;
import pub.occams.elite.dubliner.domain.image.InputImage;
import pub.occams.elite.dubliner.domain.powerplay.*;
import pub.occams.elite.dubliner.dto.eddb.PopulatedSystemDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static pub.occams.elite.dubliner.correct.Corrector.*;
import static pub.occams.elite.dubliner.domain.image.ImageType.*;
import static pub.occams.elite.dubliner.util.ImageUtil.*;

public class ImageApiImpl implements ImageApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.LOGGER_NAME);

    private final boolean debug;
    private int cnt = 0;

    private final int HORIZONTAL_KERNEL_SIZE = 20;
    private final Mat ERODE_HLINES_K = getStructuringElement(MORPH_RECT, new Size(HORIZONTAL_KERNEL_SIZE, 1));
    private final Mat DILATE_HLINES_K = getStructuringElement(MORPH_RECT, new Size(HORIZONTAL_KERNEL_SIZE, 3));

    private final SettingsDto settings;
    private final Corrector corrector;
    private final Tesseract tessForNumbers = new Tesseract();
    private final Tesseract tessForNames = new Tesseract();

    public ImageApiImpl(final SettingsDto settings, List<PopulatedSystemDto> eddbSystems, final boolean dbg) {
        this.settings = settings;
        this.corrector = Corrector.buildCorrector(settings, eddbSystems);
        this.debug = dbg;

        tessForNumbers.setDatapath(settings.ocr.tesseractForNumbers.dataPath);
        tessForNumbers.setLanguage(settings.ocr.tesseractForNumbers.language);
        settings.ocr.tesseractForNumbers.variables.forEach(tessForNumbers::setTessVariable);
        settings.ocr.tesseractForNumbers.variables.forEach(tessForNumbers::setTessVariable);

        tessForNames.setDatapath(settings.ocr.tesseractForNumbersAndLetters.dataPath);
        tessForNames.setLanguage(settings.ocr.tesseractForNumbersAndLetters.language);
        settings.ocr.tesseractForNumbersAndLetters.variables.forEach(tessForNames::setTessVariable);
    }

    private void saveImage(final String path, final Object image) throws IOException {
        if (image instanceof Mat) {
            imwrite(path + ".png", (Mat) image);
        } else if (image instanceof BufferedImage) {
            ImageIO.write((BufferedImage) image, "png", new File(path + ".png"));
        }
    }

    private void saveImageAtStage(final Object image, final File imageFile, final String stage) {
        if (!debug) {
            return;
        }

        try {

            final String imageName = imageFile.getCanonicalPath().replace(File.separator, "-").replace(":", "-");
            final String path = "out" + File.separator + imageName;
            final File dir = new File(path);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    LOGGER.info("could not create directory:" + path);
                    return;
                }
            }

            final String filePath = dir + File.separator + cnt + "-" + stage;
            saveImage(filePath, image);
            cnt++;
        } catch (IOException e) {
            LOGGER.error("Failed to write debug image", e);
        }
    }


    private String ocrNumberRectangle(final BufferedImage image) {
        LOGGER.info("starting number OCR");
        try {
            final String str = tessForNumbers.doOCR(image);
            if (null == str) {
                return "";
            }
            return str;
        } catch (TesseractException e) {
            LOGGER.error("Error when ocr-ing image.", e);
        }
        return "";
    }


    private String ocrRectangle(final BufferedImage image) {
        LOGGER.info("starting OCR");
        try {
            final String str = tessForNames.doOCR(image);
            if (null == str) {
                return "";
            }
            return str;
        } catch (Exception e) {
            LOGGER.error("Error when ocr-ing image.", e);
        }
        return "";
    }

    private List<LineSegment> detectHorizontalLines(final Mat image, final File file,
                                                    final int minLength, final int maxLength, final int mergeThreshold) {

        final Mat dst = new Mat(image.size(), 1);
        Canny(image, dst, 400, 500);

        saveImageAtStage(dst, file, "line-detection-begin-before-kern");

        erode(dst, dst, ERODE_HLINES_K);
        dilate(dst, dst, DILATE_HLINES_K);

        saveImageAtStage(dst, file, "line-detection-begin");

        final LineSegmentDetector lsd = createLineSegmentDetector();

        Mat lines = new Mat();
        lsd.detect(dst, lines);

        if (null == lines.arrayData()) {
            return new ArrayList<>();
        }

        final FloatRawIndexer indexer = lines.createIndexer();
        final long size = lines.total();
        final List<LineSegment> segments = new ArrayList<>();
        final List<LineSegment> allSegments = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            final int a = (int) indexer.get(0, i, 0);
            final int b = (int) indexer.get(0, i, 1);
            final int c = (int) indexer.get(0, i, 2);
            final int d = (int) indexer.get(0, i, 3);
            //lines coords are from right to left, make them human friendly
            final int x0 = a >= c ? c : a;
            final int x1 = a < c ? c : a;
            final int y0 = b >= d ? d : b;
            final int y1 = b < d ? d : b;
            final LineSegment s = new LineSegment("L" + i, x0, y0, x1, y1);
            allSegments.add(s);
            if (s.length() >= minLength && s.length() <= maxLength && s.y0 == s.y1) {
                segments.add(s);
            }
        }

        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(dst, allSegments), file, "line-detection-all-segments");
            saveImageAtStage(ImageUtil.drawSegments(dst, segments), file, "line-detection-fitted-segments");
        }

        final Map<Range, LineSegment> parallelClusters = new HashMap<>();
        for (final LineSegment s : segments) {
            Range foundRange = null;
            for (final Range range : parallelClusters.keySet()) {
                if (s.y0 >= range.low && s.y0 <= range.high) {
                    //don't merge if the segments are colinear-ish by a few pixels
                    final LineSegment rangeSegment = parallelClusters.get(range);
                    if (rangeSegment.x1 < s.x0 || rangeSegment.x0 > s.x1) {
                        continue;
                    }
                    foundRange = range;
                    break;
                }
            }
            final int low = s.y0 - mergeThreshold;
            final int high = s.y0 + mergeThreshold;
            if (null != foundRange) {
                final int newLow = low < foundRange.low ? low : foundRange.low;
                final int newHigh = high > foundRange.high ? high : foundRange.high;
                final Range newRange = new Range(newLow, newHigh);
                parallelClusters.put(newRange, parallelClusters.remove(foundRange));
            } else {
                parallelClusters.put(new Range(low, high), s);
            }
        }

        final List<LineSegment> sortedMergedSegments =
                parallelClusters
                        .values()
                        .stream()
                        .sorted((s1, s2) -> s1.y0 - s2.y0)
                        .collect(Collectors.toList());

        saveImageAtStage(ImageUtil.drawSegments(image, sortedMergedSegments), file, "line-detected-sorted-merged");

        return sortedMergedSegments;
    }

    private List<LineSegment> detectHorizontalLines(final Mat image, final File file,
                                                    final int minLength, final int maxLength) {
        return detectHorizontalLines(image, file, minLength, maxLength, 10);
    }

    private OcrDataRectangle<ImageType> detectSelectedTab(final InputImage inputImage) {

        final File file = inputImage.getFile();
        final Mat originalImage = inputImage.getImage();

        final int totalPowerPlayTabs = 6;
        final int minLength = originalImage.size().width() / (2 * totalPowerPlayTabs);
        final int maxLength = originalImage.size().width() / totalPowerPlayTabs;
        final List<LineSegment> segments = detectHorizontalLines(originalImage, file, minLength, maxLength);

        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(originalImage, segments), file, "detect-selected-tab-segments");
        }

        final int twoLineSegmentsOnTopAndBottomOfTheTabButton = 2;
        if (segments.size() < twoLineSegmentsOnTopAndBottomOfTheTabButton) {
            return new OcrDataRectangle<>("", UNKNOWN, null);
        }

        final double scaleDownFactor = 0.8;
        final Rectangle tab = ImageUtil.scale(new Rectangle(
                segments.get(0).x0, segments.get(0).y0,
                segments.get(1).x1, segments.get(1).y1
        ), scaleDownFactor);

        final Optional<Mat> maybeTabImage = ImageUtil.crop(tab, originalImage);
        if (!maybeTabImage.isPresent()) {
            return new OcrDataRectangle<>("", UNKNOWN, tab);
        }
        saveImageAtStage(maybeTabImage.get(), file, "detect-selected-tab-selection");

        final Mat ocrInputImage = invert(scale(maybeTabImage.get()));

        saveImageAtStage(ocrInputImage, file, "detect-selected-tab-ocr-input");

        final String str = ocrRectangle(matToBufferedImage(ocrInputImage));

        final String title = str.trim().replace("0", "O");
        LOGGER.info("Selected tab, raw OCR:[" + str + "], corrected:[" + title + "]");
        final ImageType type;
        if (Corrector.PREPARATION.equals(title)) {
            type = PP_PREPARATION;
        } else if (Corrector.EXPANSION.equals(title)) {
            type = PP_EXPANSION;
        } else if (Corrector.CONTROL.equals(title)) {
            type = PP_CONTROL;
        } else {
            type = UNKNOWN;
        }
        return new OcrDataRectangle<>(str, type, tab);
    }

    private OcrDataRectangle<Power> detectSelectedPower(final InputImage inputImage) {
        final File file = inputImage.getFile();
        final Mat img = inputImage.getImage();

        final double longSeparatorLinesLengthFactor = 0.8;
        final int minLength = (int) (img.cols() * longSeparatorLinesLengthFactor);
        final int maxLength = img.cols();
        final List<LineSegment> merged = detectHorizontalLines(img, file, minLength, maxLength, 20);
        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(img, merged), file, "detect-selected-power");
        }

        if (merged.size() != 3) {
            return new OcrDataRectangle<>("", Power.UNKNOWN, null);
        }

        final Rectangle powerNameRectangle = new Rectangle(
                merged.get(1).x0, merged.get(1).y0,
                merged.get(2).x1, merged.get(2).y1
        );

        final Optional<Mat> maybePowerImage = ImageUtil.crop(powerNameRectangle, img);
        if (!maybePowerImage.isPresent()) {
            return new OcrDataRectangle<>("", Power.UNKNOWN, powerNameRectangle);
        }
        final Mat powerImage = maybePowerImage.get();
        saveImageAtStage(powerImage, file, "detect-selected-power");

        final BufferedImage ocrInputImage = matToBufferedImage(invert(scale(filterRedAndBinarize(powerImage, settings
                .ocr.filterRedChannelMin))));

        saveImageAtStage(ocrInputImage, file, "detect-selected-power-ocr-input");

        final String str = ocrRectangle(ocrInputImage);
        LOGGER.info("Selected power, raw OCR:[" + str + "]");

        Optional<Power> maybePower = corrector.powerFromString(str);
        if (!maybePower.isPresent()) {
            return new OcrDataRectangle<>(str, Power.UNKNOWN, powerNameRectangle);
        }

        LOGGER.info("Selected power, corrected to:[" + maybePower.get().name + "]");
        return new OcrDataRectangle<>(str, maybePower.get(), powerNameRectangle);
    }

    private OcrDataRectangle<String> extractSystemName(final ClassifiedImage input) {

        final File file = input.inputImage.getFile();
        final Mat img = input.inputImage.getImage();

        LOGGER.info("Name extraction start for file: " + file.getAbsolutePath());

        final Rectangle reference = input.power.rectangle;
        final int x0 = reference.x0;
        final int y0Offset = 20;//a few extra pixels to remove the line below the power name
        final int y0 = reference.y1 + y0Offset;
        final int x1 = reference.x1;
        final int y1 = img.rows();
        final Rectangle skipTabsAndPower = new Rectangle(x0, y0, x1, y1);
        final Optional<Mat> maybeImg2 = ImageUtil.crop(skipTabsAndPower, img);
        if (!maybeImg2.isPresent()) {
            return new OcrDataRectangle<>(Corrector.UNKNOWN_SYSTEM, "", null);
        }
        final Mat img2 = maybeImg2.get();
        saveImageAtStage(img2, file, "extract-name-data-details-area");

        final double horizontalLineBelowSystemNameLengthFactor = 0.3;
        final int minLength = (int) (img2.cols() * horizontalLineBelowSystemNameLengthFactor);
        final int maxLength = img2.cols();
        final List<LineSegment> merged = detectHorizontalLines(img2, file, minLength, maxLength);
        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(img2, merged), file, "extract-name-data-lines");
        }

        if (merged.size() < 1) {
            return new OcrDataRectangle<>("", Corrector.UNKNOWN_SYSTEM, null);
        }

        final LineSegment sysNameLine = merged.get(0);

        final Rectangle sysNameRect = new Rectangle(
                sysNameLine.x0 - HORIZONTAL_KERNEL_SIZE / 2, 0, sysNameLine.x1, sysNameLine.y1
        );
        final Rectangle sysNameRectReal = new Rectangle(
                sysNameRect.x0 + x0, sysNameRect.y0 + y0,
                sysNameRect.x1 + x0, sysNameRect.y1 + y0
        );

        final Optional<Mat> maybeImg3 = ImageUtil.crop(sysNameRect, img2);
        if (!maybeImg3.isPresent()) {
            return new OcrDataRectangle<>("", UNKNOWN_SYSTEM, sysNameRectReal);
        }

        final Mat img3 = maybeImg3.get();
        saveImageAtStage(img3, file, "extract-name-data");

        final BufferedImage ocrInputImage = matToBufferedImage(invert(scale(img3)));

        saveImageAtStage(ocrInputImage, file, "extract-name-ocr-input");

        final String str = ocrRectangle(ocrInputImage);
        LOGGER.info("Selected system, raw OCR:[" + str + "]");

        final String sysName = corrector.cleanSystemName(str);
        LOGGER.info("Selected system, corrected to:[" + sysName + "]");

        return new OcrDataRectangle<>(str, sysName, sysNameRectReal);
    }

    private PreparationSystem extractPreparation(final ClassifiedImage input,
                                                 final OcrDataRectangle<String> sysNameRect) {

        if (null == input.inputImage || ImageType.UNKNOWN == input.type.data
                || Power.UNKNOWN == input.power.data || Corrector.UNKNOWN_SYSTEM.equals(sysNameRect.data)) {
            return new PreparationSystem(input, sysNameRect, sysNameRect.data, null, null, null, null,
                    -1, Power.UNKNOWN, -1, -1, -1);
        }

        final File file = input.inputImage.getFile();
        final Mat img = input.inputImage.getImage();

        LOGGER.info("Preparation data extraction start for file: " + file.getAbsolutePath());

        final Rectangle reference = sysNameRect.rectangle;

        final int listX0 = 0;
        final int listY0 = reference.y0;
        final int listX1 = reference.x0;
        final int listY1 = img.rows();
        final Rectangle systemListLeftSide = new Rectangle(listX0, listY0, listX1, listY1);
        final Optional<Mat> maybeSystemListImg = ImageUtil.crop(systemListLeftSide, img);
        if (!maybeSystemListImg.isPresent()) {
            LOGGER.info("Error cropping preparation system list rectangle: " + systemListLeftSide.toString() +
                    " for file: " + file.getAbsolutePath());
            return new PreparationSystem(input, sysNameRect, sysNameRect.data, null, null, null, null,
                    -1, Power.UNKNOWN, -1, -1, -1);
        }

        final Mat systemListImg = maybeSystemListImg.get();

        saveImageAtStage(systemListImg, file, "extract-preparation-data-system-list-area");

        final int minLength = systemListImg.cols() / 2;
        final int maxLength = systemListImg.cols();
        final List<LineSegment> systemListSegments = detectHorizontalLines(systemListImg, file, minLength, maxLength, 5);
        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(systemListImg, systemListSegments), file,
                    "extract-preparation-data-system-list-horizontal-lines");
        }

        if (systemListSegments.size() < 4) {
            LOGGER.info("Not enough segments to determine preparation system list rectangles for file:" + file
                    .getAbsolutePath());
            return new PreparationSystem(input, sysNameRect, sysNameRect.data, null, null, null, null,
                    -1, Power.UNKNOWN, -1, -1, -1);
        }

        final int toSpendX0 = systemListSegments.get(0).x0 + (systemListSegments.get(0).x1 - systemListSegments.get(0).x0) / 2;
        final int toSpendY0Offset = 5; //shave top line
        final int toSpendY0 = systemListSegments.get(0).y0 + toSpendY0Offset;
        final int toSpendX1 = systemListSegments.get(0).x1;
        final int toSpendY1 = systemListSegments.get(1).y1;

        final Rectangle toSpendRect = new Rectangle(toSpendX0, toSpendY0, toSpendX1, toSpendY1);
        final Optional<Mat> maybeToSpendImg = ImageUtil.crop(toSpendRect, systemListImg);
        if (!maybeToSpendImg.isPresent()) {
            LOGGER.info("Error cropping preparation system list to-spend rectangle: " + toSpendRect.toString() +
                    " for file: " + file.getAbsolutePath());
            return new PreparationSystem(input, sysNameRect, sysNameRect.data, null, null, null, null,
                    -1, Power.UNKNOWN, -1, -1, -1);
        }

        final Mat toSpendImg = maybeToSpendImg.get();

        saveImageAtStage(toSpendImg, file, "extract-preparation-data-system-list-to-spend-area");

        final BufferedImage toSpendOcrInputImage = matToBufferedImage(invert(scale(filterRedAndBinarize(toSpendImg, settings
                .ocr.filterRedChannelMin))));

        saveImageAtStage(toSpendOcrInputImage, file, "extract-preparation-data-system-list-to-spend-ocr-input");

        final String toSpendStr = ocrRectangle(toSpendOcrInputImage);
        LOGGER.info("To-spend preparation, raw OCR:[" + toSpendStr + "]");

        String[] parts = corrector.correctGlobalOcrErrors(toSpendStr).split("\n| ");
        String previous = "";
        Integer toSpend = -1;
        for (final String part : parts) {
            if (Corrector.CC.equals(part)) {
                toSpend = corrector.cleanPositiveInteger(previous);
                break;
            } else if (part.contains(Corrector.CC)) {
                toSpend = corrector.cleanPositiveInteger(part);
                break;
            }
            previous = part;
        }

        final int toSpendRealX0 = listX0 + toSpendX0;
        final int toSpendRealY0 = listY0 + toSpendY0;
        final int toSpendRealX1 = listX0 + toSpendX1;
        final int toSpendRealY1 = listY0 + toSpendY1;
        final OcrDataRectangle<Integer> toSpendRealRect = new OcrDataRectangle<>(toSpendStr, toSpend,
                new Rectangle(toSpendRealX0, toSpendRealY0, toSpendRealX1, toSpendRealY1));

        LOGGER.info("System to-spend preparation, corrected to: [" + toSpend + "]");

        final int prepX0 = systemListSegments.get(0).x0;
        final int prepY0 = systemListSegments.get(2).y0;
        final int prepX1 = systemListSegments.get(0).x1 - (systemListSegments.get(0).x1 - systemListSegments.get(0).x0) / 2;
        final int prepY1 = systemListSegments.get(3).y1;

        final Rectangle prepAmountRect = new Rectangle(prepX0, prepY0, prepX1, prepY1);
        final Optional<Mat> maybePrepAmountImg = ImageUtil.crop(prepAmountRect, systemListImg);
        if (!maybePrepAmountImg.isPresent()) {
            LOGGER.info("Error cropping preparation system list prep-amount rectangle: " + prepAmountRect.toString() +
                    " for file: " + file.getAbsolutePath());
            return new PreparationSystem(input, sysNameRect, sysNameRect.data, toSpendRealRect, null, null, null,
                    toSpend, Power.UNKNOWN, -1, -1, -1);
        }

        final Mat prepAmountImg = maybePrepAmountImg.get();

        saveImageAtStage(prepAmountImg, file, "extract-preparation-data-system-list-prep-amount-area");

        final BufferedImage prepAmountOcrInputImage = matToBufferedImage(scale(prepAmountImg));

        saveImageAtStage(prepAmountOcrInputImage, file, "extract-preparation-data-system-prep-amount-ocr-input");

        final String prepAmountStr = ocrRectangle(prepAmountOcrInputImage);
        LOGGER.info("Prep-amount preparation, raw OCR:[" + prepAmountStr + "]");

        parts = corrector.correctGlobalOcrErrors(prepAmountStr).split("\n| ");
        previous = "";
        Integer prepAmount = -1;
        for (final String part : parts) {
            if (Corrector.PREP.equals(previous)) {
                prepAmount = corrector.cleanPositiveInteger(part);
                break;
            }
            previous = part;
        }
        //make a second pass but awesome there's no space between PREP and the amount
        if (-1 == prepAmount) {
            for (final String part : parts) {
                if (part.contains(Corrector.PREP)) {
                    prepAmount = corrector.cleanPositiveInteger(part);
                    break;
                }
            }
        }

        final int prepRealX0 = listX0 + prepX0;
        final int prepRealY0 = listY0 + prepY0;
        final int prepRealX1 = listX0 + prepX1;
        final int prepRealY1 = listY0 + prepY1;
        final OcrDataRectangle<Integer> prepRealRect = new OcrDataRectangle<>(prepAmountStr, prepAmount,
                new Rectangle(prepRealX0, prepRealY0, prepRealX1, prepRealY1));


        LOGGER.info("Prep-amount preparation, corrected to: [" + prepAmount + "]");

        final int costX0 = systemListSegments.get(2).x0 + (systemListSegments.get(0).x1 - systemListSegments.get(0).x0) / 2;
        final int costY0Offset = 5; //shave the top line
        final int costY0 = systemListSegments.get(2).y0 + costY0Offset;
        final int costX1 = systemListSegments.get(2).x1;
        final int costY1 = systemListSegments.get(3).y1;

        final Rectangle costRect = new Rectangle(costX0, costY0, costX1, costY1);
        final Optional<Mat> maybeCostImg = ImageUtil.crop(costRect, systemListImg);
        if (!maybeCostImg.isPresent()) {
            LOGGER.info("Error cropping preparation system list cost rectangle: " + costRect.toString() +
                    " for file: " + file.getAbsolutePath());
            return new PreparationSystem(input, sysNameRect, sysNameRect.data, toSpendRealRect, prepRealRect, null, null, toSpend, Power.UNKNOWN, -1, -1, prepAmount);
        }

        final Mat costImg = maybeCostImg.get();

        saveImageAtStage(costImg, file, "extract-preparation-data-system-list-cost-area");

        final BufferedImage costOcrInputImage = matToBufferedImage(scale(costImg));

        saveImageAtStage(costOcrInputImage, file, "extract-preparation-data-system-cost-ocr-input");

        final String costStr = ocrRectangle(costOcrInputImage);

        LOGGER.info("Cost preparation, raw OCR:[" + costStr + "]");

        parts = corrector.correctGlobalOcrErrors(costStr).split("\n| ");
        previous = "";
        Integer cost = -1;
        for (final String part : parts) {
            if (Corrector.CC.equals(part)) {
                cost = corrector.cleanPositiveInteger(previous);
                break;
                //sometime space is not detected between the prep value and "CC"
            } else if (part.contains(Corrector.CC)) {
                cost = corrector.cleanPositiveInteger(part);
                break;
            }
            previous = part;
        }


        final int costRealX0 = listX0 + costX0;
        final int costRealY0 = listY0 + costY0;
        final int costRealX1 = listX0 + costX1;
        final int costRealY1 = listY0 + costY1;
        final OcrDataRectangle<Integer> costRealRect = new OcrDataRectangle<>(costStr, cost,
                new Rectangle(costRealX0, costRealY0, costRealX1, costRealY1));

        LOGGER.info("Cost preparation, corrected to: [" + cost + "]");

        final int detailsX0 = reference.x0 + (reference.x1 - reference.x0) / 3;
        final int detailsY0 = reference.y1;
        final int detailsX1 = reference.x1;
        final int detailsY1 = reference.y0 + (img.rows() - reference.y0) / 2;
        final Rectangle detailsArea = new Rectangle(detailsX0, detailsY0, detailsX1, detailsY1);
        final Optional<Mat> maybeDetailsImg = ImageUtil.crop(detailsArea, img);
        if (!maybeDetailsImg.isPresent()) {
            LOGGER.info("Error cropping preparation details rectangle: " + detailsArea.toString() +
                    " for file: " + file.getAbsolutePath());
            return new PreparationSystem(input, sysNameRect, sysNameRect.data,
                    toSpendRealRect, prepRealRect, costRealRect, null,
                    toSpend, Power.UNKNOWN, -1, cost, prepAmount);
        }

        final Mat detailsImg = maybeDetailsImg.get();

        saveImageAtStage(detailsImg, file, "extract-preparation-data-details-area");

        final BufferedImage prepDetailsOcrInputImage = matToBufferedImage(filterRedAndBinarize(invert
                (detailsImg), settings.ocr.filterRedChannelMin));

        saveImageAtStage(prepDetailsOcrInputImage, file, "extract-preparation-data-system-prep-details-ocr-input");

        final String prepDetailsStr = ocrRectangle(prepDetailsOcrInputImage);
        LOGGER.info("Prep-details preparation, raw OCR:[" + prepDetailsStr + "]");

        //AISUNG DUVAL [24660]
        final String[] lines = corrector.correctGlobalOcrErrors(prepDetailsStr).split("\n");
        Integer highestContributingPowerAmount = -1;
        Power highestContributingPower = Power.UNKNOWN;
        for (final String line : lines) {
            final Optional<Power> mayberPower = corrector.powerFromString(line);
            if (!mayberPower.isPresent()) {
                continue;
            }
            highestContributingPower = mayberPower.get();
            final String[] words = line.split(" ");

            //firstName lastName [highest contribution]
            if (words.length < 3) {
                continue;
            }

            final String lastWord = words[words.length - 1];
            if (!(lastWord.startsWith("[") && lastWord.endsWith("]"))) {
                continue;
            }
            highestContributingPowerAmount = corrector.cleanPositiveInteger(lastWord.replace("[", "").replace("]", ""));
            break;
        }

        final int detailsRealX0 = detailsX0;
        final int detailsRealY0 = detailsY0;
        final int detailsRealX1 = detailsX1;
        final int detailsRealY1 = detailsY1;
        final OcrDataRectangle<String> detailsRealRect = new OcrDataRectangle<>(prepDetailsStr,
                "[" + highestContributingPower + "(" + highestContributingPowerAmount + ")" + "]",
                new Rectangle(detailsRealX0, detailsRealY0, detailsRealX1, detailsRealY1));

        LOGGER.info("System preparation highest contribution, corrected to: [" + highestContributingPower.name +
                "," + highestContributingPowerAmount + "]");

        return new PreparationSystem(input, sysNameRect, sysNameRect.data, toSpendRealRect, prepRealRect,
                costRealRect, detailsRealRect, toSpend, highestContributingPower, highestContributingPowerAmount,
                cost, prepAmount);
    }


    private ControlSystem extractControl(final ClassifiedImage input,
                                         final OcrDataRectangle<String> sysNameRect) {

        if (null == input.inputImage || ImageType.UNKNOWN == input.type.data
                || Power.UNKNOWN == input.power.data || Corrector.UNKNOWN_SYSTEM.equals(sysNameRect.data)) {
            return new ControlSystem(input, sysNameRect, sysNameRect.data, null, null, null, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1);
        }

        final File file = input.inputImage.getFile();
        final Mat img = input.inputImage.getImage();

        LOGGER.info("Control data extraction start for file: " + file.getAbsolutePath());

        final Rectangle reference = sysNameRect.rectangle;
        final int x0 = reference.x0;
        final int y0Offset = 10;//a few extra px remove the line below the system name
        final int y0 = reference.y1 + y0Offset;
        final int x1 = reference.x1;
        final int y1 = img.rows();
        final Rectangle skipTabsPowerAndSystemName = new Rectangle(x0, y0, x1, y1);
        final Optional<Mat> maybeImg2 = ImageUtil.crop(skipTabsPowerAndSystemName, img);
        if (!maybeImg2.isPresent()) {
            LOGGER.info("Error cropping control details rectangle: " + skipTabsPowerAndSystemName.toString() +
                    " for file: " + file.getAbsolutePath());
            return new ControlSystem(input, sysNameRect, sysNameRect.data, null, null, null, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1);
        }

        final Mat img2 = maybeImg2.get();

        saveImageAtStage(img2, file, "extract-control-data-details-area");

        final double horizontalLinesOnTopAndBottomOfCountersRectangles = 0.4;
        final int minLength = (int) (img2.cols() * horizontalLinesOnTopAndBottomOfCountersRectangles);
        final int maxLength = (int) (img2.cols() * 0.6);
        final List<LineSegment> merged = detectHorizontalLines(img2, file, minLength, maxLength);
        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(img2, merged), file, "extract-control-data-horizontal-lines");
        }

        /*
        Find highest , leftmost 2 lines (fortification 2nd from the top and bottom line). If two lines
        are colinear by 5 pixels or less then we pick the leftmost line.

        Considering the right left end of the top line (x1,y1) as the origin of a coordinate system the right upper
        quadrant will be system costs, lower left quadrant fortifications and lower right undermining

        lowest segment should be the bottom line in either fortification or undermining, we use it to remove
        the redundant parts below it's y value
        */
        final List<LineSegment> sortedFilteredSegments = merged
                .stream()
                .filter(s -> (s.x1 + 10) - img2.cols() <= 0)
                .sorted(
                        (s1, s2) -> {
                            final int res = s1.y0 - s2.y0;
                            if (Math.abs(res) < 5) {
                                return s1.x0 - s2.x0;
                            }
                            return res;
                        }
                )
                .collect(Collectors.toList());

        if (sortedFilteredSegments.size() < 2) {
            LOGGER.info("Not enough left side segments to determine control details rectangles for file: " +
                    file.getAbsolutePath());
            return new ControlSystem(input, sysNameRect, sysNameRect.data, null, null, null, -1, -1, -1, -1, -1, -1, -1,
                    -1, -1);
        }

        final LineSegment topFortificationSegment = sortedFilteredSegments.get(sortedFilteredSegments.size() - 2);
        final LineSegment bottomFortificationSegment = sortedFilteredSegments.get(sortedFilteredSegments.size() - 1);

        final Rectangle costsRectangle = new Rectangle(
                bottomFortificationSegment.x1, 0,
                img2.cols(), topFortificationSegment.y1
        );
        final Rectangle fortificationRectangle = new Rectangle(
                0, topFortificationSegment.y0,
                bottomFortificationSegment.x1, bottomFortificationSegment.y0
        );
        final Rectangle underminingRectangle = new Rectangle(
                bottomFortificationSegment.x1, topFortificationSegment.y0,
                img2.cols(), bottomFortificationSegment.y0
        );

        Integer upkeepFromLastCycle = -1;
        Integer defaultUpkeepCost = -1;
        Integer costIfFortified = -1;
        Integer costIfUndermined = -1;
        Integer baseIncome = -1;
        String costsStr = "";
        final Optional<Mat> maybeCostsImg = ImageUtil.crop(costsRectangle, img2);
        if (maybeCostsImg.isPresent()) {

            final Mat costsImg = maybeCostsImg.get();

            final BufferedImage costsInputForOcr = matToBufferedImage(scale(filterRedAndBinarize(costsImg, settings.ocr
                    .filterRedChannelMin)));

            saveImageAtStage(costsInputForOcr, file, "extract-control-data-costs-ocr-input");

            costsStr = ocrNumberRectangle(costsInputForOcr);

            LOGGER.info("System costs, raw OCR:[" + costsStr + "]");

            final String[] parts = corrector.correctGlobalOcrErrors(costsStr).split("\n");
            if (parts.length >= 5) {
                upkeepFromLastCycle = corrector.cleanPositiveInteger(parts[0]);
                defaultUpkeepCost = corrector.cleanPositiveInteger(parts[1]);
                costIfFortified = corrector.cleanPositiveInteger(parts[2]);
                costIfUndermined = corrector.cleanPositiveInteger(parts[3]);
                baseIncome = corrector.cleanPositiveInteger(parts[4]);
            }
            LOGGER.info("System costs, corrected to:["
                    + upkeepFromLastCycle + "," + defaultUpkeepCost + "," + costIfFortified + "," + costIfUndermined
                    + "," + baseIncome + "]");
        }

        final OcrDataRectangle<String> costsReal = new OcrDataRectangle<>(
                costsStr,
                "[" + upkeepFromLastCycle + "," + defaultUpkeepCost + "," + costIfFortified + "," + costIfUndermined
                        + "," + baseIncome + "]",
                new Rectangle(
                        costsRectangle.x0 + x0, costsRectangle.y0 + y0,
                        costsRectangle.x1 + x0, costsRectangle.y1 + y0)
        );

        Integer fortifyTotal = -1;
        Integer fortifyTrigger = -1;
        String fortificationStr = "";
        final Optional<Mat> maybeFortificationImg = ImageUtil.crop(fortificationRectangle, img2);
        if (maybeFortificationImg.isPresent()) {

            final Mat fortificationImg = maybeFortificationImg.get();

            final BufferedImage fortificationOcrInput = matToBufferedImage(scale(filterRedAndBinarize(fortificationImg,
                    settings.ocr.filterRedChannelMin)));

            saveImageAtStage(fortificationOcrInput, file, "extract-control-data-fortif-ocr-input");

            fortificationStr = ocrNumberRectangle(fortificationOcrInput);
            LOGGER.info("Fortifications, raw OCR:[" + fortificationStr + "]");

            final String[] parts = corrector.correctGlobalOcrErrors(fortificationStr).split("\n| ");
            String previous = "";
            for (final String part : parts) {
                if (previous.startsWith(TOTAL)) {
                    fortifyTotal = corrector.cleanPositiveInteger(part);
                } else if (previous.startsWith(TRIGGER)) {
                    fortifyTrigger = corrector.cleanPositiveInteger(part);
                }
                previous = part;
            }
            LOGGER.info("Fortifications, corrected to: [" + fortifyTotal + "," + fortifyTrigger + "]");
        }
        final OcrDataRectangle<String> fortificationReal = new OcrDataRectangle<>(
                fortificationStr,
                "[" + fortifyTotal + "," + fortifyTrigger + "]",
                new Rectangle(
                        fortificationRectangle.x0 + x0, fortificationRectangle.y0 + y0,
                        fortificationRectangle.x1 + x0, fortificationRectangle.y1 + y0)
        );

        Integer undermineTotal = -1;
        Integer undermineTrigger = -1;
        String underminingStr = "";
        final Optional<Mat> maybeUnderminingImg = ImageUtil.crop(underminingRectangle, img2);
        if (maybeUnderminingImg.isPresent()) {

            final Mat underminingImg = maybeUnderminingImg.get();

            saveImageAtStage(underminingImg, file, "extract-control-data-undermine");

            final BufferedImage underminingOcrInput = matToBufferedImage(scale(filterRedAndBinarize(underminingImg,
                    settings.ocr.filterRedChannelMin)));

            saveImageAtStage(underminingOcrInput, file, "extract-control-data-undermine-ocr-input");

            underminingStr = ocrNumberRectangle(underminingOcrInput);
            LOGGER.info("Undermining, raw OCR:[" + underminingStr + "]");

            final String[] parts = corrector.correctGlobalOcrErrors(underminingStr).split("\n| ");
            String previous = "";
            for (final String part : parts) {
                if (previous.contains(TOTAL)) {
                    undermineTotal = corrector.cleanPositiveInteger(part);
                } else if (previous.contains(TRIGGER) && !part.contains(REACHED)) {
                    undermineTrigger = corrector.cleanPositiveInteger(part);
                }
                previous = part;
            }
            LOGGER.info("Undermining, after corrections:[" + undermineTotal + "," + undermineTrigger + "]");
        }
        final OcrDataRectangle<String> undermineReal = new OcrDataRectangle<>(
                underminingStr,
                "[" + undermineTotal + "," + undermineTrigger + "]", new Rectangle(
                underminingRectangle.x0 + x0, underminingRectangle.y0 + y0,
                underminingRectangle.x1 + x0, underminingRectangle.y1 + y0)
        );
        if (debug) {
            saveImageAtStage(
                    ImageUtil.drawDataRectangles(img, input.type, input.power, sysNameRect, costsReal,
                            undermineReal, fortificationReal),
                    file,
                    "extract-data-rects");
        }

        return new ControlSystem(
                input, sysNameRect, sysNameRect.data, costsReal, fortificationReal, undermineReal,
                upkeepFromLastCycle, defaultUpkeepCost, costIfFortified, costIfUndermined, baseIncome, fortifyTotal,
                fortifyTrigger, undermineTotal, undermineTrigger
        );
    }


    private ExpansionSystem extractExpansion(final ClassifiedImage input,
                                             final OcrDataRectangle<String> sysNameRect) {

        if (null == input.inputImage || ImageType.UNKNOWN == input.type.data
                || Power.UNKNOWN == input.power.data || Corrector.UNKNOWN_SYSTEM.equals(sysNameRect.data)) {
            return new ExpansionSystem(input, sysNameRect, sysNameRect.data, null, null, null, -1, -1, -1, -1, -1);
        }

        final File file = input.inputImage.getFile();
        final Mat img = input.inputImage.getImage();

        LOGGER.info("Expansion data extraction start for file: " + file.getAbsolutePath());

        final Rectangle reference = sysNameRect.rectangle;
        final int x0 = reference.x0;
        final int y0Offset = 10;//a few extra px remove the line below the system name
        final int y0 = reference.y1 + y0Offset;
        final int x1 = reference.x1;
        final int y1 = img.rows();
        final Rectangle skipTabsPowerAndSystemName = new Rectangle(x0, y0, x1, y1);
        final Optional<Mat> maybeImg2 = ImageUtil.crop(skipTabsPowerAndSystemName, img);
        if (!maybeImg2.isPresent()) {
            LOGGER.info("Error cropping expansion details rectangle: " + skipTabsPowerAndSystemName.toString() +
                    " for file: " + file.getAbsolutePath());
            return new ExpansionSystem(input, sysNameRect, sysNameRect.data, null, null, null, -1, -1, -1, -1, -1);
        }

        final Mat img2 = maybeImg2.get();

        saveImageAtStage(img2, file, "extract-expansion-data-details-area");

        final double horizontalLinesOnTopAndBottomOfCountersRectangles = 0.4;
        final int minLength = (int) (img2.cols() * horizontalLinesOnTopAndBottomOfCountersRectangles);
        final int maxLength = (int) (img2.cols() * 0.6);
        final List<LineSegment> merged = detectHorizontalLines(img2, file, minLength, maxLength);
        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(img2, merged), file, "extract-expansion-data-horizontal-lines");
        }

        /*
        Find highest , leftmost 2 lines (fortification 2nd from the top and bottom line). If two lines
        are colinear by 5 pixels or less then we pick the leftmost line.

        Considering the right left end of the top line (x1,y1) as the origin of a coordinate system the right upper
        quadrant will be system costs, lower left quadrant fortifications and lower right undermining

        lowest segment should be the bottom line in either fortification or undermining, we use it to remove
        the redundant parts below it's y value
        */
        final List<LineSegment> sortedFilteredSegments = merged
                .stream()
                .filter(s -> (s.x1 + 10) - img2.cols() <= 0)
                .sorted(
                        (s1, s2) -> {
                            final int res = s1.y0 - s2.y0;
                            if (Math.abs(res) < 5) {
                                return s1.x0 - s2.x0;
                            }
                            return res;
                        }
                )
                .collect(Collectors.toList());

        if (sortedFilteredSegments.size() < 2) {
            LOGGER.info("Not enough left side segments to determine expansion details rectangles for file: " +
                    file.getAbsolutePath());
            return new ExpansionSystem(input, sysNameRect, sysNameRect.data, null, null, null, -1, -1, -1, -1, -1);
        }

        final LineSegment topExpansionSegment = sortedFilteredSegments.get(sortedFilteredSegments.size() - 2);
        final LineSegment bottomExpansionSegment = sortedFilteredSegments.get(sortedFilteredSegments.size() - 1);

        final Rectangle potentialValueRectangle = new Rectangle(
                bottomExpansionSegment.x1, 0,
                img2.cols(), topExpansionSegment.y1
        );
        final Rectangle expansionRectangle = new Rectangle(
                0, topExpansionSegment.y0,
                bottomExpansionSegment.x1, bottomExpansionSegment.y0
        );
        final Rectangle oppositionRectangle = new Rectangle(
                bottomExpansionSegment.x1, topExpansionSegment.y0,
                img2.cols(), bottomExpansionSegment.y0
        );

        Integer potentialValue = -1;
        String potentialValueStr = "";
        final Optional<Mat> maybePotentialValueImg = ImageUtil.crop(potentialValueRectangle, img2);
        if (maybePotentialValueImg.isPresent()) {

            final Mat potentialValueImg = maybePotentialValueImg.get();

            final BufferedImage potentialValueInputForOcr = matToBufferedImage(scale(filterRedAndBinarize(potentialValueImg, settings.ocr
                    .filterRedChannelMin)));

            saveImageAtStage(potentialValueInputForOcr, file, "extract-expansion-data-potential-value-ocr-input");

            potentialValueStr = ocrNumberRectangle(potentialValueInputForOcr);

            LOGGER.info("System costs, raw OCR:[" + potentialValueStr + "]");

            final String[] parts = corrector.correctGlobalOcrErrors(potentialValueStr).split("\n");
            if (parts.length >= 1) {
                potentialValue = corrector.cleanPositiveInteger(parts[0]);
            }
            LOGGER.info("System potential value, corrected to:[" + potentialValue + "]");
        }

        final OcrDataRectangle<Integer> potentialValueReal = new OcrDataRectangle<>(
                potentialValueStr,
                potentialValue,
                new Rectangle(
                        potentialValueRectangle.x0 + x0, potentialValueRectangle.y0 + y0,
                        potentialValueRectangle.x1 + x0, potentialValueRectangle.y1 + y0)
        );

        Integer expansionTotal = -1;
        Integer expansionTrigger = -1;
        String expansionStr = "";
        final Optional<Mat> maybeExpansionImg = ImageUtil.crop(expansionRectangle, img2);
        if (maybeExpansionImg.isPresent()) {

            final Mat expansionImg = maybeExpansionImg.get();

            final BufferedImage expansionOcrInput = matToBufferedImage(scale(filterRedAndBinarize(expansionImg,
                    settings.ocr.filterRedChannelMin)));

            saveImageAtStage(expansionOcrInput, file, "extract-expansion-data-expansion-ocr-input");

            expansionStr = ocrNumberRectangle(expansionOcrInput);
            LOGGER.info("Expansion, raw OCR:[" + expansionStr + "]");

            final String[] parts = corrector.correctGlobalOcrErrors(expansionStr).split("\n| ");
            String previous = "";
            for (final String part : parts) {
                if (previous.startsWith(TOTAL)) {
                    expansionTotal = corrector.cleanPositiveInteger(part);
                } else if (previous.startsWith(TRIGGER)) {
                    expansionTrigger = corrector.cleanPositiveInteger(part);
                }
                previous = part;
            }
            LOGGER.info("Expansion, corrected to: [" + expansionTotal + "," + expansionTrigger + "]");
        }
        final OcrDataRectangle<String> expansionReal = new OcrDataRectangle<>(
                expansionStr,
                "[" + expansionTotal + "," + expansionTrigger + "]",
                new Rectangle(
                        expansionRectangle.x0 + x0, expansionRectangle.y0 + y0,
                        expansionRectangle.x1 + x0, expansionRectangle.y1 + y0)
        );

        Integer oppositionTotal = -1;
        Integer oppositionTrigger = -1;
        String oppositionStr = "";
        final Optional<Mat> maybeOppositionImg = ImageUtil.crop(oppositionRectangle, img2);
        if (maybeOppositionImg.isPresent()) {

            final Mat oppositionImg = maybeOppositionImg.get();

            saveImageAtStage(oppositionImg, file, "extract-expansion-data-opposition");

            final BufferedImage oppositionOcrInput = matToBufferedImage(scale(filterRedAndBinarize(oppositionImg,
                    settings.ocr.filterRedChannelMin)));

            saveImageAtStage(oppositionOcrInput, file, "extract-expansion-data-opposition-ocr-input");

            oppositionStr = ocrNumberRectangle(oppositionOcrInput);
            LOGGER.info("Opposition, raw OCR:[" + oppositionStr + "]");

            final String[] parts = corrector.correctGlobalOcrErrors(oppositionStr).split("\n| ");
            String previous = "";
            for (final String part : parts) {
                if (previous.contains(TOTAL)) {
                    oppositionTotal = corrector.cleanPositiveInteger(part);
                } else if (previous.contains(TRIGGER) && !part.contains(REACHED)) {
                    oppositionTrigger = corrector.cleanPositiveInteger(part);
                }
                previous = part;
            }
            LOGGER.info("Opposition, after corrections:[" + oppositionTotal + "," + oppositionTrigger + "]");
        }
        final OcrDataRectangle<String> oppositionReal = new OcrDataRectangle<>(
                oppositionStr,
                "[" + oppositionTotal + "," + oppositionTrigger + "]", new Rectangle(
                oppositionRectangle.x0 + x0, oppositionRectangle.y0 + y0,
                oppositionRectangle.x1 + x0, oppositionRectangle.y1 + y0)
        );
        if (debug) {
            saveImageAtStage(
                    ImageUtil.drawDataRectangles(img, input.type, input.power, sysNameRect, potentialValueReal,
                            expansionReal, oppositionReal),
                    file,
                    "extract-data-rects");
        }

        return new ExpansionSystem(
                input, sysNameRect, sysNameRect.data, potentialValueReal, expansionReal, oppositionReal,
                potentialValue, expansionTotal, expansionTrigger, oppositionTotal, oppositionTrigger
        );
    }

    @Override
    public SettingsDto getSettings() {
        return this.settings;
    }

    @Override
    public InputImage load(final File file) {
        LOGGER.info("loading image:" + file.getAbsolutePath());

        final Optional<Mat> maybeImg = ImageUtil.readImageFromFile(file);
        if (!maybeImg.isPresent()) {
            return new InputImage(file, null);
        }

        saveImageAtStage(maybeImg.get(), file, "loading");

        return new InputImage(file, maybeImg.get());
    }


    @Override
    public ClassifiedImage classify(final InputImage inputImage) {
        final File file = inputImage.getFile();
        LOGGER.info("classification start: " + file.getAbsolutePath());

        if (null == inputImage.getImage()) {
            return new ClassifiedImage(inputImage, null, null);
        }

        final OcrDataRectangle<ImageType> type = detectSelectedTab(inputImage);
        if (ImageType.UNKNOWN == type.data) {
            return new ClassifiedImage(inputImage, type, null);
        }

        final OcrDataRectangle<Power> power = detectSelectedPower(inputImage);
        if (Power.UNKNOWN == power.data) {
            return new ClassifiedImage(inputImage, type, power);
        }

        LOGGER.info("classification end: " + power.data + "/" + type.data + ", for file: " + file.getAbsolutePath());

        return new ClassifiedImage(inputImage, type, power);
    }

    @Override
    public SystemBase extract(final ClassifiedImage input) {

        if (null == input.inputImage.getImage() || ImageType.UNKNOWN == input.type.data
                || Power.UNKNOWN == input.power.data) {
            return new SystemBase(input, null, null);
        }

        final Mat img = input.inputImage.getImage();
        final File file = input.inputImage.getFile();

        LOGGER.info("Extraction start for file: " + file.getAbsolutePath());

        final OcrDataRectangle<String> sysNameRect = extractSystemName(input);
        if (Corrector.UNKNOWN_SYSTEM.equals(sysNameRect.data)) {
            return new SystemBase(input, sysNameRect, UNKNOWN_SYSTEM);
        }

        if (debug) {
            saveImageAtStage(ImageUtil.drawDataRectangles(img, input.type, input.power, sysNameRect), file,
                    "extract-data-rects");
        }

        switch (input.type.data) {
            case PP_PREPARATION:
                return extractPreparation(input, sysNameRect);
            case PP_EXPANSION:
                return extractExpansion(input, sysNameRect);
            case PP_CONTROL:
                return extractControl(input, sysNameRect);
            default:
                LOGGER.error("Unknown image type:" + file.getAbsolutePath());
                break;
        }
        LOGGER.info("Extraction end for file: " + file.getAbsolutePath());

        return new SystemBase(input, sysNameRect, null);
    }

    @Override
    public OcrResult generateReport(final List<File> files, final BiConsumer<Double, String> progressCallback) {

        final long startMillis = System.currentTimeMillis();

        final AtomicLong processedFiles = new AtomicLong(0);

        final OcrResult report = new OcrResult();
        files
                .stream()
                .map(file -> {
                            final String fileName = file.getAbsolutePath();
                            final Double progress = processedFiles.getAndIncrement() / (double) files.size();
                            progressCallback.accept(progress, fileName);
                            return file;
                        }
                )
                .map(this::load)
                .map(this::classify)
                .map(this::extract)
                .forEach(
                        system -> {
                            //gc friendly
                            system.classifiedImage.inputImage.nullImage();
                            try {
                                if (system instanceof PreparationSystem) {
                                    report.preparation.add((PreparationSystem) system);
                                } else if (system instanceof ExpansionSystem) {
                                    report.expansion.add((ExpansionSystem) system);
                                } else if (system instanceof ControlSystem) {
                                    report.control.add((ControlSystem) system);
                                }
                            } catch (final NullPointerException e) {
                                //FIXME
                            }
                        }
                );

//        try {
//            LOGGER.info("Data extracted:" + App.JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(report));
//        } catch (IOException e) {
//            LOGGER.error("Error in json conversion of extraction results", e);
//        }

        final long endMillis = System.currentTimeMillis();

        LOGGER.info("--------------------------------------------------------------Finished, duration: "
                + (endMillis - startMillis) + " ms");

        return report;
    }
}