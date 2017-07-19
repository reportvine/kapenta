package com.creditdatamw.labs.sparkpentaho.reports;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * ReportDefinitionTest
 */
public class ReportDefinitionTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetParameterType() {
        List<ParameterDefinition> params = new ArrayList<>();
        params.add(ReportDefinition.optionalParameter("subreport_1", Boolean.class));
        ReportDefinition reportDefinition = new ReportDefinition("report",
                "./src/test/resources/test_report.prpt",
                params);

        assertEquals(Boolean.class, reportDefinition.parameterType("subreport_1"));
    }

    @Test
    public void testFailToGetParameterType() {
        List<ParameterDefinition> params = new ArrayList<>();
        params.add(ReportDefinition.optionalParameter("subreport_1", Boolean.class));
        ReportDefinition reportDefinition = new ReportDefinition("report",
                "./src/test/resources/test_report.prpt",
                params);

        assertEquals(null, reportDefinition.parameterType("non_existent_parameter"));
    }

    @Test
    public void testGetParameters() {
        List<ParameterDefinition> params = new ArrayList<>();

        params.add(ReportDefinition.optionalParameter("subreport_1", Boolean.class));
        params.add(ReportDefinition.optionalParameter("subreport_2", Boolean.class));
        params.add(ReportDefinition.requiredParameter("report_id", String.class));

        ReportDefinition reportDefinition = new ReportDefinition("report",
                "./src/test/resources/test_report.prpt",
                params);

        assertEquals(3, reportDefinition.getParameters().size());
    }

    @Test
    public void testThrowIllegalArgOnEmptyName() {
        ReportDefinition reportDefinition = new ReportDefinition("",
                "./src/test/resources/test_report.prpt",
                Collections.emptyList());

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("ReportDefinion must have a valid name");
        reportDefinition.validate();
    }
}
