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

import static com.ericsson.component.aia.sdk.applicationmanager.common.Constants.UTF_8_ENCODING;
import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.sdk.applicationmanager.common.FileExtensions;
import com.ericsson.component.aia.sdk.applicationmanager.util.AppSdkUtil;

/**
 * Class to replace with extended PBA model properties.
 */
public class ReplaceWithApplicationPropertiesVisitor extends SimpleFileVisitor<Path> {

    private static final Logger Log = LoggerFactory.getLogger(ReplaceWithApplicationPropertiesVisitor.class);
    //create constants file
    private static final String CLEANUP_FILE = "CLEANUP";
    private static final String GLOBAL_TRUE = "TRUE";

    private final Map<String, String> replaceValues;
    private final String pbaNameInCamelCaseWithJavaExt;
    private final String extendedPbaNameInCamelCaseWithJavaExt;

    /**
     * Constructor.
     *
     * @param pbaName
     *            name of the application.
     * @param newPbaName
     *            the new pba name.
     * @param replaceValues
     *            the replace values.
     */
    ReplaceWithApplicationPropertiesVisitor(final String pbaName, final String newPbaName, final Map<String, String> replaceValues) {
        this.replaceValues = replaceValues;
        this.pbaNameInCamelCaseWithJavaExt = AppSdkUtil.convertApplicationName(pbaName) + FileExtensions.JAVA.getExtensionType();
        this.extendedPbaNameInCamelCaseWithJavaExt = AppSdkUtil.convertApplicationName(newPbaName) + FileExtensions.JAVA.getExtensionType();
    }

    /**
     * This method will check if the file name matches {@link Constants.PBA_NAME_CAMELCASE}. If found will rename the file with name as provided
     * renameTo value.
     *
     * @param file
     *            the file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void renameFile(final Path file) throws IOException {
        if (pbaNameInCamelCaseWithJavaExt.equals(file.getFileName().toString())) {
            Log.info("Found file with name {} and path {}", pbaNameInCamelCaseWithJavaExt, file.toAbsolutePath());
            final Path renameToPath = file.getParent().resolve(extendedPbaNameInCamelCaseWithJavaExt);
            Files.move(file, renameToPath, LinkOption.NOFOLLOW_LINKS);
            Log.info("Renamed File path is {}", extendedPbaNameInCamelCaseWithJavaExt);
        }
    }

    /**
     * This method will replace all the pba properties place holder with values from PBA.
     *
     * @param file
     *            to check for pba properties place holder.
     * @throws IOException
     *             if operation fails
     */
    private void replaceWithExtendedProperties(final Path file) throws IOException {
        String fileContents = IOUtils.toString(Files.newInputStream(file, StandardOpenOption.READ), UTF_8_ENCODING);

        for (final Map.Entry<String, String> entry : replaceValues.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            fileContents = fileContents.replaceAll(key, value);
        }

        Files.copy(IOUtils.toInputStream(fileContents, UTF_8_ENCODING), file, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Delete files
     * @param file
     * @throws IOException
     */
    private void cleanUpFiles(final Path file) throws IOException {

        boolean isAvailable = false;
        if (null != replaceValues.get(CLEANUP_FILE) && replaceValues.get(CLEANUP_FILE).equals(GLOBAL_TRUE)) {
            Log.info("DELETE FILES" + file.toString());

            //delete other files
            for (final Map.Entry<String, String> entry : replaceValues.entrySet()) {

                if (file.toString().contains(entry.getValue())) {

                    isAvailable = true;
                    break;
                }
            }

            if (!isAvailable) {
                Files.delete(file);
            }

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
        replaceWithExtendedProperties(file);
        renameFile(file);
        cleanUpFiles(file);
        return CONTINUE;
    }
}