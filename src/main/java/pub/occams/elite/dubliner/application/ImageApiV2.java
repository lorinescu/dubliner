package pub.occams.elite.dubliner.application;

import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.domain.ImageType;
import pub.occams.elite.dubliner.domain.InputImage;
import pub.occams.elite.dubliner.dto.settings.SegmentsCoordinatesDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pub.occams.elite.dubliner.util.ImageUtil.*;

public class ImageApiV2 extends ImageApiBase {

    public ImageApiV2(final SettingsDto settings, final boolean debug) {
        super(settings, debug);
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

        final Optional<BufferedImage> croppedImage = crop(coord.overviewTab, image);
        if (!croppedImage.isPresent()) {
            return new InputImage(file, image, ImageType.UNKNOWN);
        }

        LOGGER.info("ocr-ing selected tab title");
        final BufferedImage ocrInputImage = scale(
                filterRedAndBinarize(invert(croppedImage.get()), settings.ocr.filterRedChannelMin)
        );
        saveImageAtStage(ocrInputImage, file.getName(), "classify-image");
        final String tabTitle = ocrSegment(ocrInputImage);

        LOGGER.info("image " + file.getName() + "has [" + tabTitle + "] in the Overview tab position");

        if (null == tabTitle || tabTitle.isEmpty() || !"OVERVIEW".equals(tabTitle.trim().replace("0", "O"))) {
            return new InputImage(file, image, ImageType.UNKNOWN);
        }

        //FIXME: ok, we are in a power play tab but which one - control, prep or expansion tab ?
        return new InputImage(file, image, ImageType.UNKNOWN);
    }

    @Override
    public List<ControlSystem> extractDataFromImages(final List<File> files) {

        files
                .stream()
                .map(this::classifyImage)
                .filter(img -> ImageType.UNKNOWN != img.getType())
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
            final ImageApiV2 api = new ImageApiV2(App.loadSettings(), true);
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
