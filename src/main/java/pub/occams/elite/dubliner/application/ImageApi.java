package pub.occams.elite.dubliner.application;

import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.dto.settings.SettingsDto;

import java.io.File;
import java.util.List;

public interface ImageApi {

    SettingsDto getSettings();

    boolean isImageControlTab(final File file);

    List<ControlSystem> extractControlDataFromImages(final List<File> files);

}
