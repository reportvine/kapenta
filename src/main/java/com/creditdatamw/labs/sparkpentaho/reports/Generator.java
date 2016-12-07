package com.creditdatamw.labs.sparkpentaho.reports;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.plaintext.PlainTextReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.engine.classic.core.util.ReportParameterValues;
import org.pentaho.reporting.libraries.docbundle.bundleloader.MemoryResourceBundleLoader;
import org.pentaho.reporting.libraries.docbundle.bundleloader.ZipResourceBundleLoader;
import org.pentaho.reporting.libraries.repository.zipreader.ZipReadRepository;
import org.pentaho.reporting.libraries.resourceloader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Pentaho Report Generator
 *
 */
public final class Generator {

    static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);
    static final ZipResourceBundleLoader bundleLoader = new ZipResourceBundleLoader();

    static final ResourceManager resourceManager;

    static {
        resourceManager = new ResourceManager();
        resourceManager.registerDefaults();
        /* For some reason pentaho doesn't load the ZipResourceBundleLoader
         * as a default resource loader - so we force it to do that here -
         * kinda messy if you ask me. But you won't ask me, will you?
         *
         */
        resourceManager.registerBundleLoader(bundleLoader);
        resourceManager.registerBundleLoader(new MemoryResourceBundleLoader());
    }

    /**
     * Generates a report and outputs it to the given outputStream.
     *
     * @param reportFileName the path to the `.prpt` report file and it must not be in a zipped folder or
     * anything. Just something on the file system that you have read-permissions to.
     * @param parameters values for the parameters that the report accepts/requires
     * @param outputType The output type of the report. Either HTML, PDF, TEXT. Defaults to HTML
     * @param outputStream the output stream to write the generated report to
     * @throws GeneratorException Wraps exceptions thrown while trying to produce the report. Use {@linkplain Exception#getCause} to get actual exception
     */
    public static void generateReport(String reportFileName,
                                      Map<String, Object> parameters,
                                      OutputType outputType,
                                      OutputStream outputStream) throws GeneratorException {
        Path filePath = Paths.get(reportFileName);
        try (InputStream fis = Files.newInputStream(filePath, LinkOption.NOFOLLOW_LINKS)){
            URL url = filePath.toUri().toURL();
            ZipReadRepository zipReadRepository = new ZipReadRepository(fis);

            Map<ParameterKey, Object> parameterKeyObjectMap = new HashMap<>();

            parameterKeyObjectMap.put(new FactoryParameterKey("repository-loader"), bundleLoader);
            parameterKeyObjectMap.put(new FactoryParameterKey("repository"), zipReadRepository);

            /* You might be wondering what this whole resource key stuff is about - here
             * is a short inaccurate explanation. Pentaho identifies things it needs to
             * load as resources. A resource key gives information about the type of resource it's
             * trying to load e.g. URL resource, File resource or Zip resource.
             *
             * Now, pentaho reports are in essence Zip files so the resource has to be loaded
             * as such, now pentaho will represent a resource as a hierarchy, like:
             *
             * file.prpt - the parent resource (zip file)
             * `-- content.xml - the primary report definition in the zip file
             *
             * So we need to tell Pentaho that it needs to load the `content.xml` since
             * that's where the actual report definition starts and Pentaho's
             * too stupid to figure that out by itself when running in an executable jar. :/
             */
            final ResourceKey parentResourceKey = resourceManager.createKey(url);

            final ResourceKey reportResourceKey = new ResourceKey(
                    parentResourceKey,
                    ZipResourceBundleLoader.class.getName(),
                    "content.xml",
                    parameterKeyObjectMap
            );

            final Resource resource = resourceManager.create(reportResourceKey, parentResourceKey, MasterReport.class);

            LOGGER.debug("Loaded resource: {}", resource.getSource());

            // We finally have the pentaho report instance
            final MasterReport masterReport = (MasterReport) resource.getResource();

            ReportParameterValues params = masterReport.getParameterValues();

            parameters.forEach((key, value) -> {
                params.put(key, value);
            });

            // Defaults to HTML output
            switch (outputType) {
                case TEXT:
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
