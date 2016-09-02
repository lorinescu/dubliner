package pub.occams.elite.dubliner.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pub.occams.elite.dubliner.App;
import pub.occams.elite.dubliner.dto.ocr.ReportDto;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImageApiImplTest {

    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final String DATA_CONTROL_IMAGES = "data/control_images/";
    private ImageApi imageApi;

    @Before
    public void setUp() throws Exception {
        imageApi = new ImageApiImpl(App.loadSettings(), false);
    }

    @Test
    public void testExtractControlDataFromImages() throws Exception {

        final String[] extensions = new String[1];
        extensions[0] = "bmp";
        final Collection<File> referenceFiles = FileUtils.listFiles(new File(DATA_CONTROL_IMAGES), extensions, true);

        assertNotNull(referenceFiles);

        final File referenceJson = new File(DATA_CONTROL_IMAGES + "reference.json");
        assertNotNull(referenceJson);

        final ReportDto referenceData = JSON_MAPPER.readValue(referenceJson, ReportDto.class);

        final ReportDto dto = imageApi.extractDataFromImages(new ArrayList<>(referenceFiles));

        //TODO: deep Equals
        Assert.fail();
        assertEquals(referenceData, dto);
    }
}