package cloud.nndi.labs.kapenta.pentaho;

import cloud.nndi.labs.kapenta.OutputType;
import org.junit.BeforeClass;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * {@link Generator} tests
 */
public class GeneratorTest {

    @BeforeClass
    public static void beforeClass() {
        ClassicEngineBoot.getInstance().start();
    }


    // @Test TODO(zikani03) re-enable tests
    public void testGenerateReport() throws IOException {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] bytes = Files.readAllBytes(Paths.get("./src/test/resources/test_report_out.txt"));
            String reportData = new String(bytes);
            Map<String, Object> params = new HashMap<>();
            params.put("subreport_1", true);
            params.put("subreport_2", true);
            params.put("report_id", "1");
            Generator.generateReport("./src/test/resources/test_report.prpt",
                    params,
                    OutputType.TXT,
                    bos);

            String output = bos.toString("UTF-8");

            assertEquals(reportData, output);
        }
    }
}
