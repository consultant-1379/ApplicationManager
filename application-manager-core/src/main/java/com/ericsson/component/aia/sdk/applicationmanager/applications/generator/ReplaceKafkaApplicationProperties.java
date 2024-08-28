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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReplaceKafkaApplicationProperties used replace kafka selected source, sink properties files
 */
public class ReplaceKafkaApplicationProperties extends SimpleFileVisitor<Path> {

    private static final Logger Log = LoggerFactory.getLogger(ReplaceKafkaApplicationProperties.class);

    Map<String, String> sourceValues;
    Map<String, String> sinkValues;
    Map<String, String> selectedFileValues;

    Map<String, List<String>> propertyMap;

    List<Map<String, String>> sourceReplaceValueList;
    List<Map<String, String>> sinkReplaceValueList;
    List<Map<String, String>> kafkaValueList;

    /**
     * ReplaceKafkaApplicationProperties constructor
     * @param sourceReplaceValueList sourceReplaceValueList
     * @param sinkReplaceValueList sinkReplaceValueList
     * @param kafkaValueList kafkaValueList
     * @param selectedFileValues selectedFileValues
     * @param propertyMap propertyMap
     */
    ReplaceKafkaApplicationProperties(final List<Map<String, String>> sourceReplaceValueList, final List<Map<String, String>> sinkReplaceValueList,
                 final List<Map<String, String>> kafkaValueList,
                 final Map<String, String> selectedFileValues, final Map<String, List<String>> propertyMap) {
        this.sourceReplaceValueList = sourceReplaceValueList;
        this.sinkReplaceValueList = sinkReplaceValueList;
        this.kafkaValueList = kafkaValueList;
        this.selectedFileValues = selectedFileValues;
        this.propertyMap = propertyMap;
    }


    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        Log.info("Visiting file {}", file.toAbsolutePath());

        replaceValues(file);
        return CONTINUE;
    }

    /**List<Map<String,String>> sourceReplaceValueList
     * Targeted for handling multiple files.......
     * @param file
     * @throws IOException
     */
    private void replaceValues(final Path file) throws IOException {

        final String fileContents = IOUtils.toString(Files.newInputStream(file, StandardOpenOption.READ), StandardCharsets.UTF_8);

        //identify source or sink
        if (file.toString().contains("source")) {

            if (!sourceReplaceValueList.isEmpty()) {
                Log.debug("sourceReplaceValueList.size()::{}", sourceReplaceValueList.size());
                replaceFileContent(file, fileContents, sourceReplaceValueList);
            }

        } else if (file.toString().contains("sink")) {
            if (!sinkReplaceValueList.isEmpty()) {
                Log.debug("sourceReplaceValueList.size()::{}", sinkReplaceValueList.size());
                replaceFileContent(file, fileContents, sinkReplaceValueList);
            }
        } else {
            if (file.toString().contains("kafka-connect")) {
                Log.debug("kafkaValueList.size()::{}", kafkaValueList.size());
                replaceFileContent(file, fileContents, kafkaValueList);
            } else if (file.toString().contains("README")) {
                Log.debug("About to replace README content with selectedFileValues");
                replaceReadMeContent(file, fileContents, selectedFileValues);
            }

        }

    }

    private void replaceFileContent(final Path file, String fileContents, final List<Map<String, String>> replaceValueList) throws IOException {
        for (final Map<String, String> replaceValue: replaceValueList) {
            Log.debug("About to replace kafka values {}", file.toString());
            Log.info("Start replacing fileContents with passed values {}", file.toString());
            for (final Map.Entry<String, String> entry : replaceValue.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();

                fileContents = fileContents.replaceAll(key, value);
            }

            Files.copy(IOUtils.toInputStream(fileContents, UTF_8_ENCODING), file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void replaceReadMeContent(final Path file, String fileContents, final Map<String, String> selectedFileValues) throws IOException {

        final StringBuilder fileNameBuilder = new StringBuilder();
        final StringBuilder kafkaNameBuilder = new StringBuilder();

        for (final Map.Entry<String, String> entry : selectedFileValues.entrySet()) {
            final String key = entry.getKey();

            if (key.contains("pba") || key.contains("README") || key.contains("CLEANUP")) {
                continue;
            } else if (key.contains("kafka")) {
                Log.debug("SelectedFile is KAFKA :: {}", key);
                kafkaNameBuilder.append(key).append(".properties");
            } else {
                Log.debug("SelectedFile is {}", key);
                if (fileNameBuilder.length() > 0) {
                    fileNameBuilder.append(", ");
                }
                fileNameBuilder.append(key).append(".properties");
            }
        }

        fileContents = fileContents.replace("${fileName}.properties", fileNameBuilder);
        fileContents = fileContents.replace("kafka-connect-${deploymentMode}.properties", kafkaNameBuilder);

        Files.copy(IOUtils.toInputStream(fileContents, UTF_8_ENCODING), file, StandardCopyOption.REPLACE_EXISTING);
    }

}