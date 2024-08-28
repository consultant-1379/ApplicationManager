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
package com.ericsson.component.aia.sdk.applicationmanager.maas.mapper;


/**
 * MAASFileMapper used to mark what values to replaced in configuration files while generating MAAS application
 * filenames
 */
public enum MAASFileMapper {

    // TODO:: TO REFINE FURTHER....MOVE TO PROPERTIES FILE....

    //config
    CONFIG_INPUT_SOURCE("<SOURCE_INPUT>", File.CONFIG),
    CONFIG_PARSER_NAME("<NAME>", File.CONFIG),
    CONFIG_PARSER_TYPES("<TYPES>", File.CONFIG),

    //flow
    FLOW_INPUT_SOURCE_DESC("<EVENT_NAME_DESC>", File.FLOW),

    //Stream Terminator publisher
    STREAM_TERMINATOR_PUBLISHER("<SUBSCRIBER_TOPIC_NAME>", File.STREAM_TERMINATOR_PUBLISHER),

    //kafka subscriber
    INPUT_ADAPTER_TOPIC_NAME("<SUBSCRIBER_TOPIC_NAME>", File.KAFKA_SUBSCRIBER),

    //kafka publisher
    OUTPUT_ADAPTER_TOPIC_NAME("<PUBLISHER_TOPIC_NAME>", File.KAFKA_PUBLISHER),

    //mass-ui
    MAAS_UI_SOURCE_INPUT("<SOURCE_INPUT>", File.MASS_UI),
    MAAS_UI_PARSERDEF_NAME("<NAME>", File.MASS_UI),
    MAAS_UI_PARSERDEF_DESC("<TYPES>", File.MASS_UI),
    MAAS_UI_INPUT_ADAPTER_TOPIC_NAME("<SUBSCRIBER_TOPIC_NAME>", File.MASS_UI),
    MAAS_UI_OUTPUT_ADAPTER_TOPIC_NAME("<PUBLISHER_TOPIC_NAME>", File.MASS_UI),

    //pba
    PBA_PUBLISHER_TOPIC_NAME("<PUBLISHER_TOPIC_NAME>", File.PBA)
    ;


    private final File file;
    private final String propValue;


    /**
     * MAASFileMapper constructor
     * @param propValue propValue
     * @param file file
     */
    MAASFileMapper(final String propValue, final File file) {
        this.propValue = propValue;
        this.file = file;
    }

    public String getPropValue() {
        return propValue;
    }

    public File getFile() {
        return file;
    }




}

/**
 * File contains the file names which are used for generating maas application
 *
 */
enum File {

    CONFIG("config"),
    FLOW("flow"),
    MASS_UI("maas-ui"),
    KAFKA_SUBSCRIBER("EsnLteRanParserSubscriberRaw_INTEGRATION_POINT"),
    KAFKA_PUBLISHER("EsnLteRanParserPublisherDecoded_INTEGRATION_POINT"),
    PBA("pba"),
    STREAM_TERMINATOR_PUBLISHER("RAW_PUBLISHER_INTEGRATION_POINT");

    String name;

    /**
     * File Constructor
     * @param name name
     */
    File(final String name) {

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {

        this.name = name;
    }

}