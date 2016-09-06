package pub.occams.elite.dubliner.application;

import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.InputImage;
import pub.occams.elite.dubliner.dto.ocr.PowerPlayDto;
import pub.occams.elite.dubliner.dto.ocr.ReportDto;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface ImageApi {

    SettingsDto getSettings();

    InputImage load(final File file);

    ClassifiedImage classify(final InputImage file);

    PowerPlayDto extract(final ClassifiedImage input);

    ReportDto extractDataFromImages(final List<File> files);

}
