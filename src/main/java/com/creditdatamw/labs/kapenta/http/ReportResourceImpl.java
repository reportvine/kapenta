package com.creditdatamw.labs.kapenta.http;

import com.creditdatamw.labs.kapenta.OutputType;
import com.creditdatamw.labs.kapenta.reportdefinition.ReportDefinition;

import java.util.EnumSet;


public class ReportResourceImpl implements ReportResource {

    public final String routePath;

    public final ReportDefinition reportDefinition;

    public final String[] methods;

    public final EnumSet<OutputType> outputTypes;

    public ReportResourceImpl(String routePath, String[] methods, EnumSet<OutputType> outputTypes, ReportDefinition reportDefinition) {
        this.routePath = routePath;
        this.reportDefinition = reportDefinition;
        this.methods = methods.clone();
        this.outputTypes = outputTypes;
    }

    @Override
    public String path() {
        return routePath;
    }

    @Override
    public ReportDefinition reportDefinition() {
        return reportDefinition;
    }

    @Override
    public String[] methods() {
        return methods.clone();
    }

    @Override
    public EnumSet<OutputType> outputTypes() {
        return outputTypes.clone();
    }
}
