// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.doclet;
import javax.tools.*;
import javax.lang.model.SourceVersion;

import com.sun.source.util.DocTrees;
import com.zimbra.doc.soap.Root;
import com.zimbra.doc.soap.SoapDocException;
import com.zimbra.doc.soap.WsdlDocGenerator;
import com.zimbra.doc.soap.apidesc.SoapApiDescription;
import com.zimbra.doc.soap.template.ApiReferenceTemplateHandler;
import com.zimbra.doc.soap.template.TemplateHandler;
import com.zimbra.soap.JaxbUtil;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.DocletEnvironment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

public class CarbonioApiDoclet implements Doclet {
    private String templatesDir;
    private String outputDir;
    private String apiDescJson;
    private String buildVersion;
    private String buildDate;
    private Reporter reporter;

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.reporter = reporter;
        reporter.print(Diagnostic.Kind.NOTE, "Initializing CarbonioApiDoclet...");
    }
    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Set.of(
                new Option() {
                    @Override
                    public int getArgumentCount() { return 1; }

                    @Override
                    public String getDescription() { return "Directory for templates"; }

                    @Override
                    public Kind getKind() { return Kind.STANDARD; }

                    @Override
                    public List<String> getNames() { return List.of("--templates-dir"); }

                    @Override
                    public String getParameters() { return "<templatesDir>"; }

                    @Override
                    public boolean process(String option, List<String> arguments) {
                        templatesDir = arguments.get(0);
                        return true;
                    }
                },
                new Option() {
                    @Override
                    public int getArgumentCount() { return 1; }

                    @Override
                    public String getDescription() { return "Directory for output"; }

                    @Override
                    public Kind getKind() { return Kind.STANDARD; }

                    @Override
                    public List<String> getNames() { return List.of("--output-dir"); }

                    @Override
                    public String getParameters() { return "<outputDir>"; }

                    @Override
                    public boolean process(String option, List<String> arguments) {
                        outputDir = arguments.get(0);
                        return true;
                    }
                },
                new Option() {
                    @Override
                    public int getArgumentCount() { return 1; }

                    @Override
                    public String getDescription() { return "API description JSON file"; }

                    @Override
                    public Kind getKind() { return Kind.STANDARD; }

                    @Override
                    public List<String> getNames() { return List.of("--apidesc-json"); }

                    @Override
                    public String getParameters() { return "<apiDescJson>"; }

                    @Override
                    public boolean process(String option, List<String> arguments) {
                        apiDescJson = arguments.get(0);
                        return true;
                    }
                },
                new Option() {
                    @Override
                    public int getArgumentCount() { return 1; }

                    @Override
                    public String getDescription() { return "Build version"; }

                    @Override
                    public Kind getKind() { return Kind.STANDARD; }

                    @Override
                    public List<String> getNames() { return List.of("--build-version"); }

                    @Override
                    public String getParameters() { return "<buildVersion>"; }

                    @Override
                    public boolean process(String option, List<String> arguments) {
                        buildVersion = arguments.get(0);
                        return true;
                    }
                },
                new Option() {
                    @Override
                    public int getArgumentCount() { return 1; }

                    @Override
                    public String getDescription() { return "Build date"; }

                    @Override
                    public Kind getKind() { return Kind.STANDARD; }

                    @Override
                    public List<String> getNames() { return List.of("--build-date"); }

                    @Override
                    public String getParameters() { return "<buildDate>"; }

                    @Override
                    public boolean process(String option, List<String> arguments) {
                        buildDate = arguments.get(0);
                        return true;
                    }
                }
        );
    }

    @Override
    public boolean run(DocletEnvironment docEnv) {
        DocTrees docTrees = docEnv.getDocTrees();
        try {
            // Ensure output directory exists
            File dir = new File(outputDir);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    System.out.println("Output directory created: " + outputDir);
                } else {
                    reporter.print(Diagnostic.Kind.ERROR, "Failed to create output directory: " + outputDir);
                }
            }

            // Create an API documentation file (could be HTML, JSON, etc.)
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try (FileWriter writer = new FileWriter(new File(outputDir, "apidoc.txt"))) {
                writer.write("API Documentation\n");
                writer.write("Build Version: " + buildVersion + "\n");
                writer.write("Build Date: " + buildDate + "\n");
                writer.write("API Description: " + apiDescJson + "\n");
                writer.write("Templates Directory: " + templatesDir + "\n");

                // Iterate over all classes and document them

                CarbonioApiListener  listener = new CarbonioApiListener(docTrees);

                listener.processJavadocResults(docEnv);


                Thread.currentThread().setContextClassLoader(JaxbUtil.class.getClassLoader());
                Root soapApiDataModelRoot = WsdlDocGenerator.processJaxbClasses(listener.getDocMap());
                Properties templateContext = new Properties();
                templateContext.setProperty(TemplateHandler.PROP_TEMPLATES_DIR, templatesDir);
                templateContext.setProperty(TemplateHandler.PROP_OUTPUT_DIR, outputDir);
                templateContext.setProperty(TemplateHandler.PROP_BUILD_VERSION, buildVersion);
                templateContext.setProperty(TemplateHandler.PROP_BUILD_DATE, buildDate);

                // generate the API Reference documentation
                ApiReferenceTemplateHandler templateHandler = new ApiReferenceTemplateHandler(templateContext);
                templateHandler.process(soapApiDataModelRoot);

                // generate a JSON representation of the API used when creating a changelog
                SoapApiDescription jsonDesc = new SoapApiDescription(buildVersion, buildDate);
                jsonDesc.build(soapApiDataModelRoot);
                File json = new File(apiDescJson);
                jsonDesc.serializeToJson(json);

            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }

        } catch (IOException | SoapDocException e) {
            reporter.print(Diagnostic.Kind.ERROR, "Error generating API documentation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "CarbonioApiDoclet";
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
