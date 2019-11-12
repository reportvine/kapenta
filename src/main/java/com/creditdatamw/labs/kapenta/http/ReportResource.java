package com.creditdatamw.labs.kapenta.http;

import com.creditdatamw.labs.kapenta.OutputType;
import com.creditdatamw.labs.kapenta.reportdefinition.ReportDefinition;

import java.util.EnumSet;

/**
 * Report Resource represents an API route that users can hit up to
 * generate a report
 *
 */
public interface ReportResource {

    /**
     * Report route or path  e.g. /my_report
     *
     * @return
     */
    String path();

    /**
     * Report definition
     *
     * @return
     */
    ReportDefinition reportDefinition();

    /**
     * Supported methods. Only valid are GET, POST at the moment
     *
     * @return
     */
    String[] methods();

    /**
     * Supported report output types
     *
     * @return
     */
    EnumSet<OutputType> outputTypes();
}
