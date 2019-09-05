package com.creditdatamw.labs.sparkpentaho;

import com.creditdatamw.labs.sparkpentaho.reports.OutputType;
import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import com.creditdatamw.labs.sparkpentaho.http.ReportResourceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * {@link SparkPentahoAPI} Test
 */
public class SparkPentahoAPITest {

    SparkPentahoAPI sparkPentahoAPI;

    @Before
    public void setUp() {
        sparkPentahoAPI = new SparkPentahoAPI(
            "/api",
            ImmutableList.of(
                new ReportResourceImpl(
                    "/hello",
                    new String[]{"GET", "POST"},
                    EnumSet.of(OutputType.PDF, OutputType.HTML, OutputType.TXT),
                    new ReportDefinition("helloReport",
                        Paths.get("src/test/resources/test_report.prpt").toAbsolutePath().toString(),
                        ImmutableList.of(
                            ReportDefinition.requiredParameter("report_id", String.class),
                            ReportDefinition.requiredParameter("subreport_1", true, Boolean.class),
                            ReportDefinition.requiredParameter("subreport_2", true, Boolean.class))
                    )
                )
            )
        );
    }

    @Test
    public void testShouldCreateReports() {
        assertThat(sparkPentahoAPI.getReports()).isNotNull();
        assertThat(sparkPentahoAPI.getReports().resources()).isNotEmpty();
        assertThat(sparkPentahoAPI.getReports().resources()).hasSize(1);
    }

    @Test
    public void testCanGETReportDocument() throws IOException {
        sparkPentahoAPI.start();
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
        sparkPentahoAPI.stop();
    }

}
