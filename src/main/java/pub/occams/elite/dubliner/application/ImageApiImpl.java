package pub.occams.elite.dubliner.application;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.domain.ControlSystemSegments;
import pub.occams.elite.dubliner.domain.InputImage;
import pub.occams.elite.dubliner.dto.settings.SegmentsCoordinatesDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pub.occams.elite.dubliner.util.ImageUtil.*;

public class ImageApiImpl implements ImageApi {

    private static final Logger LOGGER = Logger.getLogger(App.LOGGER_NAME);
    private final SettingsDto settings;

    private final Tesseract tessForNumbers = new Tesseract();
    private final Tesseract tessForNames = new Tesseract();

    private int cnt = 0;

    public ImageApiImpl(final SettingsDto settings) {
        this.settings = settings;

        tessForNumbers.setDatapath(settings.ocr.tesseractForNumbers.dataPath);
        tessForNumbers.setLanguage(settings.ocr.tesseractForNumbers.language);
        settings.ocr.tesseractForNumbers.variables.forEach(tessForNumbers::setTessVariable);
        settings.ocr.tesseractForNumbers.variables.forEach(tessForNumbers::setTessVariable);

        tessForNames.setDatapath(settings.ocr.tesseractForSystemNames.dataPath);
        tessForNames.setLanguage(settings.ocr.tesseractForSystemNames.language);
        settings.ocr.tesseractForSystemNames.variables.forEach(tessForNames::setTessVariable);
    }

    private BufferedImage saveImageAtStage(final BufferedImage image, final String imageName, final String stage) {
        return image;
//        try {
//            final String path = "out/" + imageName;
//            final File dir = new File(path);
//            if (!dir.exists()) {
//                dir.mkdir();
//            }
//            ImageIO.write(image, "png", new File(path + "/" + cnt + "-" + stage + " .png"));
//            cnt++;
//        } catch (IOException e) {
//            LOGGER.error("Failed to write debug image", e);
//        }
//        return image;
    }


    private Optional<InputImage> loadImage(final File file) {
        final Optional<BufferedImage> image = ImageUtil.readImageFromFile(file);
        if (image.isPresent()) {
            return Optional.of(new InputImage(file, image.get()));
        }

        return Optional.empty();
    }

    private ControlSystemSegments saveControlSystem(final ControlSystemSegments segments, final String stage) {
        saveImageAtStage(segments.getSystemName(), segments.getInputImage().getFile().getName(), stage);
        saveImageAtStage(segments.getUpkeepCost(), segments.getInputImage().getFile().getName(), stage);
        saveImageAtStage(segments.getDefaultUpkeepCost(), segments.getInputImage().getFile().getName(), stage);
        saveImageAtStage(segments.getCostIfFortified(), segments.getInputImage().getFile().getName(), stage);
        saveImageAtStage(segments.getCostIfUndermined(), segments.getInputImage().getFile().getName(), stage);
        saveImageAtStage(segments.getFortificationTotal(), segments.getInputImage().getFile().getName(), stage);
        saveImageAtStage(segments.getUnderminingTotal(), segments.getInputImage().getFile().getName(), stage);
        saveImageAtStage(segments.getUnderminingTrigger(), segments.getInputImage().getFile().getName(), stage);
        return segments;
    }

    private ControlSystemSegments isolateTextColors(final ControlSystemSegments segments) {
        LOGGER.info("isolating text colors for image " + segments.getInputImage().getFile().getName());
        int minRed = settings.ocr.filterRedChannelMin;
        return new ControlSystemSegments(
                segments.getInputImage(),
                filterRedAndBinarize(segments.getSystemName(), minRed),
                filterRedAndBinarize(segments.getUpkeepCost(), minRed),
                filterRedAndBinarize(segments.getDefaultUpkeepCost(), minRed),
                filterRedAndBinarize(segments.getCostIfFortified(), minRed),
                filterRedAndBinarize(segments.getCostIfUndermined(), minRed),
                filterRedAndBinarize(segments.getFortificationTotal(), minRed),
                filterRedAndBinarize(segments.getFortificationTrigger(), minRed),
                filterRedAndBinarize(segments.getUnderminingTotal(), minRed),
                filterRedAndBinarize(segments.getUnderminingTrigger(), minRed)
        );
    }

    private Optional<ControlSystemSegments> extractSegments(final InputImage inputImage) {

        LOGGER.info("extracting segments from image " + inputImage.getFile().getName());
        final BufferedImage image = inputImage.getImage();
        final Optional<SegmentsCoordinatesDto> maybeCoord = ImageUtil.getCoordinatesForImage(image, settings);
        if (!maybeCoord.isPresent()) {
            return Optional.empty();
        }

        final SegmentsCoordinatesDto coord = maybeCoord.get();

        final Optional<BufferedImage> systemName = crop(coord.name, image);
        if (!systemName.isPresent()) {
            return Optional.empty();
        }

        final Optional<BufferedImage> upkeepCost = crop(coord.upkeepCost, image);
        if (!upkeepCost.isPresent()) {
            return Optional.empty();
        }

        final Optional<BufferedImage> defaultUpkeepCost = crop(coord.defaultUpkeepCost, image);
        if (!defaultUpkeepCost.isPresent()) {
            return Optional.empty();
        }

        final Optional<BufferedImage> costIfFortified = crop(coord.costIfFortified, image);
        if (!costIfFortified.isPresent()) {
            return Optional.empty();
        }

        final Optional<BufferedImage> costIfUndermined = crop(coord.costIfUndermined, image);
        if (!costIfUndermined.isPresent()) {
            return Optional.empty();
        }

        final Optional<BufferedImage> fortificationTotal = crop(coord.fortificationTotal, image);
        if (!fortificationTotal.isPresent()) {
            return Optional.empty();
        }

        final Optional<BufferedImage> fortificationTrigger = crop(coord.fortificationTrigger, image);
        if (!fortificationTrigger.isPresent()) {
            return Optional.empty();
        }

        final Optional<BufferedImage> underminingTotal = crop(coord.underminingTotal, image);
        if (!underminingTotal.isPresent()) {
            return Optional.empty();
        }

        final Optional<BufferedImage> underminingTrigger = crop(coord.underminingTrigger, image);
        if (!underminingTrigger.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new ControlSystemSegments(
                inputImage,
                systemName.get(),
                upkeepCost.get(), defaultUpkeepCost.get(), costIfFortified.get(), costIfUndermined.get(),
                fortificationTotal.get(), fortificationTrigger.get(),
                underminingTotal.get(), underminingTrigger.get()));
    }


    private ControlSystemSegments scaleSegments(final ControlSystemSegments segments) {
        LOGGER.info("scaling segments for image " + segments.getInputImage().getFile().getName());
        return new ControlSystemSegments(
                segments.getInputImage(),
                scale(segments.getSystemName()),
                scale(segments.getUpkeepCost()),
                scale(segments.getDefaultUpkeepCost()),
                scale(segments.getCostIfFortified()),
                scale(segments.getCostIfUndermined()),
                scale(segments.getFortificationTotal()),
                scale(segments.getFortificationTrigger()),
                scale(segments.getUnderminingTotal()),
                scale(segments.getUnderminingTrigger())
        );
    }

    private String cleanNumber(final String str) {
        return str
                .trim()
                .replace("CC", "")
                .replace("O", "0")
                .replace("'", "")
                .replace(" ", "")
                .replace("-", "");
    }

    private String cleanName(final String str) {
        final String nameWithoutTurmoil = str.replaceAll("(\\(|\\[).*", "").trim();
        if (settings.corrections.systemName.containsKey(nameWithoutTurmoil)) {
            return settings.corrections.systemName.get(nameWithoutTurmoil);
        } else {
            return nameWithoutTurmoil;
        }
    }

    //FIXME: dedup cleanups and everything in here
    private ControlSystem segmentsToText(final ControlSystemSegments segments) {

        LOGGER.info("converting segments to text for image " + segments.getInputImage().getFile().getName());
        String name = ocrSegment(segments.getSystemName()).trim();
        name = cleanName(name);

        int upkeepCost = -1;
        final String upkeepCostStr = ocrNumberSegment(segments.getUpkeepCost());
        if (null != upkeepCostStr && !upkeepCostStr.isEmpty()) {
            try {
                final String cleanString = cleanNumber(upkeepCostStr);
                upkeepCost = Integer.parseInt(cleanString);
                LOGGER.info("[upkeepCost] extracted raw string: " + upkeepCostStr.trim() + " and cleaned to: " + cleanString);
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse upkeepCost from: [" + upkeepCostStr + "]");
            }
        }

        int defaultUpkeepCost = -1;
        final String defaultUpkeepCostStr = ocrNumberSegment(segments.getDefaultUpkeepCost());
        if (null != defaultUpkeepCostStr && !defaultUpkeepCostStr.isEmpty()) {
            try {
                defaultUpkeepCost = Integer.parseInt(cleanNumber(defaultUpkeepCostStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse defaultUpkeepCost from: [" + defaultUpkeepCostStr + "]");
            }
        }

        int costIfFortified = -1;
        final String costIfFortifiedStr = ocrNumberSegment(segments.getCostIfFortified());
        if (null != costIfFortifiedStr && !costIfFortifiedStr.isEmpty()) {
            try {
                costIfFortified = Integer.parseInt(cleanNumber(costIfFortifiedStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse costIfFortified from: [" + costIfFortifiedStr + "]");
            }
        }

        int costIfUndermined = -1;
        final String costIfUnderminedStr = ocrNumberSegment(segments.getCostIfUndermined());
        if (null != costIfUnderminedStr && !costIfUnderminedStr.isEmpty()) {
            try {
                costIfUndermined = Integer.parseInt(cleanNumber(costIfUnderminedStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse costIfUndermined from: [" + costIfUnderminedStr + "]");
            }
        }

        int fortifyTotal = -1;
        final String fortifyTotalStr = ocrNumberSegment(segments.getFortificationTotal());
        if (null != fortifyTotalStr && !fortifyTotalStr.isEmpty()) {
            try {
                fortifyTotal = Integer.parseInt(cleanNumber(fortifyTotalStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse fortifyTotal from: [" + fortifyTotalStr + "]");
            }
        }

        int fortifyTrigger = -1;
        final String fortifyTriggerStr = ocrNumberSegment(segments.getFortificationTrigger());
        if (null != fortifyTriggerStr && !fortifyTriggerStr.isEmpty()) {

            try {
                fortifyTrigger = Integer.parseInt(cleanNumber(fortifyTriggerStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse fortifyTrigger from: [" + fortifyTriggerStr + "]");
            }
        }

        int undermineTotal = -1;
        final String undermineTotalStr = ocrNumberSegment(segments.getUnderminingTotal());
        if (null != undermineTotalStr && !undermineTotalStr.isEmpty()) {

            try {
                undermineTotal = Integer.parseInt(cleanNumber(undermineTotalStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse undermineTotal from: [" + undermineTotalStr + "]");
            }
        }

        int undermineTrigger = -1;
        final String undermineTriggerStr = ocrNumberSegment(segments.getUnderminingTrigger());
        if (null != undermineTriggerStr && !undermineTriggerStr.isEmpty()) {
            try {
                undermineTrigger = Integer.parseInt(cleanNumber(undermineTriggerStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse undermineTrigger from: [" + undermineTriggerStr + "]");
            }
        }

        return new ControlSystem(
                segments,
                name,
                upkeepCost, defaultUpkeepCost, costIfFortified, costIfUndermined,
                fortifyTotal, fortifyTrigger,
                undermineTotal, undermineTrigger
        );
    }

    private String ocrNumberSegment(final BufferedImage image) {
        try {
            return tessForNumbers.doOCR(image);
        } catch (TesseractException e) {
            LOGGER.error("Error when ocr-ing image.", e);
        }
        return "";
    }

    private String ocrSegment(final BufferedImage image) {
        LOGGER.info("starting OCR");
        try {
            return tessForNames.doOCR(image);
        } catch (Exception e) {
            LOGGER.error("Error when ocr-ing image.", e);
        }
        return "";
    }

    @Override
    public SettingsDto getSettings() {
        return this.settings;
    }

    @Override
    public boolean isImageControlTab(final File file) {

        LOGGER.info("verifying if file " + file.getName() + " is a screenshot of the Control tab");

        final Optional<InputImage> inputImage = loadImage(file);
        if (!inputImage.isPresent()) {
            LOGGER.info("Could not load an image from file " + file.getName());
            return false;
        }

        final BufferedImage image = inputImage.get().getImage();

        final Optional<SegmentsCoordinatesDto> maybeCoord = ImageUtil.getCoordinatesForImage(image, settings);
        if (!maybeCoord.isPresent()) {
            LOGGER.info("Could not find coordinates settings for image " + file.getName() +
                    " at " + image.getWidth() + "x" + image.getHeight());
            return false;
        }
        final SegmentsCoordinatesDto coord = maybeCoord.get();

        final Optional<BufferedImage> croppedImage = crop(coord.controlTab, image);
        if (!croppedImage.isPresent()) {
            return false;
        }

        LOGGER.info("ocr-ing selected tab title");
        final String tabTitle = ocrSegment(
                scale(filterRedAndBinarize(
                        invert(croppedImage.get()),
                        settings.ocr.filterRedChannelMin
                        )
                )
        );

        if (null != tabTitle && !tabTitle.isEmpty()) {
            final boolean isControlTabSelected = "CONTROL".equals(tabTitle.trim().replace("0", "O"));
            LOGGER.info(("image " + file.getName() + " is a control tab screenshot: " + isControlTabSelected));
            return isControlTabSelected;
        }
        return false;
    }

    @Override
    public List<ControlSystem> extractControlDataFromImages(final List<File> images) {
        final File debugDir = new File("out");
        if (debugDir.exists()) {
            try {
                FileUtils.deleteDirectory(debugDir);
            } catch (IOException e) {
                LOGGER.error("Failed to delete debug dir", e);
            }
        }
        debugDir.mkdir();

        //TODO: thread pool here if tesseract permits
        return
                images
                        .stream()
                        .filter(this::isImageControlTab)
                        .map(this::loadImage)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(this::extractSegments)
                        .filter(Optional::isPresent)
                        .map(Optional::get).map(css -> saveControlSystem(css, "segments"))
                        .map(this::isolateTextColors).map(css -> saveControlSystem(css, "isolateTextColors"))
                        .map(this::scaleSegments).map(css -> saveControlSystem(css, "scale"))
//                        .map(this::increaseSegmentsContrast).map(css -> saveControlSystem(css, "contrast"))
//                        .map(this::binarizeSegments).map(css -> saveControlSystem(css, "binarize"))
//                        .map(this::invertSegments).map(css -> saveControlSystem(css, "invert"))
////                        .map(this::segmentsToGray).map(css -> saveControlSystem(css, "toGray"))

                        .map(this::segmentsToText)
                        .collect(Collectors.toList());
    }
}
