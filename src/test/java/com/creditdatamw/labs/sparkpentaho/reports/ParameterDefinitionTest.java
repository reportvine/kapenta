package com.creditdatamw.labs.sparkpentaho.reports;

import org.junit.Test;

import java.sql.Date;

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
        assertEquals(String.class, ParameterDefinition.typeFromStr("string"));
        assertEquals(String.class, ParameterDefinition.typeFromStr("STRING"));
        assertEquals(String.class, ParameterDefinition.typeFromStr("string  "));
        assertEquals(String.class, ParameterDefinition.typeFromStr("text"));
    }
    @Test
    public void testTypeFromInteger() {
        assertEquals(Integer.class, ParameterDefinition.typeFromStr("integer"));
        assertEquals(Integer.class, ParameterDefinition.typeFromStr("int"));
        assertEquals(Integer.class, ParameterDefinition.typeFromStr("INTEGER"));
        assertEquals(Integer.class, ParameterDefinition.typeFromStr("INT  "));
        assertEquals(Integer.class, ParameterDefinition.typeFromStr(" int "));
    }

    @Test
    public void testTypeFromLong() {
        assertEquals(Long.class, ParameterDefinition.typeFromStr("long"));
        assertEquals(Long.class, ParameterDefinition.typeFromStr("LONG"));
        assertEquals(Long.class, ParameterDefinition.typeFromStr("long  "));
        assertEquals(Long.class, ParameterDefinition.typeFromStr("number"));
    }

    @Test
    public void testTypeFromBoolean() {
        assertEquals(Boolean.class, ParameterDefinition.typeFromStr("boolean"));
        assertEquals(Boolean.class, ParameterDefinition.typeFromStr("bool"));
        assertEquals(Boolean.class, ParameterDefinition.typeFromStr("BOOLEAN"));
        assertEquals(Boolean.class, ParameterDefinition.typeFromStr("BOOL "));
        assertEquals(Boolean.class, ParameterDefinition.typeFromStr("boolean    "));
    }

    @Test
    public void testTypeFromDate() {
        assertEquals(Date.class, ParameterDefinition.typeFromStr("date"));
        assertEquals(Date.class, ParameterDefinition.typeFromStr("date  "));
        assertEquals(Date.class, ParameterDefinition.typeFromStr("DATE"));
    }
}
