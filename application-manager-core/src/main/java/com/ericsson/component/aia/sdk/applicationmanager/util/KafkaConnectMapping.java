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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * KafkaConnectMapping is an enum used to map technology,
 * standalone/distributed, source/sink and file mappings; can also be used as
 * generic mapping
 */
public enum KafkaConnectMapping {

    KAFKA_SOURCE_STANDALONE("KAFKA-CONNECT-KAFKA", EntryPointMapping.KAFKA_STANDALONE,
            KafkaConnectFileMapping.KAFKA_STANDALONE_FILEMAPPING), KAFKA_SINK_STANDALONE("KAFKA-CONNECT-KAFKA",
                EntryPointMapping.KAFKA_STANDALONE, KafkaConnectFileMapping.KAFKA_STANDALONE_FILEMAPPING),

    KAFKA_SOURCE_DISTRIBUTED("KAFKA-CONNECT-KAFKA", EntryPointMapping.KAFKA_DISTRIBUTED,
            KafkaConnectFileMapping.KAFKA_DISTRIBUTED_FILEMAPPING), KAFKA_SINK_DISTRIBUTED("KAFKA-CONNECT-KAFKA",
                EntryPointMapping.KAFKA_DISTRIBUTED, KafkaConnectFileMapping.KAFKA_DISTRIBUTED_FILEMAPPING),

    JDBC_SOURCE("jdbc", EntryPointMapping.SOURCE, KafkaConnectFileMapping.JDBC_SOURCE), JDBC_SINK("jdbc",
        EntryPointMapping.SINK, KafkaConnectFileMapping.JDBC_SINK),

    // HDFS
    HDFS_SOURCE("HDFS", EntryPointMapping.SOURCE, KafkaConnectFileMapping.HDFS_SOURCE), HDFS_SINK("HDFS",
        EntryPointMapping.SINK, KafkaConnectFileMapping.HDFS_SINK),

    // FILE
    FILE_SOURCE("FILE", EntryPointMapping.SOURCE, KafkaConnectFileMapping.FILE_SOURCE), FILE_SINK("FILE",
            EntryPointMapping.SINK, KafkaConnectFileMapping.FILE_SINK);

    private static Map<String, KafkaConnectMapping> lookup = new HashMap<>();
    private String technology;
    private KafkaConnectFileMapping kafkaConnectFileMapping;
    private EntryPointMapping entryPointMapping;

    /**
     * KafkaConnectMapping Constructor
     * @param technology technology
     * @param entryPointMapping entryPointMapping
     * @param kafkaConnectFileMapping kafkaConnectFileMapping
     */
    KafkaConnectMapping(final String technology, final EntryPointMapping entryPointMapping,
            final KafkaConnectFileMapping kafkaConnectFileMapping) {
        this.technology = technology;
        this.kafkaConnectFileMapping = kafkaConnectFileMapping;
        this.entryPointMapping = entryPointMapping;
    }

    public String getTechnology() {
        return technology;
    }

    public KafkaConnectFileMapping getKafkaConnectFileMapping() {
        return kafkaConnectFileMapping;
    }

    public EntryPointMapping getEntryPointMapping() {
        return entryPointMapping;
    }


    static {
        for (final KafkaConnectMapping kafkaConnectInnerMapping : EnumSet.allOf(KafkaConnectMapping.class)) {
            lookup.put(kafkaConnectInnerMapping.getTechnology().toUpperCase()
                + kafkaConnectInnerMapping.getEntryPointMapping(), kafkaConnectInnerMapping);
        }
    }

    /**
     * getKafkaConnectInnerMapping method used to retrieve enum based on passed key
     * @param key  pass the key
     * @return KafkaConnectMapping
     */
    public static KafkaConnectMapping getKafkaConnectInnerMapping(final String key) {
        return lookup.get(key.toUpperCase());
    }

}
