package pub.occams.elite.dubliner.application;

import pub.occams.elite.dubliner.domain.image.ClassifiedImage;
import pub.occams.elite.dubliner.domain.image.InputImage;
import pub.occams.elite.dubliner.domain.image.ProcessedImage;
import pub.occams.elite.dubliner.domain.powerplay.OcrResult;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

public interface ImageApi {

    SettingsDto getSettings();

    InputImage load(final File file);

    ClassifiedImage classify(final InputImage file);

    ProcessedImage extract(final ClassifiedImage input);

    OcrResult generateReport(final List<File> files, final BiConsumer<Double, String> progressCallback);

}
