package pub.occams.elite.dubliner.application;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.log4j.Logger;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    protected BufferedImage saveImageAtStage(final BufferedImage image, final String imageName, final String stage) {
        if (!debug) {
            return image;
        }
        try {
            final String path = "out/" + imageName;
            final File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdir();
            }
            ImageIO.write(image, "png", new File(path + "/" + cnt + "-" + stage + " .png"));
            cnt++;
        } catch (IOException e) {
            LOGGER.error("Failed to write debug image", e);
        }
        return image;
    }


    protected String cleanNumber(final String str) {
        return str
                .trim()
                .replace("CC", "")
                .replace("O", "0")
                .replace("'", "")
                .replace(" ", "")
                .replace("-", "");
    }


    protected String cleanName(final String str) {
        final String nameWithoutTurmoil = str.replaceAll("(\\(|\\[).*", "").trim();
        if (settings.corrections.systemName.containsKey(nameWithoutTurmoil)) {
            return settings.corrections.systemName.get(nameWithoutTurmoil);
        } else {
            return nameWithoutTurmoil;
        }
    }

    protected String ocrNumberSegment(final BufferedImage image) {
        try {
            return tessForNumbers.doOCR(image);
        } catch (TesseractException e) {
            LOGGER.error("Error when ocr-ing image.", e);
        }
        return "";
    }

    protected String ocrSegment(final BufferedImage image) {
        LOGGER.info("starting OCR");
        try {
            return tessForNames.doOCR(image);
        } catch (Exception e) {
            LOGGER.error("Error when ocr-ing image.", e);
        }
        return "";
    }
}
