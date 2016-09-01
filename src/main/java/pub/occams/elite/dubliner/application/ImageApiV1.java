package pub.occams.elite.dubliner.application;

import org.apache.commons.io.FileUtils;
import pub.occams.elite.dubliner.domain.*;
import pub.occams.elite.dubliner.domain.geometry.DataRectangle;
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
        final File file = rectangles.getInput().getInputImage().getFile();
        saveImageAtStage(rectangles.getSystemName(), file, stage);
        saveImageAtStage(rectangles.getUpkeepFromLastCycle(), file, stage);
        saveImageAtStage(rectangles.getDefaultUpkeepCost(), file, stage);
        saveImageAtStage(rectangles.getCostIfFortified(), file, stage);
        saveImageAtStage(rectangles.getCostIfUndermined(), file, stage);
        saveImageAtStage(rectangles.getFortificationTotal(), file, stage);
        saveImageAtStage(rectangles.getUnderminingTotal(), file, stage);
        saveImageAtStage(rectangles.getUnderminingTrigger(), file, stage);
        return rectangles;
    }

    private ControlSystemRectangles isolateTextColors(final ControlSystemRectangles rectangles) {
        LOGGER.info("isolating text colors for image " + rectangles.getInput().getInputImage().getFile().getName());
        int minRed = settings.ocr.filterRedChannelMin;
        return new ControlSystemRectangles(
                rectangles.getInput(),
                filterRedAndBinarize(rectangles.getSystemName(), minRed),
                filterRedAndBinarize(rectangles.getUpkeepFromLastCycle(), minRed),
                filterRedAndBinarize(rectangles.getDefaultUpkeepCost(), minRed),
                filterRedAndBinarize(rectangles.getCostIfFortified(), minRed),
                filterRedAndBinarize(rectangles.getCostIfUndermined(), minRed),
                filterRedAndBinarize(rectangles.getBaseIncome(), minRed),
                filterRedAndBinarize(rectangles.getFortificationTotal(), minRed),
                filterRedAndBinarize(rectangles.getFortificationTrigger(), minRed),
                filterRedAndBinarize(rectangles.getUnderminingTotal(), minRed),
                filterRedAndBinarize(rectangles.getUnderminingTrigger(), minRed)
        );
    }

    private Optional<ControlSystemRectangles> extractRectangles(final ClassifiedImage input) {

        LOGGER.info("extracting rectangles from image " + input.getInputImage().getFile().getName());
        final BufferedImage image = input.getInputImage().getImage();
        final Optional<RectangleCoordinatesDto> maybeCoord = ImageUtil.getCoordinatesForImage(image, settings);
        if (!maybeCoord.isPresent()) {
            return Optional.empty();
        }

        final RectangleCoordinatesDto coord = maybeCoord.get();

        final Optional<BufferedImage> systemName = crop(coord.name, image);
        if (!systemName.isPresent()) {
            return Optional.empty();
        }

        final Optional<BufferedImage> upkeepFromLastCycle = crop(coord.upkeepFromLastCycle, image);
        if (!upkeepFromLastCycle.isPresent()) {
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

        final Optional<BufferedImage> baseIncome = crop(coord.baseIncome, image);
        if (!baseIncome.isPresent()) {
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
                input,
                systemName.get(),
                upkeepFromLastCycle.get(), defaultUpkeepCost.get(), costIfFortified.get(), costIfUndermined.get(),
                baseIncome.get(),
                fortificationTotal.get(), fortificationTrigger.get(),
                underminingTotal.get(), underminingTrigger.get()));
    }


    private ControlSystemRectangles scaleRectangle(final ControlSystemRectangles rectangles) {
        LOGGER.info("scaling rectangles for image " + rectangles.getInput().getInputImage().getFile().getName());
        return new ControlSystemRectangles(
                rectangles.getInput(),
                scale(rectangles.getSystemName()),
                scale(rectangles.getUpkeepFromLastCycle()),
                scale(rectangles.getDefaultUpkeepCost()),
                scale(rectangles.getCostIfFortified()),
                scale(rectangles.getCostIfUndermined()),
                scale(rectangles.getBaseIncome()),
                scale(rectangles.getFortificationTotal()),
                scale(rectangles.getFortificationTrigger()),
                scale(rectangles.getUnderminingTotal()),
                scale(rectangles.getUnderminingTrigger())
        );
    }

    //FIXME: dedup cleanups and everything in here
    private ControlSystem rectanglesToText(final ControlSystemRectangles rectangles) {

        LOGGER.info("converting rectangles to text for image " + rectangles.getInput().getInputImage().getFile()
                .getName());
        String name = ocrRectangle(rectangles.getSystemName()).trim();
        name = cleanSystemName(name);

        int upkeepCost = -1;
        final String upkeepCostStr = ocrNumberRectangle(rectangles.getUpkeepFromLastCycle());
        if (null != upkeepCostStr && !upkeepCostStr.isEmpty()) {
            try {
                final String cleanString = cleanPositiveNumber(upkeepCostStr);
                upkeepCost = Integer.parseInt(cleanString);
                LOGGER.info("[upkeepFromLastCycle] extracted raw string: " + upkeepCostStr.trim() + " and cleaned to: " + cleanString);
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse upkeepFromLastCycle from: [" + upkeepCostStr + "]");
            }
        }

        int upkeepFromLastCycle = -1;
        final String defaultUpkeepFromLastCycleStr = ocrNumberRectangle(rectangles.getUpkeepFromLastCycle());
        if (null != defaultUpkeepFromLastCycleStr && !defaultUpkeepFromLastCycleStr.isEmpty()) {
            try {
                upkeepFromLastCycle = Integer.parseInt(cleanPositiveNumber(defaultUpkeepFromLastCycleStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse defaultUpkeepFromLastCycle from: [" + defaultUpkeepFromLastCycleStr +
                        "]");
            }
        }

        int costIfFortified = -1;
        final String costIfFortifiedStr = ocrNumberRectangle(rectangles.getCostIfFortified());
        if (null != costIfFortifiedStr && !costIfFortifiedStr.isEmpty()) {
            try {
                costIfFortified = Integer.parseInt(cleanPositiveNumber(costIfFortifiedStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse costIfFortified from: [" + costIfFortifiedStr + "]");
            }
        }

        int costIfUndermined = -1;
        final String costIfUnderminedStr = ocrNumberRectangle(rectangles.getCostIfUndermined());
        if (null != costIfUnderminedStr && !costIfUnderminedStr.isEmpty()) {
            try {
                costIfUndermined = Integer.parseInt(cleanPositiveNumber(costIfUnderminedStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse costIfUndermined from: [" + costIfUnderminedStr + "]");
            }
        }

        int baseIncome = -1;
        final String defaultBaseIncomeStr = ocrNumberRectangle(rectangles.getBaseIncome());
        if (null != defaultBaseIncomeStr && !defaultBaseIncomeStr.isEmpty()) {
            try {
                baseIncome = Integer.parseInt(cleanPositiveNumber(defaultBaseIncomeStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse baseIncome from: [" + defaultUpkeepFromLastCycleStr +
                        "]");
            }
        }

        int fortifyTotal = -1;
        final String fortifyTotalStr = ocrNumberRectangle(rectangles.getFortificationTotal());
        if (null != fortifyTotalStr && !fortifyTotalStr.isEmpty()) {
            try {
                fortifyTotal = Integer.parseInt(cleanPositiveNumber(fortifyTotalStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse fortifyTotal from: [" + fortifyTotalStr + "]");
            }
        }

        int fortifyTrigger = -1;
        final String fortifyTriggerStr = ocrNumberRectangle(rectangles.getFortificationTrigger());
        if (null != fortifyTriggerStr && !fortifyTriggerStr.isEmpty()) {

            try {
                fortifyTrigger = Integer.parseInt(cleanPositiveNumber(fortifyTriggerStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse fortifyTrigger from: [" + fortifyTriggerStr + "]");
            }
        }

        int undermineTotal = -1;
        final String undermineTotalStr = ocrNumberRectangle(rectangles.getUnderminingTotal());
        if (null != undermineTotalStr && !undermineTotalStr.isEmpty()) {

            try {
                undermineTotal = Integer.parseInt(cleanPositiveNumber(undermineTotalStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse undermineTotal from: [" + undermineTotalStr + "]");
            }
        }

        int undermineTrigger = -1;
        final String undermineTriggerStr = ocrNumberRectangle(rectangles.getUnderminingTrigger());
        if (null != undermineTriggerStr && !undermineTriggerStr.isEmpty()) {
            try {
                undermineTrigger = Integer.parseInt(cleanPositiveNumber(undermineTriggerStr));
            } catch (final NumberFormatException | ArrayIndexOutOfBoundsException e) {
                LOGGER.error("could not parse undermineTrigger from: [" + undermineTriggerStr + "]");
            }
        }

        return new ControlSystem(
                rectangles, null,
                name,
                upkeepFromLastCycle, costIfFortified, costIfUndermined, baseIncome,
                fortifyTotal, fortifyTrigger,
                undermineTotal, undermineTrigger
        );
    }

    @Override
    public Optional<ClassifiedImage> classify(final InputImage input) {

        LOGGER.info("verifying if file " + input.getFile().getName() + " is a screenshot of the Control tab");

        final BufferedImage image = input.getImage();
        final Optional<RectangleCoordinatesDto> maybeCoord = ImageUtil.getCoordinatesForImage(image, settings);
        if (!maybeCoord.isPresent()) {
            LOGGER.info("Could not find coordinates settings for image " + input.getFile().getName() +
                    " at " + image.getWidth() + "x" + image.getHeight());
            return Optional.empty();
        }
        final RectangleCoordinatesDto coord = maybeCoord.get();

        final Optional<BufferedImage> maybeCroppedImage = crop(coord.controlTab, image);
        if (!maybeCroppedImage.isPresent()) {
            return Optional.empty();
        }
        final BufferedImage croppedImage = maybeCroppedImage.get();
        saveImageAtStage(croppedImage, input.getFile(), "classify-cropped");

        final BufferedImage ocrInputImage = scale(filterRedAndBinarize(invert(croppedImage), settings.ocr
                .filterRedChannelMin));

        saveImageAtStage(ocrInputImage, input.getFile(), "classify-ocr-input");
        LOGGER.info("ocr-ing selected tab title");
        final String tabTitle = ocrRectangle(ocrInputImage);

        if (null != tabTitle && !tabTitle.isEmpty()) {
            final boolean isControlTabSelected = "CONTROL".equals(tabTitle.trim().replace("0", "O"));
            LOGGER.info(("image " + input.getFile().getName() + " is a control tab screenshot: " + isControlTabSelected));
            if (isControlTabSelected) {
                return Optional.of(new ClassifiedImage(input, new DataRectangle<>(ImageType.PP_CONTROL,null), null));
            }
        }
        return Optional.empty();
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
                        .map(this::load)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(this::classify)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(img -> img.getType().getData() == ImageType.PP_CONTROL)
                        .map(this::extractRectangles)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(css -> saveControlSystem(css, "rectangles"))
                        .map(this::isolateTextColors)
                        .map(css -> saveControlSystem(css, "isolateTextColors"))
                        .map(this::scaleRectangle)
                        .map(css -> saveControlSystem(css, "scale"))
//                        .map(this::increaseSegmentsContrast).map(css -> saveControlSystem(css, "contrast"))
//                        .map(this::binarizeSegments).map(css -> saveControlSystem(css, "binarize"))
//                        .map(this::invertSegments).map(css -> saveControlSystem(css, "invert"))
////                        .map(this::rectanglesToGray).map(css -> saveControlSystem(css, "toGray"))

                        .map(this::rectanglesToText)
                        .collect(Collectors.toList());
    }
}
