package pub.occams.elite.dubliner.application;

import org.apache.commons.io.FileUtils;
import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.domain.ControlSystemRectangles;
import pub.occams.elite.dubliner.domain.ImageType;
import pub.occams.elite.dubliner.domain.InputImage;
import pub.occams.elite.dubliner.dto.settings.RectangleCoordinatesDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pub.occams.elite.dubliner.util.ImageUtil.*;

public class ImageApiV1 extends ImageApiBase {

    public ImageApiV1(final SettingsDto settings, final boolean debug) {
        super(settings, debug);
    }

    private ControlSystemRectangles saveControlSystem(final ControlSystemRectangles rectangles, final String stage) {
        saveImageAtStage(rectangles.getSystemName(), rectangles.getInputImage().getFile().getName(), stage);
        saveImageAtStage(rectangles.getUpkeepCost(), rectangles.getInputImage().getFile().getName(), stage);
        saveImageAtStage(rectangles.getDefaultUpkeepCost(), rectangles.getInputImage().getFile().getName(), stage);
        saveImageAtStage(rectangles.getCostIfFortified(), rectangles.getInputImage().getFile().getName(), stage);
        saveImageAtStage(rectangles.getCostIfUndermined(), rectangles.getInputImage().getFile().getName(), stage);
        saveImageAtStage(rectangles.getFortificationTotal(), rectangles.getInputImage().getFile().getName(), stage);
        saveImageAtStage(rectangles.getUnderminingTotal(), rectangles.getInputImage().getFile().getName(), stage);
        saveImageAtStage(rectangles.getUnderminingTrigger(), rectangles.getInputImage().getFile().getName(), stage);
        return rectangles;
    }

    private ControlSystemRectangles isolateTextColors(final ControlSystemRectangles rectangles) {
        LOGGER.info("isolating text colors for image " + rectangles.getInputImage().getFile().getName());
        int minRed = settings.ocr.filterRedChannelMin;
        return new ControlSystemRectangles(
                rectangles.getInputImage(),
                filterRedAndBinarize(rectangles.getSystemName(), minRed),
                filterRedAndBinarize(rectangles.getUpkeepCost(), minRed),
                filterRedAndBinarize(rectangles.getDefaultUpkeepCost(), minRed),
                filterRedAndBinarize(rectangles.getCostIfFortified(), minRed),
                filterRedAndBinarize(rectangles.getCostIfUndermined(), minRed),
                filterRedAndBinarize(rectangles.getFortificationTotal(), minRed),
                filterRedAndBinarize(rectangles.getFortificationTrigger(), minRed),
                filterRedAndBinarize(rectangles.getUnderminingTotal(), minRed),
                filterRedAndBinarize(rectangles.getUnderminingTrigger(), minRed)
        );
    }

    private Optional<ControlSystemRectangles> extractRectangles(final InputImage inputImage) {

        LOGGER.info("extracting rectangles from image " + inputImage.getFile().getName());
        final BufferedImage image = inputImage.getImage();
        final Optional<RectangleCoordinatesDto> maybeCoord = ImageUtil.getCoordinatesForImage(image, settings);
        if (!maybeCoord.isPresent()) {
            return Optional.empty();
        }

        final RectangleCoordinatesDto coord = maybeCoord.get();

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

        return Optional.of(new ControlSystemRectangles(
                inputImage,
                systemName.get(),
                upkeepCost.get(), defaultUpkeepCost.get(), costIfFortified.get(), costIfUndermined.get(),
                fortificationTotal.get(), fortificationTrigger.get(),
                underminingTotal.get(), underminingTrigger.get()));
    }


    private ControlSystemRectangles scaleRectangle(final ControlSystemRectangles rectangles) {
        LOGGER.info("scaling rectangles for image " + rectangles.getInputImage().getFile().getName());
        return new ControlSystemRectangles(
                rectangles.getInputImage(),
                scale(rectangles.getSystemName()),
                scale(rectangles.getUpkeepCost()),
                scale(rectangles.getDefaultUpkeepCost()),
                scale(rectangles.getCostIfFortified()),
                scale(rectangles.getCostIfUndermined()),
                scale(rectangles.getFortificationTotal()),
                scale(rectangles.getFortificationTrigger()),
                scale(rectangles.getUnderminingTotal()),
                scale(rectangles.getUnderminingTrigger())
        );
    }

    //FIXME: dedup cleanups and everything in here
    private ControlSystem rectanglesToText(final ControlSystemRectangles rectangles) {

        LOGGER.info("converting rectangles to text for image " + rectangles.getInputImage().getFile().getName());
        String name = ocrRectangle(rectangles.getSystemName()).trim();
        name = cleanName(name);

        int upkeepCost = -1;
        final String upkeepCostStr = ocrNumberRectangle(rectangles.getUpkeepCost());
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
        final String defaultUpkeepCostStr = ocrNumberRectangle(rectangles.getDefaultUpkeepCost());
        if (null != defaultUpkeepCostStr && !defaultUpkeepCostStr.isEmpty()) {
            try {
                defaultUpkeepCost = Integer.parseInt(cleanNumber(defaultUpkeepCostStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse defaultUpkeepCost from: [" + defaultUpkeepCostStr + "]");
            }
        }

        int costIfFortified = -1;
        final String costIfFortifiedStr = ocrNumberRectangle(rectangles.getCostIfFortified());
        if (null != costIfFortifiedStr && !costIfFortifiedStr.isEmpty()) {
            try {
                costIfFortified = Integer.parseInt(cleanNumber(costIfFortifiedStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse costIfFortified from: [" + costIfFortifiedStr + "]");
            }
        }

        int costIfUndermined = -1;
        final String costIfUnderminedStr = ocrNumberRectangle(rectangles.getCostIfUndermined());
        if (null != costIfUnderminedStr && !costIfUnderminedStr.isEmpty()) {
            try {
                costIfUndermined = Integer.parseInt(cleanNumber(costIfUnderminedStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse costIfUndermined from: [" + costIfUnderminedStr + "]");
            }
        }

        int fortifyTotal = -1;
        final String fortifyTotalStr = ocrNumberRectangle(rectangles.getFortificationTotal());
        if (null != fortifyTotalStr && !fortifyTotalStr.isEmpty()) {
            try {
                fortifyTotal = Integer.parseInt(cleanNumber(fortifyTotalStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse fortifyTotal from: [" + fortifyTotalStr + "]");
            }
        }

        int fortifyTrigger = -1;
        final String fortifyTriggerStr = ocrNumberRectangle(rectangles.getFortificationTrigger());
        if (null != fortifyTriggerStr && !fortifyTriggerStr.isEmpty()) {

            try {
                fortifyTrigger = Integer.parseInt(cleanNumber(fortifyTriggerStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse fortifyTrigger from: [" + fortifyTriggerStr + "]");
            }
        }

        int undermineTotal = -1;
        final String undermineTotalStr = ocrNumberRectangle(rectangles.getUnderminingTotal());
        if (null != undermineTotalStr && !undermineTotalStr.isEmpty()) {

            try {
                undermineTotal = Integer.parseInt(cleanNumber(undermineTotalStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse undermineTotal from: [" + undermineTotalStr + "]");
            }
        }

        int undermineTrigger = -1;
        final String undermineTriggerStr = ocrNumberRectangle(rectangles.getUnderminingTrigger());
        if (null != undermineTriggerStr && !undermineTriggerStr.isEmpty()) {
            try {
                undermineTrigger = Integer.parseInt(cleanNumber(undermineTriggerStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse undermineTrigger from: [" + undermineTriggerStr + "]");
            }
        }

        return new ControlSystem(
                rectangles,
                name,
                upkeepCost, defaultUpkeepCost, costIfFortified, costIfUndermined,
                fortifyTotal, fortifyTrigger,
                undermineTotal, undermineTrigger
        );
    }

    @Override
    public InputImage prepareAndClassifyImage(final File file) {

        LOGGER.info("verifying if file " + file.getName() + " is a screenshot of the Control tab");

        final Optional<BufferedImage> maybeImage = ImageUtil.readImageFromFile(file);
        if (!maybeImage.isPresent()) {
            return new InputImage(file, null, ImageType.UNKNOWN);
        }

        final BufferedImage image = maybeImage.get();

        final Optional<RectangleCoordinatesDto> maybeCoord = ImageUtil.getCoordinatesForImage(image, settings);
        if (!maybeCoord.isPresent()) {
            LOGGER.info("Could not find coordinates settings for image " + file.getName() +
                    " at " + image.getWidth() + "x" + image.getHeight());
            return new InputImage(file, image, ImageType.UNKNOWN);
        }
        final RectangleCoordinatesDto coord = maybeCoord.get();

        final Optional<BufferedImage> croppedImage = crop(coord.controlTab, image);
        if (!croppedImage.isPresent()) {
            return new InputImage(file, image, ImageType.UNKNOWN);
        }

        LOGGER.info("ocr-ing selected tab title");
        final String tabTitle = ocrRectangle(
                scale(filterRedAndBinarize(
                        invert(croppedImage.get()),
                        settings.ocr.filterRedChannelMin
                        )
                )
        );

        if (null != tabTitle && !tabTitle.isEmpty()) {
            final boolean isControlTabSelected = "CONTROL".equals(tabTitle.trim().replace("0", "O"));
            LOGGER.info(("image " + file.getName() + " is a control tab screenshot: " + isControlTabSelected));
            if (isControlTabSelected) {
                return new InputImage(file, image, ImageType.CONTROL);
            }
        }
        return new InputImage(file, image, ImageType.UNKNOWN);
    }

    @Override
    public List<ControlSystem> extractDataFromImages(final List<File> images) {
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
                        .map(this::prepareAndClassifyImage)
                        .filter(img -> img.getType() == ImageType.CONTROL)
                        .map(this::extractRectangles)
                        .filter(Optional::isPresent)
                        .map(Optional::get).map(css -> saveControlSystem(css, "rectangles"))
                        .map(this::isolateTextColors).map(css -> saveControlSystem(css, "isolateTextColors"))
                        .map(this::scaleRectangle).map(css -> saveControlSystem(css, "scale"))
//                        .map(this::increaseSegmentsContrast).map(css -> saveControlSystem(css, "contrast"))
//                        .map(this::binarizeSegments).map(css -> saveControlSystem(css, "binarize"))
//                        .map(this::invertSegments).map(css -> saveControlSystem(css, "invert"))
////                        .map(this::rectanglesToGray).map(css -> saveControlSystem(css, "toGray"))

                        .map(this::rectanglesToText)
                        .collect(Collectors.toList());
    }
}
