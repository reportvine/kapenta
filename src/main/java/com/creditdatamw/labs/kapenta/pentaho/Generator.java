package com.creditdatamw.labs.kapenta.pentaho;

import com.creditdatamw.labs.kapenta.OutputType;
import com.creditdatamw.labs.kapenta.config.Database;
import com.creditdatamw.labs.kapenta.pentaho.sql.SqlDataSourceVisitor;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.plaintext.PlainTextReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Pentaho Report Generator
 *
 */
public final class Generator {

    static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

    static final ResourceManager resourceManager;

    static {
        resourceManager = new ResourceManager();
        resourceManager.registerDefaults();
    }

    /**
     * Generates a report and outputs it to the given outputStream.
     *
     * @param reportFileName the path to the `.prpt` report file and it must not be in a zipped folder or
     * anything. Just something on the file system that you have read-permissions to.
     * @param parameters values for the parameters that the report accepts/requires
     * @param outputType The output type of the report. Either HTML, PDF, TXT. Defaults to HTML
     * @param outputStream the output stream to write the generated report to
     * @throws GeneratorException Wraps exceptions thrown while trying to produce the report. Use {@linkplain Exception#getCause} to get actual exception
     */
    public static void generateReport(String reportFileName,
                                      Map<String, Object> parameters,
                                      OutputType outputType,
                                      OutputStream outputStream) throws GeneratorException {
        Path filePath = Paths.get(reportFileName);
        try {
            URL url = filePath.toUri().toURL();

            final Resource resource = resourceManager.createDirectly(url, MasterReport.class);

            LOGGER.debug("Loaded resource: {}", resource.getSource());

            // We finally have the pentaho report instance
            final MasterReport masterReport = (MasterReport) resource.getResource();

            ReportParameterValues params = masterReport.getParameterValues();

            parameters.forEach(params::put);

            // Defaults to HTML output
            switch (outputType) {
                case TXT:
                    PlainTextReportUtil.createPlainText(masterReport, outputStream);
                    break;
                case PDF:
                    PdfReportUtil.createPDF(masterReport, outputStream);
                    break;
                case HTML:
                default:
                    HtmlReportUtil.createStreamHTML(masterReport, outputStream);
            }
        } catch (Exception e) {
            throw new GeneratorException("Failed to generate report", e);
        }
    }

    public static void generateReport(String reportFileName,
                                      Map<String, Object> parameters,
                                      OutputType outputType,
                                      OutputStream outputStream,
                                      Database database) throws GeneratorException {
        Path filePath = Paths.get(reportFileName);
        try {
            URL url = filePath.toUri().toURL();

            final Resource resource = resourceManager.createDirectly(url, MasterReport.class);

            LOGGER.debug("Loaded resource: {}", resource.getSource());

            // We finally have the pentaho report instance
            final MasterReport masterReport = (MasterReport) resource.getResource();

            SqlDataSourceVisitor sqlDataSourceVisitor = new SqlDataSourceVisitor(database.getUri());

            sqlDataSourceVisitor.visit(masterReport);

            ReportParameterValues params = masterReport.getParameterValues();

            parameters.forEach(params::put);

            // Defaults to HTML output
            switch (outputType) {
                case TXT:
                    PlainTextReportUtil.createPlainText(masterReport, outputStream);
                    break;
                case PDF:
                    PdfReportUtil.createPDF(masterReport, outputStream);
                    break;
                case HTML:
                default:
                    HtmlReportUtil.createStreamHTML(masterReport, outputStream);
            }
        } catch (Exception e) {
            throw new GeneratorException("Failed to generate report", e);
        }
    }
}
