package pub.occams.elite.dubliner.application;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.junit.Before;
import org.junit.Test;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.ControlSystem;
import pub.occams.elite.dubliner.domain.ImageType;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.Assert.*;

public class ImageApiV1Test {

    private static final String DATA_CONTROL_IMAGES = "data/control_images/";
    private ImageApi imageApi;

    @Before
    public void setUp() throws Exception {
        imageApi = new ImageApiV1(App.loadSettingsV1(), false);
    }

    @Test
    public void testIsImageControlTab() throws Exception {
        assertEquals(ImageType.CONTROL, imageApi.classifyImage(new File(DATA_CONTROL_IMAGES + "1920x1080_1.bmp")).getType());

        //FIXME: find a bad image
        //assertFalse(imageApi.classifyImage(new File("data/images/some_bad_image.bmp")));
    }

    @Test
    public void testExtractControlDataFromImages() throws Exception {

        final File[] referenceFiles = new File(DATA_CONTROL_IMAGES).listFiles((FileFilter) new SuffixFileFilter("bmp"));

        assertNotNull(referenceFiles);

        final File referenceCsv = new File(DATA_CONTROL_IMAGES + "0000_manually_extracted_text_from_images.csv");
        assertNotNull(referenceCsv);

        final List<ControlSystem> systems = imageApi.extractDataFromImages(Arrays.asList(referenceFiles));
        assertEquals(referenceFiles.length, systems.size());

        final Scanner scanner = new Scanner(referenceCsv);
        scanner.nextLine(); //skip header
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] parts = line.split(",");
            final String fileName = parts[0].replace("\"", "");
            final Optional<ControlSystem> system =
                    systems
                            .stream()
                            .filter(cs -> fileName.equals(cs.getControlSystemSegments().getInputImage().getFile().getName()))
                            .findFirst();

            assertTrue("file not found:" + fileName, system.isPresent());

            final ControlSystem s = system.get();

            //FIXME: use SuperCSV
            assertEquals(parts[1], s.getSystemName());
            assertEquals(parts[2], String.valueOf(s.getUpkeepCost()));
            assertEquals(parts[3], String.valueOf(s.getDefaultUpkeepCost()));
            assertEquals(parts[4], String.valueOf(s.getCostIfFortified()));
            assertEquals(parts[5], String.valueOf(s.getCostIfUndermined()));
            assertEquals(parts[6], String.valueOf(s.getFortifyTotal()));
            assertEquals(parts[7], String.valueOf(s.getFortifyTrigger()));
            assertEquals(parts[8], String.valueOf(s.getUnderminingTotal()));
            assertEquals("incorrect undermining trigger in  image : " + fileName, parts[9], String.valueOf(s.getUnderminingTrigger()));
        }
    }
}