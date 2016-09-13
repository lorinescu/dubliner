package pub.occams.elite.dubliner.application;

import pub.occams.elite.dubliner.domain.image.ClassifiedImage;
import pub.occams.elite.dubliner.domain.image.InputImage;
import pub.occams.elite.dubliner.domain.powerplay.SystemBase;
import pub.occams.elite.dubliner.domain.powerplay.PowerPlayReport;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

public interface ImageApi {

    SettingsDto getSettings();

    InputImage load(final File file);

    ClassifiedImage classify(final InputImage file);

    SystemBase extract(final ClassifiedImage input);

    PowerPlayReport generateReport(final List<File> files, final BiConsumer<Double, String> progressCallback);

}
