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
package com.ericsson.component.aia.sdk.applicationmanager.util;

/**
 * KafkaConnectFileMapping enum mapping between entryPoint/deploy mode and filenames
 */
public enum KafkaConnectFileMapping {

    KAFKA_STANDALONE_FILEMAPPING("kafka-connect-standalone"),
    KAFKA_DISTRIBUTED_FILEMAPPING("kafka-connect-distributed"),
    JDBC_SOURCE("jdbc.source"),
    JDBC_SINK("jdbc.sink"),

    //HDFS
    HDFS_SOURCE("hdfs.source"),
    HDFS_SINK("hdfs.sink"),

    //FILE
    FILE_SOURCE("filestream.source"),
    FILE_SINK("filestream.sink");

    String entryPoint;
    String fileName;

    /**
     * KafkaConnectFileMapping constructor
     * @param fileName fileName
     */
    KafkaConnectFileMapping(final String fileName) {
        this.fileName = fileName;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public String getFileName() {
        return fileName;
    }

}
