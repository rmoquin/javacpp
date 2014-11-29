/*
 * Copyright (C) 2012,2013,2014 Arnaud Nauwynck, Samuel Audet
 *
 * This file is part of JavaCPP.
 *
 * JavaCPP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * JavaCPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaCPP.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bytedeco.javacpp.tools;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * A Maven Mojo to call the {@link Builder} (C++ header file -> Java class -> C++ JNI -> native library). Can also be
 * considered as an example of how to use the Builder programmatically.
 *
 * @author Arnaud Nauwynck
 * @author Samuel Audet
 */
@Mojo(name = "build", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class BuildMojo extends AbstractMojo {

    /**
     * The location where to target projects classes are compiled to, which is then read by javacpp for processing.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private String classPath;

    /**
     * The location where to target projects classes are compiled to, which is then read by javacpp for processing.
     */
    @Parameter
    private String[] classPaths;

    /**
     * Add the path to the "platform.includepath" property.
     */
    @Parameter
    private String includePath;

    /**
     * Add the paths to the "platform.includepath" property.
     */
    @Parameter
    private String[] includePaths;

    /**
     * Add the path to the "platform.linkpath" property.
     */
    @Parameter
    private String linkPath;

    /**
     * Add the paths to the "platform.linkpath" property.
     */
    @Parameter
    private String[] linkPaths;

    /**
     * Add the path to the "platform.preloadpath" property.
     */
    @Parameter
    private String preloadPath;

    /**
     * Add the paths to the "platform.preloadpath" property.
     */
    @Parameter
    private String[] preloadPaths;

    /**
     * Output all generated files to outputDirectory
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;

    /**
     * Output everything in a file named after given outputName
     */
    @Parameter
    private String outputName;

    /**
     * Compile and delete the generated .cpp files
     */
    @Parameter(defaultValue = "true")
    private boolean compile;

    /**
     * Generate header file with declarations of callbacks functions
     */
    @Parameter(defaultValue = "false")
    private boolean header;

    /**
     * Copy to output directory dependent libraries (link and preload)
     */
    @Parameter(defaultValue = "false")
    private boolean copyLibs;

    /**
     * Also create a JAR file named {@code <jarPrefix>-<platform>.jar}
     */
    @Parameter
    private String jarPrefix;

    /**
     * Load all properties from resource
     */
    @Parameter
    private String properties;

    /**
     * Load all properties from file
     */
    @Parameter
    private File propertyFile;

    /**
     * Set property keys to values
     */
    @Parameter
    private Properties propertyKeysAndValues;

    /**
     * Process only this class, or a package ending with .* to retrieve classes only in this package, or .** to
     * recursively look for classes in this package or subpackages. 
    */
    @Parameter
    private String classOrPackageName;

    /**
     * Process only this classes, or packages ending with either .*, to retrieve classes only in this package, or .**, to
     * recursively look for classes in this package or subpackages.
     */
    @Parameter
    private String[] classOrPackageNames;

    /**
     * Environment variables added to the compiler subprocess
     */
    @Parameter
    private Map<String, String> environmentVariables;

    /**
     * Pass compilerOptions directly to compiler
     */
    @Parameter
    private String[] compilerOptions;

    /**
     * Skip the execution.
     */
    @Parameter(defaultValue = "false")
    private boolean skip;

    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject project;

    String[] merge(String[] ss, String s) {
        if (ss != null && s != null) {
            ss = Arrays.copyOf(ss, ss.length + 1);
            ss[ss.length - 1] = s;
        } else if (s != null) {
            ss = new String[]{s};
        }
        return ss;
    }

    @Override
    public void execute() throws MojoExecutionException {
        final Log log = getLog();
        try {
            log.info("Executing JavaCPP Builder");
            if (log.isDebugEnabled()) {
                log.debug("classPath: " + classPath);
                log.debug("classPaths: " + Arrays.deepToString(classPaths));
                log.debug("includePath: " + includePath);
                log.debug("includePaths: " + Arrays.deepToString(includePaths));
                log.debug("linkPath: " + linkPath);
                log.debug("linkPaths: " + Arrays.deepToString(linkPaths));
                log.debug("preloadPath: " + preloadPath);
                log.debug("preloadPaths: " + Arrays.deepToString(preloadPaths));
                log.debug("outputDirectory: " + outputDirectory);
                log.debug("outputName: " + outputName);
                log.debug("compile: " + compile);
                log.debug("header: " + header);
                log.debug("copyLibs: " + copyLibs);
                log.debug("jarPrefix: " + jarPrefix);
                log.debug("properties: " + properties);
                log.debug("propertyFile: " + propertyFile);
                log.debug("propertyKeysAndValues: " + propertyKeysAndValues);
                log.debug("classOrPackageName: " + classOrPackageName);
                log.debug("classOrPackageNames: " + Arrays.deepToString(classOrPackageNames));
                log.debug("environmentVariables: " + environmentVariables);
                log.debug("compilerOptions: " + Arrays.deepToString(compilerOptions));
                log.debug("skip: " + skip);
            }

            if (skip) {
                log.info("Skipped execution of JavaCPP Builder");
                return;
            }

            classPaths = merge(classPaths, classPath);
            classOrPackageNames = merge(classOrPackageNames, classOrPackageName);

            Logger logger = new Logger() {
                @Override
                public void debug(CharSequence cs) {
                    log.debug(cs);
                }

                @Override
                public void info(CharSequence cs) {
                    log.info(cs);
                }

                @Override
                public void warn(CharSequence cs) {
                    log.warn(cs);
                }

                @Override
                public void error(CharSequence cs) {
                    log.error(cs);
                }
            };
            Builder builder = new Builder(logger)
                    .classPaths(classPaths)
                    .outputDirectory(outputDirectory)
                    .outputName(outputName)
                    .compile(compile)
                    .header(header)
                    .copyLibs(copyLibs)
                    .jarPrefix(jarPrefix)
                    .properties(properties)
                    .propertyFile(propertyFile)
                    .properties(propertyKeysAndValues)
                    .classesOrPackages(classOrPackageNames)
                    .environmentVariables(environmentVariables)
                    .compilerOptions(compilerOptions);
            Properties properties = builder.properties;
            String separator = properties.getProperty("platform.path.separator");
            for (String s : merge(includePaths, includePath)) {
                String v = properties.getProperty("platform.includepath", "");
                properties.setProperty("platform.includepath",
                        v.length() == 0 || v.endsWith(separator) ? v + s : v + separator + s);
            }
            for (String s : merge(linkPaths, linkPath)) {
                String v = properties.getProperty("platform.linkpath", "");
                properties.setProperty("platform.linkpath",
                        v.length() == 0 || v.endsWith(separator) ? v + s : v + separator + s);
            }
            for (String s : merge(preloadPaths, preloadPath)) {
                String v = properties.getProperty("platform.preloadpath", "");
                properties.setProperty("platform.preloadpath",
                        v.length() == 0 || v.endsWith(separator) ? v + s : v + separator + s);
            }
            project.getProperties().putAll(properties);
            File[] outputFiles = builder.build();
            log.info("Successfully executed JavaCPP Builder");
            if (log.isDebugEnabled()) {
                log.debug("outputFiles: " + Arrays.deepToString(outputFiles));
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute JavaCPP Builder", e);
        }
    }
}
