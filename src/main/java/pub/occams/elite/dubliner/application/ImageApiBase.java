package pub.occams.elite.dubliner.application;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.log4j.Logger;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.InputImage;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;
import pub.occams.elite.dubliner.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public abstract class ImageApiBase implements ImageApi {


    protected static final Logger LOGGER = Logger.getLogger(App.LOGGER_NAME);

    protected final boolean debug;
    protected int cnt = 0;

    protected final SettingsDto settings;
    protected final Tesseract tessForNumbers = new Tesseract();
    protected final Tesseract tessForNames = new Tesseract();

    public ImageApiBase(final SettingsDto settings, final boolean debug) {
        this.settings = settings;
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


    protected String cleanPositiveNumber(final String str) {
        return str
                .trim()
                .replace("CC", "")
                .replace("O", "0")
                .replace("'", "")
                .replace(" ", "")
                .replace("-", "")
                .replace("B", "8")
                .replace("D", "0");
    }


    protected String cleanSystemName(final String str) {
        final String nameWithoutTurmoil = str.replaceAll("(\\(|\\[).*", "").trim();
        if (settings.corrections.systemName.containsKey(nameWithoutTurmoil)) {
            return settings.corrections.systemName.get(nameWithoutTurmoil);
        } else {
            return nameWithoutTurmoil;
        }
    }

    protected String cleanPowerName(final String str) {
        final String upperTrimmed = str.trim().toUpperCase();

        final Map<String, String> corrections = settings.corrections.powerName;

        String correctedName = upperTrimmed;
        for (final String incorrectPowerName : corrections.keySet()) {
            final String correctPowerName = corrections.get(incorrectPowerName);
            correctedName = upperTrimmed.replace(incorrectPowerName, correctPowerName);
        }

        return correctedName;
    }

    protected String ocrNumberRectangle(final BufferedImage image) {
        try {
            return tessForNumbers.doOCR(image);
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
}
