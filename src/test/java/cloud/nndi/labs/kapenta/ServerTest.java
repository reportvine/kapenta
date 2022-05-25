package cloud.nndi.labs.kapenta;

import cloud.nndi.labs.kapenta.http.ReportResourceImpl;
import cloud.nndi.labs.kapenta.reportdefinition.ReportDefinition;
import com.google.common.collect.ImmutableList;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import spark.Spark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;

import static cloud.nndi.labs.kapenta.parameters.ParameterDefinition.requiredParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * {@link Server} Test
 */
public class ServerTest {

    Server kapentaAPI;

    @Before
    public void setUp() {
        kapentaAPI = new Server(
            "/api",
            ImmutableList.of(
                new ReportResourceImpl(
                    "/hello",
                    new String[]{"GET", "POST"},
                    EnumSet.of(OutputType.PDF, OutputType.HTML, OutputType.TXT),
                    new ReportDefinition("helloReport",
                        Paths.get("src/test/resources/test_report.prpt").toAbsolutePath().toString(),
                        ImmutableList.of(
                            requiredParameter("report_id", String.class),
                            requiredParameter("subreport_1", true, Boolean.class),
                            requiredParameter("subreport_2", true, Boolean.class))
                    )
                )
            )
        );
    }

    @Test
    public void testShouldCreateReports() {
        assertThat(kapentaAPI.getReports()).isNotNull();
        assertThat(kapentaAPI.getReports().resources()).isNotEmpty();
        assertThat(kapentaAPI.getReports().resources()).hasSize(1);
    }

    // @Test TODO(zikani03) - enable test
    public void testCanGETReportDocument() throws IOException {
        kapentaAPI.start();
        String host = String.format("http://localhost:%s/api/hello.txt?subreport_1=true&subreport_2=true&report_id=1", Spark.port());

        try (CloseableHttpClient client = HttpClients.createDefault();
             ByteArrayOutputStream bos = new ByteArrayOutputStream() ) {

            byte[] bytes = Files.readAllBytes(Paths.get("./src/test/resources/test_report_out.txt"));
            String reportData = new String(bytes);

            HttpGet get = new HttpGet(host);
            CloseableHttpResponse response = client.execute(get);

            response.getEntity().writeTo(bos);

            assertEquals(reportData, bos.toString("UTF-8"));
        }
    }

}
