package com.creditdatamw.labs.sparkpentaho;

import com.creditdatamw.labs.sparkpentaho.reports.OutputType;
import com.creditdatamw.labs.sparkpentaho.reports.ReportDefinition;
import com.creditdatamw.labs.sparkpentaho.resources.ReportResourceImpl;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.EnumSet;

import static com.creditdatamw.labs.sparkpentaho.SparkPentahoAPI.sparkPentaho;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SparkPentahoAPI} Test
 */
public class SparkPentahoAPITest {

    @Test
    public void testShouldCreateReports() {
        SparkPentahoAPI sparkPentahoAPI = new SparkPentahoAPI(
                "/api",
            ImmutableList.of(
                new ReportResourceImpl(
                    "/hello",
                    new String[]{"GET", "POST"},
                    EnumSet.of(OutputType.PDF, OutputType.HTML),
                    new ReportDefinition("helloReport",
                        Paths.get("src/test/resources/test_report.prpt").toAbsolutePath().toString(),
                        ImmutableList.of(
                            ReportDefinition.requiredParameter("report_id", String.class),
                            ReportDefinition.requiredParameter("subreport_1", true, Boolean.class),
                            ReportDefinition.requiredParameter("subreport_2", true, Boolean.class))
                    )
                ),
                new ReportResourceImpl(
                    "/more_params",
                    new String[]{"GET", "POST"},
                    EnumSet.of(OutputType.PDF, OutputType.HTML),
                    new ReportDefinition("moreParamsReport",
                        Paths.get("src/test/resources/test_report_2.prpt").toAbsolutePath().toString(),
                        ImmutableList.of(
                            ReportDefinition.requiredParameter("report_id", String.class),
                            ReportDefinition.optionalParameter("customer_name", "Your name", String.class)))
                )
            )
        );

        assertThat(sparkPentahoAPI.getReports()).isNotNull();
        assertThat(sparkPentahoAPI.getReports().resources()).isNotEmpty();
        assertThat(sparkPentahoAPI.getReports().resources()).hasSize(2);
    }
}
