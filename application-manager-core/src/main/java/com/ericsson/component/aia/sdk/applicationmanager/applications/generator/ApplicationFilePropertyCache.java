/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.applicationmanager.applications.generator;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  ApplicationFilePropertyCache generic Cache which populates in map with fileName as key and values
 *  as properties of that file
 *
 */
public class ApplicationFilePropertyCache extends SimpleFileVisitor<Path> {

    private static final Logger Log = LoggerFactory.getLogger(ApplicationFilePropertyCache.class);

    private final Map<String, List<String>> propertyMap;

    /**
     * ApplicationFilePropertyCache constructor initializes propertyMap
     */
    public ApplicationFilePropertyCache() {
        propertyMap = new HashMap<>();
    }

    public Map<String, List<String>> getPropertyMap() {
        return propertyMap;
    }

    /**
     * Visit file.
     *
     * @param file
     *            the file
     * @param attrs
     *            the attrs
     * @return the file visit result
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        Log.info("Visiting file {}", file.toAbsolutePath());
        addProperties(file);
        return CONTINUE;
    }

    private void addProperties(final Path file) {
        if (!(file.toString().contains("pba.json") || file.toString().contains("README.md"))) {
            final List<String> propList = new ArrayList<>();
            Log.info("fileName=" + file.toString());
            try {

                final List<String> contents = Files.readAllLines(file);

                //Read from the stream
                for (final String content:contents) {
                    if (!(content.startsWith("#") || content.toString().trim().length() == 0)) {
                        Log.debug(content);
                        propList.add(content);
                    }
                }

                if (!(propList.isEmpty())) {
                    propertyMap.put(file.toString(), propList);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

}