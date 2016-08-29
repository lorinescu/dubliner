package pub.occams.elite.dubliner.application;

import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.domain.ImageType;
import pub.occams.elite.dubliner.domain.InputImage;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;

import java.io.File;
import java.util.List;

public interface ImageApi {

    SettingsDto getSettings();

    InputImage classifyImage(final File file);

    List<ControlSystem> extractDataFromImages(final List<File> files);

}
