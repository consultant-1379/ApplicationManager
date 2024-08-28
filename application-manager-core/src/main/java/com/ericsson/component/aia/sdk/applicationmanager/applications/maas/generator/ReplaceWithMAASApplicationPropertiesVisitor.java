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
package com.ericsson.component.aia.sdk.applicationmanager.applications.maas.generator;

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.UTF_8_ENCODING;
import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to replace files with MAAS properties.
 */
public class ReplaceWithMAASApplicationPropertiesVisitor extends SimpleFileVisitor<Path> {

    private static final Logger Log = LoggerFactory.getLogger(ReplaceWithMAASApplicationPropertiesVisitor.class);
    private final Map<String, Map<String, String>> replaceValues;


   /**
     * Constructor.
     *
     * @param replaceValues
     *            the replace values.
     */
    public ReplaceWithMAASApplicationPropertiesVisitor(final Map<String, Map<String, String>> replaceValues) {
        this.replaceValues = replaceValues;
    }

    /**
     * This method will replace all the pba properties place holder with values from
     * Maas.
     *
     * @param file
     *            to check for maas properties place holder.
     * @throws IOException
     *             if operation fails
     */
    private void replaceFileProperties(final Path file) throws IOException {

        String fileContents = IOUtils.toString(Files.newInputStream(file, StandardOpenOption.READ), UTF_8_ENCODING);

        for (final Map.Entry<String, Map<String, String>> entry : replaceValues.entrySet()) {
            final String fileKey = entry.getKey();

            if (file.getFileName().toString().startsWith(fileKey)) {
                final Map<String, String> value = entry.getValue();
                for (final Map.Entry<String, String> item : value.entrySet()) {
                    Log.debug(item.getKey());
                    Log.debug(item.getValue());

                    fileContents = fileContents.replaceAll(item.getKey(), item.getValue());
                }
            } else {
                continue;
            }

            Files.copy(IOUtils.toInputStream(fileContents, UTF_8_ENCODING), file, StandardCopyOption.REPLACE_EXISTING);
        }

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
        replaceFileProperties(file);
        return CONTINUE;
    }
}