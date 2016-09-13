package pub.occams.elite.dubliner.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import pub.occams.elite.dubliner.App;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImageApiImplTest {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final String DATA_CONTROL_IMAGES = "data/control_images/";
    private ImageApi imageApi;

    @Before
    public void setUp() throws Exception {
        imageApi = new ImageApiImpl(App.loadSettings(), App.loadEddbSystems(), false);
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
//        final File referenceJson = new File(DATA_CONTROL_IMAGES + "reference.json");
//        assertNotNull(referenceJson);
//
//        final String referenceData = JSON_MAPPER.writeValueAsString(JSON_MAPPER.readValue(referenceJson, PowerPlayReport
//                .class));
//
//        final String extractedData = JSON_MAPPER.writeValueAsString(imageApi.generateReport(new ArrayList<>
//                (referenceFiles)));

//        assertEquals(referenceData, extractedData);
    }

    //playground
    public static void main(String[] args) {
        try {
            final ImageApiImpl api = new ImageApiImpl(App.loadSettings(), App.loadEddbSystems(), true);

            api.generateReport(
                    Arrays.asList(
                            //control
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0019.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0050.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0058.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0080.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0013.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0000.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0024.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0059.bmp"),
//                            new File("data/control_images/1920x1080/winters/control/undermined-fortified.bmp"),
//                            new File("data/control_images/1920x1080/delaine/control/fortified-undermined.bmp"),
//                            new File("data/control_images/1920x1080/mahon/control/undermined-fortified.bmp"),
//                            new File("data/control_images/1920x1080/patreus/control/fortified-undermined.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0028.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0011.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0056.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0062.bmp"),
//                            new File("data/control_images/1920x1200/mahon/control/Screenshot_0101.bmp")
                            //preparation
                            new File("data/control_images/1920x1200/ad/preparation/1.bmp")
                    ),
                    (x,y) -> {}
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}