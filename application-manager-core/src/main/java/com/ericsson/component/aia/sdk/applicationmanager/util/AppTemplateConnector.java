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

import java.util.Optional;

/**
 *
 * AppTemplateConnector defines templates available from template manager
 */
public enum AppTemplateConnector {

    SPARK("Spark"), FLINK("Flink"), KAFKA("Kafka"), BEAM("Beam");

    private final String connector;

    /**
     * AppTemplateConnector constructor
     *
     * @param connector
     *            connector
     */
    AppTemplateConnector(final String connector) {
        this.connector = connector;
    }

    public String getConnector() {
        return connector;
    }

    /**
     * findAppTemplateConnector method used to retrieve AppTemplateConnector based on passed template
     *
     * @param value
     *            return value
     * @return AppTemplateConnector
     */
    public static Optional<AppTemplateConnector> findAppTemplateConnector(final String value) {
        for (final AppTemplateConnector connector : AppTemplateConnector.values()) {
            if (value.toLowerCase().contains(connector.name().toLowerCase())) {
                return Optional.of(connector);
            }
        }

        return Optional.empty();
    }

}
