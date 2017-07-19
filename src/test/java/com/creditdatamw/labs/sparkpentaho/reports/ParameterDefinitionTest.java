package com.creditdatamw.labs.sparkpentaho.reports;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * ParameterDefinitionTest
 *
 */
public class ParameterDefinitionTest {

    @Test
    public void testCreateOptionalParameter() {
        ParameterDefinition p = new ParameterDefinition("test",false,String.class);
        assertEquals(p, ReportDefinition.optionalParameter("test", String.class));
    }

    @Test
    public void testCreateOptionalParameterWithDefault() {
        ParameterDefinition p = new ParameterDefinition("test",false,String.class, "none");
        assertEquals(p, ReportDefinition.optionalParameter("test", "none", String.class));
    }

    @Test
    public void testCreateRequiredParameter() {
        ParameterDefinition p = new ParameterDefinition("test",true,String.class);

        assertEquals(p, ReportDefinition.requiredParameter("test", "String"));
    }

    @Test
    public void testCreateRequiredParameterWithDefault() {
        ParameterDefinition p = new ParameterDefinition("test",true,String.class, "none");
        assertEquals(p, ReportDefinition.requiredParameter("test", "none", String.class));
    }

    @Test
    public void testTypeFromString() {
        assertEquals(String.class, ParameterDefinition.typeFromStr("String"));
    }
}
