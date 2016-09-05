package pub.occams.elite.dubliner.application;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.log4j.Logger;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core.*;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.correct.Corrector;
import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.ImageType;
import pub.occams.elite.dubliner.domain.InputImage;
import pub.occams.elite.dubliner.domain.Power;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;
import pub.occams.elite.dubliner.domain.geometry.LineSegment;
import pub.occams.elite.dubliner.domain.geometry.Range;
import pub.occams.elite.dubliner.domain.geometry.Rectangle;
import pub.occams.elite.dubliner.dto.ocr.*;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static pub.occams.elite.dubliner.correct.Corrector.*;
import static pub.occams.elite.dubliner.domain.ImageType.*;
import static pub.occams.elite.dubliner.util.ImageUtil.*;
import static pub.occams.elite.dubliner.util.ImageUtil.invert;

public class ImageApiImpl implements ImageApi {

    private static final Logger LOGGER = Logger.getLogger(App.LOGGER_NAME);

    private final boolean debug;
    private int cnt = 0;

    private final SettingsDto settings;
    private final Corrector corrector;
    private final Tesseract tessForNumbers = new Tesseract();
    private final Tesseract tessForNames = new Tesseract();

    public ImageApiImpl(final SettingsDto settings, final boolean debug) {
        this.settings = settings;
        this.corrector = new Corrector(settings.corrections);
        this.debug = debug;

        tessForNumbers.setDatapath(settings.ocr.tesseractForNumbers.dataPath);
        tessForNumbers.setLanguage(settings.ocr.tesseractForNumbers.language);
        settings.ocr.tesseractForNumbers.variables.forEach(tessForNumbers::setTessVariable);
        settings.ocr.tesseractForNumbers.variables.forEach(tessForNumbers::setTessVariable);

        tessForNames.setDatapath(settings.ocr.tesseractForSystemNames.dataPath);
        tessForNames.setLanguage(settings.ocr.tesseractForSystemNames.language);
        settings.ocr.tesseractForSystemNames.variables.forEach(tessForNames::setTessVariable);
    }


    @Override
    public SettingsDto getSettings() {
        return this.settings;
    }

    @Override
    public Optional<InputImage> load(final File file) {
        LOGGER.info("loading image:" + file.getAbsolutePath());

        final Optional<BufferedImage> maybeImg = ImageUtil.readImageFromFile(file);
        if (!maybeImg.isPresent()) {
            return Optional.empty();
        }

        final BufferedImage img = maybeImg.get();

        saveImageAtStage(img, file, "loading");

        return Optional.of(new InputImage(file, maybeImg.get()));
    }

    protected BufferedImage saveImageAtStage(final BufferedImage image, final File imageFile, final String stage) {
        if (!debug) {
            return image;
        }

        try {
            final String imageName = imageFile.getCanonicalPath().replace(File.separator, "-").replace(":", "-");
            final String path = "out" + File.separator + imageName;
            final File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            ImageIO.write(image, "png", new File(dir + File.separator + cnt + "-" + stage + ".png"));
            cnt++;
        } catch (IOException e) {
            LOGGER.error("Failed to write debug image", e);
        }
        return image;
    }


    protected String ocrNumberRectangle(final BufferedImage image) {
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

    protected String ocrRectangle(final BufferedImage image) {
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

    private List<LineSegment> detectLines(final BufferedImage image, final File file, final int pixelResolution,
                                          final int threshold, final int maxGap,
                                          final int minLength, final int maxLength) {
        final IplImage img = ImageUtil.bufferedImageToIplImage(image);
        final IplImage dst = cvCreateImage(cvGetSize(img), img.depth(), 1);

        cvCanny(img, dst, 100, 800, 3);
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
                .filter(s -> s.y0 == s.y1)
                .sorted((s1, s2) -> s1.y0 - s2.y0)
                .collect(Collectors.toList());
    }

    //TODO: merge Horiz/Vert methods can be ... merged
    private List<LineSegment> mergeHorizontalSegments(final BufferedImage img, final File file,
                                                      final List<LineSegment> segments, final int threshold) {

        final Map<Range, LineSegment> clusters = new HashMap<>();

        for (final LineSegment s : segments) {
            Range foundRange = null;
            for (final Range range : clusters.keySet()) {
                if (s.y0 >= range.low && s.y0 <= range.high) {
                    //don't merge if the segments are colinear-ish by a few pixels
                    final LineSegment rangeSegment = clusters.get(range);
                    if (rangeSegment.x1 < s.x0 || rangeSegment.x0 > s.x1) {
                        continue;
                    }
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
        saveImageAtStage(ImageUtil.drawSegments(img, out), file, "merge-horizontal-progress");
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

        final BufferedImage ocrInputImage = invert(scale(filterRedAndBinarize(powerImage, 85)));

        saveImageAtStage(ocrInputImage, file, "detect-selected-power-ocr-input");

        final String str = ocrRectangle(ocrInputImage);
        LOGGER.info("Selected power, raw OCR:[" + str + "]");
        if (null == str || str.isEmpty()) {
            return Optional.empty();
        }

        Optional<Power> maybePower = corrector.powerFromString(str);
        if (!maybePower.isPresent()) {
            return Optional.empty();
        }
        LOGGER.info("Selected power, corrected to:[" + maybePower.get().getName() + "]");
        return Optional.of(new DataRectangle<>(maybePower.get(), powerNameSlice));
    }

    private Optional<DataRectangle<String>> extractSystemName(final ClassifiedImage input) {

        final File file = input.getInputImage().getFile();
        final BufferedImage img = input.getInputImage().getImage();

        LOGGER.info("Name extraction start for file: " + file.getAbsolutePath());

        final Rectangle reference = input.getPower().getRectangle();
        final int x0 = reference.x0;
        final int y0Offset = 20;//a few extra pixels to remove the line below the power name
        final int y0 = reference.y1 + y0Offset;
        final int x1 = reference.x1;
        final int y1 = img.getHeight();
        final Rectangle skipTabsAndPower = new Rectangle(x0, y0, x1, y1);
        final Optional<BufferedImage> maybeImg2 = ImageUtil.crop(skipTabsAndPower, img);
        if (!maybeImg2.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage img2 = maybeImg2.get();
        saveImageAtStage(img2, file, "extract-name-data-details-area");

        final double horizontalLineBelowSystemNameLengthFactor = 0.3;
        final int minLength = (int) (img2.getWidth() * horizontalLineBelowSystemNameLengthFactor);
        final int maxLength = img2.getWidth();
        final List<LineSegment> segments = detectHorizontalLines(img2, file, 1, 80, 1, minLength, maxLength);
        final List<LineSegment> merged = mergeHorizontalSegments(img2, file, segments, 3);
        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(img2, merged), file, "extract-name-data-lines");
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
        saveImageAtStage(img3, file, "extract-name-data");

        final BufferedImage ocrInputImage = invert(scale(img3));

        saveImageAtStage(ocrInputImage, file, "extract-name-ocr-input");

        final String str = ocrRectangle(ocrInputImage);
        LOGGER.info("Selected system, raw OCR:[" + str + "]");
        if (null == str || str.isEmpty()) {
            return Optional.empty();
        }

        final String sysName = corrector.cleanSystemName(str);
        LOGGER.info("Selected system, corrected to:[" + sysName + "]");

        final Rectangle realRect = new Rectangle(
                sysNameRect.x0 + x0, sysNameRect.y0 + y0,
                sysNameRect.x1 + x0, sysNameRect.y1 + y0
        );
        return Optional.of(new DataRectangle<>(sysName, realRect));
    }

    private Optional<ControlDto> extractControl(final ClassifiedImage input,
                                                final DataRectangle<String> sysNameRect) {
        final File file = input.getInputImage().getFile();
        final BufferedImage img = input.getInputImage().getImage();

        LOGGER.info("Control data extraction start for file: " + file.getAbsolutePath());

        final Rectangle reference = sysNameRect.getRectangle();
        final int x0 = reference.x0;
        final int y0Offset = 10;//a few extra pictures to remove the line below the system name
        final int y0 = reference.y1 + y0Offset;
        final int x1 = reference.x1;
        final int y1 = img.getHeight();
        final Rectangle skipTabsPowerAndSystemName = new Rectangle(x0, y0, x1, y1);
        final Optional<BufferedImage> maybeImg2 = ImageUtil.crop(skipTabsPowerAndSystemName,
                img
        );
        if (!maybeImg2.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage img2 = maybeImg2.get();

        saveImageAtStage(img2, file, "extract-control-data-details-area");

        final double horizontalLinesOnTopAndBottomOfCountersRectangles = 0.25;
        final int minLength = (int) (img2.getWidth() * horizontalLinesOnTopAndBottomOfCountersRectangles);
        final int maxLength = img2.getWidth();
        final List<LineSegment> segments = detectHorizontalLines(img2, file, 1, 30, 5, minLength, maxLength);
        final List<LineSegment> merged = mergeHorizontalSegments(img2, file, segments, 3);
        if (debug) {
            saveImageAtStage(ImageUtil.drawSegments(img2, merged), file, "extract-control-data-lines");
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
                .filter(s -> (s.x1 + 10) - img2.getWidth() <= 0)
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
            return Optional.empty();
        }

        final LineSegment topFortificationSegment = sortedFilteredSegments.get(sortedFilteredSegments.size() - 2);
        final LineSegment bottomFortificationSegment = sortedFilteredSegments.get(sortedFilteredSegments.size() - 1);

        final Rectangle costsRectangle = new Rectangle(
                bottomFortificationSegment.x1, 0,
                img2.getWidth(), topFortificationSegment.y1
        );
        final Rectangle fortificationRectangle = new Rectangle(
                0, topFortificationSegment.y0,
                bottomFortificationSegment.x1, bottomFortificationSegment.y0
        );
        final Rectangle underminingRectangle = new Rectangle(
                bottomFortificationSegment.x1, topFortificationSegment.y0,
                img2.getWidth(), bottomFortificationSegment.y0
        );

        final Optional<BufferedImage> maybeCostsImg = ImageUtil.crop(costsRectangle, img2);
        final Optional<BufferedImage> maybeFortificationImg = ImageUtil.crop(fortificationRectangle, img2);
        final Optional<BufferedImage> maybeUnderminingImg = ImageUtil.crop(underminingRectangle, img2);

        if (!(maybeCostsImg.isPresent() && maybeFortificationImg.isPresent() && maybeUnderminingImg.isPresent())) {
            return Optional.empty();
        }

        //FIXME: all strings cleanup/extraction below are really meassy, npe, out of bounds etc
        final BufferedImage costsImg = maybeCostsImg.get();
        final BufferedImage fortificationImg = maybeFortificationImg.get();
        final BufferedImage underminingImg = maybeUnderminingImg.get();

        final BufferedImage costsInputForOcr = scale(filterRedAndBinarize(costsImg, settings.ocr.filterRedChannelMin));

        saveImageAtStage(costsInputForOcr, file, "extract-control-data-costs-ocr-input");

        final String costsStr = ocrNumberRectangle(costsInputForOcr);
        LOGGER.info("System costs, raw OCR:[" + costsStr + "]");
        if (null == costsStr || costsStr.isEmpty()) {
            return Optional.empty();
        }
        final String[] parts1 = corrector.correctGlobalOcrErrors(costsStr).split("\n");
        if (parts1.length < 5) {
            return Optional.empty();
        }

        final Integer upkeepFromLastCycle = corrector.cleanPositiveInteger(parts1[0]);
        final Integer defaultUpkeepCost = corrector.cleanPositiveInteger(parts1[1]);
        final Integer costIfFortified = corrector.cleanPositiveInteger(parts1[2]);
        final Integer costIfUndermined = corrector.cleanPositiveInteger(parts1[3]);
        final Integer baseIncome = corrector.cleanPositiveInteger(parts1[4]);

        LOGGER.info("System costs, corrected to:["
                + upkeepFromLastCycle + "," + defaultUpkeepCost + "," + costIfFortified + "," + costIfUndermined
                + "," + baseIncome + "]");


        final BufferedImage fortificationOcrInput = filterRedAndBinarize(fortificationImg, settings.ocr.filterRedChannelMin);

        saveImageAtStage(fortificationOcrInput, file, "extract-control-data-fortif-ocr-input");

        final String fortificationStr = ocrNumberRectangle(fortificationOcrInput);
        LOGGER.info("Fortifications, raw OCR:[" + fortificationStr + "]");
        if (null == fortificationStr || fortificationStr.isEmpty()) {
            return Optional.empty();
        }

        final String[] fortificationParts = corrector.correctGlobalOcrErrors(fortificationStr).split("\n| ");
        Integer fortifyTotal = null, fortifyTrigger = null;
        String previous = "";
        for (final String part : fortificationParts) {
            if (previous.startsWith(TOTAL)) {
                fortifyTotal = corrector.cleanPositiveInteger(part);
            } else if (previous.startsWith(TRIGGER)) {
                fortifyTrigger = corrector.cleanPositiveInteger(part);
            }
            previous = part;
        }
        LOGGER.info("Fortifications, corrected to: [" + fortifyTotal + "," + fortifyTrigger + "]");

        saveImageAtStage(underminingImg, file, "extract-control-data-undermine");
        final BufferedImage underminingOcrInput = filterRedAndBinarize(underminingImg, settings.ocr.filterRedChannelMin);
        saveImageAtStage(underminingOcrInput, file, "extract-control-data-undermine-ocr-input");

        final String underminingStr = ocrNumberRectangle(underminingOcrInput);
        LOGGER.info("Undermining, raw OCR:[" + underminingStr + "]");
        if (null == underminingStr || underminingStr.isEmpty()) {
            return Optional.empty();
        }

        Integer undermineTotal = null, undermineTrigger = null;
        final String[] underminingParts = corrector.correctGlobalOcrErrors(underminingStr).split("\n| ");
        String previouss = "";
        for (final String part : underminingParts) {
            if (previouss.contains(TOTAL)) {
                undermineTotal = corrector.cleanPositiveInteger(part);
            } else if (previouss.contains(TRIGGER) && !part.contains(REACHED)) {
                undermineTrigger = corrector.cleanPositiveInteger(part);
            }
            previouss = part;
        }
        LOGGER.info("Undermining, after corrections:[" + undermineTotal + "," + undermineTrigger + "]");

        final DataRectangle<String> costsReal = new DataRectangle<>(
                "costs", new Rectangle(
                costsRectangle.x0 + x0, costsRectangle.y0 + y0,
                costsRectangle.x1 + x0, costsRectangle.y1 + y0
        )
        );
        final DataRectangle<String> undermineReal = new DataRectangle<>(
                "undermine", new Rectangle(
                underminingRectangle.x0 + x0, underminingRectangle.y0 + y0,
                underminingRectangle.x1 + x0, underminingRectangle.y1 + y0
        )
        );
        final DataRectangle<String> fortificationReal = new DataRectangle<>(
                "fortification", new Rectangle(
                fortificationRectangle.x0 + x0, fortificationRectangle.y0 + y0,
                fortificationRectangle.x1 + x0, fortificationRectangle.y1 + y0
        )
        );

        final ControlDto dto = new ControlDto();
        dto.systemNameRectangle = sysNameRect;
        dto.systemName = sysNameRect.getData();
        dto.costsRectangle = costsReal;
        dto.fortifyRectangle = fortificationReal;
        dto.undermineRectangle = undermineReal;
        dto.upkeepFromLastCycle = upkeepFromLastCycle;
        dto.defaultUpkeepCost = defaultUpkeepCost;
        dto.baseIncome = baseIncome;
        dto.costIfFortified = costIfFortified;
        dto.costIfUndermined = costIfUndermined;
        dto.fortifyTotal = fortifyTotal;
        dto.fortifyTrigger = fortifyTrigger;
        dto.undermineTotal = undermineTotal;
        dto.undermineTrigger = undermineTrigger;
        if (debug) {
            saveImageAtStage(
                    ImageUtil.drawDataRectangles(img, input.getType(), input.getPower(), sysNameRect, costsReal,
                            undermineReal, fortificationReal),
                    file,
                    "extract-data-rects");
        }

        return Optional.of(dto);
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

    public Optional<PowerPlayDto> extract(final ClassifiedImage input) {

        final BufferedImage img = input.getInputImage().getImage();
        final File file = input.getInputImage().getFile();

        LOGGER.info("Extraction start for file: " + file.getAbsolutePath());

        final Optional<DataRectangle<String>> maybeSysName = extractSystemName(input);
        if (!maybeSysName.isPresent()) {
            return Optional.empty();
        }
        final DataRectangle<String> sysNameRect = maybeSysName.get();

        if (debug) {
            saveImageAtStage(ImageUtil.drawDataRectangles(img, input.getType(), input.getPower(), sysNameRect), file,
                    "extract-data-rects");
        }

        switch (input.getType().getData()) {
            case PP_CONTROL:
                final Optional<ControlDto> dto = extractControl(input, sysNameRect);
                if (dto.isPresent()) {
                    dto.get().classifiedImage = input;
                    return Optional.of(dto.get());
                }
                return Optional.empty();
            case PP_EXPANSION:
                return Optional.empty();
            case PP_PREPARATION:
                return Optional.empty();
            default:
                break;
        }
        LOGGER.info("Extraction end for file: " + file.getAbsolutePath());

        return Optional.empty();
    }

    @Override
    public ReportDto extractDataFromImages(final List<File> files) {

        final ReportDto reportDto = new ReportDto();
        reportDto.powers = new HashMap<>();
        files
                .stream()
                .map(this::load)
                .filter(Optional::isPresent).map(Optional::get)
                .map(this::classify)
                .filter(Optional::isPresent).map(Optional::get)
                .map(this::extract)
                .filter(Optional::isPresent).map(Optional::get)
                .forEach(
                        ppDto -> {
                            final Power power = ppDto.classifiedImage.getPower().getData();
                            PowerReportDto powerReport = reportDto.powers.get(power);
                            if (null == powerReport) {
                                powerReport = new PowerReportDto();
                            }
                            if (ppDto instanceof ControlDto) {
                                powerReport.control.add((ControlDto) ppDto);
                            } else if (ppDto instanceof ExpansionDto) {
                                powerReport.expansion.add((ExpansionDto) ppDto);
                            } else if (ppDto instanceof PreparationDto) {
                                powerReport.preparation.add((PreparationDto) ppDto);
                            }
                            reportDto.powers.put(power, powerReport);
                        }
                );

        try {
            LOGGER.info("Data extracted:" + App.JSON_MAPPER.writeValueAsString(reportDto));
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return reportDto;
    }

    public static void main(String[] args) {
        try {
            final ImageApiImpl api = new ImageApiImpl(App.loadSettings(), true);
            final List<File> images = Arrays.asList(
//                    new File("data/control_images/1920x1200/mahon/control/not-undermined-not-fortified.bmp"),
//                    new File("data/control_images/1920x1200/mahon/control/undermined.bmp"),
//                    new File("data/control_images/1920x1200/mahon/control/undermined-fortified.bmp"),
//                    new File("data/control_images/1920x1080/mahon/control/undermined-fortified.bmp"),
//                    new File("data/control_images/1920x1200/ald/control/fortified.bmp"),
                    new File("data/control_images/1920x1080/ad/control/not-undermined-fortified.bmp"),
                    new File("data/control_images/1920x1200/winters/control/default.bmp")
            );
            api.extractDataFromImages(images);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}