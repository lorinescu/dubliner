package pub.occams.elite.dubliner.application;

import org.junit.Before;
import org.junit.Test;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.domain.ClassifiedImage;
import pub.occams.elite.dubliner.domain.ImageType;
import pub.occams.elite.dubliner.domain.InputImage;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImageApiImplTest {

    private static final String DATA_CONTROL_IMAGES = "data/control_images/";
    private ImageApi imageApi;

    @Before
    public void setUp() throws Exception {
        imageApi = new ImageApiImpl(App.loadSettings(), false);
    }

    @Test
    public void testIsImageControlTab() throws Exception {
        final Optional<InputImage> maybeImg = imageApi.load(new File(DATA_CONTROL_IMAGES + "1920x1080/mahon/control/1.bmp"));
        assertTrue(maybeImg.isPresent());
        final Optional<ClassifiedImage> maybeImg2 = imageApi.classify(maybeImg.get());
        assertTrue(maybeImg2.isPresent());
        assertEquals(ImageType.PP_CONTROL, maybeImg2.get().getType());

        //FIXME: find a bad image
        //assertFalse(imageApi.prepareAndClassifyImage(new File("data/images/some_bad_image.bmp")));
    }

    @Test
    public void testExtractControlDataFromImages() throws Exception {
//
//        final String[] extensions = new String[1];
//        extensions[0] = "bmp";
//        final Collection<File> referenceFiles = FileUtils.listFiles(new File(DATA_CONTROL_IMAGES), extensions, true);
//
//        assertNotNull(referenceFiles);
//
//        final File referenceCsv = new File(DATA_CONTROL_IMAGES + "0000_manually_extracted_text_from_images.csv");
//        assertNotNull(referenceCsv);
//
//        final List<ControlSystem> systems = imageApi.extractDataFromImages(new ArrayList<>(referenceFiles));
//        assertEquals(referenceFiles.size(), systems.size());
//
//        final Scanner scanner = new Scanner(referenceCsv);
//        scanner.nextLine(); //skip header
//        while (scanner.hasNextLine()) {
//            final String line = scanner.nextLine();
//            final String[] parts = line.split(",");
//            final String fileName = parts[0].replace("\"", "");
//            final File file = new File(DATA_CONTROL_IMAGES + "/" + fileName);
//            final Optional<ControlSystem> system =
//                    systems
//                            .stream()
//                            .filter(cs -> file.getAbsolutePath().equals(cs.getControlSystemRectangles().getInput()
//                                    .getInputImage().getFile().getAbsolutePath()))
//                            .findFirst();
//
//            assertTrue("file not found:" + fileName, system.isPresent());
//
//            final ControlSystem s = system.get();
//
//            //FIXME: use SuperCSV
//            assertEquals(parts[1], s.getSystemName());
//            assertEquals(parts[2], String.valueOf(s.getUpkeepFromLastCycle()));
//            assertEquals(parts[4], String.valueOf(s.getCostIfFortified()));
//            assertEquals(parts[5], String.valueOf(s.getCostIfUndermined()));
//            assertEquals(parts[6], String.valueOf(s.getBaseIncome()));
//            assertEquals(parts[7], String.valueOf(s.getFortifyTotal()));
//            assertEquals(parts[8], String.valueOf(s.getFortifyTrigger()));
//            assertEquals(parts[9], String.valueOf(s.getUnderminingTotal()));
//            assertEquals("incorrect undermining trigger in  image : " + fileName, parts[9], String.valueOf(s.getUnderminingTrigger()));
//        }
    }
}